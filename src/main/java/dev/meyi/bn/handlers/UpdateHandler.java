package dev.meyi.bn.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.meyi.bn.BazaarNotifier;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

public class UpdateHandler {

  boolean firstJoin = true;

  @SubscribeEvent
  public void onPlayerJoinEvent(FMLNetworkEvent.ClientConnectedToServerEvent event) {
    if (firstJoin) {
      firstJoin = false;
      new ScheduledThreadPoolExecutor(1).schedule(() -> {
        Gson gson = new Gson();
        try {
          String[] currentTag;
          String[] latestTag;
          int currentBeta = 0;
          int latestBeta = 0;
          boolean beta = false;
          Pattern p = Pattern
              .compile("\"?([0-9]+\\.[0-9]+\\.[0-9]+)-beta([0-9]+)\"?$", Pattern.MULTILINE);
          Matcher m = p.matcher(BazaarNotifier.VERSION);
          if (m.find()) {
            beta = true;
            String version = m.group(1);
            currentTag = version.split("\\.");
            currentBeta = Integer.parseInt(m.group(2));

            // Using the same pattern to check can be risky if there is another version in the build.gradle that also matches, but so far that isn't a problem.

            String buildGradle;
            try {
              buildGradle = IOUtils.toString(new BufferedReader(new InputStreamReader(
                  HttpClientBuilder.create().build().execute(new HttpGet(
                      "https://raw.githubusercontent.com/symt/BazaarNotifier/beta/build.gradle.kts"))
                      .getEntity().getContent())));
            } catch (IOException e) {
              Minecraft.getMinecraft().thePlayer.addChatMessage(
                  new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
                      + "There was an error reading the version. Make sure you aren't on an old beta version. /bn discord"));
              e.printStackTrace();
              return;
            }
            Matcher m2 = p.matcher(buildGradle);
            if (m2.find()) {
              latestTag = m2.group(1).split("\\.");
              latestBeta = Integer.parseInt(m2.group(2));
            } else {
              Minecraft.getMinecraft().thePlayer.addChatMessage(
                  new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
                      + "There was an error reading the beta version. No version matched in the build.gradle file."));
              return;
            }
          } else {
            JsonObject json = gson
                .fromJson(IOUtils.toString(new BufferedReader(new InputStreamReader(
                        HttpClientBuilder.create().build().execute(new HttpGet(
                            "https://api.github.com/repos/symt/BazaarNotifier/releases/latest"))
                            .getEntity().getContent()))),
                    JsonObject.class).getAsJsonObject();
            latestTag = json.get("tag_name").getAsString().split("\\.");
            currentTag = BazaarNotifier.VERSION.split("\\.");
          }

          // Assuming the user is on the correct beta version, this will pass without any chat messages.

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
                beta = false; // Versions don't match, so betas don't matter
                break;
              }
            }
          }

          if (beta) {
            if (currentBeta > latestBeta) {
              Minecraft.getMinecraft().thePlayer.addChatMessage(
                  new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
                      + "This beta version hasn't been released yet. Please report any bugs that you come across."));
            } else if (currentBeta < latestBeta) {
              Minecraft.getMinecraft().thePlayer.addChatMessage(
                  new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.RED
                      + "You are on an outdated beta version. Please update via the discord server. Run /bn discord for the link"));
            } else {
              Minecraft.getMinecraft().thePlayer.addChatMessage(
                  new ChatComponentText(BazaarNotifier.prefix + EnumChatFormatting.GREEN
                      + "You are on a beta version. Please report any bugs you come across in the discord server."));
            }
          }

          if (BazaarNotifier.config.api.isEmpty() && !BazaarNotifier.apiKeyDisabled) {
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