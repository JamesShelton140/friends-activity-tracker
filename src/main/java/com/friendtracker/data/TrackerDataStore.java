package com.friendtracker.data;

import com.friendtracker.FriendTrackerPlugin;
import com.friendtracker.friends.Friend;
import com.friendtracker.friends.FriendManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
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
    private ConfigManager configManager;

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

        Type mapType = new TypeToken<Map<String, Friend>>(){}.getType();

        try
        {
            String friendsJson = configManager.getConfiguration(FriendTrackerPlugin.CONFIG_GROUP_NAME, buildConfigKey(accountHash));

            Map<String, Friend> friends = gson.fromJson(friendsJson, mapType);

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

        Type mapType = new TypeToken<Map<String, Friend>>(){}.getType();

        configManager.setConfiguration(FriendTrackerPlugin.CONFIG_GROUP_NAME,
                buildConfigKey(accountHash),
                gson.toJson(friends, mapType));
    }

}
