## Example Datapack - 1.20.1
Simple datapack that shows how to override default 'Improvable' modifier recipe. Full description of Modifier Recipes and Salvage Recipes can be found [here](https://slimeknights.github.io/docs/json/recipes/modifiers/). 

In this example we changed default ingredients needed to apply 'Improvable' modifier from **_4x Bottle o' Enchanting_** and **_1x Nether Star_**:
```
"inputs": [
    {
      "item": "minecraft:experience_bottle"
    },
    {
      "item": "minecraft:nether_star"
    },
    {
      "item": "minecraft:experience_bottle"
    },
    {
      "item": "minecraft:experience_bottle"
    },
    {
      "item": "minecraft:experience_bottle"
    }
]
```
to just _**1x Cobblestone**_:
```
"inputs": [
    {
      "item": "minecraft:cobblestone"
    }
]
```

We also made the modifier **'slotless'**, meaning no modifier slots are required. We did that just by removing _slots_ key (showed below) from recipe.
```
"slots": {
    "abilities": 1
}
```

**Don't forget to also adjust accordingly the salvaging recipe!**