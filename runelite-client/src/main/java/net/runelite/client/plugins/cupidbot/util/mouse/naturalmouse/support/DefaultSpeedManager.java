package net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.support;


import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.api.SpeedManager;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.util.FlowTemplates;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class DefaultSpeedManager implements SpeedManager {
    private static final double SMALL_DELTA = 10e-6;
    private final List<Flow> flows = new ArrayList<>();
    private final Random random;
    private long mouseMovementTimeMs = 500;

    public DefaultSpeedManager(Collection<Flow> flows) {
        this(flows, new Random());
    }

    public DefaultSpeedManager(Collection<Flow> flows, Random random) {
        this.flows.addAll(flows);
        this.random = random == null ? new Random() : random;
    }

    public DefaultSpeedManager() {
        this(Arrays.asList(
                new Flow(FlowTemplates.constantSpeed()),
                new Flow(FlowTemplates.variatingFlow()),
                new Flow(FlowTemplates.interruptedFlow()),
                new Flow(FlowTemplates.interruptedFlow2()),
                new Flow(FlowTemplates.slowStartupFlow()),
                new Flow(FlowTemplates.slowStartup2Flow()),
                new Flow(FlowTemplates.adjustingFlow()),
                new Flow(FlowTemplates.jaggedFlow()),
                new Flow(FlowTemplates.stoppingFlow())
        ));
    }

    @Override
    public Pair<Flow, Long> getFlowWithTime(double distance) {
        double time = mouseMovementTimeMs + (long) (random.nextDouble() * mouseMovementTimeMs);
        Flow flow = flows.get(random.nextInt(flows.size()));

        // Let's ignore waiting time, e.g 0's in flow, by increasing the total time
        // by the amount of 0's there are in the flow multiplied by the time each bucket represents.
        double timePerBucket = time / (double) flow.getFlowCharacteristics().length;
        for (double bucket : flow.getFlowCharacteristics()) {
            if (Math.abs(bucket - 0) < SMALL_DELTA) {
                time += timePerBucket;
            }
        }

        return new Pair<>(flow, (long) time);
    }

    public void setMouseMovementBaseTimeMs(long mouseMovementSpeedMs) {
        this.mouseMovementTimeMs = mouseMovementSpeedMs;
    }
}
