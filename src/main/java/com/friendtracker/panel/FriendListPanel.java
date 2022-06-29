package com.friendtracker.panel;

import com.friendtracker.FriendTrackerConfig;
import com.friendtracker.FriendTrackerPlugin;
import com.friendtracker.data.FriendDataClient;
import com.friendtracker.friends.Friend;
import com.friendtracker.friends.FriendManager;
import com.friendtracker.panel.components.FixedWidthPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.Labeled;
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
