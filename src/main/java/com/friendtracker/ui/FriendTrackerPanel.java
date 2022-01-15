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
package com.friendtracker.ui;

import com.friendtracker.Friend;
import com.friendtracker.FriendTrackerConfig;
import com.friendtracker.FriendTrackerPlugin;
import com.friendtracker.io.SaveManager;
import com.google.common.base.Strings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.hiscore.HiscoreClient;
import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import okhttp3.OkHttpClient;

@Slf4j
public class FriendTrackerPanel extends PluginPanel
{

    private final Client client;
    private final FriendTrackerPlugin plugin;
    private final FriendTrackerConfig config;
    private final HiscoreClient hiscoreClient;
    private final SaveManager saveManager;

    // List of friend boxes
    @Getter
    private final Map<String, FriendTrackerBox> friendBoxes = new HashMap<>();

    // Handle friend boxes
    private final JPanel friendBoxContainer = new JPanel();

    // Details and control
    private JPanel headerContainer = new JPanel();
    private JLabel titleLabel = new JLabel();
    private JButton refreshListBtn = new JButton();

    // Display if not refreshed
    private final PluginErrorPanel errorPanel = new PluginErrorPanel();

    public FriendTrackerPanel(@Nullable Client client, FriendTrackerPlugin plugin, FriendTrackerConfig config, SaveManager saveManager, OkHttpClient okHttpClient)
    {
        this.plugin = plugin;
        this.config = config;
        this.hiscoreClient = new HiscoreClient(okHttpClient);
        this.client = client;
        this.saveManager = saveManager;

        setBorder(new EmptyBorder(6, 6, 6, 6));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        // Create layout panel for wrapping
        final JPanel layoutPanel = new JPanel();
        layoutPanel.setLayout(new BoxLayout(layoutPanel, BoxLayout.Y_AXIS));
        add(layoutPanel, BorderLayout.NORTH);

        // Create header container
        headerContainer.setLayout(new BorderLayout());
        headerContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        headerContainer.setPreferredSize(new Dimension(0, 30));
        headerContainer.setBorder(new EmptyBorder(5, 5, 5, 10));

        // Create title label
        titleLabel.setText("Friend Tracker");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(FontManager.getRunescapeSmallFont());

        // Create refresh list button
        refreshListBtn.setText("Refresh List");
        refreshListBtn.addActionListener(e ->
        {
            plugin.refreshList();
        });

        headerContainer.add(titleLabel, BorderLayout.WEST);
        headerContainer.add(refreshListBtn, BorderLayout.EAST);

        // Create Friend box wrapper
        friendBoxContainer.setLayout(new BoxLayout(friendBoxContainer, BoxLayout.Y_AXIS));

        layoutPanel.add(headerContainer);
        layoutPanel.add(friendBoxContainer);

        // Add error pane
        errorPanel.setContent("Friend Tracker", "You have not checked your friends' xp yet.");
        add(errorPanel);
    }

    /**
     * Lookup the specified player on the normal hiscores
     *
     * @param playerName name of the player to lookup
     */
    public void lookup(String playerName)
    {
        final String sanitizedName = sanitize(playerName);

        if (Strings.isNullOrEmpty(sanitizedName))
        {
            return;
        }

        hiscoreClient.lookupAsync(sanitizedName, HiscoreEndpoint.NORMAL).whenCompleteAsync((result, ex) ->
                SwingUtilities.invokeLater(() ->
                {
                    if (result == null || ex != null)
                    {
                        if (ex != null)
                        {
                            log.warn("Error fetching Hiscore data " + ex.getMessage());
                        }
                        return;
                    }

                    //successful player search
                    applyHiscoreResult(result);
                }));
    }

    private void applyHiscoreResult(HiscoreResult result)
    {
        Friend friend = new Friend(result);

        FriendTrackerBox friendBox = new FriendTrackerBox(plugin, this, friend);

        friendBoxes.put(friend.getName(), friendBox);
        saveManager.addToSave(friend);

        SwingUtilities.invokeLater(() ->
        {
            friendBoxContainer.add(friendBox);
            friendBoxContainer.revalidate();
            friendBoxContainer.repaint();
        });

        remove(errorPanel);
    }

    public void reset() {
        for(FriendTrackerBox box : friendBoxes.values())
        {
            SwingUtilities.invokeLater(() -> friendBoxContainer.remove(box));
        }
        friendBoxes.clear();
    }

    public void rebuild()
    {
//        List<FriendTrackerBox> sortedList = friendBoxes.entrySet().stream()
//                .sorted(Comparator.comparingLong((Map.Entry<String,FriendTrackerBox> entry) -> entry.getValue().getFriend().getGainedSkillXP(OVERALL)))
//                .map((Map.Entry<String,FriendTrackerBox> entry) -> entry.getValue())
//                .collect(Collectors.toList());
//
//
//        for (FriendTrackerBox friendBox : sortedList)
//        {
//            SwingUtilities.invokeLater(() ->
//                    {
//                        friendBoxContainer.add(friendBox);
//                        friendBoxContainer.revalidate();
//                        friendBoxContainer.repaint();
//                    });
//        }

//        validate();
        revalidate();
        repaint();
    }

    /**
     * Replace no-break space characters with regular spaces in the given string
     *
     * @param lookup the string to sanitize
     * @return a string with spaces in place of no-break spaces
     */
    private static String sanitize(String lookup)
    {
        return lookup.replace('\u00A0', ' ');
    }


}
