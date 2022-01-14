package com.friendtracker;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import static net.runelite.client.hiscore.HiscoreSkill.*;
import net.runelite.client.hiscore.Skill;

public class Friend {

    /**
     * Real skills, ordered in the way they should be displayed in the panel.
     */
    private static final List<HiscoreSkill> SKILLS = ImmutableList.of(
            ATTACK, HITPOINTS, MINING,
            STRENGTH, AGILITY, SMITHING,
            DEFENCE, HERBLORE, FISHING,
            RANGED, THIEVING, COOKING,
            PRAYER, CRAFTING, FIREMAKING,
            MAGIC, FLETCHING, WOODCUTTING,
            RUNECRAFT, SLAYER, FARMING,
            CONSTRUCTION, HUNTER, OVERALL
    );

    /**
     * Bosses, ordered in the way they should be displayed in the panel.
     */
    private static final List<HiscoreSkill> BOSSES = ImmutableList.of(
            ABYSSAL_SIRE, ALCHEMICAL_HYDRA, BARROWS_CHESTS,
            BRYOPHYTA, CALLISTO, CERBERUS,
            CHAMBERS_OF_XERIC, CHAMBERS_OF_XERIC_CHALLENGE_MODE, CHAOS_ELEMENTAL,
            CHAOS_FANATIC, COMMANDER_ZILYANA, CORPOREAL_BEAST,
            DAGANNOTH_PRIME, DAGANNOTH_REX, DAGANNOTH_SUPREME,
            CRAZY_ARCHAEOLOGIST, DERANGED_ARCHAEOLOGIST, GENERAL_GRAARDOR,
            GIANT_MOLE, GROTESQUE_GUARDIANS, HESPORI,
            KALPHITE_QUEEN, KING_BLACK_DRAGON, KRAKEN,
            KREEARRA, KRIL_TSUTSAROTH, MIMIC,
            NEX, NIGHTMARE, PHOSANIS_NIGHTMARE,
            OBOR, SARACHNIS, SCORPIA,
            SKOTIZO, TEMPOROSS, THE_GAUNTLET,
            THE_CORRUPTED_GAUNTLET, THEATRE_OF_BLOOD, THEATRE_OF_BLOOD_HARD_MODE,
            THERMONUCLEAR_SMOKE_DEVIL, TZKAL_ZUK, TZTOK_JAD,
            VENENATIS, VETION, VORKATH,
            WINTERTODT, ZALCANO, ZULRAH
    );

    @Getter
    private String name;
    @Getter
    private String oldName;
    private final Map<HiscoreSkill, Long> totalExperience = new HashMap<>();
    private final Map<HiscoreSkill, Long> gainedExperience = new HashMap<>();

    public Friend(String name)
    {
        this.name = name;
    }

    public Friend(HiscoreResult result)
    {
        this.name = result.getPlayer();

        Skill skillResult;
        for(HiscoreSkill skill : HiscoreSkill.values()) {
            if ((skillResult = result.getSkill(skill)) != null)
            {
                if(SKILLS.contains(skill))
                {
                    totalExperience.put(skill, skillResult.getExperience());
                    gainedExperience.put(skill, skillResult.getExperience());
                }
                else
                {
                    totalExperience.put(skill, (long)skillResult.getLevel());
                    gainedExperience.put(skill, (long)skillResult.getLevel());
                }

            }
        }
    }

    public Long getTotalSkillXP(HiscoreSkill skill)
    {
        return totalExperience.get(skill);
    }

    public Long getGainedSkillXP(HiscoreSkill skill)
    {
        return gainedExperience.get(skill);
    }

    /**
     * Creates a formatted string snapshot containing the names and total experience for all skills for this Friend.
     * This is intended for save/load purposes.
     *
     * @return a string
     */
    public String dataSnapshot()
    {
        String snapshot = "Friend{";

        snapshot += "name:" + this.name + ",";
        snapshot += "oldname:" + this.oldName + "";

        snapshot += totalExperience.entrySet().stream()
                                .map(entry -> entry.getKey() + ":" + entry.getValue())
                                .reduce("", (a, b) -> a + "," + b);

        snapshot += "}";

        return snapshot;
    }
}
