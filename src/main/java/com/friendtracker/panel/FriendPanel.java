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
import com.friendtracker.data.FriendDataClient;
import com.friendtracker.friends.Friend;
import com.friendtracker.panel.components.FixedWidthPanel;
import com.google.common.collect.ImmutableList;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.hiscore.HiscoreSkill;
import static net.runelite.client.hiscore.HiscoreSkill.*;
import net.runelite.client.hiscore.HiscoreSkillType;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.QuantityFormatter;
import net.runelite.client.util.SwingUtil;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class FriendPanel extends FixedWidthPanel {

    /**
     * Real skills, ordered in the way they should be displayed in the panel.
     */
    private static final List<HiscoreSkill> SKILLS = ImmutableList.of(
            ATTACK, HITPOINTS, MINING,
            STRENGTH, AGILITY, SMITHING,
            DEFENCE, HERBLORE, FISHING,
            RANGED, THIEVING, COOKING,
            PRAYER, CRAFTING, FIREMAKING,
            MAGIC, FLETCHING, WOODCUTTING,
            RUNECRAFT, SLAYER, FARMING,
            CONSTRUCTION, HUNTER
    );

    /**
     * Bosses, ordered in the way they should be displayed in the panel.
     */
    private static final List<HiscoreSkill> BOSSES = ImmutableList.of(
            ABYSSAL_SIRE, ALCHEMICAL_HYDRA, BARROWS_CHESTS,
            BRYOPHYTA, CALLISTO, CERBERUS,
            CHAMBERS_OF_XERIC, CHAMBERS_OF_XERIC_CHALLENGE_MODE, CHAOS_ELEMENTAL,
            CHAOS_FANATIC, COMMANDER_ZILYANA, CORPOREAL_BEAST,
            DAGANNOTH_PRIME, DAGANNOTH_REX, DAGANNOTH_SUPREME,
            CRAZY_ARCHAEOLOGIST, DERANGED_ARCHAEOLOGIST, GENERAL_GRAARDOR,
            GIANT_MOLE, GROTESQUE_GUARDIANS, HESPORI,
            KALPHITE_QUEEN, KING_BLACK_DRAGON, KRAKEN,
            KREEARRA, KRIL_TSUTSAROTH, MIMIC,
            NEX, NIGHTMARE, PHOSANIS_NIGHTMARE,
            OBOR, SARACHNIS, SCORPIA,
            SKOTIZO, TEMPOROSS, THE_GAUNTLET,
            THE_CORRUPTED_GAUNTLET, THEATRE_OF_BLOOD, THEATRE_OF_BLOOD_HARD_MODE,
            THERMONUCLEAR_SMOKE_DEVIL, TZKAL_ZUK, TZTOK_JAD,
            VENENATIS, VETION, VORKATH,
            WINTERTODT, ZALCANO, ZULRAH
    );

    private final FriendTrackerPlugin plugin;
    private final FriendTrackerConfig config;
    private final FriendDataClient friendDataClient;

    private final Friend friend;

    // Box title components
    private final JPanel logTitle = new JPanel();
    private final JLabel overallLabel = new JLabel();

    // skill list components
    private final JPanel skillContainer = new JPanel();


    private final JButton collapseBtn = new JButton();

    // Not an enummap because we need null keys for combat
    private final Map<HiscoreSkill, JLabel> skillLabels = new HashMap<>();

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

    public FriendPanel(FriendTrackerPlugin plugin, FriendTrackerConfig config, FriendDataClient friendDataClient, Friend friend) {
        this.plugin = plugin;
        this.config = config;
        this.friend = friend;
        this.friendDataClient = friendDataClient;

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
        skillContainer.add(makeHiscorePanel());

        add(logTitle, BorderLayout.NORTH);
        add(skillContainer, BorderLayout.CENTER);

        collapse();
        rebuild();
    }

    void rebuild()
    {
        overallLabel.setText("+" + QuantityFormatter.quantityToStackSize(0) + " xp");
        overallLabel.setToolTipText("+" + QuantityFormatter.formatNumber(0) + " xp");

        validate();
        repaint();
    }

    public JPanel makeHiscorePanel()
    {
        JPanel hiscorePanel = new FixedWidthPanel();
        hiscorePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        hiscorePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        hiscorePanel.setLayout(new GridBagLayout());

        // Expand sub items to fit width of panel, align to top of panel
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.insets = new Insets(0, 0, 10, 0);

        // Panel that holds skill icons
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(8, 3));
        statsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        statsPanel.setBorder(new EmptyBorder(5, 0, 5, 0));

        // For each skill on the ingame skill panel, create a Label and add it to the UI
        for (HiscoreSkill skill : SKILLS)
        {
            JPanel panel = makeSkillPanel(skill);
            statsPanel.add(panel);
        }

        hiscorePanel.add(statsPanel, c);
        c.gridy++;

        JPanel totalPanel = new JPanel();
        totalPanel.setLayout(new GridLayout(1, 2));
        totalPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        totalPanel.add(makeSkillPanel(null)); //combat has no hiscore skill, referred to as null
        totalPanel.add(makeSkillPanel(OVERALL));

        hiscorePanel.add(totalPanel, c);
        c.gridy++;

        JPanel minigamePanel = new JPanel();
        // These aren't all on one row because when there's a label with four or more digits it causes the details
        // panel to change its size for some reason...
        minigamePanel.setLayout(new GridLayout(2, 3));
        minigamePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        minigamePanel.add(makeSkillPanel(CLUE_SCROLL_ALL));
        minigamePanel.add(makeSkillPanel(LEAGUE_POINTS));
        minigamePanel.add(makeSkillPanel(LAST_MAN_STANDING));
        minigamePanel.add(makeSkillPanel(SOUL_WARS_ZEAL));
        minigamePanel.add(makeSkillPanel(RIFTS_CLOSED));
        minigamePanel.add(makeSkillPanel(BOUNTY_HUNTER_ROGUE));
        minigamePanel.add(makeSkillPanel(BOUNTY_HUNTER_HUNTER));

        hiscorePanel.add(minigamePanel, c);
        c.gridy++;

        JPanel bossPanel = new JPanel();
        bossPanel.setLayout(new GridLayout(0, 3));
        bossPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // For each boss on the hi-scores, create a Label and add it to the UI
        for (HiscoreSkill skill : BOSSES)
        {
            JPanel panel = makeSkillPanel(skill);
            bossPanel.add(panel);
        }

        hiscorePanel.add(bossPanel, c);
        c.gridy++;

        return hiscorePanel;
    }

    public JPanel makeSkillPanel(HiscoreSkill skill)
    {
        HiscoreSkillType skillType = skill == null ? HiscoreSkillType.SKILL : skill.getType();

        JLabel label = new JLabel();
        label.setToolTipText(skill == null ? "Combat" : skill.getName());
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setText(pad("--", skillType));

        String directory;
        if (skill == null || skill == OVERALL)
        {
            directory = "/skill_icons/";
        }
        else if (skill.getType() == HiscoreSkillType.BOSS)
        {
            directory = "bosses/";
        }
        else
        {
            directory = "/skill_icons_small/";
        }

        String skillName = (skill == null ? "combat" : skill.name().toLowerCase());
        String skillIcon = directory + skillName + ".png";
        log.debug("Loading skill icon from {}", skillIcon);

        label.setIcon(new ImageIcon(ImageUtil.loadImageResource(FriendTrackerPlugin.class, skillIcon)));

        boolean totalLabel = skill == OVERALL || skill == null; //overall or combat
        label.setIconTextGap(totalLabel ? 10 : 4);

        JPanel skillPanel = new JPanel();
        skillPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        skillPanel.setBorder(new EmptyBorder(2, 0, 2, 0));
        skillLabels.put(skill, label);
        skillPanel.add(label);

        return skillPanel;
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

    private static String pad(String str, HiscoreSkillType type)
    {
        // Left pad label text to keep labels aligned
        int pad = type == HiscoreSkillType.BOSS ? 4 : 2;
        return StringUtils.leftPad(str, pad);
    }
}
