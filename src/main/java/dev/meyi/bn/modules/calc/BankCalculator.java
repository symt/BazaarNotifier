package dev.meyi.bn.modules.calc;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.Exchange;
import dev.meyi.bn.json.Order.OrderType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BankCalculator {

  private static boolean changed = false;
  private static final List<Exchange> orderHistory = new ArrayList<>();
  private static double calculatedProfit = 0;

  public static synchronized double getBazaarProfit() {
    if (changed) {
      for (int i = orderHistory.size() - 1; i >= 0; i--) {
        if (orderHistory.get(i).getAmount() != 0) {
          for (int j = orderHistory.size() - 1; j >= 0; j--) {
            if (orderHistory.get(j).getAmount() != 0 && orderHistory.get(i)
                .matchesOrder(orderHistory.get(j))) {
              Exchange buy, sell;
              int buyIndex, sellIndex;

              if (orderHistory.get(i).getType() == OrderType.BUY) {
                buy = orderHistory.get((buyIndex = i));
                sell = orderHistory.get((sellIndex = j));
              } else {
                buy = orderHistory.get((buyIndex = j));
                sell = orderHistory.get((sellIndex = i));
              }

              if (buy.getAmount() >= sell.getAmount()) {
                calculatedProfit +=
                    buy.getAmount() * (sell.getPricePerUnit() * .99 - buy.getPricePerUnit());
                buy.removeAmount(buy.getAmount());
                sell.removeAmount(buy.getAmount());
              } else {
                calculatedProfit +=
                    sell.getAmount() * (sell.getPricePerUnit() * .99 - buy.getPricePerUnit());
                sell.removeAmount(sell.getAmount());
                buy.removeAmount(sell.getAmount());
              }

              if ((buyIndex == i && buy.getAmount() == 0) ||
                  (sellIndex == i && sell.getAmount() == 0)) {
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
            if (craftingResources.containsKey(exchange.getProductId())) {
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

    return calculatedProfit;
  }

  public static synchronized void evaluate(String message) {
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


    productId = BazaarNotifier.bazaarConv.inverse().get(m.group(2));

    Exchange e = new Exchange(type, productId, pricePerUnit, amount);

    int index;
    if ((index = orderHistory.indexOf(e)) != -1) {
      orderHistory.get(index).addAmount(amount);
    } else {
      orderHistory.add(e);
    }

    changed = type == OrderType.SELL; // Profit is only changed if something is sold
  }

  public static synchronized void reset() {
    orderHistory.clear();
    calculatedProfit = 0;
  }

  private static final Pattern sellOffer = Pattern.compile(
      "\\[Bazaar] Claimed .* coins from selling (.*)x (.*) at (.*) each!");
  private static final Pattern buyOrder = Pattern.compile(
      "\\[Bazaar] Claimed (.*)x (.*) worth .* coins bought for (.*) each!");
  private static final Pattern instantSell = Pattern.compile(
      "\\[Bazaar] Sold (.*)x (.*) for (.*) coins!");
  private static final Pattern instantBuy = Pattern.compile(
      "\\[Bazaar] Bought (.*)x (.*) for (.*) coins!");

}