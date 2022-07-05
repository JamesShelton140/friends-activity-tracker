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
import com.friendtracker.friends.Friend;
import com.friendtracker.friends.FriendManager;
import com.friendtracker.panel.components.FixedWidthPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;

@Slf4j
public class FriendListPanel extends FixedWidthPanel
{
    private final FriendTrackerPlugin plugin;
    private final FriendTrackerConfig config;
    private final JButton refreshListBtn = new JButton();
    JPanel listWrapper = new JPanel();


    public FriendListPanel(FriendTrackerPlugin plugin, FriendTrackerConfig config)
    {
        this.plugin = plugin;
        this.config = config;
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        // Create refresh list button
        refreshListBtn.setText("Refresh List");
        refreshListBtn.addActionListener(e ->
        {
            plugin.refreshList();
        });

        this.add(refreshListBtn, BorderLayout.NORTH);

        listWrapper.setLayout(new BoxLayout(listWrapper, BoxLayout.Y_AXIS));

        this.add(listWrapper, BorderLayout.CENTER);
    }

    /**
     * Returns a sorted list of Friends retrieved from the current FriendManager.
     *
     * @return the sorted list of Friend objects
     */
    public List<Friend> getFriends()
    {
        FriendManager friendManager = plugin.getFriendManager();

        if(friendManager == null) return new ArrayList<>();

        return friendManager.getFriends().values()
                .stream()
                .sorted(Comparator.comparing(Friend::getName, String.CASE_INSENSITIVE_ORDER)) // Sort by name @Todo change to configurable sort comparator
                .collect(Collectors.toList());
    }

    public void refresh()
    {

    }

    public void redraw()
    {
        assert SwingUtilities.isEventDispatchThread();

        listWrapper.removeAll();

        getFriends().forEach(friend -> listWrapper.add(friend.generatePanel(plugin, config)));

        validate();
        repaint();
    }
}
