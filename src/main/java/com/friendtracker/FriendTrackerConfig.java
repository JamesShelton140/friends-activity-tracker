package com.friendtracker;

import com.friendtracker.config.ConfigValues;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(FriendTrackerPlugin.CONFIG_GROUP_NAME)
public interface FriendTrackerConfig extends Config
{
	@ConfigSection
	(
		name = "Sort Priority",
		description = "Priority for sort criteria",
		position = 0,
		closedByDefault = true
	)
	String sortSection = "sortPriority";

	@ConfigItem
	(
		position = 12,
		keyName = "secondarySort",
		name = "Secondary Sort",
		description = "Set the sort criteria to be used if the primary criteria is equal.",
		section = sortSection
	)
	default ConfigValues.SortOptions secondarySort()
	{
		return ConfigValues.SortOptions.ALPHA;
	}

	@ConfigItem
	(
		position = 13,
		keyName = "tertiarySort",
		name = "Tertiary Sort",
		description = "Set the sort criteria to be used if the secondary criteria is equal.",
		section = sortSection
	)
	default ConfigValues.SortOptions tertiarySort()
	{
		return ConfigValues.SortOptions.XP;
	}

	@ConfigItem
	(
		position = 14,
		keyName = "quaternarySort",
		name = "Quaternary Sort",
		description = "Set the sort criteria to be used if the tertiary criteria is equal.",
		section = sortSection
	)
	default ConfigValues.SortOptions quaternarySort()
	{
		return ConfigValues.SortOptions.KC;
	}



	@ConfigItem
	(
		position = 1,
		keyName = "wrapMergeCandidates",
		name = "Wrap merge panel candidates",
		description = "When enabled allows the user to increment the selected merge candidate infinitely in either direction."
	)
	default boolean wrapMergeCandidates()
	{
		return true;
	}

	@ConfigItem
	(
		position = 2,
		keyName = "deleteDataOnRemovedFriend",
		name = "Delete data for removed friends",
		description = "When enabled data will automatically be deleted for friends that are removed from your friends list."
	)
	default boolean deleteDataOnRemovedFriend()
	{
		return true;
	}

	@ConfigItem
	(
		position = 3,
		keyName = "refreshMenuOption",
		name = "Refresh menu option",
		description = "Show Refresh Tracker menu option in friend list menus"
	)
	default boolean refreshMenuOption()
	{
		return true;
	}

	@ConfigItem
	(
		position = 4,
		keyName = "deleteMenuOption",
		name = "Delete menu option",
		description = "Show Delete Tracker Data menu option in friend list menus"
	)
	default boolean deleteMenuOption()
	{
		return true;
	}


	@ConfigItem
	(
		position = 101,
		keyName = "selectedRange",
		name = "Range to display",
		description = "",
		hidden = true
	)
	default ConfigValues.RangeOptions selectedRange()
	{
		return ConfigValues.RangeOptions.ALL;
	}

	@ConfigItem
	(
		position = 1011,
		keyName = "rangeNumber",
		name = "Range Number",
		description = "",
		hidden = true
	)
	default int rangeNumber()
	{
		return 1;
	}

	@ConfigItem
	(
		position = 102,
		keyName = "rangeTolerance",
		name = "Activity range tolerance",
		description = "Configures the tolerance of the activity range. Select All to set tolerance to zero."
	)
	default ConfigValues.RangeOptions rangeTolerance()
	{
		return ConfigValues.RangeOptions.ALL;
	}

	@ConfigItem
	(
		position = 103,
		keyName = "sortCriteria",
		name = "Sorting criteria",
		description = "",
		hidden = true
	)
	default ConfigValues.SortOptions sortCriteria()
	{
		return ConfigValues.SortOptions.ALPHA;
	}

	@ConfigItem
	(
		position = 104,
		keyName = "sortOrder",
		name = "Sorting order",
		description = "",
		hidden = true
	)
	default ConfigValues.OrderOptions sortOrder()
	{
		return ConfigValues.OrderOptions.ASCENDING;
	}
}
