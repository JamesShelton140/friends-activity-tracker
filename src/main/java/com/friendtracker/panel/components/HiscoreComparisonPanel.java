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
package com.friendtracker.panel.components;

import com.friendtracker.data.HiscoreKeys;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import static net.runelite.client.hiscore.HiscoreSkill.*;
import net.runelite.client.hiscore.HiscoreSkillType;
import net.runelite.client.hiscore.Skill;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.QuantityFormatter;
import org.apache.commons.lang3.StringUtils;


@Slf4j
public class HiscoreComparisonPanel extends AbstractHiscorePanel
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

    protected void createPanel()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(makeSkillComparisonPanel(OVERALL));

        for(HiscoreSkill skill : HiscoreKeys.SKILLS_COMPARISON_ORDER)
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
        JLabel lowLabel = getSkillLabel(skill, HiscoreSkillType.SKILL);
        lowLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel highLabel = getSkillLabel(skill, HiscoreSkillType.SKILL);
        highLabel.setAlignmentX(RIGHT_ALIGNMENT);
        highLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel diffLabel = getSkillLabelWithIcon(skill, HiscoreSkillType.SKILL);
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

    public void resetComparisonPanel()
    {
        for (Map.Entry<HiscoreSkill, CompLabel> entry : compSkillLabels.entrySet())
        {
            entry.getValue().diffLabel.setText(StringUtils.leftPad("--", 2));

            entry.getValue().highLabel.setText(StringUtils.leftPad("--", 2));

            entry.getValue().lowLabel.setText(StringUtils.leftPad("--", 2));
        }
    }

    /**
     * Satisfies superclass. Use {@link HiscoreComparisonPanel#applyHiscoreResult(HiscoreResult, HiscoreResult)}.
     * @param result
     */
    public void applyHiscoreResult(HiscoreResult result)
    {
        applyHiscoreResult(result, result);
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
                        label.setText(StringUtils.leftPad(formatLevel(level), 2));
                    }
                }
                label.setToolTipText(detailsHtml(result, skill));
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
                if (isSkill && highExp != -1L)
                {
                    // for skills, level is only valid if exp is not -1
                    // otherwise level is always valid
                    exp = Math.toIntExact(highExp);

                    if(lowExp != -1L)
                    {
                        exp -= lowExp;
                    }

                    diffLabel.setText(pad(QuantityFormatter.quantityToRSDecimalStack(exp, true), HiscoreSkillType.SKILL));
                }

                // set difference to level for non-skills (bosses etc.)
                if (!isSkill && hs.getLevel() != -1)
                {
                    level = hs.getLevel();

                    if(ls != null && ls.getLevel() != -1)
                    {
                        level -= ls.getLevel();
                    }

                    diffLabel.setText(pad(formatLevel(level), HiscoreSkillType.SKILL));
                }
            }
        }
    }
}
