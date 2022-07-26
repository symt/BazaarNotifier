package dev.meyi.bn.commands;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.resp.BazaarItem;
import dev.meyi.bn.modules.ModuleName;
import dev.meyi.bn.modules.calc.BankCalculator;
import dev.meyi.bn.modules.calc.CraftingCalculator;
import dev.meyi.bn.modules.calc.SuggestionCalculator;
import dev.meyi.bn.utilities.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang3.text.WordUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class BazaarNotifierCommand extends CommandBase {

  private static long date = System.currentTimeMillis();

  @Override
  public List<String> getCommandAliases() {
    return new ArrayList<String>() {
      {
        add("bn");
      }
    };
  }


  @Override
  public String getCommandName() {
    return "bazaarnotifier";
  }

  @Override
  public String getCommandUsage(ICommandSender sender) {
    return "/bazaarnotifier [subcommand]";
  }

  @Override
  public void processCommand(ICommandSender ics, String[] args) {
    if (ics instanceof EntityPlayer) {
      EntityPlayer player = (EntityPlayer) ics;
      if (args.length >= 1 && args[0].equalsIgnoreCase("toggle")) {
        if (args.length == 1 || args[1].equalsIgnoreCase("all")) {
          if (!BazaarNotifier.config.api.equals("") || BazaarNotifier.apiKeyDisabled) {
            BazaarNotifier.orders.clear();
            BazaarNotifier.activeBazaar ^= true;
            player.addChatMessage(new ChatComponentText(
                BazaarNotifier.prefix + (BazaarNotifier.activeBazaar ? EnumChatFormatting.GREEN
                    : EnumChatFormatting.RED) + "The mod has been toggled " + (
                    BazaarNotifier.activeBazaar ? EnumChatFormatting.DARK_GREEN + "on"
                        : EnumChatFormatting.DARK_RED + "off")));
          } else {
            player.addChatMessage(new ChatComponentText(
                BazaarNotifier.prefix + EnumChatFormatting.RED
                    + "Run /bn api (key) to set your api key. Do /api if you need to get your api key"));
          }
        } else if (args.length == 2) {
          boolean toggle;
          if (args[1].equalsIgnoreCase("suggester")) {
            toggle = BazaarNotifier.modules.toggleModule(ModuleName.SUGGESTION);
          } else if (args[1].equalsIgnoreCase("crafting")) {
            toggle = BazaarNotifier.modules.toggleModule(ModuleName.CRAFTING);
          } else if (args[1].equalsIgnoreCase("bank")) {
            toggle = BazaarNotifier.modules.toggleModule(ModuleName.BANK);
          } else if (args[1].equalsIgnoreCase("notification")) {
            toggle = BazaarNotifier.modules.toggleModule(ModuleName.NOTIFICATION);
          } else {
            player.addChatMessage(new ChatComponentText(
                BazaarNotifier.prefix +
                    EnumChatFormatting.RED + "That module doesn't exist."));
            return;
          }
          player.addChatMessage(new ChatComponentText(
              BazaarNotifier.prefix + (toggle ? EnumChatFormatting.GREEN
                  : EnumChatFormatting.RED) + "The module has been toggled " + (
                  toggle ? EnumChatFormatting.DARK_GREEN + "on"
                      : EnumChatFormatting.DARK_RED + "off")));
        }
      } else if (args.length >= 1 && args[0].equalsIgnoreCase("api")) {
        if (args.length == 2) {
          BazaarNotifier.config.api = args[1];
          try {
            if (Utils.validateApiKey()) {
              player.addChatMessage(new ChatComponentText(
                  BazaarNotifier.prefix + EnumChatFormatting.RED
                      + "Your api key has been set."));
              BazaarNotifier.config.api = args[1];
              BazaarNotifier.validApiKey = true;
              BazaarNotifier.activeBazaar = true;
              CraftingCalculator.getUnlockedRecipes();
              BazaarNotifier.config.collectionCheckDisabled = false;
            } else {
              player.addChatMessage(new ChatComponentText(
                  BazaarNotifier.prefix + EnumChatFormatting.RED
                      + "Your api key is invalid. Please run /api new to get a fresh api key & use that in /bn api (key)"));
              BazaarNotifier.validApiKey = false;
            }
          } catch (IOException e) {
            player.addChatMessage(new ChatComponentText(
                BazaarNotifier.prefix + EnumChatFormatting.RED
                    + "An error occurred when trying to set your api key. Please re-run the command to try again."));
            BazaarNotifier.validApiKey = false;
            e.printStackTrace();
          }
        } else {
          player.addChatMessage(new ChatComponentText(
              BazaarNotifier.prefix + EnumChatFormatting.RED
                  + "Run /bn api (key) to set your api key. Do /api if you need to get your api key."));
          BazaarNotifier.validApiKey = false;
        }
      } else if (args.length >= 1 && args[0].equalsIgnoreCase("settings")) {
        ChatComponentText wikiLink = new ChatComponentText(EnumChatFormatting.RED
            + "" + EnumChatFormatting.BOLD
            + "Go to the wiki ");
        IChatComponent help = new ChatComponentText(BazaarNotifier.prefix).appendSibling(wikiLink
            .setChatStyle(wikiLink.getChatStyle().setChatClickEvent(new ClickEvent(
                Action.OPEN_URL,
                "https://github.com/symt/BazaarNotifier/wiki/How-to-use-BazaarNotifier#settings"))))
            .appendSibling(
                new ChatComponentText(
                    EnumChatFormatting.RED
                        + "for more information on proper usage of this command."));
        if (args.length == 1) {
          player.addChatMessage(help);
        } else {
          switch (args[1].toLowerCase()) {
            case "collection":
              if (BazaarNotifier.config.collectionCheckDisabled && !BazaarNotifier.config.api
                  .equals("")) {
                player.addChatMessage(
                    new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
                        + "Only showing unlocked recipes"));
                BazaarNotifier.config.collectionCheckDisabled = false;
              } else if (BazaarNotifier.config.collectionCheckDisabled) {
                player.addChatMessage(
                    new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
                        + "Please set an API-Key first.(/bn api)"));
                BazaarNotifier.config.collectionCheckDisabled = true;
              } else {
                player.addChatMessage(
                    new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
                        + "Showing all recipes"));
                BazaarNotifier.config.collectionCheckDisabled = true;
              }
              break;
            case "crafting_display":
              if (args.length == 3) {
                player.addChatMessage(new ChatComponentText(
                    BazaarNotifier.prefix
                        + CraftingCalculator.editCraftingModuleGUI(args[2])));
              } else {
                player.addChatMessage(help);
              }
              break;
            case "crafting_sort":
              if (args.length == 2) {
                player.addChatMessage(
                    new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.GREEN
                        + CraftingCalculator.toggleCrafting()));
              } else {
                player.addChatMessage(help);
              }
              break;
            case "crafting_length":
              if (args.length == 3) {
                if (Utils.isInteger(args[2])) {
                  player
                      .addChatMessage(
                          new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.GREEN
                              + CraftingCalculator
                              .setCraftingLength(Integer.parseInt(args[2]))));
                } else {
                  player.addChatMessage(
                      new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
                          + "Please enter a valid number."));
                }
              } else {
                player.addChatMessage(help);
              }
              break;
            case "suggester_length":
              if (args.length == 3) {
                if (Utils.isInteger(args[2])) {
                  player
                      .addChatMessage(
                          new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.GREEN
                              + SuggestionCalculator
                              .setSuggestionLength(Integer.parseInt(args[2]))));
                } else {
                  player.addChatMessage(
                      new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
                          + "Please enter a valid number"));
                }
              } else {
                player.addChatMessage(help);
              }
              break;
            case ("show_chat_messages"):
              BazaarNotifier.config.showChatMessages ^= true;
              if (BazaarNotifier.config.showChatMessages) {
                player.addChatMessage(new ChatComponentText(
                    BazaarNotifier.prefix + EnumChatFormatting.GREEN
                        + "Chat messages are now enabled "));
              } else {
                player.addChatMessage(new ChatComponentText(
                    BazaarNotifier.prefix + EnumChatFormatting.GREEN
                        + "Chat messages are now disabled "));
              }
              break;
            default:
              player.addChatMessage(new ChatComponentText(
                  BazaarNotifier.prefix + EnumChatFormatting.RED + "\"" + args[1]
                      + "\" is an invalid setting."));
              player.addChatMessage(help);
          }
        }
      } else if (args.length > 0 && args[0].equalsIgnoreCase("reset")) {
        if (args.length == 1 || args[1].equalsIgnoreCase("all")) {
          BazaarNotifier.resetMod();
          player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
              + "All module locations have been reset and the order list has been emptied."));
        } else if (args[1].equalsIgnoreCase("orders") && args.length == 2) {
          BazaarNotifier.orders.clear();
          player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
              + "Your orders have been cleared."));
        } else if (args[1].equalsIgnoreCase("scale") && args.length == 2) {
          BazaarNotifier.resetScale();
          player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
              + "Your scale for every module has been reset."));
        } else if (args[1].equalsIgnoreCase("bank") && args.length == 2) {
          BankCalculator.reset();
          player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
              + "Your bank module has been reset."));
        } else {
          player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
              + "That module doesn't exist."));
        }
      } else if (args.length >= 1 && args[0].equalsIgnoreCase("find")) {
        if (args.length == 1) {
          player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
              + "Use the following format: /bn find (item)"));
        } else {
          String itemName = String.join(" ", args).substring(5).toLowerCase().replace('-', ' ');
          itemName = WordUtils.capitalize(itemName);
          if (BazaarNotifier.bazaarConv.containsValue(itemName)
              && BazaarNotifier.bazaarDataRaw != null) {

            String itemConv = BazaarNotifier.bazaarConv.inverse()
                .get(WordUtils.capitalize(itemName.toLowerCase()));
            BazaarItem item = BazaarNotifier.bazaarDataRaw.products.get(itemConv);

            if (BazaarNotifier.enchantCraftingList.getAsJsonObject("normal").has(itemConv)
                || BazaarNotifier.enchantCraftingList.getAsJsonObject("other").has(itemConv)) {
              String[] prices = CraftingCalculator.getEnchantCraft(itemName);

              player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + "\n" +
                  EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + WordUtils
                  .capitalize(itemName) + "\n" +
                  EnumChatFormatting.DARK_RED + "Buy Order: " +
                  EnumChatFormatting.RED + BazaarNotifier.df.format(item.sell_summary.size() == 0 ?
                  0 : item.sell_summary.get(0).pricePerUnit) + "\n" +
                  EnumChatFormatting.DARK_RED + "Sell Offer: " +
                  EnumChatFormatting.RED + BazaarNotifier.df.format(item.buy_summary.size() == 0 ?
                  0 : item.buy_summary.get(0).pricePerUnit) + "\n" +
                  EnumChatFormatting.DARK_RED + "Estimated Profit: " +
                  EnumChatFormatting.RED + BazaarNotifier.df
                  .format(SuggestionCalculator.calculateEP(item)) + "\n" +
                  EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + "Crafting:" + "\n" +
                  EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + "Buy order materials:" + "\n" +
                  EnumChatFormatting.DARK_RED + "Profit (Instant Sell): " +
                  EnumChatFormatting.RED + prices[0] + "\n" +
                  EnumChatFormatting.DARK_RED + "Profit (Sell Offer): " +
                  EnumChatFormatting.RED + prices[1] + "\n" +
                  EnumChatFormatting.DARK_RED + "Profit per 1M: " +
                  EnumChatFormatting.RED + prices[2] + "\n" +
                  EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + "Instant buy materials:" + "\n" +
                  EnumChatFormatting.DARK_RED + "Profit (Instant Sell): " +
                  EnumChatFormatting.RED + prices[3] + "\n" +
                  EnumChatFormatting.DARK_RED + "Profit (Sell Offer): " +
                  EnumChatFormatting.RED + prices[4] + "\n" +
                  EnumChatFormatting.DARK_RED + "Profit per 1M: " +
                  EnumChatFormatting.RED + prices[5] + "\n" +
                  BazaarNotifier.prefix
              ));
            } else {
              player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + "\n" +
                  EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + WordUtils
                  .capitalize(itemName) + "\n" +
                  EnumChatFormatting.DARK_RED + "Buy Order: " +
                  EnumChatFormatting.RED + BazaarNotifier.df.format(item.sell_summary.size() == 0 ?
                  0 : item.sell_summary.get(0).pricePerUnit) + "\n" +
                  EnumChatFormatting.DARK_RED + "Sell Offer: " +
                  EnumChatFormatting.RED + BazaarNotifier.df.format(item.buy_summary.size() == 0 ?
                  0 : item.buy_summary.get(0).pricePerUnit) + "\n" +
                  EnumChatFormatting.DARK_RED + "Estimated Profit: " +
                  EnumChatFormatting.RED + BazaarNotifier.df
                  .format(SuggestionCalculator.calculateEP(item)) + "\n" +
                  BazaarNotifier.prefix
              ));
            }


          } else if (BazaarNotifier.bazaarDataRaw == null) {
            player.addChatMessage(new ChatComponentText(
                BazaarNotifier.prefix + EnumChatFormatting.RED
                    + "Please wait a moment for the mod to get bazaar information"));
          } else {
            player.addChatMessage(new ChatComponentText(
                BazaarNotifier.prefix + EnumChatFormatting.RED
                    + "Please provide a valid item to find."));
          }
        }
      }else if(args.length == 1 && args[0].equalsIgnoreCase("help")){
        player.addChatMessage(new ChatComponentText(
                BazaarNotifier.prefix + "\n" + EnumChatFormatting.RED + "/bn reset (value)\n"
                        + EnumChatFormatting.RED + "/bn api (key)\n\n" + EnumChatFormatting.RED
                        + "/bn toggle\n"
                        + EnumChatFormatting.RED
                        + "/bn settings (setting) [value]\n"
                        + EnumChatFormatting.RED + "/bn find (item)\n" + EnumChatFormatting.RED
                        + "/bn discord\n"
                        + BazaarNotifier.prefix
        ));
      } else if (args.length == 1 && args[0].equalsIgnoreCase("__force")) {
        BazaarNotifier.forceRender ^= true;
        player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
            + "This command is intended for testing purposes only, use it at your own peril. Forced rendering has been turned "
            + EnumChatFormatting.DARK_RED + (BazaarNotifier.forceRender ? "on" : "off")));
      } else if (args.length == 1 && args[0].equalsIgnoreCase("discord")) {
        ChatComponentText discordLink = new ChatComponentText(
            EnumChatFormatting.DARK_GREEN + "" + EnumChatFormatting.BOLD
                + "[DISCORD LINK]");
        discordLink
            .setChatStyle(discordLink.getChatStyle().setChatClickEvent(new ClickEvent(
                Action.OPEN_URL,
                "https://discord.com/invite/wjpJSVSwvD")));
        ChatComponentText supportLink = new ChatComponentText(
            EnumChatFormatting.DARK_GREEN + "" + EnumChatFormatting.BOLD
                + "[PATREON LINK]");
        supportLink
            .setChatStyle(supportLink.getChatStyle().setChatClickEvent(new ClickEvent(
                Action.OPEN_URL,
                "https://patreon.com/meyi")));

        player.addChatMessage(new ChatComponentText(
            BazaarNotifier.prefix + "\n" + EnumChatFormatting.GREEN + "Join the discord server: ")
            .appendSibling(discordLink).appendSibling(
                new ChatComponentText(
                    "\n" + EnumChatFormatting.GREEN + "If you want, you can support my work: ")
                    .appendSibling(supportLink))
            .appendSibling(new ChatComponentText("\n" + BazaarNotifier.prefix)));

      } else if (args.length == 1 && args[0].equalsIgnoreCase("update")) {
        if (date < System.currentTimeMillis() - (10 * 60 * 1000)) {
          try {
            Utils.updateResources();
            player.addChatMessage(
                new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.GREEN
                    + "Successfully updated required resources from GitHub"));
            date = System.currentTimeMillis();
          } catch (IOException ignored) {
            player
                .addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
                    + "There was an error when updating your resources. Try again later"));
            date = System.currentTimeMillis();
          }
        } else {
          player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
              + "Please wait 10 minutes before running that command again"));
        }
      } else if (args.length > 0) {
        player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
            + "The command you just tried to do doesn't exist. Do /bn"));
      } else {
        BazaarNotifier.guiToOpen = "settings";
      }
    }
  }

  public boolean canCommandSenderUseCommand(final ICommandSender sender) {
    return true;
  }


  @Override
  public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
    // TODO: When dropping org.json, this should be replaced with a json object that helps generates this automatically.

    /*
    api
    discord
    find
    reset
      => all
      => orders
      => scale
      => bank
    toggle
      => all
      => bank
      => suggester
      => crafting
      => notification
    settings
      => collection
      => crafting_display
        => instant_sell
        => sell_offer
        => ppm
      => crafting_sort
      => crafting_length
      => suggester_length
     */
    List<String> arguments = new ArrayList<>();

    if (args.length <= 1) {
      new ArrayList<String>() {
        {
          add("api");
          add("discord");
          add("find");
          add("reset");
          add("settings");
          add("toggle");
          add("update");
        }
      }.forEach(cmd -> {
        if (args[0].trim().length() == 0 || cmd.startsWith(args[0].toLowerCase())) {
          arguments.add(cmd);
        }
      });
    } else {
      if (args.length <= 2 && args[0].equalsIgnoreCase("reset")) {
        new ArrayList<String>() {
          {
            add("all");
            add("orders");
            add("scale");
            add("bank");
          }
        }.forEach(cmd -> {
          if (args[1].trim().length() == 0 || cmd.startsWith(args[1].toLowerCase())) {
            arguments.add(cmd);
          }
        });
      } else if (args.length <= 2 && args[0].equalsIgnoreCase("find")) {
        ArrayList<String> a = new ArrayList<>();
        for (String s : BazaarNotifier.bazaarConv.values()) {
          s = s.replace(' ', '-');
          a.add(s.toLowerCase());
        }
        a.forEach(cmd -> {
          if (args[1].trim().length() == 0 || cmd.startsWith(args[1].toLowerCase())) {
            arguments.add(cmd);
          }
          if (!arguments.contains(cmd) && (cmd.contains(args[1].toLowerCase()))) {
            arguments.add(cmd);
          }
        });
      } else if (args.length <= 2 && args[0].equalsIgnoreCase("toggle")) {
        new ArrayList<String>() {
          {
            add("all");
            add("suggester");
            add("crafting");
            add("bank");
            add("notification");
          }
        }.forEach(cmd -> {
          if (args[1].trim().length() == 0 || cmd.startsWith(args[1].toLowerCase())) {
            arguments.add(cmd);
          }
        });
      } else if (args[0].equalsIgnoreCase("settings")) {
        if (args.length == 2) {
          new ArrayList<String>() {
            {
              add("collection");
              add("crafting_display");
              add("crafting_sort");
              add("crafting_length");
              add("suggester_length");
              add("show_chat_messages");
            }
          }.forEach(cmd -> {
            if (args[1].trim().length() == 0 || cmd.startsWith(args[1].toLowerCase())) {
              arguments.add(cmd);
            }
          });
        } else if (args.length == 3 && args[1].equalsIgnoreCase("crafting_display")) {
          new ArrayList<String>() {
            {
              add("instant_sell");
              add("sell_offer");
              add("ppm");
            }
          }.forEach(cmd -> {
            if (args[2].trim().length() == 0 || cmd.startsWith(args[2].toLowerCase())) {
              arguments.add(cmd);
            }
          });
        }

      }
    }
    return arguments;
  }
}


