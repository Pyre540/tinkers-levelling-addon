## Example Datapack - 1.16.5
Simple datapack that shows how to override default 'Improvable' modifier recipe. Full description of Modifier Recipes can be found [here](https://github.com/SlimeKnights/TinkersConstruct/wiki/Modifier-Recipe-JSON), and Salvage recipe description can be found [here](https://github.com/SlimeKnights/TinkersConstruct/wiki/Salvage-Recipe-JSON). 

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