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

import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Experience;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.hiscore.HiscoreSkillType;
import net.runelite.client.hiscore.Skill;
import net.runelite.client.util.QuantityFormatter;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class HiscoreUtil {
    /**
     * Builds a html string to display on tooltip (when hovering a skill).
     */
    static String detailsHtml(HiscoreResult result, HiscoreSkill skill)
    {
        String openingTags = "<html><body style = 'padding: 5px;color:#989898'>";
        String closingTags = "</html><body>";

        String content = "";

        if (skill == null)
        {
            double combatLevel = Experience.getCombatLevelPrecise(
                    result.getAttack().getLevel(),
                    result.getStrength().getLevel(),
                    result.getDefence().getLevel(),
                    result.getHitpoints().getLevel(),
                    result.getMagic().getLevel(),
                    result.getRanged().getLevel(),
                    result.getPrayer().getLevel()
            );

            double combatExperience = result.getAttack().getExperience()
                    + result.getStrength().getExperience() + result.getDefence().getExperience()
                    + result.getHitpoints().getExperience() + result.getMagic().getExperience()
                    + result.getRanged().getExperience() + result.getPrayer().getExperience();

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
                    String allRank = (result.getClueScrollAll().getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(result.getClueScrollAll().getRank());
                    String beginnerRank = (result.getClueScrollBeginner().getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(result.getClueScrollBeginner().getRank());
                    String easyRank = (result.getClueScrollEasy().getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(result.getClueScrollEasy().getRank());
                    String mediumRank = (result.getClueScrollMedium().getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(result.getClueScrollMedium().getRank());
                    String hardRank = (result.getClueScrollHard().getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(result.getClueScrollHard().getRank());
                    String eliteRank = (result.getClueScrollElite().getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(result.getClueScrollElite().getRank());
                    String masterRank = (result.getClueScrollMaster().getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(result.getClueScrollMaster().getRank());
                    String all = (result.getClueScrollAll().getLevel() == -1 ? "0" : QuantityFormatter.formatNumber(result.getClueScrollAll().getLevel()));
                    String beginner = (result.getClueScrollBeginner().getLevel() == -1 ? "0" : QuantityFormatter.formatNumber(result.getClueScrollBeginner().getLevel()));
                    String easy = (result.getClueScrollEasy().getLevel() == -1 ? "0" : QuantityFormatter.formatNumber(result.getClueScrollEasy().getLevel()));
                    String medium = (result.getClueScrollMedium().getLevel() == -1 ? "0" : QuantityFormatter.formatNumber(result.getClueScrollMedium().getLevel()));
                    String hard = (result.getClueScrollHard().getLevel() == -1 ? "0" : QuantityFormatter.formatNumber(result.getClueScrollHard().getLevel()));
                    String elite = (result.getClueScrollElite().getLevel() == -1 ? "0" : QuantityFormatter.formatNumber(result.getClueScrollElite().getLevel()));
                    String master = (result.getClueScrollMaster().getLevel() == -1 ? "0" : QuantityFormatter.formatNumber(result.getClueScrollMaster().getLevel()));
                    content += "<p><span style = 'color:white'>Clues</span></p>";
                    content += "<p><span style = 'color:white'>All:</span> " + all + " <span style = 'color:white'>Rank:</span> " + allRank + "</p>";
                    content += "<p><span style = 'color:white'>Beginner:</span> " + beginner + " <span style = 'color:white'>Rank:</span> " + beginnerRank + "</p>";
                    content += "<p><span style = 'color:white'>Easy:</span> " + easy + " <span style = 'color:white'>Rank:</span> " + easyRank + "</p>";
                    content += "<p><span style = 'color:white'>Medium:</span> " + medium + " <span style = 'color:white'>Rank:</span> " + mediumRank + "</p>";
                    content += "<p><span style = 'color:white'>Hard:</span> " + hard + " <span style = 'color:white'>Rank:</span> " + hardRank + "</p>";
                    content += "<p><span style = 'color:white'>Elite:</span> " + elite + " <span style = 'color:white'>Rank:</span> " + eliteRank + "</p>";
                    content += "<p><span style = 'color:white'>Master:</span> " + master + " <span style = 'color:white'>Rank:</span> " + masterRank + "</p>";
                    break;
                }
                case BOUNTY_HUNTER_ROGUE:
                {
                    Skill bountyHunterRogue = result.getBountyHunterRogue();
                    String rank = (bountyHunterRogue.getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(bountyHunterRogue.getRank());
                    content += "<p><span style = 'color:white'>Bounty Hunter - Rogue</span></p>";
                    content += "<p><span style = 'color:white'>Rank:</span> " + rank + "</p>";
                    if (bountyHunterRogue.getLevel() > -1)
                    {
                        content += "<p><span style = 'color:white'>Score:</span> " + QuantityFormatter.formatNumber(bountyHunterRogue.getLevel()) + "</p>";
                    }
                    break;
                }
                case BOUNTY_HUNTER_HUNTER:
                {
                    Skill bountyHunterHunter = result.getBountyHunterHunter();
                    String rank = (bountyHunterHunter.getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(bountyHunterHunter.getRank());
                    content += "<p><span style = 'color:white'>Bounty Hunter - Hunter</span></p>";
                    content += "<p><span style = 'color:white'>Rank:</span> " + rank + "</p>";
                    if (bountyHunterHunter.getLevel() > -1)
                    {
                        content += "<p><span style = 'color:white'>Score:</span> " + QuantityFormatter.formatNumber(bountyHunterHunter.getLevel()) + "</p>";
                    }
                    break;
                }
                case LAST_MAN_STANDING:
                {
                    Skill lastManStanding = result.getLastManStanding();
                    String rank = (lastManStanding.getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(lastManStanding.getRank());
                    content += "<p><span style = 'color:white'>Last Man Standing</span></p>";
                    content += "<p><span style = 'color:white'>Rank:</span> " + rank + "</p>";
                    if (lastManStanding.getLevel() > -1)
                    {
                        content += "<p><span style = 'color:white'>Score:</span> " + QuantityFormatter.formatNumber(lastManStanding.getLevel()) + "</p>";
                    }
                    break;
                }
                case SOUL_WARS_ZEAL:
                {
                    Skill soulWarsZeal = result.getSoulWarsZeal();
                    String rank = (soulWarsZeal.getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(soulWarsZeal.getRank());
                    content += "<p><span style = 'color:white'>Soul Wars Zeal</span></p>";
                    content += "<p><span style = 'color:white'>Rank:</span> " + rank + "</p>";
                    if (soulWarsZeal.getLevel() > -1)
                    {
                        content += "<p><span style = 'color:white'>Score:</span> " + QuantityFormatter.formatNumber(soulWarsZeal.getLevel()) + "</p>";
                    }
                    break;
                }
                case RIFTS_CLOSED:
                {
                    Skill riftsClosed = result.getRiftsClosed();
                    String rank = (riftsClosed.getRank() == -1) ? "Unranked" : QuantityFormatter.formatNumber(riftsClosed.getRank());
                    content += "<p><span style = 'color:white'>Rifts closed</span></p>";
                    content += "<p><span style = 'color:white'>Rank:</span> " + rank + "</p>";
                    if (riftsClosed.getLevel() > -1)
                    {
                        content += "<p><span style = 'color:white'>Rifts:</span> " + QuantityFormatter.formatNumber(riftsClosed.getLevel()) + "</p>";
                    }
                    break;
                }
                case LEAGUE_POINTS:
                {
                    Skill leaguePoints = result.getLeaguePoints();
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

    /**
     * Formats given int with a 'k' suffix if it is >=10000
     *
     * @param level the int to format
     * @return a formatted string representing the value of the supplied int
     */
    static String formatLevel(int level)
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
    static String pad(String str, HiscoreSkillType type)
    {
        // Left pad label text to keep labels aligned
        int pad = type == HiscoreSkillType.BOSS ? 4 : 2;
        return StringUtils.leftPad(str, pad);
    }

    /**
     * Returns the HiscoreResult field name of the HiscoreSkill represented by the supplied string.
     *
     * If the String given as parameter does not represent a HiscoreSkill then the parameter will be returned
     * formatted to lower camel case.
     *
     * @param hiscoreSkill a string representing a HiscoreSkill in upper underscore case format
     * @return a string representing the same HiscoreSkill in lower camel case format
     */
    public static String hiscoreSkillToHiscoreResultSkill(String hiscoreSkill)
    {
        String skill = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, hiscoreSkill);

        switch (skill)
        {
            case "theGauntlet":
                skill = "gauntlet";
                break;
            case "theCorruptedGauntlet":
                skill = "corruptedGauntlet";
                break;
            case "tzkalZuk":
                skill = "tzKalZuk";
                break;
            case "tztokJad":
                skill = "tzTokJad";
                break;
        }

        return skill;
    }

    public static HiscoreResult getDifference(HiscoreResult highResult, HiscoreResult lowResult)
    {
        HiscoreResult result = new HiscoreResult();

        for(HiscoreSkill hiscoreSkill : HiscoreSkill.values())
        {
            Skill highSkill = highResult.getSkill(hiscoreSkill);
            Skill lowSkill = lowResult.getSkill(hiscoreSkill);

            Skill skill = getDifference(highSkill, lowSkill);

            String fieldName = HiscoreUtil.hiscoreSkillToHiscoreResultSkill(hiscoreSkill.name());
            try
            {
                HiscoreResult.class.getMethod("set" + StringUtils.capitalize(fieldName), Skill.class)
                        .invoke(result, skill);
            }
            catch (Exception e)
            {
                log.error("Failed to set {} for {} during deserialization. Exception: {}", fieldName, result.getPlayer(), e.getStackTrace());
            }
        }

        return result;
    }

    public static Skill getDifference(Skill highSkill, Skill lowSkill)
    {
        int rank = (int)skillPropDiff(highSkill.getRank(), lowSkill.getRank());
        long experience = skillPropDiff(highSkill.getExperience(), lowSkill.getExperience());
        int level = (int)skillPropDiff(highSkill.getLevel(), lowSkill.getLevel());

        return new Skill(rank, level, experience);
    }

    public static long skillPropDiff(long high, long low)
    {
        long diff = high;
        diff -= low == -1 ? 0 : low;
        return diff;
    }
}
