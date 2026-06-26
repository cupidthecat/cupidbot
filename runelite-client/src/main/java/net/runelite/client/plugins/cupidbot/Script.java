package net.runelite.client.plugins.cupidbot;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.cupidbot.shortestpath.ShortestPathPlugin;
import net.runelite.client.plugins.cupidbot.util.Global;
import net.runelite.client.plugins.cupidbot.agentserver.handler.ScriptHeartbeatRegistry;
import net.runelite.client.plugins.cupidbot.util.antiban.SessionFatigue;
import net.runelite.client.plugins.cupidbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.cupidbot.util.player.Rs2Player;
import net.runelite.client.plugins.cupidbot.util.walker.Rs2Walker;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for CupidBot automation scripts.
 * Provides scheduling helpers, guards against client-thread misuse, and common shutdown/reset logic.
 */
@Slf4j
public abstract class Script extends Global implements IScript {
    protected ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10,
        new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread t = new Thread(r);
                t.setName(Script.this.getClass().getSimpleName() + "-" + threadNumber.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        });
    protected ScheduledFuture<?> scheduledFuture;
    protected ScheduledFuture<?> mainScheduledFuture;

    /**
     * Indicates whether the main scheduled script loop is still active.
     */
    public boolean isRunning() {
        return mainScheduledFuture != null && !mainScheduledFuture.isDone();
    }

    @Getter
    protected static WorldPoint initialPlayerLocation;

    /**
     * Cancel scheduled tasks, clear shared state, and reset helpers.
     * Safe to call multiple times; no-ops if already shut down.
     */
    public void shutdown() {
        ScriptHeartbeatRegistry.remove(this.getClass().getName());
        if (mainScheduledFuture != null && !mainScheduledFuture.isDone()) {
            mainScheduledFuture.cancel(true);
            ShortestPathPlugin.exit();
            if (CupidBot.getClientThread().scheduledFuture != null)
                CupidBot.getClientThread().scheduledFuture.cancel(true);
            initialPlayerLocation = null;
            CupidBot.pauseAllScripts.set(false);
            Rs2Walker.disableTeleports = false;
            CupidBot.getSpecialAttackConfigs().reset();
        }
        if (scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(true);
        }
    }

    /**
     * Script shutdown cancels running tasks with interruption so long waits exit promptly.
     * Utility/client-thread calls wrap that signal in RuntimeException; classify it so
     * normal plugin toggles do not get logged as script failures.
     */
    public static boolean isInterruption(Throwable throwable) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (current instanceof InterruptedException) {
                return true;
            }

            String message = current.getMessage();
            if (message != null && message.contains("Interrupted waiting for client thread")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Default pre-loop guard invoked by script schedulers.
     * Returns {@code false} to pause a loop when a blocking event is executing, scripts are paused,
     * tutorial island is incomplete, or the current thread is interrupted.
     */
    public boolean run() {
        ScriptHeartbeatRegistry.recordHeartbeat(this.getClass().getName());

        if (CupidBot.isLoggedIn() && !SessionFatigue.isActive()) {
            SessionFatigue.startSession();
        }

        if (CupidBot.isLoggedIn() && !Rs2Player.hasCompletedTutorialIsland())
            return true;

        if (Rs2Player.hasCompletedTutorialIsland() && CupidBot.getBlockingEventManager().shouldBlockAndProcess()) {
            // A blocking event was found & is executing
            return false;
        }
        if (CupidBot.pauseAllScripts.get())
            return false;
        if (Thread.currentThread().isInterrupted())
            return false;

        if (CupidBot.isLoggedIn()) {
            boolean hasRunEnergy = CupidBot.getClientThread().runOnClientThreadOptional(() -> CupidBot.getClient().getEnergy()).orElse(0) > CupidBot.runEnergyThreshold;
            if (CupidBot.enableAutoRunOn && hasRunEnergy)
                Rs2Player.toggleRunEnergy(true);
            if (!hasRunEnergy && CupidBot.useStaminaPotsIfNeeded && Rs2Player.isMoving()) {
                Rs2Inventory.useRestoreEnergyItem();
            }
            CupidBot.getConfigManager().setConfiguration(CupidBotConfig.configGroup, CupidBotConfig.keyEnableAutoRunOn, CupidBot.enableAutoRunOn);
            CupidBot.getConfigManager().setConfiguration(CupidBotConfig.configGroup, CupidBotConfig.keyUseStaminaPotsIfNeeded, CupidBot.useStaminaPotsIfNeeded);
        }
        return true;
    }
}
