## Bazaar Bot


This is the bot that's used at the discord server: https://discord.gg/sjNFags

Currently, it should work in every server. The `~find` command, however, hasn't been implemented to work for every server yet.

To setup, you need a .env file for storing the hypixel api key and the bot's token:
```
HYPIXEL_API_KEY=___________________________
BOT=_______________________
```

To set the channel in which the bot spits out the information, go into index.js and change the second parameter in `require("./bazaar/bazaar.js")(client, "732332400440508496");` to match the channel id of your choice. 
If you want the `~find` command to work, edit [bazaar.js](https://github.com/symt/BazaarNotifier/blob/master/discord-bot/src/bazaar/bazaar.js#L22-L25) to match the prefix and channel you want.



If you want to merge the bot with your own, you just need the `require("./bazaar/bazaar.js")(client, channel);` line somewhere in `client.on('ready'...`. Of course, the import should lead to bazaar.js. You might have to edit the file like stated above so you can use the command.
