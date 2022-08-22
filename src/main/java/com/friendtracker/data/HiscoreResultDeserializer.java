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
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.hiscore.Skill;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class HiscoreResultDeserializer implements JsonDeserializer<HiscoreResult>
{
    @Override
    public HiscoreResult deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
    {
        Gson gson = new Gson();
        HiscoreResult result;

        result = gson.fromJson(jsonElement, HiscoreResult.class);

        if(result.getSkills() == null)
        {
            return deserializeLegacyFormat(jsonElement, type, jsonDeserializationContext);
        }

        Map<HiscoreSkill, Skill> skills = result.getSkills();

        for(HiscoreSkill hiscoreSkill : HiscoreSkill.values())
        {
            if(!skills.containsKey(hiscoreSkill) || skills.get(hiscoreSkill) == null)
            {
                skills.put(hiscoreSkill, new Skill(-1,-1,-1));
            }
        }

        return new HiscoreResult(result.getPlayer(), skills);
    }

    public HiscoreResult deserializeLegacyFormat(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
    {
        log.info("Deserializing legacy HiscoreResult data.");
        Gson gson = new Gson();

        JsonObject jsonObject  = jsonElement.getAsJsonObject();
        Map<HiscoreSkill, Skill> skills = new EnumMap<>(HiscoreSkill.class);

        for(HiscoreSkill hiscoreSkill : HiscoreSkill.values())
        {
            String fieldName = HiscoreUtil.hiscoreSkillToHiscoreResultSkill(hiscoreSkill.name());
            if(jsonObject.has(fieldName))
            {
                skills.put(hiscoreSkill, gson.fromJson(jsonObject.get(fieldName), Skill.class));
            }
            else
            {
                skills.put(hiscoreSkill, new Skill(-1,-1,-1));
            }
        }

        return new HiscoreResult(jsonObject.get("player").getAsString(), skills);
    }
}
