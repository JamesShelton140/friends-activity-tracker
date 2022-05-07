package com.friendtracker.friends;

import com.friendtracker.data.FriendDataClient;
import java.util.HashMap;
import java.util.Map;

public class FriendManager {

    private final FriendDataClient friendDataClient;
    private final long accountHash;
    private Map<String, Friend> friends = new HashMap<>();;

    public FriendManager(long accountHash, FriendDataClient friendDataClient) {
        this.accountHash = accountHash;
        this.friendDataClient = friendDataClient;
    }
}
