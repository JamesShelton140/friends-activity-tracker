package com.friendtracker.panel;

import com.friendtracker.FriendTrackerConfig;
import com.friendtracker.FriendTrackerPlugin;
import com.friendtracker.data.FriendDataClient;
import com.friendtracker.friends.Friend;
import com.friendtracker.panel.components.FixedWidthPanel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import net.runelite.client.ui.ColorScheme;

public class FriendListPanel extends FixedWidthPanel
{
    private final FriendTrackerPlugin plugin;
    private final FriendTrackerConfig config;
    private final FriendDataClient friendDataClient;

    public FriendListPanel(FriendTrackerPlugin plugin, FriendTrackerConfig config, FriendDataClient friendDataClient)
    {
        this.plugin = plugin;
        this.config = config;
        this.friendDataClient = friendDataClient;
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Friend friend = new Friend("Test Friend");
        add(friend.generatePanel(plugin, config, friendDataClient));
        Friend friend2 = new Friend("Test Friend 2");
        add(friend2.generatePanel(plugin, config, friendDataClient));
    }
}
