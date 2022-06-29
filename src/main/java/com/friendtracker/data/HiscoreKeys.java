package com.friendtracker.data;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.runelite.client.hiscore.HiscoreSkill;
import static net.runelite.client.hiscore.HiscoreSkill.*;

public class HiscoreKeys
{
    /**
     * Real skills, ordered in the way they should be displayed in the panel.
     */
    public static final List<HiscoreSkill> SKILLS = ImmutableList.of(
            ATTACK, HITPOINTS, MINING,
            STRENGTH, AGILITY, SMITHING,
            DEFENCE, HERBLORE, FISHING,
            RANGED, THIEVING, COOKING,
            PRAYER, CRAFTING, FIREMAKING,
            MAGIC, FLETCHING, WOODCUTTING,
            RUNECRAFT, SLAYER, FARMING,
            CONSTRUCTION, HUNTER
    );

    /**
     * Bosses, ordered in the way they should be displayed in the panel.
     */
    public static final List<HiscoreSkill> BOSSES = ImmutableList.of(
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

    /**
     * Real skills, ordered in the way they should be displayed in the panel.
     */
    public static final List<HiscoreSkill> SKILLS_COMPARISON_ORDER = ImmutableList.of(
            ATTACK,
            STRENGTH,
            DEFENCE,
            HITPOINTS,
            RANGED,
            PRAYER,
            MAGIC,
            COOKING,
            WOODCUTTING,
            FLETCHING,
            FISHING,
            FIREMAKING,
            CRAFTING,
            SMITHING,
            MINING,
            HERBLORE,
            AGILITY,
            THIEVING,
            SLAYER,
            FARMING,
            RUNECRAFT,
            HUNTER,
            CONSTRUCTION
    );
}
