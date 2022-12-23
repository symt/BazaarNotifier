package dev.meyi.bn.modules.calc;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.Exchange;
import dev.meyi.bn.json.Order.OrderType;
import dev.meyi.bn.utilities.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BankCalculator {

  private static final List<Exchange> orderHistory = new ArrayList<>();
  private static final Pattern sellOffer = Pattern.compile(
      "\\[Bazaar] Claimed .* coins from selling (.*)x (.*) at (.*) each!");
  private static final Pattern buyOrder = Pattern.compile(
      "\\[Bazaar] Claimed (.*)x (.*) worth .* coins bought for (.*) each!");
  private static final Pattern instantSell = Pattern.compile(
      "\\[Bazaar] Sold (.*)x (.*) for (.*) coins!");
  private static final Pattern instantBuy = Pattern.compile(
      "\\[Bazaar] Bought (.*)x (.*) for (.*) coins!");
  private static double calculatedProfit = 0;
  private static boolean startup = true;

  public static double getBazaarProfit() {
    return calculatedProfit;
  }

  public static void calculateBazaarProfit() {
    for (int i = orderHistory.size() - 1; i >= 0; i--) {
      Exchange sell = orderHistory.get(i);
      if (sell.getAmount() != 0 && sell.getType() == OrderType.SELL) {
        for (int j = orderHistory.size() - 1; j >= 0; j--) {
          Exchange buy = orderHistory.get(j);
          if (buy.getAmount() != 0 && sell.matchesOrder(buy)) {
            if (buy.getAmount() >= sell.getAmount()) {
              calculatedProfit +=
                  sell.getAmount() * (sell.getPricePerUnit() * .99 - buy.getPricePerUnit());
              buy.removeAmount(sell.getAmount());
              sell.removeAmount(sell.getAmount());
            } else {
              calculatedProfit +=
                  buy.getAmount() * (sell.getPricePerUnit() * .99 - buy.getPricePerUnit());
              sell.removeAmount(buy.getAmount());
              buy.removeAmount(buy.getAmount());
            }

            if (sell.getAmount() == 0) {
              break;
            }
          }
        }
      }
    }

    orderHistory.removeIf(v -> v.getAmount() == 0);

    craftingLoop:
    for (int i = orderHistory.size() - 1; i >= 0; i--) {
      if (orderHistory.get(i).getAmount() != 0 && orderHistory.get(i).canCraft()) {
        Map<String, Integer> craftingResources = orderHistory.get(i).getCraftingResources();
        Map<String, List<Exchange>> availableResources = new HashMap<>();
        craftingResources.keySet().forEach(key -> availableResources.put(key, new ArrayList<>()));
        for (Exchange exchange : orderHistory) {
          if (exchange.getType() == OrderType.BUY && craftingResources.containsKey(
              exchange.getProductId())) {
            availableResources.get(exchange.getProductId()).add(exchange);
          }
        }

        int maxCrafting = orderHistory.get(i).getAmount();

        for (Entry<String, List<Exchange>> entry : availableResources.entrySet()) {
          int amountAvailable = 0;
          for (Exchange e : entry.getValue()) {
            amountAvailable += e.getAmount();
          }

          int craftCost = craftingResources.get(entry.getKey());

          if (amountAvailable < craftCost) {
            break craftingLoop;
          } else {
            maxCrafting = Math.min(maxCrafting, amountAvailable / craftCost);
          }
        }

        double buyValue = 0;

        if (maxCrafting > 0) {
          for (String key : availableResources.keySet()) {
            int valueToRemove = maxCrafting * craftingResources.get(key);
            for (Exchange e : availableResources.get(key)) {
              if (e.getAmount() >= valueToRemove) {
                buyValue += valueToRemove * e.getPricePerUnit();
                e.removeAmount(valueToRemove);
                valueToRemove = 0;
              } else {
                buyValue += e.getAmount() * e.getPricePerUnit();
                valueToRemove -= e.getAmount();
                e.removeAmount(e.getAmount());
              }
            }
          }
        }
        orderHistory.get(i).removeAmount(maxCrafting);
        calculatedProfit +=
            ((double) maxCrafting * orderHistory.get(i).getPricePerUnit()) * .99 - buyValue;
      }
    }

    orderHistory.removeIf(v -> v.getAmount() == 0);
  }

  public static void evaluate(String message) {
    String productId;
    double pricePerUnit;
    int amount;
    OrderType type;

    Matcher m;

    if ((m = sellOffer.matcher(message)).find()) {
      type = OrderType.SELL;
      amount = Integer.parseInt(m.group(1).replaceAll("[,.]", ""));
      pricePerUnit = Double.parseDouble(m.group(3).replaceAll(",", ""));
    } else if ((m = instantSell.matcher(message)).find()) {
      type = OrderType.SELL;
      amount = Integer.parseInt(m.group(1).replaceAll("[,.]", ""));
      double coins = Double.parseDouble(m.group(3).replaceAll(",", ""));
      pricePerUnit = coins / (double) amount;
    } else if ((m = buyOrder.matcher(message)).find()) {
      type = OrderType.BUY;
      amount = Integer.parseInt(m.group(1).replaceAll("[,.]", ""));
      pricePerUnit = Double.parseDouble(m.group(3).replaceAll(",", ""));
    } else if ((m = instantBuy.matcher(message)).find()) {
      type = OrderType.BUY;
      amount = Integer.parseInt(m.group(1).replaceAll("[,.]", ""));
      double coins = Double.parseDouble(m.group(3).replaceAll(",", ""));
      pricePerUnit = coins / (double) amount;
    } else {
      return;
    }

    if (BazaarNotifier.bazaarConv.containsValue(m.group(2))) {
      productId = BazaarNotifier.bazaarConv.inverse().get(m.group(2));
    } else {
      productId = Utils.getItemIdFromName(m.group(2))[1];
    }

    if (!productId.isEmpty()) {
      Exchange e = new Exchange(type, productId, pricePerUnit, amount);

      int index;
      if ((index = orderHistory.indexOf(e)) != -1) {
        orderHistory.get(index).addAmount(amount);
      } else {
        orderHistory.add(e);
      }

      // Regardless of type, because reverse flipping is possible
      //  aka selling an item you already own and buying back cheaper
      calculateBazaarProfit();
    }
  }

  public static synchronized void reset() {
    if (startup) {
      startup = false;
      calculatedProfit = BazaarNotifier.config.bazaarProfit;
    } else {
      calculatedProfit = 0;
    }
    orderHistory.clear();
  }

}