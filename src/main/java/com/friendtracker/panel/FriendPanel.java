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
import com.friendtracker.panel.components.FixedWidthPanel;
import com.friendtracker.panel.components.HiscorePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.QuantityFormatter;
import net.runelite.client.util.SwingUtil;

public class FriendPanel extends FixedWidthPanel {

    private final FriendTrackerPlugin plugin;
    private final FriendTrackerConfig config;

    private final Friend friend;

    private HiscoreResult displayedResult;

    // Box title components
    private final JPanel logTitle = new JPanel();
    private final JLabel overallLabel = new JLabel();

    // skill list components
    private final JPanel skillContainer = new JPanel();
    private final HiscorePanel hiscorePanel = new HiscorePanel();


    private final JButton collapseBtn = new JButton();

    private static final int TITLE_PADDING = 5;
    private static final ImageIcon COLLAPSE_ICON;
    private static final ImageIcon EXPAND_ICON;

    static
    {
        final BufferedImage collapseImg = ImageUtil.loadImageResource(FriendTrackerPlugin.class, "collapsed.png");
        final BufferedImage expandedImg = ImageUtil.loadImageResource(FriendTrackerPlugin.class, "expanded.png");

        COLLAPSE_ICON = new ImageIcon(collapseImg);
        EXPAND_ICON = new ImageIcon(expandedImg);
    }

    public FriendPanel(FriendTrackerPlugin plugin, FriendTrackerConfig config, Friend friend) {
        this.plugin = plugin;
        this.config = config;
        this.friend = friend;

        build();
    }

    public void build()
    {
        setLayout(new BorderLayout(0, 1));
        setBorder(new EmptyBorder(5, 0, 0, 0));

        logTitle.setLayout(new BoxLayout(logTitle, BoxLayout.X_AXIS));
        logTitle.setBorder(new EmptyBorder(7, 7, 7, 7));
        logTitle.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());

        SwingUtil.removeButtonDecorations(collapseBtn);
        collapseBtn.setIcon(COLLAPSE_ICON);
        collapseBtn.setSelectedIcon(EXPAND_ICON);
        SwingUtil.addModalTooltip(collapseBtn, "Collapse", "Expand");
        collapseBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
        collapseBtn.setUI(new BasicButtonUI()); // substance breaks the layout
        collapseBtn.addActionListener(ev -> changeCollapse());
        logTitle.add(collapseBtn);

        JLabel titleLabel = new JLabel();
        titleLabel.setText(friend.getName());
        titleLabel.setFont(FontManager.getRunescapeSmallFont());
        titleLabel.setForeground(Color.WHITE);
        // Set a size to make BoxLayout truncate the name
        titleLabel.setMinimumSize(new Dimension(1, titleLabel.getPreferredSize().height));
        logTitle.add(titleLabel);

        logTitle.add(Box.createRigidArea(new Dimension(TITLE_PADDING, 0)));
        logTitle.add(Box.createHorizontalGlue());
        logTitle.add(Box.createRigidArea(new Dimension(TITLE_PADDING, 0)));

        overallLabel.setFont(FontManager.getRunescapeSmallFont());
        overallLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        overallLabel.setIcon(new ImageIcon(ImageUtil.loadImageResource(FriendTrackerPlugin.class, "/skill_icons_small/overall.png")));
        logTitle.add(overallLabel);

        skillContainer.setBorder(new EmptyBorder(0,0,0,0));
        skillContainer.add(hiscorePanel);

        add(logTitle, BorderLayout.NORTH);
        add(skillContainer, BorderLayout.CENTER);

        collapse();
        redraw();
    }

    public void applyHiscoreResult(HiscoreResult result)
    {
        hiscorePanel.applyHiscoreResult(result);
        overallLabel.setText("+" + QuantityFormatter.quantityToStackSize(result.getOverall().getExperience()) + " xp");
        overallLabel.setToolTipText("+" + QuantityFormatter.formatNumber(result.getOverall().getExperience()) + " xp");
        displayedResult = result;
    }

    void redraw()
    {
        validate();
        repaint();
    }

    /**
     * Changes the collapse status of this box
     */
    private void changeCollapse()
    {
        if (isCollapsed())
        {
            expand();
        }
        else if (!isCollapsed())
        {
            collapse();
        }

        updateCollapseText();
    }

    private void collapse()
    {
        if (!isCollapsed())
        {
            skillContainer.setVisible(false);
            applyDimmer(false, logTitle);
        }
    }

    private void expand()
    {
        if (isCollapsed())
        {
            skillContainer.setVisible(true);
            applyDimmer(true, logTitle);
        }
    }

    private void updateCollapseText()
    {
        collapseBtn.setSelected(!isCollapsed());
    }

    private boolean isCollapsed()
    {
        return !skillContainer.isVisible();
    }

    private void applyDimmer(boolean brighten, JPanel panel)
    {
        for (Component component : panel.getComponents())
        {
            Color color = component.getForeground();

            component.setForeground(brighten ? color.brighter() : color.darker());
        }
    }

    public boolean meetsCriteria()
    {
        String nameLowercase = friend.getName().toLowerCase();
        List<String> descriptionLowercase = friend.getPreviousNames();
        if (plugin.getFriendTextFilter() != null &&
                !nameLowercase.contains(plugin.getFriendTextFilter()) &&
                !descriptionLowercase.contains(plugin.getFriendTextFilter()))
        {
            return false;
        }

        return true;
    }
}
