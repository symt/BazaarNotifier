package dev.meyi.bn.commands;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.resp.BazaarItem;
import dev.meyi.bn.modules.calc.BankCalculator;
import dev.meyi.bn.modules.calc.CraftingCalculator;
import dev.meyi.bn.modules.calc.SuggestionCalculator;
import dev.meyi.bn.utilities.Utils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.text.WordUtils;


public class BazaarNotifierCommand extends CommandBase {

  private static long date = 0; // This can be set to System.currentTimeMillis, but it makes testing annoying

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
      if (args.length > 0 && args[0].equalsIgnoreCase("reset")) {
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
          String itemName = WordUtils
              .capitalize(String.join(" ", args).substring(5).replaceAll("-", " "));
          if (BazaarNotifier.bazaarDataRaw != null) {
            String[] itemSet = Utils.getItemIdFromName(itemName);
            itemName = itemSet[0];
            String itemConv = itemSet[1];
            BazaarItem item = BazaarNotifier.bazaarDataRaw.products.get(itemConv);
            String findItemString = BazaarNotifier.prefix + EnumChatFormatting.RED
                + "Please provide a valid item to find.";

            String bulletPoint = EnumChatFormatting.WHITE + "\u2022 ";
            String separator = EnumChatFormatting.RED + " / ";

            if (BazaarNotifier.bazaarConv.containsKey(itemConv)) {
              findItemString = BazaarNotifier.header + "\n" +
                  EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + WordUtils
                  .capitalize(itemName) + "\n  " + bulletPoint +
                  EnumChatFormatting.RED + "Buy Order: " +
                  EnumChatFormatting.GRAY + BazaarNotifier.df.format(item.sell_summary.size() == 0 ?
                  0 : item.sell_summary.get(0).pricePerUnit) + "\n  " + bulletPoint +
                  EnumChatFormatting.RED + "Sell Offer: " +
                  EnumChatFormatting.GRAY + BazaarNotifier.df.format(item.buy_summary.size() == 0 ?
                  0 : item.buy_summary.get(0).pricePerUnit) + "\n  " + bulletPoint +
                  EnumChatFormatting.RED + "Estimated Profit: " +
                  EnumChatFormatting.GRAY + BazaarNotifier.df
                  .format(SuggestionCalculator.calculateEP(item)) + "\n";
            }

            if (BazaarNotifier.enchantCraftingList.getAsJsonObject("other").has(itemConv)) {

              String[] prices = CraftingCalculator.getEnchantCraft(itemConv);

              findItemString +=
                  EnumChatFormatting.DARK_RED + "" + EnumChatFormatting.BOLD + "Crafting (" +
                      EnumChatFormatting.GRAY
                      + "Buy order" + separator + EnumChatFormatting.GRAY + "Instant buy"
                      + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + ")" + "\n  " +
                      bulletPoint +
                      EnumChatFormatting.RED + "Profit (Instant Sell): " +
                      EnumChatFormatting.GRAY + BazaarNotifier.df
                      .format(Double.parseDouble(prices[0])) + separator + EnumChatFormatting.GRAY +
                      BazaarNotifier.df.format(Double.parseDouble(prices[3])) + "\n  " + bulletPoint
                      +
                      EnumChatFormatting.RED + "Profit (Sell Offer): " +
                      EnumChatFormatting.GRAY + BazaarNotifier.df
                      .format(Double.parseDouble(prices[1])) + separator + EnumChatFormatting.GRAY +
                      BazaarNotifier.df.format(Double.parseDouble(prices[4])) + "\n  " + bulletPoint
                      +
                      EnumChatFormatting.RED + "Profit per 1M: " +
                      EnumChatFormatting.GRAY + BazaarNotifier.df
                      .format(Double.parseDouble(prices[2])) + separator + EnumChatFormatting.GRAY +
                      BazaarNotifier.df.format(Double.parseDouble(prices[5])) + "\n" +
                      BazaarNotifier.header;

            } else if (BazaarNotifier.bazaarConv.containsKey(itemConv)) {
              findItemString += BazaarNotifier.header;
            }

            player.addChatMessage(new ChatComponentText(findItemString));

          } else {
            player.addChatMessage(new ChatComponentText(
                BazaarNotifier.prefix + EnumChatFormatting.RED
                    + "Please wait a moment for the mod to get bazaar information"));
          }
        }
      } else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
        player.addChatMessage(new ChatComponentText(
            BazaarNotifier.header + "\n"
                + EnumChatFormatting.RED + "/bn " + EnumChatFormatting.DARK_RED + "\u2192"
                + EnumChatFormatting.GRAY + " Opens the GUI\n"
                + EnumChatFormatting.RED + "/bn reset (value) " + EnumChatFormatting.DARK_RED
                + "\u2192" + EnumChatFormatting.GRAY
                + " Reset specific modules to default settings\n"
                + EnumChatFormatting.RED + "/bn api (key) " + EnumChatFormatting.DARK_RED + "\u2192"
                + EnumChatFormatting.GRAY + " Sets your api key for crafting module\n"
                + EnumChatFormatting.RED + "/bn find (item) " + EnumChatFormatting.DARK_RED
                + "\u2192" + EnumChatFormatting.GRAY + " Search specific item's prices and EP\n"
                + EnumChatFormatting.RED + "/bn discord " + EnumChatFormatting.DARK_RED + "\u2192"
                + EnumChatFormatting.GRAY + " Provides discord link (beta access + more)\n"
                + BazaarNotifier.header
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
          new Thread(() -> {
            try {
              Utils.updateResources();
              date = System.currentTimeMillis();
            } catch (Exception e) {
              player.addChatMessage(new ChatComponentText(
                  BazaarNotifier.prefix + EnumChatFormatting.RED
                      + "Resource update failed. Please try again."));
            }
          }).start();
          player.addChatMessage(
              new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.GREEN
                  + "Updating required resources from GitHub"));

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
    List<String> arguments = new ArrayList<>();

    if (args.length <= 1) {
      new ArrayList<String>() {
        {
          add("discord");
          add("find");
          add("reset");
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
      }
    }
    return arguments;
  }
}