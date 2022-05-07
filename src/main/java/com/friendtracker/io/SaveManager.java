package com.friendtracker.io;

import com.friendtracker.OldFriend;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Client;
import static net.runelite.client.RuneLite.RUNELITE_DIR;

public class SaveManager {

    private static final File SESSION_DIR = new File(RUNELITE_DIR, "friend-tracker");

    private final Client client;

    private final Map<String, OldFriend> friends = new HashMap<>();

    public SaveManager(Client client)
    {
        this.client = client;

        if (!SESSION_DIR.exists())
        {
            SESSION_DIR.mkdir();
        }
    }

    public void addToSave(OldFriend oldFriend)
    {
        friends.put(oldFriend.getName(), oldFriend);

        buildSaveFile();
    }

    public void buildSaveFile()
    {
        try {
            long timeStamp = Instant.now().truncatedTo(ChronoUnit.MINUTES).toEpochMilli();

            File saveFile = new File(RUNELITE_DIR + "/friend-tracker/" + client.getAccountHash() + "/" + timeStamp + ".txt");

            if (!saveFile.createNewFile()) {

                saveFile.delete();
                saveFile.createNewFile();
            }

            try (FileWriter f = new FileWriter(saveFile, true); BufferedWriter b = new BufferedWriter(f); PrintWriter p = new PrintWriter(b);)
            {
                for (OldFriend oldFriend : friends.values())
                {
                    p.println(oldFriend.dataSnapshot());
                }
            }
            catch (IOException i)
            {
                i.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
