/*
 * Copyright (c) 2026, CupidBot
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.tileindicators;

import java.awt.Graphics2D;
import java.lang.reflect.Constructor;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TileIndicatorsOverlayTest
{
	@Test
	public void testRenderSkipsTileIndicatorsDuringCutscenes() throws Exception
	{
		Client client = mock(Client.class);
		TileIndicatorsConfig config = mock(TileIndicatorsConfig.class);
		TileIndicatorsOverlay overlay = createOverlay(client, config);

		when(client.getVarbitValue(VarbitID.CUTSCENE_STATUS)).thenReturn(1);
		when(config.highlightCurrentTile()).thenReturn(true);

		overlay.render(mock(Graphics2D.class));

		verify(client, never()).getLocalPlayer();
	}

	private static TileIndicatorsOverlay createOverlay(Client client, TileIndicatorsConfig config) throws Exception
	{
		Constructor<TileIndicatorsOverlay> constructor = TileIndicatorsOverlay.class.getDeclaredConstructor(
			Client.class, TileIndicatorsConfig.class);
		constructor.setAccessible(true);
		return constructor.newInstance(client, config);
	}
}
