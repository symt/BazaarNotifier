package dev.meyi.bn.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Button;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.annotations.HypixelKey;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.annotations.Text;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.config.migration.JsonMigrator;
import cc.polyfrost.oneconfig.config.migration.JsonName;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.calc.BankCalculator;
import dev.meyi.bn.modules.module.BankModule;
import dev.meyi.bn.modules.module.CraftingModule;
import dev.meyi.bn.modules.module.NotificationModule;
import dev.meyi.bn.modules.module.SuggestionModule;
import dev.meyi.bn.utilities.Defaults;

import java.awt.Color;


public class Configuration extends Config {

  public Configuration() {
    super(new Mod("BazaarNotifier", ModType.SKYBLOCK,"/icon.png", new JsonMigrator("./config/BazaarNotifier/config.json")), "bazaarnotifier.json");
    initialize();
    addDependency("collectionCheck", "Requires ApiKey", () -> {
      if ("".equals(api)) collectionCheck = false;
      return !"".equals(api);
    });
  }



  @JsonName("version")
  public String version = BazaarNotifier.VERSION;

  @JsonName("api")
  @HypixelKey
  @Text(name = "API-Key",
          secure = true
  )
  public String api = "";

  @HUD(name = "Suggestion Module",
          category = "Suggestion Module"
  )
  public SuggestionModule suggestionModule = new SuggestionModule();
  @HUD(name = "Crafting Module",
          category = "Crafting Module"
  )
  public CraftingModule craftingModule = new CraftingModule();
  @HUD(name = "Notification Module",
          category = "Notification Module"
  )
  public NotificationModule notificationModule = new NotificationModule();
  @HUD(name = "Bank Module",
          category = "Bank Module"
  )
  public BankModule bankModule = new BankModule();

  @Switch(name = "Allow old Movement and Rescaling",
          category = "General",
          description = "Allows movement and rescaling outside the edit hud window"
  )
  public boolean legacyMovement = true;
  @JsonName("showChatMessages")
  @Switch(name = "Show Chat Messages",
          description = "Disables messages from Bazaar Notifier"
  )
  public boolean showChatMessages = Defaults.SEND_CHAT_MESSAGES;

  @JsonName("collectionCheck")
  @Switch(name = "Collection checks",
          category = "Crafting Module",
          description = "Only shows unlocked recipes"
  )
  public boolean collectionCheck = Defaults.COLLECTION_CHECKING;


  @cc.polyfrost.oneconfig.config.annotations.Color(name = "Info Color", allowAlpha = false)
  public OneColor infoColor = new OneColor(Color.GRAY);
  @cc.polyfrost.oneconfig.config.annotations.Color(name = "Item Color", allowAlpha = false)
  public OneColor itemColor = new OneColor(Color.CYAN);
  @cc.polyfrost.oneconfig.config.annotations.Color(name = "Number Color", allowAlpha = false)
  public OneColor numberColor = new OneColor(Color.MAGENTA);

  @SuppressWarnings("unused")
  @Button(name = "Reset Colors", text = "Reset")
  Runnable r = () ->{
    infoColor = new OneColor(Color.GRAY);
    itemColor = new OneColor(Color.CYAN);
    numberColor = new OneColor(Color.MAGENTA);
  };


  @SuppressWarnings("unused")
  @Button(
          name = "Reset Profit",
          text = "Reset",
          category = "Bank Module"
  )
  Runnable resetBank = BankCalculator::reset;
}
