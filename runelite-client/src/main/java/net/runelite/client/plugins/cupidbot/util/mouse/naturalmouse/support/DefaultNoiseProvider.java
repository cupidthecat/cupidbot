package net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.support;

import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.api.NoiseProvider;

import java.util.Random;

public class DefaultNoiseProvider implements NoiseProvider {
    public static final double DEFAULT_NOISINESS_DIVIDER = 2;
    private static final double SMALL_DELTA = 10e-6;
    private final double noisinessDivider;
    private final double pathNoiseMultiplier;
    private final double microJitterMultiplier;
    private double driftX;
    private double driftY;

    /**
     * @param noisinessDivider bigger value means less noise.
     */
    public DefaultNoiseProvider(double noisinessDivider) {
        this(noisinessDivider, 1.0, 1.0);
    }

    public DefaultNoiseProvider(double noisinessDivider, double pathNoiseMultiplier, double microJitterMultiplier) {
        this.noisinessDivider = noisinessDivider;
        this.pathNoiseMultiplier = Math.max(0.0, pathNoiseMultiplier);
        this.microJitterMultiplier = Math.max(0.0, microJitterMultiplier);
    }

    @Override
    public DoublePoint getNoise(Random random, double xStepSize, double yStepSize) {
        if (Math.abs(xStepSize - 0) < SMALL_DELTA && Math.abs(yStepSize - 0) < SMALL_DELTA) {
            return DoublePoint.ZERO;
        }
        double noiseX = 0;
        double noiseY = 0;
        double stepSize = Math.hypot(xStepSize, yStepSize);
        double pathAmplitude = Math.max(0, (6 - Math.min(6, stepSize))) / Math.max(1.0, noisinessDivider) / 12.0;
        driftX = driftX * 0.86 + (random.nextDouble() - 0.5) * pathAmplitude * pathNoiseMultiplier;
        driftY = driftY * 0.86 + (random.nextDouble() - 0.5) * pathAmplitude * pathNoiseMultiplier;
        double noisiness = Math.max(0, (8 - stepSize)) / 50;
        if (random.nextDouble() < noisiness) {
            noiseX = (random.nextDouble() - 0.5) * Math.max(0, (8 - stepSize)) / noisinessDivider * microJitterMultiplier;
            noiseY = (random.nextDouble() - 0.5) * Math.max(0, (8 - stepSize)) / noisinessDivider * microJitterMultiplier;
        }
        return new DoublePoint(driftX + noiseX, driftY + noiseY);
    }
}
