package com.friendtracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(FriendTrackerPlugin.CONFIG_GROUP_NAME)
public interface FriendTrackerConfig extends Config
{
	@ConfigItem(
		position = 1,
		keyName = "wrapMergeCandidates",
		name = "Wrap merge panel candidates",
		description = "When enabled allows the user to increment the selected merge candidate infinitely in either direction."
	)
	default boolean wrapMergeCandidates()
	{
		return true;
	}
}
