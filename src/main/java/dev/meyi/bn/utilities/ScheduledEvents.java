package dev.meyi.bn.utilities;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.Order;
import dev.meyi.bn.modules.calc.CraftingCalculator;
import dev.meyi.bn.modules.calc.SuggestionCalculator;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class ScheduledEvents {

  public static ScheduledEvents instance;

  private final Map<String, ScheduledExecutorService> executors = new HashMap<>();

  private ScheduledEvents() {
    executors.put("bazaar", getScheduler("bazaar"));
    executors.put("notification", getScheduler("notification"));
    executors.put("crafting", getScheduler("crafting"));
    executors.put("suggestion", getScheduler("suggestion"));
    executors.put("collection", getScheduler("collection"));
    executors.put("purse", getScheduler("purse"));

    shutdownWatcher();
  }

  public static void create() {
    CraftingCalculator.getUnlockedRecipes();
    if (instance == null) {
      instance = new ScheduledEvents();
    }
  }

  private ScheduledExecutorService getScheduler(String key) {
    switch (key) {
      case "bazaar":
        return getBazaarData();
      case "notification":
        return notificationLoop();
      case "crafting":
        return craftingBankLoop();
      case "suggestion":
        return suggestionLoop();
      case "collection":
        return collectionLoop();
    }
    return null;
  }

  private void shutdownWatcher() {
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
      for (Entry<String, ScheduledExecutorService> entry : executors.entrySet()) {
        if (entry.getValue().isShutdown()) {
          if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                BazaarNotifier.prefix + EnumChatFormatting.RED + "Something has caused the " + entry
                    .getKey()
                    + " scheduler to crash. Please report this to the discord server (/bn discord)"));

          }
          executors.put(entry.getKey(), getScheduler(entry.getKey()));
        }
      }
    }, 1, 1, TimeUnit.MINUTES);
  }

  public ScheduledExecutorService craftingBankLoop() {
    ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);
    ex.scheduleAtFixedRate(() -> {
      if (BazaarNotifier.activeBazaar) {
        try {
          CraftingCalculator.getBestEnchantRecipes();

          // Reset the bank calculator on the new day
          // It probably isn't necessary to do this in a scheduler, but here we are.
          long currentTime = System.currentTimeMillis();
          Date reset = new Date(currentTime - (currentTime % 86400000));
          if (reset.after(new Date(BazaarNotifier.config.lastLogin))) {
            BazaarNotifier.config.lastLogin = System.currentTimeMillis();
            BazaarNotifier.config.bankModule.bazaarDailyAmount = 0;
          }
        } catch (Exception t) {
          t.printStackTrace();
        }
      }
    }, 5, 5, TimeUnit.SECONDS);
    return ex;
  }

  public ScheduledExecutorService getBazaarData() {
    ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);
    ex.scheduleAtFixedRate(() -> {
      if (BazaarNotifier.activeBazaar) {
        try {
          BazaarNotifier.bazaarDataRaw = Utils.getBazaarData();
        } catch (Exception t) {
          t.printStackTrace();
        }
      }
    }, 0, 2, TimeUnit.SECONDS);
    return ex;
  }

  public ScheduledExecutorService suggestionLoop() {
    ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);
    ex.scheduleAtFixedRate(() -> {
      if (BazaarNotifier.activeBazaar) {
        try {
          SuggestionCalculator.basic();
        } catch (Exception t) {
          t.printStackTrace();
        }
      }
    }, 5, 5, TimeUnit.SECONDS);
    return ex;
  }

  public ScheduledExecutorService collectionLoop() {
    ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);
    ex.scheduleAtFixedRate(() -> {
      if (BazaarNotifier.activeBazaar) {
        try {
          CraftingCalculator.getUnlockedRecipes();
        } catch (Exception t) {
          t.printStackTrace();
        }
      }
    }, 5, 5, TimeUnit.MINUTES);
    return ex;
  }


  public ScheduledExecutorService notificationLoop() {
    ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);
    ex.scheduleAtFixedRate(() -> {
      if (BazaarNotifier.activeBazaar) {
        try {
          for (Order order : BazaarNotifier.orders) {
            order.updateStatus();
          }
        } catch (Exception t) {
          t.printStackTrace();
        }
      }
    }, 0, 2, TimeUnit.SECONDS);

    return ex;
  }
}
