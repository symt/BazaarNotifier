const Discord = require("discord.js");
const client = new Discord.Client();
require('dotenv').config()

client.on("ready", () => {
    require("./bazaar/bazaar.js")(client, "732332400440508496");
    console.log("Login successful!")
});

client.login(process.env.BOT);