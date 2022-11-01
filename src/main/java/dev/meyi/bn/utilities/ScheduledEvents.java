package dev.meyi.bn.utilities;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.Order;
import dev.meyi.bn.modules.calc.BankCalculator;
import dev.meyi.bn.modules.calc.CraftingCalculator;
import dev.meyi.bn.modules.calc.SuggestionCalculator;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ScheduledEvents {

  public static ScheduledEvents instance;


  private ScheduledEvents() {
    getBazaarData();
    notificationLoop();
    craftingLoop();
    suggestionLoop();
    collectionLoop();
    purseLoop();
  }

  public static void create() {
    CraftingCalculator.getUnlockedRecipes();
    if (instance == null) {
      new ScheduledEvents();
    }
  }

  public void craftingLoop() {
    Executors.newScheduledThreadPool(1)
        .scheduleAtFixedRate(CraftingCalculator::getBestEnchantRecipes, 5, 5, TimeUnit.SECONDS);
  }

  public void getBazaarData(){
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
      if (BazaarNotifier.activeBazaar && (BazaarNotifier.validApiKey || BazaarNotifier.apiKeyDisabled)) {
        try {
          BazaarNotifier.bazaarDataRaw = Utils.getBazaarData();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }, 0, 2, TimeUnit.SECONDS);
  }

  public void suggestionLoop() {
    Executors.newScheduledThreadPool(1)
        .scheduleAtFixedRate(SuggestionCalculator::basic, 5, 5, TimeUnit.SECONDS);
  }

  public void collectionLoop() {
    Executors.newScheduledThreadPool(1)
        .scheduleAtFixedRate(CraftingCalculator::getUnlockedRecipes, 5, 5, TimeUnit.MINUTES);
  }

  public void purseLoop() {
    Executors.newScheduledThreadPool(1)
        .scheduleAtFixedRate(BankCalculator::getPurse, 7, 5, TimeUnit.SECONDS);
  }



  public void notificationLoop(){
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(()->{
       for (Order order:BazaarNotifier.orders) {
         order.updateStatus();
       }
      }
     , 0, 2, TimeUnit.SECONDS);
  }
}
