## Bazaar Bot


This is the bot that's used at the discord server: https://discord.gg/sjNFags

Currently, it should work in every server. The `~find` command, however, hasn't been implemented to work for every server yet. You can implement it for other servers by changing the channel id, but it won't work if the instance isn't yours.

To setup, you need a .env file for storing the hypixel api key and the bot's token:
```
HYPIXEL_API_KEY=___________________________
BOT=_______________________
CHANNEL=______________
```

To set the channel in which the bot spits out the information, go into index.js and change the channel variable to the id of the channel of your choosing. You can also modify the .env file as seen above. The prefix, defaulting to `~`, can be set in index.js.

If you want to merge the bot with your own, change the prefix, client and channel in the import variables of bazaar/bazaar.js to your own and run cacheUpdate function in your ready event (it will automatically run the rest of the files);

```
