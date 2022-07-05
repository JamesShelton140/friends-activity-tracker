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
package com.friendtracker.panel.components;

import com.friendtracker.FriendTrackerPlugin;
import com.friendtracker.data.HiscoreKeys;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
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

@Slf4j
public class HiscorePanel extends FixedWidthPanel
{

    // Not an enummap because we need null keys for combat
    private final Map<HiscoreSkill, JLabel> skillLabels = new HashMap<>();

    public HiscorePanel()
    {
        createPanel();
    }


    private void createPanel()
    {
        this.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        this.setBackground(ColorScheme.DARK_GRAY_COLOR);
        this.setLayout(new GridBagLayout());

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
        for (HiscoreSkill skill : HiscoreKeys.SKILLS)
        {
            JPanel panel = makeSkillPanel(skill);
            statsPanel.add(panel);
        }

        this.add(statsPanel, c);
        c.gridy++;

        JPanel totalPanel = new JPanel();
        totalPanel.setLayout(new GridLayout(1, 2));
        totalPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        totalPanel.add(makeSkillPanel(null)); //combat has no hiscore skill, referred to as null
        totalPanel.add(makeSkillPanel(OVERALL));

        this.add(totalPanel, c);
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

        this.add(minigamePanel, c);
        c.gridy++;

        JPanel bossPanel = new JPanel();
        bossPanel.setLayout(new GridLayout(0, 3));
        bossPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // For each boss on the hi-scores, create a Label and add it to the UI
        for (HiscoreSkill skill : HiscoreKeys.BOSSES)
        {
            JPanel panel = makeSkillPanel(skill);
            bossPanel.add(panel);
        }

        this.add(bossPanel, c);
        c.gridy++;
    }

    public JPanel makeSkillPanel(HiscoreSkill skill)
    {
        HiscoreSkillType skillType = skill == null ? HiscoreSkillType.SKILL : skill.getType();

        JLabel label = new JLabel();
        label.setToolTipText(skill == null ? "Combat" : skill.getName());
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setText(HiscoreUtil.pad("--", skillType));

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

    public void applyHiscoreResult(HiscoreResult result)
    {
        assert SwingUtilities.isEventDispatchThread();

        for (Map.Entry<HiscoreSkill, JLabel> entry : skillLabels.entrySet())
        {
            HiscoreSkill skill = entry.getKey();
            JLabel label = entry.getValue();
            Skill s;

            if (skill == null)
            {
                if (result.getPlayer() != null)
                {
                    int combatLevel = Experience.getCombatLevel(
                            result.getAttack().getLevel(),
                            result.getStrength().getLevel(),
                            result.getDefence().getLevel(),
                            result.getHitpoints().getLevel(),
                            result.getMagic().getLevel(),
                            result.getRanged().getLevel(),
                            result.getPrayer().getLevel()
                    );
                    label.setText(Integer.toString(combatLevel));
                }
            }
            else if ((s = result.getSkill(skill)) != null)
            {
                final long exp = s.getExperience();
                final boolean isSkill = skill.getType() == HiscoreSkillType.SKILL;
                int level = -1;

                if (!isSkill || exp != -1L)
                {
                    // for skills, level is only valid if exp is not -1
                    // otherwise level is always valid
                    level = s.getLevel();
                }

                if (level != -1)
                {
                    label.setText(HiscoreUtil.pad(HiscoreUtil.formatLevel(level), skill.getType()));
                }
            }

            label.setToolTipText(HiscoreUtil.detailsHtml(result, skill));
        }
    }

}
