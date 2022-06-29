package com.friendtracker.data;

import com.friendtracker.panel.components.HiscoreUtil;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
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

        HiscoreResult result = gson.fromJson(jsonElement, HiscoreResult.class);

        for(HiscoreSkill hiscoreSkill : HiscoreSkill.values())
        {
            if(result.getSkill(hiscoreSkill) == null)
            {
                String fieldName = HiscoreUtil.hiscoreSkillToHiscoreResultSkill(hiscoreSkill.name());
                try
                {
                    HiscoreResult.class.getMethod("set" + StringUtils.capitalize(fieldName), Skill.class)
                            .invoke(result, new Skill(-1,-1,-1));
                }
                catch (Exception e)
                {
                    log.error("Failed to set {} for {} during deserialization. Exception: {}", fieldName, result.getPlayer(), e.getStackTrace());
                }
            }
        }

        return result;
    }
}
