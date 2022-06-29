package com.friendtracker.friends;

import com.friendtracker.FriendTrackerConfig;
import com.friendtracker.FriendTrackerPlugin;
import com.friendtracker.panel.FriendPanel;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.hiscore.HiscoreSkillType;
import net.runelite.client.hiscore.Skill;

@Slf4j
@Data
public class Friend {
    private final String ID;
    private String name;
    private final List<String> previousNames = new ArrayList<>();
    private TreeMap<Instant, HiscoreResult> hiscoreSnapshots = new TreeMap<>();

    public Friend(String ID, String name)
    {
        this.ID = ID;
        this.name = sanitize(name);
    }

    public Friend(String ID, @NonNull String name, String oldName)
    {
        this(ID, name);
        if(oldName != null)
        {
            previousNames.add(sanitize(oldName));
        }
    }

    public FriendPanel generatePanel(FriendTrackerPlugin plugin, FriendTrackerConfig config)
    {
        FriendPanel friendPanel = new FriendPanel(plugin, config, this);
        friendPanel.applyHiscoreResult(hiscoreSnapshots.lastEntry().getValue()); //@todo generate hiscoreResult based on filter settings

        return friendPanel;
    }

    public void addSnapshotNow(HiscoreResult result)
    {
        addSnapshot(Instant.now(), result);
    }

    public void addSnapshot(Instant instant, HiscoreResult result)
    {
        hiscoreSnapshots.put(instant, result);
    }

    public HiscoreResult getMostRecentResult()
    {
        return hiscoreSnapshots.lastEntry().getValue();
    }

    public void merge(Friend friend)
    {
        if(!friend.getName().equals(name))
        {
            previousNames.add(name);
            previousNames.addAll(friend.getPreviousNames());
            name = friend.getName();
        }

        hiscoreSnapshots.putAll(friend.getHiscoreSnapshots());
    }

    public boolean isValidToMerge(Friend friend)
    {
        HiscoreResult baseHiscoreResult = this.getMostRecentResult();
        HiscoreResult newHiscoreResult = friend.getMostRecentResult();
        Skill baseSkill, newSkill;

        for(HiscoreSkill hiscoreSkill : HiscoreSkill.values())
        {
            // skip this skill if it is null in the base result
            if ((baseSkill = baseHiscoreResult.getSkill(hiscoreSkill)) == null) continue;

            // if base is not null but new if null then return invalid
            if ((newSkill = newHiscoreResult.getSkill(hiscoreSkill)) == null)
            {
                log.warn("{} rejected for merge due to HiscoreSkill {} being null.", friend.getName(), hiscoreSkill.getName());
                return false;
            }

            final boolean isSkill = hiscoreSkill.getType() == HiscoreSkillType.SKILL;

            if (!isSkill)
            {
                // compare non-skills by level
//                log.info("Testing non-skill level. newSkill = {}, baseSkill = {}, difference = {}", newSkill.getLevel(), baseSkill.getLevel(), newSkill.getLevel() - baseSkill.getLevel());
                if(newSkill.getLevel() < baseSkill.getLevel()) return false;
            }

            if (isSkill)
            {
                // compare skills by xp
                if(newSkill.getExperience() < baseSkill.getExperience()) return false;
            }
        }

        // No reduction in xp or kc found so valid to merge
        return true;
    }


    public String previousNamesVertical()
    {
        return String.join("\n", previousNames);
    }

    /**
     * Replace no-break space characters with regular spaces in the given string
     *
     * @param name the string to sanitize
     * @return a string with spaces in place of no-break spaces
     */
    private static String sanitize(String name)
    {

        return name.replace('\u00A0', ' ');
    }
}
