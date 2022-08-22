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

import com.google.common.base.CaseFormat;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.hiscore.HiscoreSkillType;
import net.runelite.client.hiscore.Skill;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class HiscoreUtil
{
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

    /**
     * Computes the per-skill difference between two HiscoreResults and returns a new HiscoreResult containing this.
     *
     * @param highResult the result to use as the base
     * @param lowResult the result to subtract from the base result
     * @return a HiscoreResult containing the per-skill difference of the parameter HiscoreResults
     */
    public static HiscoreResult getDifference(HiscoreResult highResult, HiscoreResult lowResult)
    {
        Map<HiscoreSkill, Skill> skills = new EnumMap<>(HiscoreSkill.class);
        String player = highResult.getPlayer();

        for(HiscoreSkill hiscoreSkill : HiscoreSkill.values())
        {
            Skill highSkill = highResult.getSkill(hiscoreSkill);
            Skill lowSkill = lowResult.getSkill(hiscoreSkill);

            Skill skill;
            if(highSkill != null && lowSkill != null)
            {
                skill = getDifference(highSkill, lowSkill);
            }
            else
            {
                skill = null;
            }

            skills.put(hiscoreSkill,skill);
        }

        return new HiscoreResult(player, skills);
    }

    /**
     * Computes the per-parameter difference between two Hiscore Skills and returns a new Hiscore Skills containing this.
     *
     * @param highSkill the skill to use as the base
     * @param lowSkill the skill to subtract from the base skill
     * @return a skill that is the difference between the parameter skills
     */
    public static Skill getDifference(Skill highSkill, Skill lowSkill)
    {
        int rank = (int)skillPropDiff(highSkill.getRank(), lowSkill.getRank());
        long experience = skillPropDiff(highSkill.getExperience(), lowSkill.getExperience());
        int level = (int)skillPropDiff(highSkill.getLevel(), lowSkill.getLevel());

        return new Skill(rank, level, experience);
    }

    /**
     * Computes the difference between high and low. Returns high if low equal minus one.
     *
     * @param high the number to use as the base
     * @param low the number to be subtracted from high
     * @return the difference between low and high, or high if low is negative
     */
    public static long skillPropDiff(long high, long low)
    {
        long diff = high;
        diff -= low == -1 ? 0 : low;
        return diff;
    }

    /**
     * Sums the level of all activity and boss HiscoreSkills of the supplied HiscoreResult.
     *
     * @param result the result to sum over
     * @return the sum total level of every non-skill HiscoreSkill
     */
    public static int sumNonSkillKc(HiscoreResult result)
    {
        if (result == null) return 0;

        int totalKc = 0;

        for(HiscoreSkill hiscoreSkill : HiscoreSkill.values())
        {
            if(hiscoreSkill.getType() == HiscoreSkillType.SKILL ||
                    hiscoreSkill.getType() == HiscoreSkillType.OVERALL ||
                    (hiscoreSkill.getName().contains("Clue Scrolls") && !hiscoreSkill.equals(HiscoreSkill.CLUE_SCROLL_ALL))
            ) continue;

            Skill skill = result.getSkill(hiscoreSkill);
            int level =  skill != null ? skill.getLevel() : 0;

            totalKc += level == -1 ? 0 : level;
        }

        return totalKc;
    }
}
