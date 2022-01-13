package com.friendtracker.ui;

import com.friendtracker.FriendTrackerConfig;
import com.friendtracker.FriendTrackerPlugin;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.api.Client;
import net.runelite.client.hiscore.HiscoreClient;
import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.hiscore.HiscoreSkill;
import static net.runelite.client.hiscore.HiscoreSkill.*;
import net.runelite.client.hiscore.HiscoreSkillType;
import net.runelite.client.plugins.hiscore.HiscoreConfig;
import net.runelite.client.plugins.hiscore.HiscorePlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.components.PluginErrorPanel;
import okhttp3.OkHttpClient;

public class FriendTrackerPanel extends PluginPanel
{
    /**
     * Real skills, ordered in the way they should be displayed in the panel.
     */
    private static final List<HiscoreSkill> SKILLS = ImmutableList.of(
            ATTACK, HITPOINTS, MINING,
            STRENGTH, AGILITY, SMITHING,
            DEFENCE, HERBLORE, FISHING,
            RANGED, THIEVING, COOKING,
            PRAYER, CRAFTING, FIREMAKING,
            MAGIC, FLETCHING, WOODCUTTING,
            RUNECRAFT, SLAYER, FARMING,
            CONSTRUCTION, HUNTER
    );

    /**
     * Bosses, ordered in the way they should be displayed in the panel.
     */
    private static final List<HiscoreSkill> BOSSES = ImmutableList.of(
            ABYSSAL_SIRE, ALCHEMICAL_HYDRA, BARROWS_CHESTS,
            BRYOPHYTA, CALLISTO, CERBERUS,
            CHAMBERS_OF_XERIC, CHAMBERS_OF_XERIC_CHALLENGE_MODE, CHAOS_ELEMENTAL,
            CHAOS_FANATIC, COMMANDER_ZILYANA, CORPOREAL_BEAST,
            DAGANNOTH_PRIME, DAGANNOTH_REX, DAGANNOTH_SUPREME,
            CRAZY_ARCHAEOLOGIST, DERANGED_ARCHAEOLOGIST, GENERAL_GRAARDOR,
            GIANT_MOLE, GROTESQUE_GUARDIANS, HESPORI,
            KALPHITE_QUEEN, KING_BLACK_DRAGON, KRAKEN,
            KREEARRA, KRIL_TSUTSAROTH, MIMIC,
            NEX, NIGHTMARE, PHOSANIS_NIGHTMARE,
            OBOR, SARACHNIS, SCORPIA,
            SKOTIZO, TEMPOROSS, THE_GAUNTLET,
            THE_CORRUPTED_GAUNTLET, THEATRE_OF_BLOOD, THEATRE_OF_BLOOD_HARD_MODE,
            THERMONUCLEAR_SMOKE_DEVIL, TZKAL_ZUK, TZTOK_JAD,
            VENENATIS, VETION, VORKATH,
            WINTERTODT, ZALCANO, ZULRAH
    );

    private final Client client;
    private final FriendTrackerPlugin plugin;
    private final FriendTrackerConfig config;
    private final HiscoreClient hiscoreClient;

    // Handle friend boxes
    private final JPanel boxContainer = new JPanel();

    // Details and control
    private JPanel headerContainer = new JPanel();
    private JLabel titleLabel = new JLabel();
    private JButton refreshListBtn = new JButton();

    // Display if not refreshed
    private final PluginErrorPanel errorPanel = new PluginErrorPanel();

    @Inject
    public FriendTrackerPanel(@Nullable Client client, FriendTrackerPlugin plugin, FriendTrackerConfig config, OkHttpClient okHttpClient)
    {
        this.plugin = plugin;
        this.config = config;
        this.hiscoreClient = new HiscoreClient(okHttpClient);
        this.client = client;

        setBorder(new EmptyBorder(6, 6, 6, 6));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        // Create layout panel for wrapping
        final JPanel layoutPanel = new JPanel();
        layoutPanel.setLayout(new BoxLayout(layoutPanel, BoxLayout.Y_AXIS));
        add(layoutPanel, BorderLayout.NORTH);

        // Create header container
        headerContainer.setLayout(new BorderLayout());
        headerContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        headerContainer.setPreferredSize(new Dimension(0, 30));
        headerContainer.setBorder(new EmptyBorder(5, 5, 5, 10));

        // Create title label
        titleLabel.setText("Friend Tracker");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(FontManager.getRunescapeSmallFont());

        // Create refresh list button
        refreshListBtn.setText("Refresh List");
        refreshListBtn.addActionListener(e ->
        {
            plugin.refreshList();
        });

        headerContainer.add(titleLabel, BorderLayout.WEST);
        headerContainer.add(refreshListBtn, BorderLayout.EAST);

        // Create Friend box wrapper
        boxContainer.setLayout(new BoxLayout(boxContainer, BoxLayout.Y_AXIS));

        layoutPanel.add(headerContainer);
        layoutPanel.add(boxContainer);

        // Add error pane
        errorPanel.setContent("Friend Tracker", "You have not checked your friends' xp yet.");
        add(errorPanel);
    }

//    private void lookup()
//    {
//        final String lookup = sanitize(searchBar.getText());
//
//        if (Strings.isNullOrEmpty(lookup))
//        {
//            return;
//        }
//
//        searchBar.setEditable(false);
//        searchBar.setIcon(IconTextField.Icon.LOADING_DARKER);
//        loading = true;
//
//        for (Map.Entry<HiscoreSkill, JLabel> entry : skillLabels.entrySet())
//        {
//            HiscoreSkill skill = entry.getKey();
//            JLabel label = entry.getValue();
//            HiscoreSkillType skillType = skill == null ? HiscoreSkillType.SKILL : skill.getType();
//
//            label.setText(pad("--", skillType));
//            label.setToolTipText(skill == null ? "Combat" : skill.getName());
//        }
//
//        // if for some reason no endpoint was selected, default to normal
//        if (selectedEndPoint == null)
//        {
//            selectedEndPoint = HiscoreEndpoint.NORMAL;
//        }
//
//        hiscoreClient.lookupAsync(lookup, selectedEndPoint).whenCompleteAsync((result, ex) ->
//                SwingUtilities.invokeLater(() ->
//                {
//                    if (!sanitize(searchBar.getText()).equals(lookup))
//                    {
//                        // search has changed in the meantime
//                        return;
//                    }
//
//                    if (result == null || ex != null)
//                    {
//                        if (ex != null)
//                        {
//                            log.warn("Error fetching Hiscore data " + ex.getMessage());
//                        }
//                        return;
//                    }
//
//                    //successful player search
//                    applyHiscoreResult(result);
//                }));
//    }
}
