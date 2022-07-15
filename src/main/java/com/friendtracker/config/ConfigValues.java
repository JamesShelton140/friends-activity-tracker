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
        WEEK("Week", Period.ofWeeks(1),true),
        MONTH("Month", Period.ofMonths(1),true),
        MONTH_3("3 Months", Period.ofMonths(3),false),
        MONTH_6("6 Months", Period.ofMonths(6),false),
        YEAR("Year", Period.ofYears(1),true),
        ALL("All", Period.ZERO,true);

        public String displayName;
        public Period period;
        public boolean isCoreUnit;

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
        ALPHA_ASCENDING("Alphanumeric (asc)"),
        ALPHA_DESCENDING("Alphanumeric (desc)"),
        XP_ASCENDING("XP Gained (Asc)"),
        XP_DESCENDING("XP Gained (Desc)"),
        KC_ASCENDING("KC Gained (Asc)"),
        KC_DESCENDING("KC Gained (Desc)");

        public String displayName;

        @Override
        public String toString()
        {
            return displayName;
        }
    }
}
