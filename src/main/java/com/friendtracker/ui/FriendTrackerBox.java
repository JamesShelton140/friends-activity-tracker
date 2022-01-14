package com.friendtracker.ui;

import com.friendtracker.Friend;
import com.friendtracker.FriendTrackerPlugin;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import lombok.Getter;
import net.runelite.client.hiscore.HiscoreSkill;
import static net.runelite.client.hiscore.HiscoreSkill.*;
import net.runelite.client.hiscore.HiscoreSkillType;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.QuantityFormatter;
import net.runelite.client.util.SwingUtil;
import net.runelite.client.util.Text;

public class FriendTrackerBox extends JPanel
{
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

    private static final int ITEMS_PER_ROW = 1;
    private static final int TITLE_PADDING = 5;
    private static final ImageIcon COLLAPSE_ICON;
    private static final ImageIcon EXPAND_ICON;

    // Map of labels to be displayed
    private final Map<JPanel, Long> skillLabels = new HashMap<>();

    @Getter
    private final Friend friend;

    // Box title components
    private final JPanel logTitle = new JPanel();
    private final JLabel overallLabel = new JLabel();

    // skill list components
    private final JPanel skillContainer = new JPanel();


    private final JButton collapseBtn = new JButton();

    static
    {
        final BufferedImage collapseImg = ImageUtil.loadImageResource(FriendTrackerPlugin.class, "collapsed.png");
        final BufferedImage expandedImg = ImageUtil.loadImageResource(FriendTrackerPlugin.class, "expanded.png");

        COLLAPSE_ICON = new ImageIcon(collapseImg);
        EXPAND_ICON = new ImageIcon(expandedImg);
    }

    public FriendTrackerBox(FriendTrackerPlugin plugin, FriendTrackerPanel panel, Friend friend)
    {
        this.friend = friend;

//        this.setLayout(new BorderLayout());
//
//        JLabel label = new JLabel(friend.getName());
//        label.setForeground(Color.WHITE);
//        this.add(label,BorderLayout.CENTER);
//        this.setVisible(true);

        setLayout(new BorderLayout(0, 1));
        setBorder(new EmptyBorder(5, 0, 0, 0));

        logTitle.setLayout(new BoxLayout(logTitle, BoxLayout.X_AXIS));
        logTitle.setBorder(new EmptyBorder(7, 7, 7, 7));
        logTitle.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
//        logTitle.setBackground(eventIgnored ? ColorScheme.DARKER_GRAY_HOVER_COLOR : ColorScheme.DARKER_GRAY_COLOR.darker());

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

//        subTitleLabel.setFont(FontManager.getRunescapeSmallFont());
//        subTitleLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

//        if (!Strings.isNullOrEmpty(subtitle))
//        {
//            subTitleLabel.setText(subtitle);
//        }

        logTitle.add(Box.createRigidArea(new Dimension(TITLE_PADDING, 0)));
//        logTitle.add(subTitleLabel);
        logTitle.add(Box.createHorizontalGlue());
        logTitle.add(Box.createRigidArea(new Dimension(TITLE_PADDING, 0)));

        overallLabel.setFont(FontManager.getRunescapeSmallFont());
        overallLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        overallLabel.setIcon(new ImageIcon(ImageUtil.loadImageResource(FriendTrackerPlugin.class, "/skill_icons_small/overall.png")));
        logTitle.add(overallLabel);

        add(logTitle, BorderLayout.NORTH);
        add(skillContainer, BorderLayout.CENTER);

        collapse();
        rebuild();
    }

    void rebuild()
    {
        buildSkills();

        overallLabel.setText("+" + QuantityFormatter.quantityToStackSize(friend.getGainedSkillXP(HiscoreSkill.OVERALL)) + " xp");
        overallLabel.setToolTipText("+" + QuantityFormatter.formatNumber(friend.getGainedSkillXP(HiscoreSkill.OVERALL)) + " xp");

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

    void collapse()
    {
        if (!isCollapsed())
        {
            skillContainer.setVisible(false);
            applyDimmer(false, logTitle);
        }
    }

    void expand()
    {
        if (isCollapsed())
        {
            skillContainer.setVisible(true);
            applyDimmer(true, logTitle);
        }
    }

    void updateCollapseText()
    {
        collapseBtn.setSelected(!isCollapsed());
    }

    boolean isCollapsed()
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

    /**
     * This method creates stacked skills from the friend represented by this box, calculates xp gained and then
     * displays all the skills that have changed in the UI.
     */
    void buildSkills()
    {

        skillContainer.removeAll();
        skillContainer.setLayout(new BoxLayout(skillContainer, BoxLayout.Y_AXIS));

        for (HiscoreSkill skill : HiscoreSkill.values())
        {
            // skip total skill and specific clue types
            if(skill.equals(OVERALL) || (skill.name().toLowerCase().contains("clue_scroll")
                                            && !skill.name().toLowerCase().contains("all"))) continue;

            final JPanel slotContainer = new JPanel();
            slotContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

            final JLabel skillLabel = new JLabel();

//            final JPanel slotContainer = new JPanel();
//            slotContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

            String directory;

            if(skill.getType() == HiscoreSkillType.BOSS)
            {
                directory = "bosses/";
            }
            else
            {
                directory = "/skill_icons_small/";
            }

            String skillName = skill.name().toLowerCase();
            String skillIcon = directory + skillName + ".png";

            skillLabel.setFont(FontManager.getRunescapeSmallFont());
            skillLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
            skillLabel.setVerticalAlignment(SwingConstants.CENTER);
            skillLabel.setHorizontalAlignment(SwingConstants.CENTER);
            skillLabel.setIcon(new ImageIcon(ImageUtil.loadImageResource(FriendTrackerPlugin.class, skillIcon)));

            String labelText = "+ " + QuantityFormatter.quantityToStackSize(friend.getGainedSkillXP(skill));
            if(skill.getType() == HiscoreSkillType.SKILL)
            {
                labelText += " xp";
            }

            skillLabel.setText(labelText);
            skillLabel.setToolTipText(QuantityFormatter.formatNumber(friend.getGainedSkillXP(skill)));

            slotContainer.add(skillLabel);
            skillLabels.put(slotContainer, friend.getGainedSkillXP(skill));
        }

        List<JPanel> sortedList = skillLabels.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map((Map.Entry<JPanel,Long> entry) -> entry.getKey())
                .collect(Collectors.toList());

        SwingUtilities.invokeLater(() ->
        {
            for (JPanel panel : sortedList)
            {
                skillContainer.add(panel);
            }
        });

        skillContainer.repaint();
    }


}
