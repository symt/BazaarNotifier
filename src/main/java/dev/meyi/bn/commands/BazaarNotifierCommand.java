package dev.meyi.bn.commands;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.utilities.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class BazaarNotifierCommand extends CommandBase {

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
      if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
        if (!BazaarNotifier.apiKey.equals("")) {
          BazaarNotifier.orders = new JSONArray();
          BazaarNotifier.activeBazaar ^= true;
          player.addChatMessage(new ChatComponentText(
              BazaarNotifier.prefix + EnumChatFormatting.RED + "The mod has been toggled "
                  + EnumChatFormatting.DARK_RED + (BazaarNotifier.activeBazaar ? "on" : "off")));
        } else {
          player.addChatMessage(new ChatComponentText(
              BazaarNotifier.prefix + EnumChatFormatting.RED
                  + "Run /bn api (key) to set your api key. Do /api if you need to get your api key"));
        }
      } else if (args.length >= 1 && args[0].equalsIgnoreCase("api")) {
        if (args.length == 2) {
          BazaarNotifier.apiKey = args[1];
          try {
            if (Utils.validateApiKey()) {
              player.addChatMessage(new ChatComponentText(
                  BazaarNotifier.prefix + EnumChatFormatting.RED
                      + "Your api key has been set."));
              BazaarNotifier.validApiKey = true;
              BazaarNotifier.activeBazaar = true;
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
      } else if (args.length == 1 && args[0].equalsIgnoreCase("dump")) {
        System.out.println(BazaarNotifier.orders);
        player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
            + "Orders dumped to the log file"));
      } else if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
        BazaarNotifier.resetMod();
        player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
            + "All module locations have been reset and the order list has been emptied"));
      } else if (args.length >= 1 && args[0].equalsIgnoreCase("find")) {
        if (args.length == 1) {
          player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
              + "Use the following format: /bn find (item)"));
        } else {
          String item = String.join(" ", args).substring(5).toLowerCase();
          if (BazaarNotifier.bazaarCache.has(item)) {
            JSONObject data = BazaarNotifier.bazaarCache.getJSONObject(item);
            player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + "\n" +
                EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + WordUtils.capitalize(item)
                + "\n"
                + EnumChatFormatting.DARK_RED + "Buy Order: " + EnumChatFormatting.RED +
                BazaarNotifier.df.format(data.getDouble("buyOrderPrice")) + "\n"
                + EnumChatFormatting.DARK_RED + "Sell Offer: "
                + EnumChatFormatting.RED +
                BazaarNotifier.df.format(data.getDouble("sellOfferPrice")) + "\n"
                + EnumChatFormatting.DARK_RED
                + "Estimated Profit: " + EnumChatFormatting.RED +
                BazaarNotifier.df.format(data.getDouble("profitFlowPerMinute")) + "\n"
                + BazaarNotifier.prefix
            ));
          } else {
            player.addChatMessage(new ChatComponentText(
                BazaarNotifier.prefix + EnumChatFormatting.RED
                    + "Please provide a valid item to find."));
          }
        }
      } else if (args.length == 1 && args[0].equalsIgnoreCase("__force")) {
        BazaarNotifier.forceRender ^= true;
        player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
            + "This command is intended for testing purposes only, use it at your own peril. Forced rendering has been turned "
            + EnumChatFormatting.DARK_RED + (BazaarNotifier.forceRender ? "on" : "off")));
      } else if (args.length > 0) {
        player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
            + "The command you just tried to do doesn't exist. Do /bn"));
      } else {
        player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + "\n" +
            EnumChatFormatting.RED + "/bn dump\n" + EnumChatFormatting.RED + "/bn reset\n"
            + EnumChatFormatting.RED + "/bn api (key)\n\n" + EnumChatFormatting.RED + "/bn toggle\n"
            + EnumChatFormatting.RED + "/bn find (item)\n"
            + BazaarNotifier.prefix
        ));
      }
    }
  }

  public boolean canCommandSenderUseCommand(final ICommandSender sender) {
    return true;
  }
}