import axios from "axios"

const toTitleCase = (str) => str.replace(/\w\S*/g,
    (txt) => txt.charAt(0).toUpperCase() + txt.substring(1).toLowerCase());

const convertItemName = (item) => {
  let filtered = item;
  let romanConversion = "";

  if (item.startsWith("ENCHANTMENT")) {
    let enchantNumber = parseInt(item.split("_").pop());

    // Since it only goes 1-10, might as well hard code the list
    romanConversion = ["", "I", "II", "III", "IV", "V", "VI", "VII", "VIII",
      "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII",
      "XIX", "XX"][enchantNumber];

    filtered = filtered.split("_").slice(0, -1).join("_");

  }

  if (item.startsWith("ENCHANTMENT_ULTIMATE_WISE") || item.startsWith(
      "ENCHANTMENT_ULTIMATE_JERRY")) {
    filtered = filtered.replace("ENCHANTMENT_", "");
  } else if (item.startsWith("ENCHANTMENT_ULTIMATE")) {
    filtered = filtered.replace("ENCHANTMENT_ULTIMATE_", "");
  } else if (item.startsWith("ENCHANTMENT")) {
    filtered = filtered.replace("ENCHANTMENT_", "");
  } else if (item.startsWith("ESSENCE")) {
    filtered = filtered.replace("ESSENCE_", "") + "_ESSENCE";
  } else if (item.endsWith("SCROLL")) {
    filtered = filtered.replace("_SCROLL", "");
  } else if (item === "SIL_EX") {
    filtered = "SILEX";
  }

  filtered = toTitleCase(filtered.replace(/_/g, " "));
  filtered = filtered
  .replace(" For ", " for ")
  .replace(" Of ", " of ")
  .replace(" The ", " the ");

  if (item === "ENCHANTMENT_ULTIMATE_ONE_FOR_ALL_1") {
    filtered = filtered.replace(" for ", " For ");
  }

  if (item.startsWith("ENCHANTMENT")) {
    filtered += " " + romanConversion;
  }

  return filtered;
}

export default {
  update: async (resources, log = false) => {
    let bazaarData = (await axios.get(
        "https://api.hypixel.net/skyblock/bazaar")).data.products;
    let newItems = {}

    let currentItemList = Object.keys(bazaarData)
    let flaggedItem = {};

    for (let item of currentItemList) {
      if (!Object.keys(resources.bazaarConversions).includes(item)) {
        newItems[item] = convertItemName(item)
      } else {
        flaggedItem[item] = true;
      }
    }

    for (let item of Object.keys(newItems)) {
      flaggedItem[item] = true;
      resources.bazaarConversions[item] = newItems[item];
    }

    if (log) {
      if (Object.keys(flaggedItem).length === 0) {
        console.log("[BNRUS] No changed items.")
      } else if (Object.keys(newItems).length === 0) {
        console.log("[BNRUS] No new items.");
      } else {
        console.log(JSON.stringify(newItems, null, 2))
      }
      for (let item of Object.keys(resources.bazaarConversions)) {
        if (!(item in flaggedItem)) {
          console.log(item);
        }
      }
    }
  }
}