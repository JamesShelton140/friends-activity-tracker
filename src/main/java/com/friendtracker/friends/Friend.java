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
