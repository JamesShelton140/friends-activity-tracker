package com.friendtracker;

import com.friendtracker.data.FriendDataClient;
import com.friendtracker.data.TrackerDataStore;
import com.friendtracker.friends.Friend;
import com.friendtracker.friends.FriendManager;
import com.friendtracker.panel.FriendTrackerPanel;
import com.friendtracker.panel.MergePanel;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Friend Tracker"
)
public class FriendTrackerPlugin extends Plugin
{

	public static final String CONFIG_GROUP_NAME = "friend-tracker";
	@Inject private Client client;
	@Inject private ConfigManager configManager;
	@Inject private FriendTrackerConfig config;
	@Inject private FriendDataClient friendDataClient;
	@Inject private TrackerDataStore trackerDataStore;
	@Inject private ClientToolbar clientToolbar;

	private FriendTrackerPanel panel;
	private NavigationButton navButton;
	@Getter private FriendManager friendManager;


	@Override
	protected void startUp() throws Exception
	{
		panel = new FriendTrackerPanel(client, this, config, configManager);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "panel_icon.png");

		navButton = NavigationButton.builder()
				.tooltip("Friend Tracker")
				.icon(icon)
				.priority(9)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
	}

	private long lastAccount;

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();

		if (state == GameState.LOGGED_IN) {
			// LOGGED_IN is triggered between region changes too.
			// Check that the RuneScape account has changed
			if (client.getAccountHash() != lastAccount) {
				// Reset
				log.debug("Account change: {} -> {}",
						lastAccount, client.getAccountHash());

				friendManager = new FriendManager(client.getAccountHash());

				trackerDataStore.getFriendDataFromConfig(client.getAccountHash())
						.ifPresent(friendManager::applySaveData);

				lastAccount = client.getAccountHash();
			}
		}

		SwingUtilities.invokeLater(() -> panel.setLoggedIn(isLoggedInState(state)));
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getKey().equals("wrapMergeCandidates"))
		{
			panel.refresh();
		}
	}

	private boolean isLoggedInState(GameState gameState)
	{
		return gameState != GameState.LOGIN_SCREEN && gameState != GameState.LOGIN_SCREEN_AUTHENTICATOR;
	}

	public void refreshList()
	{
		net.runelite.api.Friend[] friendNames = this.client.getFriendContainer().getMembers();

		List<Friend> currentFriends = new ArrayList<>(friendManager.getFriends().values());

		for(net.runelite.api.Friend friendName : friendNames)
		{
			Friend friend = new Friend(UUID.randomUUID().toString(), friendName.getName(), friendName.getPrevName());

			log.info("Fetching HiscoreResult for " + friendName.getName());
			friendDataClient.lookupAsync(friendName.getName()).whenCompleteAsync((result, exception) ->
			{
				if(result == null || exception != null)
				{
					StringBuilder stringBuilder = new StringBuilder("Hiscore lookup for \"" + friendName.getName() + "\" failed");
					if(exception != null) stringBuilder.append(" with exception: ").append(exception.getMessage());

					log.warn(stringBuilder.toString());
					return;
				}

				friend.addSnapshotNow(result);

				List<Friend> mergeCandidates = friendManager.getValidMergeCandidates(friend);

				if(mergeCandidates.isEmpty() || currentFriends.isEmpty())
				{
					SwingUtilities.invokeLater(() ->
					{
						resolveMerge(friend, "");
						log.info("No valid merge candidates. Added without merge: {}", friend.getName());
					});
					return;
				}

				if(mergeCandidates.size() == 1)
				{
					SwingUtilities.invokeLater(() ->
					{
						resolveMerge(friend, mergeCandidates.get(0).getID());
						log.info("One valid merge candidate. Merged {} into {}. ", friend.getName(), mergeCandidates.get(0).getName());
					});
					return;
				}

				SwingUtilities.invokeLater(() ->
				{
					queryMerge(friend, mergeCandidates);
					log.info("Merge query called for {}.", friend.getName());
				});
			});

		}

		SwingUtilities.invokeLater(() -> panel.redraw());
	}

	/**
	 *
	 * @param baseFriend
	 * @param candidates
	 */
	public void queryMerge(Friend baseFriend, List<Friend> candidates)
	{
		MergePanel mergePanel = new MergePanel(this, config, baseFriend, candidates);
		panel.addMergePanel(mergePanel);
	}

	/**
	 *
	 * @param newFriend
	 * @param mergeTargetID
	 */
	public void resolveMerge(Friend newFriend, String mergeTargetID)
	{
		if(mergeTargetID.equals(""))
		{
			friendManager.add(newFriend);
		}
		else
		{
			friendManager.merge(newFriend, mergeTargetID);
		}

		saveCurrentFriendData();

		SwingUtilities.invokeLater(() -> panel.redraw());
	}

	public void nextMergePanel()
	{
		SwingUtilities.invokeLater(() -> panel.drawNextMergePanel());
	}

	public void saveCurrentFriendData()
	{
		trackerDataStore.saveFriendDataToConfig(friendManager.getAccountHash(), friendManager.getFriends());
	}

	public void refresh()
	{
		panel.refresh();
	}

	@Provides
	FriendTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FriendTrackerConfig.class);
	}
}
