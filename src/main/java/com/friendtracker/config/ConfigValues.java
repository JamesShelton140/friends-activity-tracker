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

    @Getter
    @AllArgsConstructor
    public enum SortOptions
    {
        ALPHA("Alphanumeric", "ALPHANUMERIC"),
        XP("XP Gained", "TOTAL_XP"),
        KC("KC Gained", "TOTAL_KC");

        private String displayName;
        private String comparator;

        @Override
        public String toString()
        {
            return displayName;
        }
    }

    @Getter
    @AllArgsConstructor
    public enum OrderOptions
    {
        ASCENDING("Ascending"),
        DESCENDING("Descending");

        private String displayName;

        @Override
        public String toString()
        {
            return displayName;
        }
    }
}
