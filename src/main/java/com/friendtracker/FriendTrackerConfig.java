package com.friendtracker;

import com.friendtracker.config.ConfigValues;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(FriendTrackerPlugin.CONFIG_GROUP_NAME)
public interface FriendTrackerConfig extends Config
{
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
		return ConfigValues.SortOptions.ALPHA_ASCENDING;
	}
}
