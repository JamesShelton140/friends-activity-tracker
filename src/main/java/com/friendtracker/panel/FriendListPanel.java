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
import com.friendtracker.config.ConfigValues;
import com.friendtracker.friends.Friend;
import com.friendtracker.friends.FriendManager;
import com.friendtracker.panel.components.FixedWidthPanel;
import com.friendtracker.panel.components.SearchBox;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

@Slf4j
public class FriendListPanel extends FixedWidthPanel
{
    private final FriendTrackerPlugin plugin;
    private final FriendTrackerConfig config;
    private final ConfigManager configManager;
    //    private final JButton refreshListBtn = new JButton();
    JPanel listWrapper = new JPanel();


    public FriendListPanel(FriendTrackerPlugin plugin, FriendTrackerConfig config, ConfigManager configManager)
    {
        this.plugin = plugin;
        this.config = config;
        this.configManager = configManager;
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        this.add(createNorthPanel(), BorderLayout.NORTH);

        listWrapper.setLayout(new BoxLayout(listWrapper, BoxLayout.Y_AXIS));

        this.add(listWrapper, BorderLayout.CENTER);
    }

    private JPanel createNorthPanel()
    {
        JPanel northPanel = new FixedWidthPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

        // Create refresh list button
        JButton refreshListBtn =  new JButton("Refresh List");
        refreshListBtn.setFocusable(false);
        refreshListBtn.setAlignmentX(CENTER_ALIGNMENT);
        refreshListBtn.addActionListener(e -> plugin.refreshList());

        JComboBox<Enum> rangeDropdown = makeNewDropdown(ConfigValues.RangeOptions.values(), "selectedRange");
        rangeDropdown.setSelectedItem(config.selectedRange());
        JPanel rangePanel = makeDropdownPanel(rangeDropdown, "Range");

        JComboBox<Enum> sortDropdown = makeNewDropdown(ConfigValues.SortOptions.values(), "sortCriteria");
        sortDropdown.setSelectedItem(config.sortCriteria());
        JPanel sortPanel = makeDropdownPanel(sortDropdown, "Sort");

        northPanel.add(refreshListBtn);
        northPanel.add(Box.createVerticalStrut(5));
        northPanel.add(rangePanel);
        northPanel.add(Box.createVerticalStrut(2));
        northPanel.add(sortPanel);
        northPanel.add(Box.createVerticalStrut(5));
        northPanel.add(getSearchPanel());

        return northPanel;
    }

    private JComboBox<Enum> makeNewDropdown(Enum[] values, String key)
    {
        JComboBox<Enum> dropdown = new JComboBox<>(values);
        dropdown.setFocusable(false);
        dropdown.setForeground(Color.WHITE);
        dropdown.setPreferredSize(new Dimension(175, 24));
        dropdown.addActionListener(e -> {
            Enum selectedItem = dropdown.getItemAt(dropdown.getSelectedIndex());
            configManager.setConfiguration(FriendTrackerPlugin.CONFIG_GROUP_NAME, key, selectedItem);
            plugin.redraw();
        });
//        dropdown.addItemListener(e ->
//        {
//            if (e.getStateChange() == ItemEvent.SELECTED)
//            {
//                Enum source = (Enum) e.getItem();
//                questHelperPlugin.getConfigManager().setConfiguration("questhelper", key,
//                        source);
//            }
//        });

        return dropdown;
    }

    private JPanel makeDropdownPanel(JComboBox dropdown, String name)
    {
        // Filters
        JLabel filterName = new JLabel(name);
        filterName.setForeground(Color.WHITE);

        JPanel filtersPanel = new JPanel();
        filtersPanel.setLayout(new BorderLayout());
        filtersPanel.setMinimumSize(new Dimension(PluginPanel.PANEL_WIDTH, 0));
        filtersPanel.add(filterName, BorderLayout.CENTER);
        filtersPanel.add(dropdown, BorderLayout.EAST);

        return filtersPanel;
    }

    private JPanel getSearchPanel()
    {
        JPanel filtersPanel = new JPanel();
        filtersPanel.setMinimumSize(new Dimension(PluginPanel.PANEL_WIDTH, 0));
//        filtersPanel.setAlignmentX(LEFT_ALIGNMENT);
        filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));

        SearchBox textSearch = new SearchBox();
        textSearch.addTextChangedListener(() -> {
//            plugin.taskTextFilter = textSearch.getText().toLowerCase();
            refresh();
        });

        filtersPanel.add(textSearch);

        return filtersPanel;
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

        refresh();

        validate();
        repaint();
    }
}
