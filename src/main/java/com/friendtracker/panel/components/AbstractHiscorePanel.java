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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Experience;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import static net.runelite.client.hiscore.HiscoreSkill.*;
import net.runelite.client.hiscore.HiscoreSkillType;
import net.runelite.client.hiscore.Skill;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.QuantityFormatter;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public abstract class AbstractHiscorePanel extends FixedWidthPanel
{

    public AbstractHiscorePanel()
    {
    }

    protected abstract void createPanel();
    public abstract void applyHiscoreResult(HiscoreResult result);

    /**
     * Gets the Icon of the supplied HiscoreSkill
     *
     * @param skill the HiscoreSkill to get the icon of
     * @return the Icon for the given skill
     */
    public Icon getIcon(HiscoreSkill skill)
    {
        String directory;
        if (skill == null || skill == OVERALL)
        {
            directory = "/skill_icons/";
        }
        else if (skill.getType() == HiscoreSkillType.BOSS)
        {
            directory = "/net/runelite/client/plugins/hiscore/bosses/";
        }
        else
        {
            directory = "/skill_icons_small/";
        }

        String skillName = (skill == null ? "combat" : skill.name().toLowerCase());
        String skillIcon = directory + skillName + ".png";
        log.debug("Loading skill icon from {}", skillIcon);

        return new ImageIcon(ImageUtil.loadImageResource(FriendTrackerPlugin.class, skillIcon));
    }

    public JLabel getSkillLabel(HiscoreSkill skill, HiscoreSkillType padType)
    {
        JLabel label = new JLabel();
        label.setToolTipText(skill == null ? "Combat" : skill.getName());
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setText(pad("--", padType));

        return label;
    }

    public JLabel getSkillLabelWithIcon(HiscoreSkill skill, HiscoreSkillType padType)
    {
        JLabel label = getSkillLabel(skill, padType);

        label.setIcon(getIcon(skill));

        boolean totalLabel = skill == OVERALL || skill == null; //overall or combat
        label.setIconTextGap(totalLabel ? 10 : 4);

        return label;
    }

    public JLabel getSkillLabelWithIcon(HiscoreSkill skill)
    {
        HiscoreSkillType skillType = skill == null ? HiscoreSkillType.SKILL : skill.getType();

        return getSkillLabelWithIcon(skill, skillType);
    }

    /**
     * Builds a html string to display on tooltip (when hovering a skill).
     */
    public String detailsHtml(HiscoreResult result, HiscoreSkill skill)
    {
        String openingTags = "<html><body style = 'padding: 5px;color:#989898'>";
        String closingTags = "</html><body>";

        String content = "";

        if (skill == null)
        {
            double combatLevel = Experience.getCombatLevelPrecise(
                    result.getSkill(ATTACK).getLevel(),
                    result.getSkill(STRENGTH).getLevel(),
                    result.getSkill(DEFENCE).getLevel(),
                    result.getSkill(HITPOINTS).getLevel(),
                    result.getSkill(MAGIC).getLevel(),
                    result.getSkill(RANGED).getLevel(),
                    result.getSkill(PRAYER).getLevel()
            );

            double combatExperience = result.getSkill(ATTACK).getExperience()
                    + result.getSkill(STRENGTH).getExperience() + result.getSkill(DEFENCE).getExperience()
                    + result.getSkill(HITPOINTS).getExperience() + result.getSkill(MAGIC).getExperience()
                    + result.getSkill(RANGED).getExperience() + result.getSkill(PRAYER).getExperience();

            content += "<p><span style = 'color:white'>Combat</span></p>";
            content += "<p><span style = 'color:white'>Exact Combat Level:</span> " + QuantityFormatter.formatNumber(combatLevel) + "</p>";
            content += "<p><span style = 'color:white'>Experience:</span> " + QuantityFormatter.formatNumber(combatExperience) + "</p>";
        }
        else
        {
            switch (skill)
            {
                case CLUE_SCROLL_ALL:
                {
                    content += "<p><span style = 'color:white'>Clues</span></p>";
                    content += buildClueLine(result, "All", CLUE_SCROLL_ALL);
                    content += buildClueLine(result, "Beginner", CLUE_SCROLL_BEGINNER);
                    content += buildClueLine(result, "Easy", CLUE_SCROLL_EASY);
                    content += buildClueLine(result, "Medium", CLUE_SCROLL_MEDIUM);
                    content += buildClueLine(result, "Hard", CLUE_SCROLL_HARD);
                    content += buildClueLine(result, "Elite", CLUE_SCROLL_ELITE);
                    content += buildClueLine(result, "Master", CLUE_SCROLL_MASTER);
                    break;
                }
                case BOUNTY_HUNTER_ROGUE:
                case BOUNTY_HUNTER_HUNTER:
                case PVP_ARENA_RANK:
                case LAST_MAN_STANDING:
                case SOUL_WARS_ZEAL:
                case RIFTS_CLOSED:
                {
                    content += buildMinigameTooltip(result.getSkill(skill), skill);
                    break;
                }
                case LEAGUE_POINTS:
                {
                    Skill leaguePoints = result.getSkill(LEAGUE_POINTS);
                    String rank = (leaguePoints.getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(leaguePoints.getRank());
                    content += "<p><span style = 'color:white'>League Points</span></p>";
                    content += "<p><span style = 'color:white'>Rank:</span> " + rank + "</p>";
                    if (leaguePoints.getLevel() > -1)
                    {
                        content += "<p><span style = 'color:white'>Points:</span> " + QuantityFormatter.formatNumber(leaguePoints.getLevel()) + "</p>";
                    }
                    break;
                }
                case OVERALL:
                {
                    Skill requestedSkill = result.getSkill(skill);
                    String rank = (requestedSkill.getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(requestedSkill.getRank());
                    String exp = (requestedSkill.getExperience() == -1L) ? "Unranked" : QuantityFormatter.formatNumber(requestedSkill.getExperience());
                    content += "<p><span style = 'color:white'>" + skill.getName() + "</span></p>";
                    content += "<p><span style = 'color:white'>Rank:</span> " + rank + "</p>";
                    content += "<p><span style = 'color:white'>Experience:</span> " + exp + "</p>";
                    break;
                }
                default:
                {
                    if (skill.getType() == HiscoreSkillType.BOSS)
                    {
                        String rank = "Unranked";
                        String lvl = null;
                        Skill requestedSkill = result.getSkill(skill);
                        if (requestedSkill != null)
                        {
                            if (requestedSkill.getRank() > -1)
                            {
                                rank = QuantityFormatter.formatNumber(requestedSkill.getRank());
                            }
                            if (requestedSkill.getLevel() > -1)
                            {
                                lvl = QuantityFormatter.formatNumber(requestedSkill.getLevel());
                            }
                        }

                        content += "<p><span style = 'color:white'>Boss:</span> " + skill.getName() + "</p>";
                        content += "<p><span style = 'color:white'>Rank:</span> " + rank + "</p>";
                        if (lvl != null)
                        {
                            content += "<p><span style = 'color:white'>KC:</span> " + lvl + "</p>";
                        }
                    }
                    else
                    {
                        Skill requestedSkill = result.getSkill(skill);
                        final long experience = requestedSkill.getExperience();

                        String rank = (requestedSkill.getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(requestedSkill.getRank());
                        String exp = (experience == -1L) ? "Unranked" : QuantityFormatter.formatNumber(experience);
                        String remainingXp;
                        if (experience == -1L)
                        {
                            remainingXp = "Unranked";
                        }
                        else
                        {
                            int currentLevel = Experience.getLevelForXp((int) experience);
                            remainingXp = (currentLevel + 1 <= Experience.MAX_VIRT_LEVEL) ? QuantityFormatter.formatNumber(Experience.getXpForLevel(currentLevel + 1) - experience) : "0";
                        }

                        content += "<p><span style = 'color:white'>Skill:</span> " + skill.getName() + "</p>";
                        content += "<p><span style = 'color:white'>Rank:</span> " + rank + "</p>";
                        content += "<p><span style = 'color:white'>Experience:</span> " + exp + "</p>";
                        content += "<p><span style = 'color:white'>Remaining XP:</span> " + remainingXp + "</p>";
                    }
                    break;
                }
            }
        }

        // Add a html progress bar to the hover information
        if (skill != null && skill.getType() == HiscoreSkillType.SKILL)
        {
            long experience = result.getSkill(skill).getExperience();
            if (experience >= 0)
            {
                int currentXp = (int) experience;
                int currentLevel = Experience.getLevelForXp(currentXp);
                int xpForCurrentLevel = Experience.getXpForLevel(currentLevel);
                int xpForNextLevel = currentLevel + 1 <= Experience.MAX_VIRT_LEVEL ? Experience.getXpForLevel(currentLevel + 1) : -1;

                double xpGained = currentXp - xpForCurrentLevel;
                double xpGoal = xpForNextLevel != -1 ? xpForNextLevel - xpForCurrentLevel : 100;
                int progress = (int) ((xpGained / xpGoal) * 100f);

                // had to wrap the bar with an empty div, if i added the margin directly to the bar, it would mess up
                content += "<div style = 'margin-top:3px'>"
                        + "<div style = 'background: #070707; border: 1px solid #070707; height: 6px; width: 100%;'>"
                        + "<div style = 'height: 6px; width: " + progress + "%; background: #dc8a00;'>"
                        + "</div>"
                        + "</div>"
                        + "</div>";
            }
        }

        return openingTags + content + closingTags;
    }

    private String buildMinigameTooltip(Skill s, HiscoreSkill hiscoreSkill)
    {
        String rank = (s.getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(s.getRank());
        String content = "";
        content += "<p><span style = 'color:white'>" + hiscoreSkill.getName() + "</span></p>";
        content += "<p><span style = 'color:white'>Rank:</span> " + rank + "</p>";
        if (s.getLevel() > -1)
        {
            content += "<p><span style = 'color:white'>Score:</span> " + QuantityFormatter.formatNumber(s.getLevel()) + "</p>";
        }
        return content;
    }

    private String buildClueLine(HiscoreResult result, String name, HiscoreSkill skill)
    {
        Skill sk = result.getSkill(skill);
        String count = sk.getLevel() == -1
                ? "0"
                : QuantityFormatter.formatNumber(sk.getLevel());
        String rank = sk.getRank() == -1
                ? "Unranked"
                : QuantityFormatter.formatNumber(sk.getRank());
        return "<p><span style = 'color:white'>" + name + ":</span> " + count + " <span style = 'color:white'>Rank:</span> " + rank + "</p>";
    }

    /**
     * Formats given int with a 'k' suffix if it is >=10000
     *
     * @param level the int to format
     * @return a formatted string representing the value of the supplied int
     */
    public String formatLevel(int level)
    {
        if (level < 10000)
        {
            return Integer.toString(level);
        }
        else
        {
            return (level / 1000) + "k";
        }
    }

    /**
     * Left pad the given string with a number of spaces depending on the given type.
     *
     * @param str the string to pad with spaces
     * @param type the type used to determine pad size
     * @return the padded string
     */
    public String pad(String str, HiscoreSkillType type)
    {
        // Left pad label text to keep labels aligned
        int pad = type == HiscoreSkillType.BOSS ? 4 : 2;
        return StringUtils.leftPad(str, pad);
    }
}
