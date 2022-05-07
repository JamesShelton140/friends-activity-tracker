package com.friendtracker.friends;

import java.time.Instant;
import lombok.Data;
import net.runelite.client.hiscore.HiscoreResult;

@Data
public class HiscoreSnapshot {
    private final Instant time;
    private final HiscoreResult hiscoreResult;
}
