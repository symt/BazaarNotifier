const {Client} = require("discord.js");
const client = new Client({intents: 32767});
require('dotenv').config()

const prefix = "~";
const channel = process.env.CHANNEL;
client.on("ready", () => {
  const {cacheUpdate} = require("./bazaar/bazaar.js");
  cacheUpdate();
  console.log("Login successful!")
});

client.login(process.env.BOT);

module.exports = {client, prefix, channel};