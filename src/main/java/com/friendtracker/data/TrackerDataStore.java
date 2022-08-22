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
package com.friendtracker.data;

import com.friendtracker.FriendTrackerPlugin;
import com.friendtracker.friends.Friend;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.hiscore.HiscoreResult;

@Slf4j
public class TrackerDataStore
{
    public static final Type MAP_TYPE = new TypeToken<Map<String, Friend>>() {}.getType();
    private final ConfigManager configManager;

    @Inject
    public TrackerDataStore(ConfigManager configManager)
    {
        this.configManager = configManager;
    }

    private Gson buildGson()
    {
        return new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer())
                .registerTypeAdapter(HiscoreResult.class, new HiscoreResultSerializer())
                .registerTypeAdapter(HiscoreResult.class, new HiscoreResultDeserializer())
                .create();
    }

    private String buildConfigKey(long accountHash)
    {
        return accountHash + "_FriendData";
    }

    public Optional<Map<String, Friend>> getFriendDataFromConfig(long accountHash)
    {
        Gson gson = buildGson();

        String friendsJson;

        try
        {
            friendsJson = configManager.getConfiguration(FriendTrackerPlugin.CONFIG_GROUP_NAME, buildConfigKey(accountHash));

            Map<String, Friend> friends = gson.fromJson(friendsJson, MAP_TYPE);

            return Optional.of(friends);
        }
        catch(Exception e)
        {
            log.warn(String.format("Configuration \"%s.%s\" not found.", FriendTrackerPlugin.CONFIG_GROUP_NAME, buildConfigKey(accountHash)));
            log.warn(e.getMessage());
            return Optional.empty();
        }
    }

    public void saveFriendDataToConfig(long accountHash, Map<String, Friend> friends)
    {
        Gson gson = buildGson();

        configManager.setConfiguration(FriendTrackerPlugin.CONFIG_GROUP_NAME,
                buildConfigKey(accountHash),
                gson.toJson(friends, MAP_TYPE));
    }

}
