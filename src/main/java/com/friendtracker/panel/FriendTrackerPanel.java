/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
 * Copyright (c) 2019, Bram91 <https://github.com/bram91>
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;

@Slf4j
public class FriendTrackerPanel extends PluginPanel
{

    private final Client client;
    private final FriendTrackerPlugin plugin;
    private final FriendTrackerConfig config;

    // Details and control
    private final JLabel titleLabel = new JLabel();

    // Display if not refreshed
    private final PluginErrorPanel errorPanel = new PluginErrorPanel();

    // Container for all panels to be displayed while the player is logged in
    private final JPanel loggedInPanel = new JPanel();

    // Panel that contains all FriendPanels to be displayed, and sort/filter controls
    private final FriendListPanel friendListPanel;

    // Container for mergePanels
    private final JPanel mergePanelContainer = new JPanel();
    // Collection of all active merge panels
    private final List<MergePanel> mergePanels = new ArrayList<>();

    private boolean loggedIn = false;

    private boolean mergeInProgress = false;

    public FriendTrackerPanel(@Nullable Client client, FriendTrackerPlugin plugin, FriendTrackerConfig config)
    {
        this.plugin = plugin;
        this.config = config;
//        this.hiscoreClient = new HiscoreClient(okHttpClient);
        this.client = client;
        this.friendListPanel = new FriendListPanel(plugin, config);

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
        titleLabel.setText("Friend Tracker");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(FontManager.getRunescapeSmallFont());

        headerContainer.add(titleLabel, BorderLayout.WEST);
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

        if(hasMergeToResolve())
        {
            mergePanelContainer.add(mergePanels.remove(0));
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

        Arrays.stream(loggedInPanel.getComponents()).forEach(component -> component.setVisible(false));

        if(mergeInProgress)
        {
            // Hide all panels other than mergePanelContainer

            // Show the mergePanelContainer
            mergePanelContainer.setVisible(true);
        }
        else
        {
            // Hide all panels other than friendListPanel
//            Arrays.stream(loggedInPanel.getComponents()).filter(component -> !component.equals(friendListPanel)).forEach(component -> component.setVisible(false));
            // return to default view
            friendListPanel.setVisible(true);
            friendListPanel.refresh();
        }

    }

    public void redraw()
    {
        assert SwingUtilities.isEventDispatchThread();
        log.info("mergeInProgress: {}", mergeInProgress);
//        invalidate();

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
