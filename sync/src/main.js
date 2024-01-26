/**
 * Simple script for updating the resources file
 */

import fs from "fs"
import "dotenv/config"

import conversions from "./mode/conversions.js"
import crafting from "./mode/crafting.js"

function jsonToAscii(jsonText) {
  let s = "";

  for (let i = 0; i < jsonText.length; ++i) {
    let c = jsonText[i];
    if (c >= '\x7F') {
      c = c.charCodeAt(0).toString(16).toUpperCase();
      switch (c.length) {
        case 2:
          c = "\\u00" + c;
          break;
        case 3:
          c = "\\u0" + c;
          break;
        default:
          c = "\\u" + c;
          break;
      }
    }
    s += c;
  }
  return s;
}

const run = async (autoUpdate = false) => {
  let resources = JSON.parse(
      fs.readFileSync("../resources.json"));

  await conversions.update(resources, true)
  await crafting.update(resources)

  if (autoUpdate) {
    fs.writeFile("../resources.json",
        jsonToAscii(JSON.stringify(resources, null, '\t')), () => {
        });
  } else {
    console.log(jsonToAscii(JSON.stringify(resources, null, '\t')))
  }
}

run(true);
