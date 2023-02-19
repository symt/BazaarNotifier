/**
 * Simple script for updating the resources file
 * The convertItemName method needs to be changed based on the items you're adding.
 */
const axios = require("axios").default

const toTitleCase = (str) => str.replace(/\w\S*/g, (txt) => txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase());

function jsonToAscii( jsonText) {
    var s = "";
    
    for( var i = 0; i < jsonText.length; ++i) {
        var c = jsonText[ i];
        if( c >= '\x7F') {
            c = c.charCodeAt(0).toString(16).toUpperCase();
            switch( c.length) {
              case 2: c = "\\u00" + c; break;
              case 3: c = "\\u0" + c; break;
              default: c = "\\u" + c; break;
            }
        }
        s += c;
    }
    return s;
}

const convertItemName = (item) => {
    let filtered = item;
    let romanConversion = "";

    if (item.startsWith("ENCHANTMENT")) {
        let enchantNumber = parseInt(item.split("_").pop());

        // Since it only goes 1-10, might as well hard code the list
        romanConversion = ["", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"][enchantNumber];
        
        filtered = filtered.split("_").slice(0, -1).join("_");

    }

    if (
      item.startsWith("ENCHANTMENT_ULTIMATE_WISE") ||
      item.startsWith("ENCHANTMENT_ULTIMATE_JERRY")
    ) {
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

const run = async () => {
    let resources = JSON.parse(require("fs").readFileSync("../resources.json"));
    let bazaarData = (await axios.get("https://api.hypixel.net/skyblock/bazaar")).data.products;
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

    for (let item of Object.keys(resources.bazaarConversions)) {
        if (!(item in flaggedItem)) {
		console.log(item);
        }
    }
    
    require('fs').writeFile("../resources.json", jsonToAscii(JSON.stringify(resources, null, '\t')), () => {});
    console.log(JSON.stringify(newItems, null, 2))
}
run();
