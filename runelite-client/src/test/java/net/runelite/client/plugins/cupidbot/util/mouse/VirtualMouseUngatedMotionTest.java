package net.runelite.client.plugins.cupidbot.util.mouse;

import net.runelite.api.Point;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseEngineMode;
import net.runelite.client.plugins.cupidbot.util.mouse.engine.MouseActionContext;
import net.runelite.client.plugins.cupidbot.util.mouse.engine.MouseMovementPlan;
import net.runelite.client.plugins.cupidbot.util.mouse.engine.MouseMovementTuning;
import net.runelite.client.plugins.cupidbot.util.mouse.engine.MouseTarget;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class VirtualMouseUngatedMotionTest {

	@Test
	public void clickCadenceDelayStaysInsideHumanBounds() {
		for (int i = 0; i < 10_000; i++) {
			int delay = VirtualMouse.nextClickStageDelayMs();
			assertTrue("click delay " + delay + " below floor", delay >= 25);
			assertTrue("click delay " + delay + " above ceiling", delay <= 90);
		}
	}

	@Test
	public void clickHoldDelayComesFromMovementPlan() {
		MouseMovementPlan plan = movementPlanWithTuning(
				new MouseMovementTuning(25, 35, 82, 100, 100, 100, 100, 100));

		assertEquals(82, VirtualMouse.nextClickHoldDelayMs(plan));
	}

	@Test
	public void clickReleaseDelayDoesNotAddLegacyRandomStageWhenPlanExists() {
		MouseMovementPlan plan = movementPlanWithTuning(
				new MouseMovementTuning(25, 135, 82, 100, 100, 100, 100, 100));

		assertEquals(0, VirtualMouse.nextClickReleaseDelayMs(plan));
	}

	@Test
	public void clickHoldDelayFallsBackToHumanBoundsWithoutPlan() {
		for (int i = 0; i < 10_000; i++) {
			int delay = VirtualMouse.nextClickHoldDelayMs(null);
			assertTrue("click hold delay " + delay + " below floor", delay >= 25);
			assertTrue("click hold delay " + delay + " above ceiling", delay <= 90);
		}
	}

	@Test
	public void clickReleaseDelayFallsBackToHumanBoundsWithoutPlan() {
		for (int i = 0; i < 10_000; i++) {
			int delay = VirtualMouse.nextClickReleaseDelayMs(null);
			assertTrue("click release delay " + delay + " below floor", delay >= 25);
			assertTrue("click release delay " + delay + " above ceiling", delay <= 90);
		}
	}

	@Test
	public void virtualMouseDoesNotBranchOnNaturalMouseFlag() throws IOException {
		List<String> hits = scanFieldReads(
				VirtualMouse.class,
				"net/runelite/client/plugins/cupidbot/util/antiban/Rs2AntibanSettings",
				"naturalMouse");
		assertTrue(
				"VirtualMouse.class must not gate motion on Rs2AntibanSettings.naturalMouse — " +
						"the trajectory is now unconditional so MenuOptionClicked always has a preceding mouse path (P4-a). " +
						"Found reads in: " + hits,
				hits.isEmpty());
	}

	@Test
	public void naturalMouseDefaultIsOn() throws ReflectiveOperationException {
		Class<?> settings = Class.forName(
				"net.runelite.client.plugins.cupidbot.util.antiban.Rs2AntibanSettings");
		Field f = settings.getField("naturalMouse");
		assertEquals(
				"Rs2AntibanSettings.naturalMouse default must be true — " +
						"motion is unconditional in VirtualMouse, and the post-click compensating sleeps in " +
						"Rs2Inventory / Rs2GrandExchange were unconditionalized, so there is no longer a reason to " +
						"leave this off by default. The flag now only toggles the click-point anchoring strategy and " +
						"a few hover-gate methods.",
				Boolean.TRUE,
				f.get(null));
	}

	@Test
	public void naturalMouseSystemCallsUseRawMoveInstantToAvoidRecursiveSmoothing() throws Exception {
		Class<?> target = Class.forName(
				"net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.NaturalMouse$SystemCallsImpl");

		List<String> rawCalls = scanMethodCalls(
				target,
				"net/runelite/client/plugins/cupidbot/util/mouse/Mouse",
				"moveInstant");
		List<String> highLevelCalls = scanMethodCalls(
				target,
				"net/runelite/client/plugins/cupidbot/util/mouse/Mouse",
				"move");

		assertTrue("NaturalMouse SystemCallsImpl must set each generated step through Mouse.moveInstant",
				rawCalls.stream().anyMatch(method -> method.startsWith("setMousePosition(")));
		assertTrue("NaturalMouse SystemCallsImpl must not call high-level Mouse.move from setMousePosition",
				highLevelCalls.stream().noneMatch(method -> method.startsWith("setMousePosition(")));
	}

	private static List<String> scanFieldReads(Class<?> target, String ownerInternal, String fieldName) throws IOException {
		try (InputStream is = classBytes(target)) {
			assertNotNull("class resource for " + target.getName() + " must be loadable for bytecode scan", is);
			ClassReader reader = new ClassReader(is.readAllBytes());
			List<String> found = new ArrayList<>();
			reader.accept(new ClassVisitor(Opcodes.ASM9) {
				@Override
				public MethodVisitor visitMethod(int access, String methodName, String descriptor,
				                                 String signature, String[] exceptions) {
					return new MethodVisitor(Opcodes.ASM9) {
						@Override
						public void visitFieldInsn(int opcode, String owner, String name, String desc) {
							if (opcode == Opcodes.GETSTATIC
									&& ownerInternal.equals(owner)
									&& fieldName.equals(name)) {
								found.add(methodName + descriptor);
							}
						}
					};
				}

				@Override
				public FieldVisitor visitField(int access, String name, String descriptor,
				                               String signature, Object value) {
					return null;
				}
			}, ClassReader.SKIP_FRAMES);
			return found;
		}
	}

	private static List<String> scanMethodCalls(Class<?> target, String ownerInternal, String methodName) throws IOException {
		try (InputStream is = classBytes(target)) {
			assertNotNull("class resource for " + target.getName() + " must be loadable for bytecode scan", is);
			ClassReader reader = new ClassReader(is.readAllBytes());
			List<String> found = new ArrayList<>();
			reader.accept(new ClassVisitor(Opcodes.ASM9) {
				@Override
				public MethodVisitor visitMethod(int access, String sourceMethod, String descriptor,
				                                 String signature, String[] exceptions) {
					return new MethodVisitor(Opcodes.ASM9) {
						@Override
						public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
							if ((opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKEINTERFACE)
									&& ownerInternal.equals(owner)
									&& methodName.equals(name)) {
								found.add(sourceMethod + descriptor);
							}
						}
					};
				}
			}, ClassReader.SKIP_FRAMES);
			return found;
		}
	}

	private static InputStream classBytes(Class<?> target) {
		String resource = "/" + target.getName().replace('.', '/') + ".class";
		return target.getResourceAsStream(resource);
	}

	private static MouseMovementPlan movementPlanWithTuning(MouseMovementTuning tuning) {
		return new MouseMovementPlan(
				new Point(0, 0),
				MouseTarget.point(new Point(10, 10)),
				new Point(10, 10),
				MouseActionContext.GENERAL,
				MouseEngineMode.BALANCED,
				42L,
				14.0,
				1.0,
				120,
				0,
				0,
				0,
				60,
				tuning);
	}
}
