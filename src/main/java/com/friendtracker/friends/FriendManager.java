package com.friendtracker.friends;

import com.friendtracker.FriendTrackerPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class FriendManager {

    private final long accountHash;
    private Map<String, Friend> friends = new HashMap<>(); //Key: UUID.randomUUID().toString();

    public FriendManager(long accountHash)
    {
        this.accountHash = accountHash;
    }

    /** Populates an empty FriendManager with the given collection of Friends.
     * Returns without changing the state of this FriendManager if it is not empty.
     * Use {@link FriendManager#add(Friend)} to add Friends to a non-empty FriendManager.
     *
     * @param friends the Friend objects to populate this FriendManager
     */
    public void applySaveData(Map<String, Friend> friends)
    {
        // Only apply save data if friends list is empty
        if(!this.friends.isEmpty())
        {
            log.error("Cannot apply save data to a FriendManager that already has Friends. Use addOrMerge() instead.");
            return;
        }

        this.friends.putAll(friends);

        log.info(this.friends.values().stream().map(Friend::getName).collect(Collectors.joining(", ")));
    }

    public void add(Friend friend)
    {
        friends.put(friend.getID(), friend);

        log.info(this.friends.values().stream().map(Friend::getName).collect(Collectors.joining(", ")));
    }

    public void merge(Friend newFriend, String mergeTargetID)
    {
        friends.get(mergeTargetID).merge(newFriend);

        log.info(this.friends.values().stream().map(Friend::getName).collect(Collectors.joining(", ")));
    }

    public List<Friend> getValidMergeCandidates(Friend newFriend)
    {
        List<Friend> mergeCandidates = new ArrayList<>();

        for(Friend friend : friends.values())
        {
            if(friend.isValidToMerge(newFriend)) mergeCandidates.add(friend);
        }

        return mergeCandidates;
    }
}
