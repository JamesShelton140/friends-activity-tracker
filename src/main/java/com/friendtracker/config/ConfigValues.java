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

import java.time.Period;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class ConfigValues
{

    @Getter
    @AllArgsConstructor
    public enum RangeOptions
    {
        DAY("Day", Period.ofDays(1),true),
        WEEK("Week", Period.ofDays(7),true),
        MONTH("Month", Period.ofDays(30),true),
        MONTH_3("3 Months", Period.ofDays(90),false),
        MONTH_6("6 Months", Period.ofDays(180),false),
        YEAR("Year", Period.ofDays(365),true),
        ALL("All", Period.ZERO,true);

        private String displayName;
        private Period period;
        private boolean isCoreUnit;

        @Override
        public String toString()
        {
            return displayName;
        }
    }

//    @Getter
//    @AllArgsConstructor
//    public enum SortOptions
//    {
//        ALPHA_ASCENDING("Alphanumeric (asc)", (friend1, friend2) -> Comparator.comparing(Friend::getName, String.CASE_INSENSITIVE_ORDER).compare(friend1, friend2)),
//        ALPHA_DESCENDING("Alphanumeric (desc)", (friend1, friend2) -> Comparator.comparing(Friend::getName, String.CASE_INSENSITIVE_ORDER).reversed().compare(friend1, friend2)),
////        XP_ASCENDING("XP Gained (Asc)", new XpGainedComparator()),
////        XP_DESCENDING("XP Gained (Desc)", new XpGainedComparator().reversed()),
//        XP_ASCENDING("XP Gained (Asc)", (friend1, friend2) -> Comparator.comparingLong(Friend::xpGainedInConfigPeriod).compare(friend1, friend2)),
//        XP_DESCENDING("XP Gained (Desc)", (friend1, friend2) -> Comparator.comparingLong(Friend::xpGainedInConfigPeriod).reversed().compare(friend1, friend2)),
//        KC_ASCENDING("KC Gained (Asc)", (friend1, friend2) -> Comparator.comparingInt(Friend::kcGainedInConfigPeriod).compare(friend1, friend2)),
//        KC_DESCENDING("KC Gained (Desc)", (friend1, friend2) -> Comparator.comparingInt(Friend::kcGainedInConfigPeriod).reversed().compare(friend1, friend2));
//
//        private String displayName;
//        private Comparator<Friend> comparator;
//
//        @Override
//        public String toString()
//        {
//            return displayName;
//        }
//    }

    @Getter
    @AllArgsConstructor
    public enum SortOptions
    {
        ALPHA_ASCENDING("Alphanumeric (asc)", "ALPHANUMERIC", true),
        ALPHA_DESCENDING("Alphanumeric (desc)", "ALPHANUMERIC", false),
        XP_ASCENDING("XP Gained (Asc)", "TOTAL_XP", true),
        XP_DESCENDING("XP Gained (Desc)", "TOTAL_XP", false),
        KC_ASCENDING("KC Gained (Asc)", "TOTAL_KC", true),
        KC_DESCENDING("KC Gained (Desc)", "TOTAL_KC", false);

        private String displayName;
        private String comparator;
        private boolean ascending;

        @Override
        public String toString()
        {
            return displayName;
        }
    }
}
