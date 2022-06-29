package com.friendtracker.data;

import com.friendtracker.panel.components.HiscoreUtil;
import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
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

        Skill skill;
        Gson gson = new Gson();

        // add skills with non-negative xp or level to json
        for(HiscoreSkill hiscoreSkill : HiscoreSkill.values())
        {
            skill = result.getSkill(hiscoreSkill);
            // @todo change to test only rank if that is valid
            if(skill.getExperience() != -1L ||
                    (hiscoreSkill.getType() != HiscoreSkillType.SKILL && skill.getLevel() != -1))
            {
                String name = HiscoreUtil.hiscoreSkillToHiscoreResultSkill(hiscoreSkill.name());
                jsonObject.add(name, gson.toJsonTree(skill));
            }
        }

        return jsonObject;
    }

}
