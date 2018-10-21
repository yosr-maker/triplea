package games.strategy.triplea.settings;

import static games.strategy.triplea.settings.SelectionComponentFactory.booleanRadioButtons;
import static games.strategy.triplea.settings.SelectionComponentFactory.filePath;
import static games.strategy.triplea.settings.SelectionComponentFactory.folderPath;
import static games.strategy.triplea.settings.SelectionComponentFactory.intValueRange;
import static games.strategy.triplea.settings.SelectionComponentFactory.proxySettings;
import static games.strategy.triplea.settings.SelectionComponentFactory.selectionBox;
import static games.strategy.triplea.settings.SelectionComponentFactory.textField;

import javax.swing.JComponent;

import games.strategy.engine.framework.lookandfeel.LookAndFeel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Binds a {@code ClientSetting} to a UI component. This is done by adding an enum element. As part of that the
 * corresponding UI component, a {@code SelectionComponent} is specified. This then automatically adds the setting
 * to the settings window.
 *
 * <p>
 * UI component construction is delegated to {@code SelectionComponentFactory}.
 * </p>
 *
 * <p>
 * There is a 1:n between {@code ClientSettingUiBinding} and {@code ClientSetting}, though
 * typically it will be 1:1, and not all {@code ClientSettings} will be available in the UI.
 * </p>
 */
@AllArgsConstructor
enum ClientSettingSwingUiBinding implements GameSettingUiBinding<JComponent> {
  AI_PAUSE_DURATION_BINDING(
      "AI Pause Duration",
      SettingType.AI,
      "Time (in milliseconds) between AI moves") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return intValueRange(ClientSetting.aiPauseDuration, 0, 3000);
    }
  },

  ARROW_KEY_SCROLL_SPEED_BINDING(
      "Arrow Key Scroll Speed",
      SettingType.MAP_SCROLLING,
      "How fast the map is scrolled (in pixels) when using the arrow keys") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return intValueRange(ClientSetting.arrowKeyScrollSpeed, 0, 500);
    }
  },

  BATTLE_CALC_SIMULATION_COUNT_DICE_BINDING(
      "Simulation Count (Dice)",
      SettingType.BATTLE_SIMULATOR,
      "Default battle simulation count in dice games") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return intValueRange(ClientSetting.battleCalcSimulationCountDice, 10, 100000);
    }
  },

  BATTLE_CALC_SIMULATION_COUNT_LOW_LUCK_BINDING(
      "Simulation Count (LL)",
      SettingType.BATTLE_SIMULATOR,
      "Default battle simulation count in low luck games") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return intValueRange(ClientSetting.battleCalcSimulationCountLowLuck, 10, 100000);
    }
  },

  CONFIRM_DEFENSIVE_ROLLS_BINDING(
      "Confirm defensive rolls",
      SettingType.COMBAT,
      "Whether battle should proceed until you confirm the dice you roll while on defense") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return booleanRadioButtons(ClientSetting.confirmDefensiveRolls);
    }
  },

  CONFIRM_ENEMY_CASUALTIES_BINDING(
      "Confirm enemy casualties",
      SettingType.COMBAT,
      "Whether battles should proceed only once every player has confirmed the casualties selected") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return booleanRadioButtons(ClientSetting.confirmEnemyCasualties);
    }
  },

  SPACE_BAR_CONFIRMS_CASUALTIES_BINDING(
      "Space bar confirms Casualties",
      SettingType.COMBAT,
      "When set to true casualty confirmation can be accepted by pressing space bar.\n"
          + "When set to false, the confirm casualty button has to always be clicked.") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return booleanRadioButtons(ClientSetting.spaceBarConfirmsCasualties);
    }
  },

  LOOK_AND_FEEL_PREF_BINDING(
      "Look and Feel",
      SettingType.LOOK_AND_FEEL,
      "Updates UI theme for TripleA.\n"
          + "WARNING: restart all running TripleA instances after changing this setting to avoid system instability.") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return selectionBox(
          ClientSetting.lookAndFeel,
          LookAndFeel.getLookAndFeelAvailableList(),
          ClientSetting.lookAndFeel.getValueOrThrow(),
          s -> s.replaceFirst(".*\\.", "").replaceFirst("LookAndFeel$", ""));
    }
  },

  MAP_EDGE_SCROLL_SPEED_BINDING(
      "Map Scroll Speed",
      SettingType.MAP_SCROLLING,
      "How fast the map scrolls (in pixels) when the mouse is moved close to the map edge") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return intValueRange(ClientSetting.mapEdgeScrollSpeed, 0, 300);
    }
  },

  MAP_EDGE_SCROLL_ZONE_SIZE_BINDING(
      "Scroll Zone Size",
      SettingType.MAP_SCROLLING,
      "How close to the edge of the map (in pixels) the mouse needs to be for the map to start scrolling") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return intValueRange(ClientSetting.mapEdgeScrollZoneSize, 0, 300);
    }
  },

  SERVER_START_GAME_SYNC_WAIT_TIME_BINDING(
      "Start game timeout",
      SettingType.NETWORK_TIMEOUTS,
      "Maximum time (in seconds) to wait for all clients to sync data on game start") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return intValueRange(ClientSetting.serverStartGameSyncWaitTime, 120, 1500);
    }
  },

  SERVER_OBSERVER_JOIN_WAIT_TIME_BINDING(
      "Observer join timeout",
      SettingType.NETWORK_TIMEOUTS,
      "Maximum time (in seconds) for host to wait for clients and observers") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return intValueRange(ClientSetting.serverObserverJoinWaitTime, 60, 1500);
    }
  },

  SHOW_BATTLES_WHEN_OBSERVING_BINDING(
      "Show battles as observer",
      SettingType.GAME,
      "Whether to show a battle if you are only observing.") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return booleanRadioButtons(ClientSetting.showBattlesWhenObserving);
    }
  },

  SHOW_BETA_FEATURES_BINDING(
      "Show Beta Features",
      SettingType.TESTING,
      "Toggles whether to show 'beta' features. These are game features that are still "
          + "under development and potentially may not be working yet.\n"
          + "Restart to fully activate") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return booleanRadioButtons(ClientSetting.showBetaFeatures);
    }
  },

  SHOW_CONSOLE_BINDING(
      "Show Console",
      SettingType.GAME,
      "Shows the TripleA console, closing the window will turn this setting off") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return booleanRadioButtons(ClientSetting.showConsole);
    }
  },

  MAP_LIST_OVERRIDE_BINDING(
      "Map List Override",
      SettingType.TESTING,
      "Overrides the location of the map listing file. You can, for example, download a copy of the listing file, "
          + "update it, and put the path to that file here.") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return filePath(ClientSetting.mapListOverride);
    }
  },

  TEST_LOBBY_HOST_BINDING(
      "Lobby Host Override",
      SettingType.TESTING,
      "Overrides the IP address or hostname used to connect to the lobby. Useful for connecting to a test lobby.") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return textField(ClientSetting.testLobbyHost);
    }
  },

  TEST_LOBBY_PORT_BINDING(
      "Lobby Port Override",
      SettingType.TESTING,
      "Specifies the port for connecting to a test lobby.\n"
          + "Set to 0 for no override") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return intValueRange(ClientSetting.testLobbyPort, 1, 65535, true);
    }
  },

  TRIPLEA_FIRST_TIME_THIS_VERSION_PROPERTY_BINDING(
      "Show First Time Prompts",
      SettingType.GAME,
      "Setting to true will trigger for any first time prompts to be shown") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return booleanRadioButtons(ClientSetting.firstTimeThisVersion);
    }
  },

  SAVE_GAMES_FOLDER_PATH_BINDING(
      "Saved Games Folder",
      SettingType.FOLDER_LOCATIONS,
      "The folder where saved game files will be stored by default") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return folderPath(ClientSetting.saveGamesFolderPath);
    }
  },

  USER_MAPS_FOLDER_PATH_BINDING(
      "Maps Folder",
      SettingType.FOLDER_LOCATIONS,
      "The folder where game engine will download and find map files.") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return folderPath(ClientSetting.userMapsFolderPath);
    }
  },

  WHEEL_SCROLL_AMOUNT_BINDING(
      "Mouse Wheel Scroll Speed",
      SettingType.MAP_SCROLLING,
      "How fast the map will scroll (in pixels) when using the mouse wheel") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return intValueRange(ClientSetting.wheelScrollAmount, 10, 300);
    }
  },

  PROXY_CHOICE(
      "Network Proxy",
      SettingType.NETWORK_PROXY,
      "Configure TripleA's Network and Proxy Settings\n"
          + "This only effects Play-By-Forum games, dice servers, and map downloads.") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return proxySettings(ClientSetting.proxyChoice, ClientSetting.proxyHost, ClientSetting.proxyPort);
    }
  },

  USE_EXPERIMENTAL_JAVAFX_UI(
      "Use JavaFX UI (Incomplete!)",
      SettingType.TESTING,
      "Enable the experimental JavaFX UI. Not recommended. Isn't working yet.\n"
          + "Just a proof-of-concept. Requires a restart.") {
    @Override
    public SelectionComponent<JComponent> newSelectionComponent() {
      return booleanRadioButtons(ClientSetting.useExperimentalJavaFxUi);
    }
  };

  @Getter(onMethod_ = {@Override})
  private final String title;
  @Getter(onMethod_ = {@Override})
  private final SettingType type;
  @Getter
  private final String description;
}
