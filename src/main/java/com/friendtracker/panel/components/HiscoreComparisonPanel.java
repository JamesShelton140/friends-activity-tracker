package com.friendtracker.panel.components;

import com.friendtracker.FriendTrackerPlugin;
import com.friendtracker.data.HiscoreKeys;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Experience;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import static net.runelite.client.hiscore.HiscoreSkill.*;
import net.runelite.client.hiscore.HiscoreSkillType;
import net.runelite.client.hiscore.Skill;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.QuantityFormatter;
import org.apache.commons.lang3.StringUtils;


@Slf4j
public class HiscoreComparisonPanel extends FixedWidthPanel
{

    private final Map<HiscoreSkill, CompLabel> compSkillLabels = new EnumMap<>(HiscoreSkill.class);

    @Data
    @AllArgsConstructor
    private class CompLabel
    {
        private JLabel lowLabel;
        private JLabel diffLabel;
        private JLabel highLabel;
    }

    public HiscoreComparisonPanel()
    {
        createPanel();
    }

    private void createPanel()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(makeSkillComparisonPanel(OVERALL));

        for(HiscoreSkill skill : HiscoreKeys.SKILLS_COMPARISON_ORDER) //@todo change order to something more sensible. Maybe OSRS Hiscore order.
        {
            add(makeSkillComparisonPanel(skill));
        }

        add(makeSkillComparisonPanel(CLUE_SCROLL_ALL));
        add(makeSkillComparisonPanel(LEAGUE_POINTS));
        add(makeSkillComparisonPanel(LAST_MAN_STANDING));
        add(makeSkillComparisonPanel(SOUL_WARS_ZEAL));
        add(makeSkillComparisonPanel(RIFTS_CLOSED));
        add(makeSkillComparisonPanel(BOUNTY_HUNTER_ROGUE));
        add(makeSkillComparisonPanel(BOUNTY_HUNTER_HUNTER));

        for(HiscoreSkill skill : HiscoreKeys.BOSSES)
        {
            add(makeSkillComparisonPanel(skill));
        }
    }

    private JPanel makeSkillComparisonPanel(HiscoreSkill skill)
    {
        JLabel lowLabel = getSkillLabel(skill);
        lowLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel highLabel = getSkillLabel(skill);
        highLabel.setAlignmentX(RIGHT_ALIGNMENT);
//        highLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        highLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel diffLabel = getSkillLabelWithIcon(skill);
        diffLabel.setAlignmentX(CENTER_ALIGNMENT);
        diffLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel skillPanel = new JPanel();
        skillPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        skillPanel.setBorder(new EmptyBorder(2, 0, 2, 0));
        skillPanel.setLayout(new GridLayout(1,3));

        CompLabel compLabel = new CompLabel(lowLabel, diffLabel, highLabel);

        compSkillLabels.put(skill, compLabel);

        skillPanel.add(lowLabel);
        skillPanel.add(diffLabel);
        skillPanel.add(highLabel);

        return skillPanel;
    }

    private JLabel getSkillLabel(HiscoreSkill skill) //@todo move to util class
    {
        HiscoreSkillType skillType = skill == null ? HiscoreSkillType.SKILL : skill.getType();

        JLabel label = new JLabel();
        label.setToolTipText(skill == null ? "Combat" : skill.getName()); // Do not need to consider "Combat" for comparison panel
        label.setFont(FontManager.getRunescapeSmallFont());
//        label.setText(HiscoreUtil.pad("--", skillType));
        label.setText(StringUtils.leftPad("--", 2));

        return label;
    }

    private JLabel getSkillLabelWithIcon(HiscoreSkill skill) //@todo move to util class
    {
        JLabel label = getSkillLabel(skill);

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

        String skillName = (skill == null ? "combat" : skill.name().toLowerCase()); // Do not need to consider "Combat" for comparison panel
        String skillIcon = directory + skillName + ".png";
        log.debug("Loading skill icon from {}", skillIcon);

        label.setIcon(new ImageIcon(ImageUtil.loadImageResource(FriendTrackerPlugin.class, skillIcon)));

        boolean totalLabel = skill == OVERALL || skill == null; //overall or combat
        label.setIconTextGap(totalLabel ? 10 : 4);
        return label;
    }

    public void resetComparisonPanel()
    {
        for (Map.Entry<HiscoreSkill, CompLabel> entry : compSkillLabels.entrySet())
        {
            entry.getValue().diffLabel.setText(StringUtils.leftPad("--", 2));

            entry.getValue().highLabel.setText(StringUtils.leftPad("--", 2));

            entry.getValue().lowLabel.setText(StringUtils.leftPad("--", 2));
        }
    }

    public void applyHiscoreResult(HiscoreResult lowResult, HiscoreResult highResult)
    {
        assert SwingUtilities.isEventDispatchThread();

//        resetComparisonPanel();

        for (Map.Entry<HiscoreSkill, CompLabel> entry : compSkillLabels.entrySet())
        {
            HiscoreSkill skill = entry.getKey();
            JLabel highLabel = entry.getValue().highLabel;
            JLabel diffLabel = entry.getValue().diffLabel;
            JLabel lowLabel = entry.getValue().lowLabel;
            Skill s;

            for (String resultName : new String[]{"low", "high"})
            {
                HiscoreResult result = resultName.equals("low") ? lowResult : highResult;
                JLabel label = resultName.equals("low") ? lowLabel : highLabel;

                // reset label
                label.setText(StringUtils.leftPad("--", 2));
                label.setToolTipText(skill == null ? "Combat" : skill.getName());

                if ((s = result.getSkill(skill)) != null)
                {
                    final long exp = s.getExperience();
                    final boolean isSkill = skill.getType() == HiscoreSkillType.SKILL;
                    int level = -1;

                    if (!isSkill || exp != -1L) {
                        // for skills, level is only valid if exp is not -1
                        // otherwise level is always valid
                        level = s.getLevel();
                    }

                    if (level != -1)
                    {
                        label.setText(StringUtils.leftPad(HiscoreUtil.formatLevel(level), 2));
                    }
                }
                label.setToolTipText(HiscoreUtil.detailsHtml(result, skill));
            }

            Skill hs, ls;

            // reset label
            diffLabel.setText(StringUtils.leftPad("--", 2));

            if ((hs = highResult.getSkill(skill)) != null)
            {
                final long highExp = hs.getExperience();
                final long lowExp = ((ls = lowResult.getSkill(skill)) != null) ? ls.getExperience() : 0;
                int exp = -1;
                final boolean isSkill = skill.getType() == HiscoreSkillType.SKILL;
                int level = -1;

                // set difference to xp for skills
                if (!isSkill || highExp != -1L) //@todo consider changing to (isSkill && highExp != -1L)
                {
                    // for skills, level is only valid if exp is not -1
                    // otherwise level is always valid
                    exp = Math.toIntExact(highExp);

                    if(lowExp != -1L)
                    {
                        exp -= lowExp;
                    }
                }

                if ((isSkill && exp != -1))
                {
                    diffLabel.setText(HiscoreUtil.pad(QuantityFormatter.quantityToRSDecimalStack(exp, true), skill.getType())); //@todo consider moving into above if block
                }

                // set difference to level for non-skills (bosses etc.)
                if (!isSkill && hs.getLevel() != -1)
                {
                    level = hs.getLevel();

                    if(ls != null && ls.getLevel() != -1)
                    {
                        level -= ls.getLevel();
                    }
                }

                if((!isSkill && level != -1))
                {
                    diffLabel.setText(HiscoreUtil.pad(HiscoreUtil.formatLevel(level), skill.getType())); //@todo consider moving into above if block
                }
            }
        }
    }
}
