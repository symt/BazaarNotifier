package dev.meyi.bn.handlers;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.utilities.Utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

public class UpdateHandler {

  boolean firstJoin = true;

  @SubscribeEvent
  public void onPlayerJoinEvent(FMLNetworkEvent.ClientConnectedToServerEvent event) {
    if (firstJoin) {
      firstJoin = false;
      new ScheduledThreadPoolExecutor(1).schedule(() -> {
        try {
          JSONObject json = new JSONObject(IOUtils.toString(new BufferedReader
              (new InputStreamReader(
                  HttpClientBuilder.create().build().execute(new HttpGet(
                      "https://api.github.com/repos/symt/BazaarNotifier/releases/latest"))
                      .getEntity().getContent()))));
          String[] latestTag = json.getString("tag_name").split("\\.");
          String[] currentTag = BazaarNotifier.VERSION.split("\\.");

          if (latestTag.length == 3 && currentTag.length == 3) {
            for (int i = 0; i < latestTag.length; i++) {
              int latestCheck = Integer.parseInt(latestTag[i]);
              int currentCheck = Integer.parseInt(currentTag[i]);

              if (latestCheck != currentCheck) {
                if (latestCheck < currentCheck) {
                  Minecraft.getMinecraft().thePlayer.addChatMessage(
                      new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
                          + "This version hasn't been released yet. Please report any bugs that you come across."));
                } else {
                  ChatComponentText updateLink = new ChatComponentText(
                      EnumChatFormatting.DARK_RED + "" + EnumChatFormatting.BOLD
                          + "[UPDATE LINK]");
                  updateLink
                      .setChatStyle(updateLink.getChatStyle().setChatClickEvent(new ClickEvent(
                          Action.OPEN_URL,
                          "https://github.com/symt/BazaarNotifier/releases/latest")));
                  Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                      BazaarNotifier.prefix + EnumChatFormatting.RED
                          + "The mod version that you're on is outdated. Please update for the best profits: ")
                      .appendSibling(updateLink));
                }
                break;
              }
            }
          }
          BazaarNotifier.validApiKey = Utils.validateApiKey();
          if (!BazaarNotifier.validApiKey && !BazaarNotifier.apiKeyDisabled) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                BazaarNotifier.prefix + EnumChatFormatting.RED
                    + "The mod doesn't have access to a valid api key yet. Please run /bn api (key) to set your key"));
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }, 3, TimeUnit.SECONDS);
    }
  }
}
