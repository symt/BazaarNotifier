const schedule = require("node-schedule");
const Discord = require("discord.js");
const axios = require("axios").default;
const bazaarConversions = require("./bazaar_conversions.json");
const performance = require("./performance.js");

const toTitleCase = (phrase) => {
  return phrase
    .toLowerCase()
    .split(" ")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
};

const Bazaar = (discordClient, text) => {
  let client = discordClient,
    channel = text;
  let inRequest = false;
  let bazaarCache = {};

  client.on("message", (message) => {
    if (
      !message.content.startsWith("~") ||
      !message.channel.id === "733213711157821452"
    )
      return;

    if (
      message.content.startsWith("~find") &&
      message.content.split(" ").length >= 2
    ) {
      let item = message.content.slice(6).toLowerCase().trim();
      if (bazaarCache[item]) {
        let data = bazaarCache[item];
        message.channel.send(
          new Discord.MessageEmbed()
            .setTitle(toTitleCase(item))
            .setColor(Math.floor(Math.random() * 16777215))
            .setDescription(
              `**Buy Order**: ${data.buyOrderPrice}\n**Sell Order**: ${
                Math.round(data.sellOrderPrice * 99) / 100
              }\n**Difference**: ${
                Math.round(
                  (data.sellOrderPrice * 0.99 - data.buyOrderPrice) * 100
                ) / 100
              }\n**Estimated Profit (coins/minute)**: ${
                Math.round(data.profitFlowPerMinute * 100) / 100
              }`
            )
        );
      } else {
        message.reply(
          `The item searched (**${item}**) doesn't exist. Maybe something went wrong on my end, but odds are you just messed up!`
        );
      }
    }
  });

  schedule.scheduleJob("*/30 * * * * *", async function () {
    try {
      if (client && !inRequest) {
        inRequest = true;
        let bazaarData = [];
        let key = process.env.HYPIXEL_API_KEY;
        await axios({
          method: "GET",
          url: `https://api.hypixel.net/skyblock/bazaar?key=${key}`,
        })
          .then((res) => {
            let products = res.data.products;
            let productIds = Object.keys(res.data.products);

            productIds.forEach((id) => {
              let product = products[id];
              bazaarData.push({
                productId: bazaarConversions[id],
                sellOrderPrice:
                  product.buy_summary[0] && product.sell_summary[0]
                    ? product.buy_summary[0].pricePerUnit
                    : 0,
                buyOrderPrice:
                  product.sell_summary[0] && product.buy_summary[0]
                    ? product.sell_summary[0].pricePerUnit
                    : 0,
                sellCount: product.quick_status.buyMovingWeek,
                buyCount: product.quick_status.sellMovingWeek,
              });
            });
          })
          .catch((e) => {
            console.log(e);
          });

        bazaarData = performance(bazaarData);

        bazaarData.forEach((data) => {
          bazaarCache[data.productId.toLowerCase()] = data;
        });

        bazaarData = bazaarData.slice(0, 16);

        let fields = [];
        let i = 1;
        bazaarData.forEach((data) => {
          fields.push({
            name: `${i++}. ${data.productId}`,
            value: `EP: ${Math.round(data.profitFlowPerMinute * 100) / 100}`,
            inline: true,
          });
          if (i % 2 == 0) {
            fields.push({ name: "\u200B", value: "\u200B", inline: true });
          }
        });
        let embed = new Discord.MessageEmbed()
          .setTitle("Bazaar")
          .setColor(Math.floor(Math.random() * 16777215))
          .addFields(...fields)
          .setFooter(
            "EP is about how much money you'd make while flipping an item per minute of flipping. It assumes you miss no instants."
          );
        client.channels.cache.get(channel).send(embed);
        inRequest = false;
      }
    } catch (e) {
      inRequest = false;
      console.log(e);
    }
  });
};
module.exports = Bazaar;
