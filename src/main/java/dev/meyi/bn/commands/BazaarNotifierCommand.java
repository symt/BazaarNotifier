package dev.meyi.bn.commands;

import dev.meyi.bn.BazaarNotifier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
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
          BazaarNotifier.orders = new JSONObject();
          BazaarNotifier.activeBazaar ^= true;
          player.addChatMessage(new ChatComponentText(
              BazaarNotifier.prefix + EnumChatFormatting.RED + "The mod has been toggled "
                  + EnumChatFormatting.DARK_RED + (BazaarNotifier.activeBazaar ? "on" : "off")));
        } else {
          player.addChatMessage(new ChatComponentText(
              BazaarNotifier.prefix + EnumChatFormatting.RED
                  + "Run /bn api (key) to set your api key. Do /api if you need to get your api key"));
        }
      } else if (args.length == 2 && args[0].equalsIgnoreCase("api")) {
        BazaarNotifier.apiKey = args[1];
        player.addChatMessage(new ChatComponentText(
            BazaarNotifier.prefix + EnumChatFormatting.RED
                + "Your api key has been set. Do /bn toggle to start the mod"));
      } else if (args.length == 1 && args[0].equalsIgnoreCase("dump")) {
        System.out.println(BazaarNotifier.orders);
        player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
            + "Orders dumped to the log file"));
      } else if (args.length == 1 && args[0].equalsIgnoreCase("empty")) {
        BazaarNotifier.orders = new JSONObject();
        player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
            + "Order list was emptied"));
      } else if (args.length == 1 && args[0].equalsIgnoreCase("suggest")) {
        BazaarNotifier.render ^= true;
        player.addChatMessage(new ChatComponentText(
            BazaarNotifier.prefix + EnumChatFormatting.RED + "Suggestions have been turned "
                + EnumChatFormatting.DARK_RED + (BazaarNotifier.activeBazaar ? "on" : "off")));
      } else if (args.length > 0) {
        player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
            + "The command you just tried to do doesn't work"));
      } else {
        player.addChatMessage(new ChatComponentText(BazaarNotifier.prefix + "\n" +
            EnumChatFormatting.RED + "/bn dump\n" + EnumChatFormatting.RED + "/bn empty\n"
            + EnumChatFormatting.RED + "/bn api (key)\n\n" + EnumChatFormatting.RED + "/bn toggle\n"
            + EnumChatFormatting.RED + "/bn suggest\n"
            + BazaarNotifier.prefix
        ));
      }
    }
  }

  public boolean canCommandSenderUseCommand(final ICommandSender sender) {
    return true;
  }
}