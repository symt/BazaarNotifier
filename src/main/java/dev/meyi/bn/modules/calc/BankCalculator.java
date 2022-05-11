package dev.meyi.bn.modules.calc;


import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import dev.meyi.bn.BazaarNotifier;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class BankCalculator {

  public static double purseLast = 0;
  private static double bazaarProfit2 = 0;
  public static boolean orderWait = false;



  public static double getBazaarProfit() {
      bazaarProfit2 += getPurse() - purseLast;
      purseLast = getPurse();
    return bazaarProfit2;
  }


  public static double purseInBank = 0; //fix for personal bank
  public static boolean isOnDangerousPage = false; //a page that can instantly close the bank gui without opening it again

  private static boolean personalBankInitialised = false;
  private static boolean coopBankInitialised = false;
  private static  boolean purseInitialised = false;



  public static double bank = 0;
  private static double moneyOnStartup = 0;
  public static double bazaarProfit = 0;

  public static double calculateProfit() {
    return getPurse() + moneyStoredInBuyOrders() + moneyStoredInSellOffers() + bank
            - moneyOnStartup;
  }


  public static double moneyStoredInSellOffers() {
    if (BazaarNotifier.orders.size() != 0) {
      double orderWorth = 0;
      for (int i = 0; i < BazaarNotifier.orders.size(); i++) {
        if (BazaarNotifier.orders.get(i).type.equals("sell")) {
          orderWorth += BazaarNotifier.orders.get(i).orderValue;
        }
      }
      return orderWorth;
    }
    return 0;
  }

  public static double moneyStoredInBuyOrders() {
    if (BazaarNotifier.orders.size() != 0) {
      double orderWorth = 0;
      for (int i = 0; i < BazaarNotifier.orders.size(); i++) {
        if (BazaarNotifier.orders.get(i).type.equals("buy")) {
          orderWorth += BazaarNotifier.orders.get(i).orderValue;
        }
      }
      return orderWorth;
    }
    return 0;
  }

  public static double getPurse() {
    double ps = getPurseFromSidebar();
    if (ps == -1) {
      double pa = getPurseFromAPI();
      if(pa != -1 && !purseInitialised){
        moneyOnStartup += pa;
        purseInitialised = true;
      }
      return  pa;
    } else {
      if(!purseInitialised) {
        moneyOnStartup += ps;
        purseInitialised = true;
      }
      return ps;
    }
  }

  public static double getPurseFromAPI() {
    if (BazaarNotifier.playerDataFromAPI.entrySet().size() != 0) {
      return BazaarNotifier.playerDataFromAPI.get("coin_purse").getAsDouble();
    }
    return -1;
  }

  private static double getPurseFromSidebar() {
    //Todo Powder can cause errors
    Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
    if (scoreboard == null) {
      return -1;
    }

    ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);

    if (objective == null) {
      return -1;
    }

    Collection<Score> scores = scoreboard.getSortedScores(objective);
    List<Score> list = scores.stream()
            .filter(input -> input != null && input.getPlayerName() != null && !input.getPlayerName()
                    .startsWith("#")).collect(Collectors.toList());

    if (list.size() > 15) {
      scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
    } else {
      scores = list;
    }

    for (Score score : scores) {
      ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
      if (ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()).contains("Purse")) {
        if (ScorePlayerTeam.formatPlayerName(team, score.getPlayerName())
                .contains(")")) {// coins get added to your purse
          String purse = StringUtils
                  .stripControlCodes(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
          int i = purse.indexOf("(");
          String s = purse.substring(i + 1, purse.length() - 1);
          purse = purse.replace(s, "").replaceAll("[^0-9 .]", "");
          return Float.parseFloat(purse);
        } else {
          String purse = StringUtils
                  .stripControlCodes(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()))
                  .replaceAll("[^0-9 +.]", "");
          return Float.parseFloat(purse);
        }
      }
    }

    return -1;
  }

  public static void extractBankFromItemDescription(IInventory chest, boolean isCoop) {
    //Todo Test Check Coop
    if (chest != null) {
      if (chest.getStackInSlot(11) != null) {
        if (chest.getStackInSlot(11).getDisplayName().toLowerCase().contains("deposit coins")) {
          if(!isCoop) {
            if (!personalBankInitialised) {
              double p = Double.parseDouble(StringUtils.stripControlCodes(
                      chest.getStackInSlot(11).getTagCompound().getCompoundTag("display")
                              .getTagList("Lore", 8)
                              .getStringTagAt(0)).split("balance: ")[1].replaceAll(",", ""));
              bank += p;
              moneyOnStartup += p;
              personalBankInitialised = true;
            }
          }else{
            if (!coopBankInitialised) {
              double p = Double.parseDouble(StringUtils.stripControlCodes(
                      chest.getStackInSlot(11).getTagCompound().getCompoundTag("display")
                              .getTagList("Lore", 8)
                              .getStringTagAt(0)).split("balance: ")[1].replaceAll(",", ""));
              bank += p;
              moneyOnStartup += p;
              coopBankInitialised = true;
            }
          }
        }
      }
    }
  }

  public static void reset() {
    moneyOnStartup = getPurse() + moneyStoredInBuyOrders() + moneyStoredInSellOffers() + bank;
    bazaarProfit = 0;
    bazaarProfit2 = 0;
  }


}