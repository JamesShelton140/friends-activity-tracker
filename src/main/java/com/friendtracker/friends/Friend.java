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
import com.friendtracker.panel.components.HiscoreUtil;
import java.time.Instant;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
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
        friendPanel.applyHiscoreResult(hiscoreChangeInTheLast(config.selectedRange().getPeriod(), config.rangeTolerance().getPeriod()));

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

            // if base is not null but new is null then return invalid
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
     *  Finds the closest snapshot to the given Instant within the following:
     * <ol>
     *     <li>The snapshot at the given instant or, if it does not exist,</li>
     *     <li>The snapshot closest to the given Instant greater than it within the configured tolerance or, if one does not exist,</li>
     *     <li>The snapshot closest to the given Instant less than it within the configured tolerance or, if one does not exist,</li>
     *     <li>The snapshot closest to the given Instant greater than it.</li>
     * </ol>
     *
     * @param instant the point in time to measure gained xp from
     * @param tolerance the tolerance period to prioritise
     * @return the closest snapshot found to the target instant
     */
    public Optional<HiscoreResult> getSnapshotAt(Instant instant, Period tolerance)
    {
        Optional<Instant> target;

        // The snapshot at the given instant
        if(hiscoreSnapshots.containsKey(instant)) return Optional.of(hiscoreSnapshots.get(instant));

        TreeSet<Instant> hiscoreSet = hiscoreSnapshots.keySet().stream()
                .filter(inst -> inst.compareTo(instant.minus(tolerance)) >= 0)
                .collect(Collectors.toCollection(TreeSet::new));

        // The snapshot closest to the given Instant greater than it within the configured tolerance
        target = hiscoreSet.stream()
                .filter(inst -> inst.compareTo(instant) >= 0 && inst.compareTo(instant.plus(tolerance)) <= 0)
                .min(Comparator.naturalOrder());

        if(target.isPresent()) return Optional.of(hiscoreSnapshots.get(target.get()));

        // The snapshot closest to the given Instant less than it within the configured tolerance
        target = hiscoreSet.stream()
                .filter(inst -> inst.compareTo(instant) <= 0)
                .max(Comparator.naturalOrder());

        if(target.isPresent()) return Optional.of(hiscoreSnapshots.get(target.get()));

        // The snapshot closest to the given Instant greater than it
        target = hiscoreSet.stream()
                .filter(inst -> inst.compareTo(instant) >= 0)
                .min(Comparator.naturalOrder());

        return target.map(value -> hiscoreSnapshots.get(value));
    }

    /**
     * Returns the total xp gained since the supplied Instant until now within the specified tolerance.
     *
     * The first hiscore snapshot used to calculate xp gained is the closest to the given Instant within the following:
     * <ol>
     *     <li>The snapshot at the given instant or, if it does not exist,</li>
     *     <li>The snapshot closest to the given Instant greater than it within the configured tolerance or, if one does not exist,</li>
     *     <li>The snapshot closest to the given Instant less than it within the configured tolerance or, if one does not exist,</li>
     *     <li>The snapshot closest to the given Instant greater than it.</li>
     * </ol>
     *
     * @param instant the point in time to measure gained xp from
     * @param tolerance the tolerance period to prioritise
     * @return the total xp gained since the specified instant
     */
    public long xpGainedSince(Instant instant, Period tolerance)
    {
        long currentTotalXp = getMostRecentResult().getOverall().getExperience();

        Optional<HiscoreResult> baseSnapshot = getSnapshotAt(instant, tolerance);

        return baseSnapshot.map(hiscoreResult -> currentTotalXp - hiscoreResult.getOverall().getExperience()).orElse(0L);
    }

    /**
     * Returns the total xp gained in the specified period before now within the specified tolerance.
     *
     * If period is equal to {@link Period#ZERO} then the most recent snapshot's overall xp is returned.
     * Otherwise, this method calls {@link Friend#xpGainedSince} with parameter {@code Instant.now().minus(period)}.
     *
     * @param period the time period before now to measure gained xp from
     * @param tolerance the tolerance period to prioritise
     * @return the total xp gained in the specified period before now
     */
    public long xpGainedInTheLast(Period period, Period tolerance)
    {
        if(period.isZero()) return getMostRecentResult().getOverall().getExperience();

        return xpGainedSince(Instant.now().minus(period), tolerance);
    }

    /**
     * Returns the total KC gained since the supplied Instant until now within the specified tolerance.
     *
     * The first hiscore snapshot used to calculate KC gained is the closest to the given Instant within the following:
     * <ol>
     *     <li>The snapshot at the given instant or, if it does not exist,</li>
     *     <li>The snapshot closest to the given Instant greater than it within the configured tolerance or, if one does not exist,</li>
     *     <li>The snapshot closest to the given Instant less than it within the configured tolerance or, if one does not exist,</li>
     *     <li>The snapshot closest to the given Instant greater than it.</li>
     * </ol>
     *
     * @param instant the point in time to measure gained KC from
     * @return the total KC gained since the specified instant
     */
    public int kcGainedSince(Instant instant, Period tolerance)
    {
        int currentTotalKc = sumNonSkillKc(getMostRecentResult());

        Optional<HiscoreResult> baseSnapshot = getSnapshotAt(instant, tolerance);

        return baseSnapshot.map(hiscoreResult -> currentTotalKc - sumNonSkillKc(hiscoreResult)).orElse(0);
    }

    /**
     * Returns the total kc gained in the specified period before now within the specified tolerance.
     *
     * If period is equal to {@link Period#ZERO} then the most recent snapshot's total kc is returned.
     * Otherwise, this method calls {@link Friend#kcGainedSince} with parameter {@code Instant.now().minus(period)}.
     *
     * @param period the time period before now to measure gained kc from
     * @return the total kc gained in the specified period before now
     */
    public int kcGainedInTheLast(Period period, Period tolerance)
    {
        if(period.isZero()) return sumNonSkillKc(getMostRecentResult());

        return kcGainedSince(Instant.now().minus(period), tolerance);
    }

    /**
     * Sums the level of all activity and boss HiscoreSkills of the supplied HiscoreResult.
     *
     * @param result the result to sum over
     * @return the sum total level of every non-skill HiscoreSkill
     */
    public int sumNonSkillKc(HiscoreResult result)
    {
        if (result == null) return 0;

        int totalKc = 0;

        for(HiscoreSkill hiscoreSkill : HiscoreSkill.values())
        {
            if(hiscoreSkill.getType() == HiscoreSkillType.SKILL ||
                    hiscoreSkill.getType() == HiscoreSkillType.OVERALL) continue;

            totalKc += result.getSkill(hiscoreSkill).getLevel();
        }

        return totalKc;
    }

    public HiscoreResult hiscoreChangeSince(Instant instant, Period tolerance)
    {
        HiscoreResult currentResult = getMostRecentResult();

        Optional<HiscoreResult> baseSnapshot = getSnapshotAt(instant, tolerance);

        return baseSnapshot.map(result -> HiscoreUtil.getDifference(currentResult, result))
                .orElseGet(() -> HiscoreUtil.getDifference(currentResult, currentResult));
    }

    public HiscoreResult hiscoreChangeInTheLast(Period period, Period tolerance)
    {
        if(period.isZero()) return getMostRecentResult();

        return hiscoreChangeSince(Instant.now().minus(period), tolerance);
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
