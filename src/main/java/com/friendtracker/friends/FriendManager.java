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
package com.friendtracker.friends;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        log.info("Save data applied. Friend list: {}", this.friends.values().stream().map(Friend::getName).collect(Collectors.joining(", ")));
    }

    public void add(Friend friend)
    {
        friends.put(friend.getID(), friend);

        log.info("Added {} to friend list.", friend.getName());
    }

    public void remove(Friend friend)
    {
        friends.remove(friend.getID());

        log.info("Removed {} from friend list.", friend.getName());
    }

    public void removeFriend(String name, String previousName)
    {
        Optional<Friend> friend = friends.values().stream()
                .filter(friend1 -> name.equals(friend1.getName()))
                .findFirst();

        if(friend.isPresent())
        {
            remove(friend.get());
            log.info("Removed {} from friend list.", name);
            return;
        }

        if(previousName == null) return;
        // If friend not found matching name search for one matching previousName
        friend = friends.values().stream()
                .filter(friend1 -> previousName.equals(friend1.getName()))
                .findFirst();

        friend.ifPresent(f -> {

        });
    }

    public void merge(Friend newFriend, String mergeTargetID)
    {
        friends.get(mergeTargetID).merge(newFriend);

        log.info("Merged {} into {}.", newFriend.getName(), friends.get(mergeTargetID).getName());
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
