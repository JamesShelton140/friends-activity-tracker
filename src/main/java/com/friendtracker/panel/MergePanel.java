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
package com.friendtracker.panel;

import com.friendtracker.FriendTrackerConfig;
import com.friendtracker.FriendTrackerPlugin;
import com.friendtracker.friends.Friend;
import com.friendtracker.panel.components.FixedWidthPanel;
import com.friendtracker.panel.components.HiscoreComparisonPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;

@Slf4j
public class MergePanel extends FixedWidthPanel
{

    private final FriendTrackerPlugin plugin;
    private final FriendTrackerConfig config;
    private final Friend baseFriend;
    private final List<Friend> mergeCandidates;

    int listIndex = 0;

    private String mergeTargetID = "";
    HiscoreComparisonPanel comparisonPanel;
    JLabel baseFriendNameLabel;
    JLabel mergeTargetNameLabel;
    JButton prevBtn;
    JButton nextBtn;

    public MergePanel(FriendTrackerPlugin plugin, FriendTrackerConfig config, Friend baseFriend, List<Friend> mergeCandidates)
    {
        this.plugin = plugin;
        this.config = config;
        this.baseFriend = baseFriend;
        this.mergeCandidates = mergeCandidates;

        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        JPanel mergeButtonPanel = new JPanel();
        mergeButtonPanel.setLayout(new GridLayout(1,3));

        // Create previous button
        prevBtn = new JButton();
        prevBtn.setText("<");
        prevBtn.setFocusPainted(false);
        prevBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        prevBtn.addActionListener(e ->
        {
            previousMergeCandidate();
        });

        // Create next button
        nextBtn = new JButton();
        nextBtn.setText(">");
        nextBtn.setFocusPainted(false);
        nextBtn.addActionListener(e ->
        {
            nextMergeCandidate();
        });

        // Create 'create new' button
        JButton createBtn = new JButton();
        createBtn.setAlignmentX(CENTER_ALIGNMENT);
        createBtn.setText("Create New");
        createBtn.addActionListener(e ->
        {
            int selectedValue = JOptionPane.showConfirmDialog(createBtn, "Add " + baseFriend.getName() + " as a new friend?", "Create New Friend Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (selectedValue == JOptionPane.YES_OPTION)
            {
                plugin.resolveMerge(baseFriend, "");
                plugin.nextMergePanel();
            }

        });

        // Create merge button
        JButton mergeBtn = new JButton();
        mergeBtn.setText("Merge");
        mergeBtn.addActionListener(e ->
        {
            int selectedValue = JOptionPane.showConfirmDialog(mergeBtn, "Merge " + baseFriend.getName() + " into " + mergeTargetNameLabel.getText() + "?", "Merge Friend Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (selectedValue == JOptionPane.YES_OPTION)
            {
                plugin.resolveMerge(baseFriend, mergeTargetID);
                plugin.nextMergePanel();
            }
        });

        mergeButtonPanel.add(prevBtn);
        mergeButtonPanel.add(mergeBtn);
        mergeButtonPanel.add(nextBtn);

        mergeTargetNameLabel = new JLabel(mergeCandidates.get(listIndex).getName());
        mergeTargetNameLabel.setAlignmentX(LEFT_ALIGNMENT);
        mergeTargetNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
        mergeTargetNameLabel.setToolTipText("Previous names:\n" + mergeCandidates.get(listIndex).previousNamesVertical());

        baseFriendNameLabel = new JLabel(baseFriend.getName());
        baseFriendNameLabel.setAlignmentX(RIGHT_ALIGNMENT);
        baseFriendNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        baseFriendNameLabel.setToolTipText("Previous names:\n" + baseFriend.previousNamesVertical());

        JPanel nameLabelWrapper = new JPanel();
        nameLabelWrapper.setLayout(new BorderLayout());
        nameLabelWrapper.add(mergeTargetNameLabel, BorderLayout.WEST);
        nameLabelWrapper.add(baseFriendNameLabel, BorderLayout.EAST);

        /*
         *  subtitles
         * "old:            new:"
         */
        JLabel mergeTargetTitleLabel = new JLabel("Old:");
        mergeTargetTitleLabel.setAlignmentX(LEFT_ALIGNMENT);
        mergeTargetTitleLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel newFriendTitleLabel = new JLabel("New:");
        newFriendTitleLabel.setAlignmentX(RIGHT_ALIGNMENT);
        newFriendTitleLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel subtitleWrapper = new JPanel();
        subtitleWrapper.setLayout(new BorderLayout());
        subtitleWrapper.add(mergeTargetTitleLabel, BorderLayout.WEST);
        subtitleWrapper.add(newFriendTitleLabel, BorderLayout.EAST);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.add(createBtn);
        controlPanel.add(mergeButtonPanel);
        controlPanel.add(subtitleWrapper);
        controlPanel.add(nameLabelWrapper);

        this.add(controlPanel, BorderLayout.NORTH);

        comparisonPanel = new HiscoreComparisonPanel();
        setMergeCandidate(0);

        this.add(comparisonPanel, BorderLayout.CENTER);
    }

    private int getCurrentTargetIndex()
    {
        int index = -1;
        Optional<Friend> currentTarget = mergeCandidates.stream().filter(friend -> friend.getID().equals(mergeTargetID)).findFirst();

        if(currentTarget.isPresent())
        {
            index = mergeCandidates.indexOf(currentTarget.get());
        }

        return index;
    }

    private void nextMergeCandidate()
    {
        listIndex = (listIndex + 1) % mergeCandidates.size();

        setMergeCandidate(listIndex);
    }

    private void previousMergeCandidate()
    {
        listIndex = (listIndex + mergeCandidates.size() - 1) % mergeCandidates.size();

        setMergeCandidate(listIndex);
    }

    private void setMergeCandidate(int index)
    {
        mergeTargetID = mergeCandidates.get(index).getID();
        setMergeTargetNameLabelText(mergeCandidates.get(index));
        comparisonPanel.applyHiscoreResult(mergeCandidates.get(index).getMostRecentResult(), baseFriend.getMostRecentResult());

        refresh();
    }

    private void setMergeTargetNameLabelText(Friend target)
    {
        mergeTargetNameLabel.setText(target.getName());
        mergeTargetNameLabel.setToolTipText("Previous names:\n" + target.previousNamesVertical());
    }

    public void refresh()
    {
        if (config.wrapMergeCandidates())
        {
            if(!prevBtn.isEnabled()) prevBtn.setEnabled(true);
            if(!nextBtn.isEnabled()) nextBtn.setEnabled(true);
        }

        if (!config.wrapMergeCandidates())
        {
            if (((listIndex == 0) == prevBtn.isEnabled()) ||
                    ((listIndex == mergeCandidates.size() - 1) == nextBtn.isEnabled()))
            {
                prevBtn.setEnabled(listIndex != 0);
                nextBtn.setEnabled(listIndex != mergeCandidates.size() - 1);
            }
        }
    }
}
