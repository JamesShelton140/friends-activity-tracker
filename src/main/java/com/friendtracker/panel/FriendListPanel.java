/*
 * Copyright (c) 2020, Zoinkwiz
 * Copyright (c) 2021, Tyler Hardy
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
import com.friendtracker.config.ComparatorFactory;
import com.friendtracker.config.ConfigValues;
import com.friendtracker.friends.Friend;
import com.friendtracker.friends.FriendManager;
import com.friendtracker.panel.components.FixedWidthPanel;
import com.friendtracker.panel.components.SearchBox;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
    private final ComparatorFactory comparatorFactory;
    //    private final JButton refreshListBtn = new JButton();
    private final JPanel listWrapper = new JPanel();

    public final ArrayList<FriendPanel> friendPanels = new ArrayList<>();


    public FriendListPanel(FriendTrackerPlugin plugin, FriendTrackerConfig config, ConfigManager configManager)
    {
        this.plugin = plugin;
        this.config = config;
        this.configManager = configManager;
        this.comparatorFactory = new ComparatorFactory(config);
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

        Enum[] rangeOptions = Arrays.stream(ConfigValues.RangeOptions.values()).filter(ConfigValues.RangeOptions::isCoreUnit).toArray(Enum[]::new);
        JComboBox<Enum> rangeDropdown = makeNewDropdown(rangeOptions, "selectedRange");
        rangeDropdown.setPreferredSize(new Dimension(115, 24));
        rangeDropdown.setSelectedItem(config.selectedRange());
        JComboBox<Integer> numberDropdown = makeNumberDropdown();
        numberDropdown.setSelectedItem(config.rangeNumber());
        JPanel rangePanel = makeDropdownPanel("Range", numberDropdown, rangeDropdown);

        JComboBox<Enum> sortDropdown = makeNewDropdown(ConfigValues.SortOptions.values(), "sortCriteria");
        sortDropdown.setSelectedItem(config.sortCriteria());
        JPanel sortPanel = makeDropdownPanel("Sort", sortDropdown);

        JComboBox<Enum> orderDropdown = makeNewDropdown(ConfigValues.OrderOptions.values(), "sortOrder");
        sortDropdown.setSelectedItem(config.sortOrder());
        JPanel orderPanel = makeDropdownPanel("Order", orderDropdown);

        northPanel.add(refreshListBtn);
        northPanel.add(Box.createVerticalStrut(5));
        northPanel.add(rangePanel);
        northPanel.add(Box.createVerticalStrut(2));
        northPanel.add(sortPanel);
        northPanel.add(Box.createVerticalStrut(2));
        northPanel.add(orderPanel);
        northPanel.add(Box.createVerticalStrut(5));
        northPanel.add(getSearchPanel());

        return northPanel;
    }

    private JComboBox<Integer> makeNumberDropdown()
    {
        String key = "rangeNumber";
        Integer[] values = IntStream.rangeClosed(1, 31).boxed().toArray(Integer[]::new);

        JComboBox<Integer> dropdown = new JComboBox<Integer>(values);
        dropdown.setFocusable(false);
        dropdown.setForeground(Color.WHITE);
        dropdown.setPreferredSize(new Dimension(60, 24));
        dropdown.addActionListener(e -> {
            Integer selectedItem = dropdown.getItemAt(dropdown.getSelectedIndex());
            configManager.setConfiguration(FriendTrackerPlugin.CONFIG_GROUP_NAME, key, selectedItem);
            plugin.redraw();
        });

        return dropdown;
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

        return dropdown;
    }

    private JPanel makeDropdownPanel(String name, JComboBox... dropdowns)
    {
        // Filters
        JLabel filterName = new JLabel(name);
        filterName.setForeground(Color.WHITE);

        JPanel filtersPanel = new JPanel();
        filtersPanel.setLayout(new BorderLayout());
        filtersPanel.setMinimumSize(new Dimension(PluginPanel.PANEL_WIDTH, 0));
        filtersPanel.add(filterName, BorderLayout.CENTER);

        JPanel dropdownPanel = new JPanel();
//        dropdownPanel.setLayout(new GridLayout(1,dropdowns.length));
        dropdownPanel.setLayout(new BoxLayout(dropdownPanel, BoxLayout.X_AXIS));
        for(JComboBox dropdown : dropdowns)
        {
            dropdownPanel.add(dropdown);
        }
        filtersPanel.add(dropdownPanel, BorderLayout.EAST);

        return filtersPanel;
    }

    private JPanel getSearchPanel()
    {
        JPanel filtersPanel = new JPanel();
        filtersPanel.setMinimumSize(new Dimension(PluginPanel.PANEL_WIDTH, 0));
        filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));

        SearchBox textSearch = new SearchBox();
        textSearch.addTextChangedListener(() -> {
            plugin.setFriendTextFilter(textSearch.getText().toLowerCase());
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
                .sorted(comparatorFactory.createFriendComparatorFromConfig())
                .collect(Collectors.toList());
    }

    public void refresh()
    {
        for(FriendPanel friendPanel : friendPanels)
        {
            friendPanel.setVisible(friendPanel.meetsCriteria());
        }
    }

    public void redraw()
    {
        assert SwingUtilities.isEventDispatchThread();

        listWrapper.removeAll();
        friendPanels.clear();

        getFriends().forEach(friend ->
        {
            FriendPanel friendPanel = friend.generatePanel(plugin, config);
            listWrapper.add(friendPanel);
            friendPanels.add(friendPanel);
        });

        refresh();

        validate();
        repaint();
    }
}
