/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
 * Copyright (c) 2019, Bram91 <https://github.com/bram91>
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

import com.google.common.base.Strings;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.hiscore.HiscoreClient;
import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.hiscore.HiscoreResult;
import okhttp3.OkHttpClient;

@Slf4j
public class FriendDataClient {

    private final HiscoreClient hiscoreClient;

    @Inject
    public FriendDataClient(OkHttpClient okHttpClient)
    {
        this.hiscoreClient = new HiscoreClient(okHttpClient);
    }

    public CompletableFuture<HiscoreResult> lookupAsync(String name)
    {
        final String lookup = sanitize(name);

        if (Strings.isNullOrEmpty(lookup))
        {
            log.warn("Tried to lookup name which is null or empty.");
            return CompletableFuture.completedFuture(null);
        }

        HiscoreEndpoint selectedEndPoint = HiscoreEndpoint.NORMAL;

        return hiscoreClient.lookupAsync(lookup, selectedEndPoint);

        // Hiscore plugin reset hiscore panel text to default
        //        for (Map.Entry<HiscoreSkill, JLabel> entry : skillLabels.entrySet())
//        {
//            HiscoreSkill skill = entry.getKey();
//            JLabel label = entry.getValue();
//            HiscoreSkillType skillType = skill == null ? HiscoreSkillType.SKILL : skill.getType();
//
//            label.setText(pad("--", skillType));
//            label.setToolTipText(skill == null ? "Combat" : skill.getName());
//        }
    }

    /**
     *
     */
    public Optional<HiscoreResult> lookup(String name)
    {
        try
        {
            HiscoreResult result = lookupAsync(name).join();
            return Optional.of(result);
        }
        catch(Exception ex)
        {
            log.warn("Error fetching Hiscore data " + ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Replace no-break space characters with regular spaces in the given string
     *
     * @param lookup the string to sanitize
     * @return a string with spaces in place of no-break spaces
     */
    private static String sanitize(String lookup)
    {
        return lookup.replace('\u00A0', ' ');
    }
}
