/*
 * Copyright (c) 2022, James Shelton <https://github.com/JamesShelton140>
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
package com.friendtracker.panel;

import com.friendtracker.FriendTrackerConfig;
import com.friendtracker.FriendTrackerPlugin;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.SwingUtil;

@Slf4j
public class FriendTrackerPanel extends PluginPanel
{

//    private final String ignoredBtnPath = "panel/components/ignored_button/";
    private final BufferedImage githubImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(FriendTrackerPlugin.class, "github.png"), 16, 16);
    private final Icon GITHUB_ICON = new ImageIcon(githubImage);
    private final Icon GITHUB_ROLLOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(githubImage, -180));

    private final BufferedImage discordImage = ImageUtil.resizeImage(ImageUtil.loadImageResource(FriendTrackerPlugin.class, "discord.png"), 16, 16);
    private final Icon DISCORD_ICON = new ImageIcon(discordImage);
    private final Icon DISCORD_ROLLOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(discordImage, -180));

    private final Client client;
    private final ConfigManager configManager;
    private final FriendTrackerPlugin plugin;
    private final FriendTrackerConfig config;

    // Display if not refreshed
    private final PluginErrorPanel errorPanel = new PluginErrorPanel();

    // Container for all panels to be displayed while the player is logged in
    private final JPanel loggedInPanel = new JPanel();

    // Panel that contains all FriendPanels to be displayed, and sort/filter controls
    private final FriendListPanel friendListPanel;

    // Container for mergePanels
    private final JPanel mergePanelContainer = new JPanel();
    private MergePanel currentMergePanel;
    // Collection of all active merge panels
    private final List<MergePanel> mergePanels = new ArrayList<>();

    private boolean loggedIn = false;

    private boolean mergeInProgress = false;

    public FriendTrackerPanel(@Nullable Client client, FriendTrackerPlugin plugin, FriendTrackerConfig config, ConfigManager configManager)
    {
        this.plugin = plugin;
        this.config = config;
        this.client = client;
        this.configManager = configManager;
        this.friendListPanel = new FriendListPanel(plugin, config, configManager);

        setBorder(new EmptyBorder(6, 6, 6, 6));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        // Create header container
        JPanel headerContainer = new JPanel();
        headerContainer.setLayout(new BorderLayout());
        headerContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
        headerContainer.setPreferredSize(new Dimension(0, 30));
        headerContainer.setBorder(new EmptyBorder(5, 5, 5, 10));

        // Create title label
        JLabel titleLabel = new JLabel();
        titleLabel.setText("Friend Tracker");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(FontManager.getRunescapeSmallFont());

        final JPanel headerButtons = new JPanel(new GridLayout(1, 2, 10, 0));
        headerButtons.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Create GitHub link button
        JButton githubButton = new JButton();
        SwingUtil.removeButtonDecorations(githubButton);
        githubButton.setBackground(ColorScheme.DARK_GRAY_COLOR);
        githubButton.setIcon(GITHUB_ICON);
        githubButton.setRolloverIcon(GITHUB_ROLLOVER_ICON);
        githubButton.setToolTipText("View source and submit issues on GitHub");
        githubButton.addActionListener(e ->
                {
                    LinkBrowser.browse("https://github.com/JamesShelton140/friends-tracker");
                });
        githubButton.setUI(new BasicButtonUI());
        headerButtons.add(githubButton);

        // Create Discord link button
        JButton discordButton = new JButton();
        SwingUtil.removeButtonDecorations(discordButton);
        discordButton.setBackground(ColorScheme.DARK_GRAY_COLOR);
        discordButton.setIcon(DISCORD_ICON);
        discordButton.setRolloverIcon(DISCORD_ROLLOVER_ICON);
        discordButton.setToolTipText("Report issues and make suggestion on Discord");
        discordButton.addActionListener(e ->
        {
            LinkBrowser.browse("https://discord.gg/Th7e3VCVWD");
        });
        discordButton.setUI(new BasicButtonUI());
        headerButtons.add(discordButton);

        headerContainer.add(titleLabel, BorderLayout.WEST);
        headerContainer.add(headerButtons, BorderLayout.EAST);
        add(headerContainer, BorderLayout.NORTH);

        loggedInPanel.setLayout(new BoxLayout(loggedInPanel, BoxLayout.Y_AXIS));
        loggedInPanel.add(friendListPanel);
        loggedInPanel.setVisible(false);

        mergePanelContainer.setLayout(new BorderLayout());
        loggedInPanel.add(mergePanelContainer);

        add(loggedInPanel, BorderLayout.CENTER);

        // Add error pane
        errorPanel.setContent("No friends found", "Log in to track friends' xp.");
        add(errorPanel, BorderLayout.SOUTH);
    }

    /**
     *
     * @param mergePanel
     * @return the ID of the Friend to merge into or an empty string to indicate a new Friend should be created instead
     */
    public void addMergePanel(MergePanel mergePanel)
    {
        mergePanels.add(mergePanel);

        if(!mergeInProgress)
        {
            mergeInProgress = true;
            drawNextMergePanel();
        }
    }

    public void removeMergePanel(MergePanel mergePanel)
    {
        mergePanels.remove(mergePanel);
    }

    public void drawNextMergePanel()
    {
        assert SwingUtilities.isEventDispatchThread();

        mergePanelContainer.removeAll();
        currentMergePanel = null;

        if(hasMergeToResolve())
        {
            currentMergePanel = mergePanels.remove(0);
            mergePanelContainer.add(currentMergePanel);
        }
        else
        {
            mergeInProgress = false;
        }

        redraw();
    }

    private boolean hasMergeToResolve()
    {
        return !mergePanels.isEmpty();
    }

    public void refresh()
    {
        assert SwingUtilities.isEventDispatchThread();

        // Hide all panels
        Arrays.stream(loggedInPanel.getComponents()).forEach(component -> component.setVisible(false));

        if(mergeInProgress)
        {
            // Show the mergePanelContainer
            mergePanelContainer.setVisible(true);
            currentMergePanel.refresh();
        }
        else
        {
            // return to default view
            friendListPanel.setVisible(true);
            friendListPanel.refresh();
        }

    }

    public void redraw()
    {
        assert SwingUtilities.isEventDispatchThread();

        if(!mergeInProgress)
        {
            friendListPanel.redraw();
        }

        refresh();

        revalidate();
        repaint();
    }

    public void setLoggedIn(boolean loggedIn)
    {
        assert SwingUtilities.isEventDispatchThread();

        if(loggedIn != this.loggedIn)
        {
            errorPanel.setVisible(!loggedIn);
            loggedInPanel.setVisible(loggedIn);

            this.loggedIn = loggedIn;
        }

        redraw();
    }

}
