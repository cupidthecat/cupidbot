/*
 * Copyright (c) 2018, Daniel Teo <https://github.com/takuyakanbr>
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
package net.runelite.client.plugins.cupidbot.ui;

import com.google.common.html.HtmlEscapers;
import lombok.Getter;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.config.SearchablePlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class CupidBotPluginListItem extends JPanel implements SearchablePlugin
{
	private static final ImageIcon ON_STAR;
	private static final ImageIcon OFF_STAR;
	static final int MIN_LIST_ITEM_HEIGHT = 20;
	static final int LIST_ITEM_VERTICAL_PADDING = 4;
	private static final String HTML_PREFIX = "<html>";
	private static final String HTML_SUFFIX = "</html>";
	private static final int PIN_BUTTON_WIDTH = 21;
	private static final int CONFIG_BUTTON_WIDTH = 25;
	private static final int TOGGLE_BUTTON_WIDTH = 34;
	private static final int TEXT_MARGIN_WIDTH = 12;

	private final CupidBotPluginListPanel pluginListPanel;

	@Getter
	private final CupidBotPluginConfigurationDescriptor pluginConfig;

	@Getter
	private final List<String> keywords = new ArrayList<>();

	private final JToggleButton pinButton;
	private final CupidBotPluginToggleButton onOffToggle;

	static
	{
		BufferedImage onStar = ImageUtil.loadImageResource(CupidBotConfigPanel.class, "star_on.png");
		ON_STAR = new ImageIcon(onStar);

		BufferedImage offStar = ImageUtil.luminanceScale(
			ImageUtil.grayscaleImage(onStar),
			0.77f
		);
		OFF_STAR = new ImageIcon(offStar);
	}

	CupidBotPluginListItem(CupidBotPluginListPanel pluginListPanel, CupidBotPluginConfigurationDescriptor pluginConfig)
	{
		this.pluginListPanel = pluginListPanel;
		this.pluginConfig = pluginConfig;

		Collections.addAll(keywords, pluginConfig.getName().toLowerCase().split(" "));
		Collections.addAll(keywords, pluginConfig.getDescription().toLowerCase().split(" "));
		Collections.addAll(keywords, pluginConfig.getTags());
		String internalName = pluginConfig.getInternalPluginHubName();
		if (internalName != null)
		{
			keywords.add("pluginhub");
			keywords.add(internalName);
		}
		else
		{
			keywords.add("plugin"); // we don't want searching plugin to only show hub plugins
		}

		setLayout(new BorderLayout(3, 0));

		JLabel nameLabel = createNameLabel(pluginConfig);
		setPreferredSize(new Dimension(CupidBotPluginListPanel.LIST_ITEM_WIDTH, preferredItemHeight(nameLabel)));

		pinButton = new JToggleButton(OFF_STAR);
		pinButton.setSelectedIcon(ON_STAR);
		SwingUtil.removeButtonDecorations(pinButton);
		SwingUtil.addModalTooltip(pinButton, "Unpin plugin", "Pin plugin");
		pinButton.setPreferredSize(new Dimension(PIN_BUTTON_WIDTH, 0));
		add(pinButton, BorderLayout.LINE_START);

		pinButton.addActionListener(e ->
		{
			pluginListPanel.savePinnedPlugins();
			pluginListPanel.refresh();
		});

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2));
		add(buttonPanel, BorderLayout.LINE_END);

		JMenuItem configMenuItem = null;
		if (pluginConfig.getConfigDescriptor() != null)
		{
			JButton configButton = new JButton(CupidBotConfigPanel.CONFIG_ICON);
			SwingUtil.removeButtonDecorations(configButton);
			configButton.setPreferredSize(new Dimension(CONFIG_BUTTON_WIDTH, 0));
			configButton.setVisible(false);
			buttonPanel.add(configButton);

			configButton.addActionListener(e ->
			{
				configButton.setIcon(CupidBotConfigPanel.CONFIG_ICON);
				openGroupConfigPanel();
			});

			configButton.setVisible(true);
			configButton.setToolTipText("Edit plugin configuration");

			configMenuItem = new JMenuItem("Configure");
			configMenuItem.addActionListener(e -> openGroupConfigPanel());
		}

		JMenuItem uninstallItem = null;
		if (internalName != null)
		{
			uninstallItem = new JMenuItem("Uninstall");
			uninstallItem.addActionListener(ev -> pluginListPanel.getExternalPluginManager().remove(internalName));
		}

		addLabelPopupMenu(nameLabel, configMenuItem, pluginConfig.createSupportMenuItem(pluginConfig.getPlugin()), uninstallItem);
		add(nameLabel, BorderLayout.CENTER);

		onOffToggle = new CupidBotPluginToggleButton();
		onOffToggle.setConflicts(pluginConfig.getConflicts());
		buttonPanel.add(onOffToggle);
		if (pluginConfig.getPlugin() != null)
		{
			PluginDescriptor pluginDescriptor = pluginConfig.getPlugin().getClass().getAnnotation(PluginDescriptor.class);

			onOffToggle.addActionListener(i ->
			{
				if (onOffToggle.isSelected())
				{
					pluginListPanel.startPlugin(pluginConfig.getPlugin());
				}
				else
				{
					pluginListPanel.stopPlugin(pluginConfig.getPlugin());
				}
			});

			if (pluginDescriptor.alwaysOn()) {
				onOffToggle.setEnabled(false);
				onOffToggle.setSelected(true);
				pluginListPanel.startPlugin(pluginConfig.getPlugin());
			}
			if (pluginDescriptor.disableOnStartUp()) {
				onOffToggle.setSelected(false);
				pluginListPanel.stopPlugin(pluginConfig.getPlugin());
			}
			if (pluginDescriptor.disable()) {
				onOffToggle.setEnabled(false);
				onOffToggle.setSelected(false);
				pluginListPanel.stopPlugin(pluginConfig.getPlugin());
			}
		}
		else
		{
			onOffToggle.setVisible(false);
		}
	}

	@NotNull
	private static JLabel createNameLabel(CupidBotPluginConfigurationDescriptor pluginConfig)
	{
		return createNameLabel(pluginConfig.getName(), pluginConfig.getDescription());
	}

	@NotNull
	static JLabel createNameLabel(String name, String description)
	{
		JLabel nameLabel = new JLabel(name);
		int buttons = PIN_BUTTON_WIDTH + CONFIG_BUTTON_WIDTH + TOGGLE_BUTTON_WIDTH + TEXT_MARGIN_WIDTH;
		int textWidth = CupidBotPluginListPanel.LIST_ITEM_WIDTH - buttons;
		String htmlName = toHtmlFragment(name);
		nameLabel.setText("<html><div style='width:" + textWidth + "px'>" + htmlName + "</div></html>");
		nameLabel.setForeground(Color.WHITE);
		nameLabel.setVerticalAlignment(SwingConstants.CENTER);

		if (!description.isEmpty())
		{
			nameLabel.setToolTipText("<html>" + htmlName + ":<br>" + toHtmlFragment(description) + "</html>");
		}
		return nameLabel;
	}

	static String toHtmlFragment(String text)
	{
		String trimmed = text.trim();
		if (trimmed.regionMatches(true, 0, HTML_PREFIX, 0, HTML_PREFIX.length()))
		{
			String fragment = trimmed.substring(HTML_PREFIX.length());
			if (fragment.regionMatches(true, fragment.length() - HTML_SUFFIX.length(), HTML_SUFFIX, 0, HTML_SUFFIX.length()))
			{
				fragment = fragment.substring(0, fragment.length() - HTML_SUFFIX.length());
			}
			return fragment;
		}

		return HtmlEscapers.htmlEscaper().escape(text);
	}

	static int preferredItemHeight(JLabel nameLabel)
	{
		return Math.max(MIN_LIST_ITEM_HEIGHT, nameLabel.getPreferredSize().height + LIST_ITEM_VERTICAL_PADDING);
	}

	@Override
	public String getSearchableName()
	{
		return pluginConfig.getName();
	}

	@Override
	public boolean isPinned()
	{
		return pinButton.isSelected();
	}

	void setPinned(boolean pinned)
	{
		pinButton.setSelected(pinned);
	}

	void setPluginEnabled(boolean enabled)
	{
		onOffToggle.setSelected(enabled);
	}

	private void openGroupConfigPanel()
	{
		pluginListPanel.openConfigurationPanel(pluginConfig);
	}

	/**
	 * Adds a mouseover effect to change the text of the passed label to {@link ColorScheme#BRAND_ORANGE} color, and
	 * adds the passed menu items to a popup menu shown when the label is clicked.
	 *
	 * @param label     The label to attach the mouseover and click effects to
	 * @param menuItems The menu items to be shown when the label is clicked
	 */
	static void addLabelPopupMenu(JLabel label, JMenuItem... menuItems)
	{
		final JPopupMenu menu = new JPopupMenu();
		final Color labelForeground = label.getForeground();
		menu.setBorder(new EmptyBorder(5, 5, 5, 5));

		for (final JMenuItem menuItem : menuItems)
		{
			if (menuItem == null)
			{
				continue;
			}

			// Some machines register mouseEntered through a popup menu, and do not register mouseExited when a popup
			// menu item is clicked, so reset the label's color when we click one of these options.
			menuItem.addActionListener(e -> label.setForeground(labelForeground));
			menu.add(menuItem);
		}

		label.addMouseListener(new MouseAdapter()
		{
			private Color lastForeground;

			@Override
			public void mouseClicked(MouseEvent mouseEvent)
			{
				Component source = (Component) mouseEvent.getSource();
				Point location = MouseInfo.getPointerInfo().getLocation();
				SwingUtilities.convertPointFromScreen(location, source);
				menu.show(source, location.x, location.y);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				lastForeground = label.getForeground();
				label.setForeground(ColorScheme.BRAND_ORANGE);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				label.setForeground(lastForeground);
			}
		});
	}
}
