package com.friendtracker;

import com.friendtracker.data.FriendDataClient;
import com.friendtracker.data.TrackerDataStore;
import com.friendtracker.friends.Friend;
import com.friendtracker.friends.FriendManager;
import com.friendtracker.panel.FriendTrackerPanel;
import com.friendtracker.panel.MergePanel;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.RemovedFriend;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Friends Activity Tracker"
)
public class FriendTrackerPlugin extends Plugin
{

	public static final String CONFIG_GROUP_NAME = "friend-tracker";
	@Inject private Client client;
	@Inject private ConfigManager configManager;
	@Getter @Inject private FriendTrackerConfig config;
	@Inject private FriendDataClient friendDataClient;
	@Inject private TrackerDataStore trackerDataStore;
	@Inject private ClientToolbar clientToolbar;

	private FriendTrackerPanel panel;
	private NavigationButton navButton;
	@Getter private FriendManager friendManager;
	private long lastAccount;

	@Getter @Setter
	private String friendTextFilter;

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
		if (event.getGroup().equals(CONFIG_GROUP_NAME))
		{
			switch(event.getKey())
			{
				case "wrapMergeCandidates":
					panel.refresh();
			}
		}
	}

	@Subscribe
	public void onRemovedFriend(RemovedFriend event)
	{
		if(config.deleteDataOnRemovedFriend())
		{
			String displayName = Text.toJagexName(event.getNameable().getName());
			String previousName = event.getNameable().getPrevName();
			if(previousName != null)
			{
				previousName = Text.toJagexName(previousName);
			}
			log.debug("Remove friend: '{}'", displayName);
			removeFriend(displayName, previousName);
		}
	}

	public void removeFriend(String displayName, String previousName)
	{
		if (!navButton.isSelected())
		{
			navButton.getOnSelect().run();
		}
		friendManager.removeFriend(displayName, previousName);
		redraw();
		saveCurrentFriendData();
	}

	private static final String REFRESH = "Refresh Tracker";
	private static final String DELETE = "Delete Tracker Data";

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!config.refreshMenuOption() && !config.deleteMenuOption()) {
			return;
		}

		final String option = event.getOption();
		final int componentId = event.getActionParam1();
		final int groupId = WidgetInfo.TO_GROUP(componentId);

		if (groupId == WidgetInfo.FRIENDS_LIST.getGroupId() && option.equals("Delete"))
		{
			if(config.refreshMenuOption())
			{
				client.createMenuEntry(-2)
					.setOption(REFRESH)
					.setTarget(event.getTarget())
					.setType(MenuAction.RUNELITE)
					.setIdentifier(event.getIdentifier())
					.onClick(e ->
					{
						String target = Text.removeTags(e.getTarget());
						lookupAndMergeAsync(target, null, false);
					});
			}

			if(config.deleteMenuOption())
			{
				client.createMenuEntry(-3)
					.setOption(DELETE)
					.setTarget(event.getTarget())
					.setType(MenuAction.RUNELITE)
					.setIdentifier(event.getIdentifier())
					.onClick(e ->
					{
						String target = Text.removeTags(e.getTarget());
						removeFriend(target, null);
					});
			}
		}
	}

	private boolean isLoggedInState(GameState gameState)
	{
		return gameState != GameState.LOGIN_SCREEN && gameState != GameState.LOGIN_SCREEN_AUTHENTICATOR;
	}

	public void refreshList()
	{
		net.runelite.api.Friend[] friendNames = this.client.getFriendContainer().getMembers();

		boolean emptyList = friendManager.getFriends().values().isEmpty();

		for(net.runelite.api.Friend friendName : friendNames)
		{
			lookupAndMergeAsync(friendName.getName(), friendName.getPrevName(), emptyList);
		}

		SwingUtilities.invokeLater(() -> panel.redraw());
	}

	public void lookupAndMergeAsync(String name, String previousName, boolean emptyList)
	{
		if (!navButton.isSelected())
		{
			navButton.getOnSelect().run();
		}
		Friend friend = new Friend(UUID.randomUUID().toString(), name, previousName);

		log.info("Fetching HiscoreResult for " + name);
		friendDataClient.lookupAsync(name).whenCompleteAsync((result, exception) ->
		{
			if(result == null || exception != null)
			{
				StringBuilder stringBuilder = new StringBuilder("Hiscore lookup for \"" + name + "\" failed");
				if(exception != null) stringBuilder.append(" with exception: ").append(exception.getMessage());

				log.warn(stringBuilder.toString());
				return;
			}

			friend.addSnapshotNow(result);

			mergeNewSnapshot(friend, emptyList);
		});
	}

	private void mergeNewSnapshot(Friend friend, boolean emptyList)
	{
		List<Friend> mergeCandidates = friendManager.getValidMergeCandidates(friend);

		if(emptyList || mergeCandidates.isEmpty())
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
		if(panel == null) return;

		panel.refresh();
	}

	public void redraw()
	{
		if(panel == null) return;

		SwingUtilities.invokeLater(() ->
		{
			panel.redraw();
		});
	}

	@Provides
	FriendTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FriendTrackerConfig.class);
	}
}
