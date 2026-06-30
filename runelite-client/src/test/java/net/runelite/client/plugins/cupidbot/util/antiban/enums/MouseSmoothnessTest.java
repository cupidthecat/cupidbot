package net.runelite.client.plugins.cupidbot.util.antiban.enums;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MouseSmoothnessTest
{
	@Test
	public void sliderIndexesCoverEveryPresetInOrder()
	{
		MouseSmoothness[] presets = MouseSmoothness.values();

		assertEquals(5, presets.length);
		for (int i = 0; i < presets.length; i++)
		{
			assertEquals(i, presets[i].getSliderIndex());
			assertEquals(String.valueOf(i + 1), presets[i].getName());
			assertSame(presets[i], MouseSmoothness.fromSliderIndex(i));
		}
		assertSame(MouseSmoothness.STANDARD, MouseSmoothness.fromSliderIndex(-1));
		assertSame(MouseSmoothness.MAX, MouseSmoothness.fromSliderIndex(100));
	}

	@Test
	public void defaultMouseSmoothnessIsBalanced()
	{
		assertSame(MouseSmoothness.BALANCED, MouseSmoothness.DEFAULT);
	}

	@Test
	public void invalidConfigValuesUseDefault()
	{
		assertSame(MouseSmoothness.DEFAULT, MouseSmoothness.fromConfigValue(null));
		assertSame(MouseSmoothness.DEFAULT, MouseSmoothness.fromConfigValue(""));
		assertSame(MouseSmoothness.DEFAULT, MouseSmoothness.fromConfigValue("not-a-preset"));
	}

	@Test
	public void smoothnessPresetsIncreaseStepDensityAndGentleTrajectoryTexture()
	{
		MouseSmoothness previous = null;
		for (MouseSmoothness smoothness : MouseSmoothness.values())
		{
			assertTrue(smoothness.getTimeToStepsDivider() > 0.0);
			assertTrue(smoothness.getMinSteps() > 0);
			assertTrue(smoothness.getEffectFadeSteps() > 0);
			assertTrue(smoothness.getDeviationSlopeDivider() > 0.0);
			assertTrue(smoothness.getNoiseDivider() > 0.0);

			if (previous != null)
			{
				assertTrue(smoothness.getTimeToStepsDivider() < previous.getTimeToStepsDivider());
				assertTrue(smoothness.getMinSteps() > previous.getMinSteps());
				assertTrue(smoothness.getEffectFadeSteps() > previous.getEffectFadeSteps());
				assertTrue(smoothness.getDeviationSlopeDivider() > previous.getDeviationSlopeDivider());
				assertTrue(smoothness.getNoiseDivider() > previous.getNoiseDivider());
			}
			previous = smoothness;
		}
	}
}
