[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](https://github.com/jasger9000/TextureExportMod/?tab=MIT-1-ov-file)
[![build](https://github.com/jasger9000/TextureExportMod/actions/workflows/build.yml/badge.svg)](https://github.com/jasger9000/TextureExportMod/actions/workflows/build.yml)
[![GitHub release](https://img.shields.io/github/release/jasger9000/TextureExportMod/all.svg)](https://github.com/jasger9000/TextureExportMod/releases)
[![Issues](https://img.shields.io/github/issues/jasger9000/TextureExportMod.svg)](https://github.com/jasger9000/TextureExportMod/issues)
# Texture Export Mod
A Minecraft mod to export pictures of all items loaded by minecraft and mods as they are rendered in your inventory.


## ⚠ Disclaimer
This mod is not affiliated with or endorsed by Mojang Studios or any mod developers.
All exported textures remain the property of Mojang Studios (for base game textures) or their respective mod developers (for modded textures).
Use this tool responsibly and in accordance with the respective End User Licence Agreements (EULAs) and Licences.
Redistribution, commercial use, or any other unauthorized use of the textures from the base game is prohibited as per the EULA. You can find Mojang's EULA [here](https://www.minecraft.net/eula)


## Installation

1. Download the jar file from the [releases tab](https://github.com/jasger9000/TextureExportMod/releases)
2. Download the [Fabric-API](https://modrinth.com/mod/fabric-api)
3. Drop the Fabric-API & Mod into your fabric installation mod folder
    This is likely `%appdata%/.minecraft/mods` or `~/.minecraft/mods`


## Usage

This mod provides several client side commands:
- `/openExportScreen` to open a gui screen for easy exporting
- `/buildItemStack` to build an Item Stack of all the mods you enabled for export
- `/shouldExportMod <mod> <shouldExport>` Toggles whether the chosen mod will be exported.
  - \<mod\> A Mod id. Example: `minecraft` or `create`
  - \<shouldExport\> A boolean. Can be `true` or `false`
- `/startExport` to begin exporting the current item stack
- `/stopExport` to stop/pause the current export


## FAQ

#### How do I export textures?

You will have to join a world (single- or multiplayer) and execute `/openExportScreen`, click the build button and then start exporting using the start button. Depending on your system specifications, this could make your game lag a little.

#### Where are the textures getting exported to?

By default, textures are getting exported to `~/Pictures/textureexportmod/` so if your username is johndoe textures would be located in `/home/johndoe/Pictures/textureexportmod` or `C:\Users\johndoe\Pictures\textureexportmod` depending on your Operating System


## Contributing

Contributions are always welcome!

If you want to add something, just [open a pull request](https://github.com/jasger9000/TextureExportMod/pulls) and I will have a look at it as soon as I can.


## Licence

The Project is Licensed under the [MIT Licence](https://github.com/jasger9000/TextureExportMod/?tab=MIT-1-ov-file)


## Authors

- [@jasger9000](https://www.github.com/jasger9000)

