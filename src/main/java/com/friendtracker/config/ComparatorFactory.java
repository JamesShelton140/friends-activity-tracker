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
package com.friendtracker.config;

import com.friendtracker.FriendTrackerConfig;
import com.friendtracker.friends.Friend;
import java.time.Period;
import java.util.Comparator;

public class ComparatorFactory
{

    private final FriendTrackerConfig config;

    public ComparatorFactory(FriendTrackerConfig config)
    {
        this.config = config;
    }

    public Comparator<Friend> createFriendComparatorFromConfig()
    {
        Comparator<Friend> comparator = null;

        ConfigValues.SortOptions sortCriteria = config.sortCriteria();
        ConfigValues.OrderOptions sortOrder = config.sortOrder();
        Period range = config.selectedRange().getPeriod();
        Period tolerance = config.rangeTolerance().getPeriod();

        final Period finalRange = range.multipliedBy(config.rangeNumber());

        switch(sortCriteria.getComparator())
        {
            case "ALPHANUMERIC":
                comparator = Comparator.comparing(Friend::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "TOTAL_XP":
                comparator = Comparator.comparingLong(friend -> friend.xpGainedInTheLast(finalRange, tolerance));
                break;
            case "TOTAL_KC":
                comparator = Comparator.comparingInt(friend -> friend.kcGainedInTheLast(finalRange, tolerance));
                break;
            default:
                comparator = (friend1, friend2) -> Comparator.comparing(Friend::getName, String.CASE_INSENSITIVE_ORDER).compare(friend1, friend2);
        }

        if(sortOrder.equals(ConfigValues.OrderOptions.DESCENDING) && comparator != null) comparator = comparator.reversed();

        return comparator;
    }
}