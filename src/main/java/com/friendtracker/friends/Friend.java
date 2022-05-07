package com.friendtracker.friends;

import com.friendtracker.FriendTrackerConfig;
import com.friendtracker.FriendTrackerPlugin;
import com.friendtracker.data.FriendDataClient;
import com.friendtracker.panel.FriendPanel;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import net.runelite.client.hiscore.HiscoreResult;

@Data
public class Friend {
    private String name;
    private final List<String> previousNames = new ArrayList<>();
    private Map<Instant, HiscoreResult> hiscoreSnapshots = new HashMap<>();

    public Friend(String name)
    {
        this.name = name;
        previousNames.add("oldName");
        hiscoreSnapshots.put(Instant.now(), new HiscoreResult());
    }

    public FriendPanel generatePanel(FriendTrackerPlugin plugin, FriendTrackerConfig config, FriendDataClient friendDataClient)
    {
        return new FriendPanel(plugin, config, friendDataClient, this);
    }
}
