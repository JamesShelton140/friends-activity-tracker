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
package com.friendtracker.data;

import com.friendtracker.panel.components.HiscoreUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Map;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.hiscore.HiscoreSkillType;
import net.runelite.client.hiscore.Skill;

public class HiscoreResultSerializer implements JsonSerializer<HiscoreResult>
{
    @Override
    public JsonElement serialize(HiscoreResult result, Type type, JsonSerializationContext jsonSerializationContext)
    {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("player", result.getPlayer());

        Map<HiscoreSkill, Skill> skills = new EnumMap<>(result.getSkills());
        Skill skill;
        Gson gson = new Gson();

        // add skills with non-negative xp or level to json
        for(HiscoreSkill hiscoreSkill : HiscoreSkill.values())
        {
            skill = skills.getOrDefault(hiscoreSkill, null);
            // @todo change to test only rank if that is valid
            if(skill == null ||
                    (hiscoreSkill.getType() == HiscoreSkillType.SKILL && skill.getExperience() == -1L) ||
                    (hiscoreSkill.getType() != HiscoreSkillType.SKILL && skill.getLevel() == -1))
            {
                skills.remove(hiscoreSkill);
            }
        }

        jsonObject.add("skills", gson.toJsonTree(skills));

        return jsonObject;
    }

}
