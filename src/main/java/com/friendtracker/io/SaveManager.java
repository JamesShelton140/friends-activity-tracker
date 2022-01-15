package com.friendtracker.io;

import com.friendtracker.Friend;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Client;
import static net.runelite.client.RuneLite.RUNELITE_DIR;

public class SaveManager {

    private static final File SESSION_DIR = new File(RUNELITE_DIR, "friend-tracker");

    private final Client client;

    private final Map<String, Friend> friends = new HashMap<>();

    public SaveManager(Client client)
    {
        this.client = client;

        if (!SESSION_DIR.exists())
        {
            SESSION_DIR.mkdir();
        }
    }

    public void addToSave(Friend friend)
    {
        friends.put(friend.getName(), friend);

        buildSaveFile();
    }

    public void buildSaveFile()
    {
        try {
            String dateAndTime = java.time.LocalDate.now().toString();

            File saveFile = new File(RUNELITE_DIR + "/friend-tracker/" + client.getUsername() + dateAndTime + ".txt");

            if (!saveFile.createNewFile()) {

                saveFile.delete();
                saveFile.createNewFile();
            }

            try (FileWriter f = new FileWriter(saveFile, true); BufferedWriter b = new BufferedWriter(f); PrintWriter p = new PrintWriter(b);)
            {
                for (Friend friend : friends.values())
                {
                    p.println(friend.dataSnapshot());
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
