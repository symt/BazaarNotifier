import axios from "axios"

axios.defaults.headers.common['Authorization'] = `Bearer ${process.env.KEY}`

const convertName = (name) => {
  if (name.match(/^ENCHANTMENT_(ULTIMATE_)?(.*)_(\d+)$/)) {
    return name.replace(/^ENCHANTMENT_(ULTIMATE_)?(.*)_(\d+)$/, "$1$2;$3");
  }

  if (name === "INK_SACK:3") name = "INK_SACK";
  else if (name === "BAZAAR_COOKIE") name = "BOOSTER_COOKIE"
  else if (name.includes(":")) name = name.replace(":", "-")


  return name;
}

const reverseName = (conversions, name) => {
  name = name.replace(/(.*);(\d+)/, "$1_$2")

  if (name === "INK_SACK:3") return "INK_SACK";
  else if (name === "BOOSTER_COOKIE") return "BAZAAR_COOKIE"
  else if (name.includes("-")) return name.replace("-", ":")

  for (let key of Object.keys(conversions)) {
    if (key.endsWith(name)) {
      return key
    }
  }

  return name
}

export default {
  update: async (resources, log = false) => {
    outer: for (let name of Object.keys(resources.bazaarConversions)) {
      let item = convertName(name)
      let template = `https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/master/items/${item}.json`

      if (name === "ENCHANTED_CARROT_ON_A_STICK" || name.startsWith("ENCHANTMENT") || Object.keys(resources.enchantCraftingList.other).includes(name) || resources.invalidBazaarCraftingRecipe.includes(name)) continue

      let response = (await axios.get(template))

      if (response.status === 200) {
        let itemData = response.data

        if ("recipe" in itemData) {
          let recipeItems = Object.values(itemData.recipe).slice(0, 9).filter(
              (str) => str !== "")

          let recipe = {}
          for (let item of recipeItems) {
            let sp = item.split(":")
            sp[1] = parseInt(sp[1])
            sp[0] = reverseName(resources.bazaarConversions, sp[0])

            if (!(Object.keys(resources.bazaarConversions).includes(sp[0]))) {
              resources.invalidBazaarCraftingRecipe.push(sp[0])
              continue outer;
            }

            if (sp[0] in recipe) {
              recipe[sp[0]] += sp[1]
            } else {
              recipe[sp[0]] = sp[1]
            }
          }

          let materials = []

          for (let itemName of Object.keys(recipe)) {
            materials.push(itemName, recipe[itemName])
          }
          resources.enchantCraftingList.other[name] = { material: materials, collection: "NONE" }
        } else {
          resources.invalidBazaarCraftingRecipe.push(name)
        }
      }
    }
  }
}