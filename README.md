# Mithras

Mithras is a client side 1.21.11 Minecraft Fabric mod to improve the playing experience of Hypixel Skyblock.

This mod is designed to be in compliance with the 
[Hypixel Server Rules](https://support.hypixel.net/hc/en-us/articles/6472550754962-Allowed-Modifications).
However, the use of the mod is entirely at your own risk.

###


## Usage
All features of this mod are disabled by default.
They can be configured in the main GUI.
To access it either use the command `/mithras` or the keybind, which by default is right shift.
The keybind can be changed by adjusting the keybind of the Main Settings module (more on that later). </br>
This mod employs a click GUI type user interface for its main menu, which is shown below.

![A screenshot of the main menu user interface. See Gallery.](https://cdn.modrinth.com/data/BTPIo2lP/images/ad39a50947b3f6b5c0ac1f7ac881f3383f58de40.jpeg)

### Using the GUI
Features / Modules are grouped by category into panels. These are the columns in the GUI.
Panels can be moved with left click, collapsed with rightclick and scrolled with the mouse wheel.

To **enable or disable a module** left click its name in list.
The settings of a module can be accessed by right clicking on the module.
This will open a dropdown with its settings, as can be seen for the Animations module in the screenshot.

Additionally you can middle click a module to access an advanced screen for that module.
The advanced GUI is visible in the screenshot for the Animations module.
In it you will find a description of what the module does as well as its settings with descriptions. </br>
Some settings are only accessible through this advanced GUI. For example the button to delete a custom keybind is hidden in the modules drop down menu to prevent accidental deletion.


## Features

<details>
  <summary>Dungeon</summary>

* Dungeon Map
* Puzzle Solvers
* Star Mob Highlights
</details>
<details>
  <summary>Rendering</summary>

* Coordinate HUD
* Item Animations - Allows for custom size and positioning of the held Item.
* Camera Tweaks - Disable fire overlay and skip front view when toggling perspective.
* Fullbright - An adjustable gamma override.
* Zoom
* Particle Reducer
</details>


<details>
  <summary>Misc</summary>

* Auto Sprint
* Hotbar Scroll Disabler
* Scrollable Tooltips
* Smooth Warp - Hides the loading screen when transfering between servers.
* Keep mouse position in Skyblock guis.
* Etherwarp Highlights
* Chat Cleaner - Filters and removes spammy server messages from chat.
* Drop Prevention - Prevents you from dropping valuable Skyblock items.
</details>

<details>
<summary>Keybinds</summary>

This mod allows you to define custom keybinds which can run any command or send a chat message.
An example use case would be a keybind to open your Skyblock wardrobe.

</details>

<details>
  <summary>Commands</summary>

The main command of the mod is `/mithras`. Running it will open the GUI.
</details>

## Gallery

<details>
  <summary>Gallery</summary>

Dungeon Map

![The dungeon map with included run information and score calculator.](https://cdn.modrinth.com/data/BTPIo2lP/images/6e772428e38d7a419a40398cb5c54b3fe20dd14d.jpeg)

Waterboard Solver

![Waterboard Solver](https://cdn.modrinth.com/data/BTPIo2lP/images/eb93b37f504c6facaaae1fbb9c36a679d28fb4c1.jpeg)

Blaze Solver

![Blaze Solver](https://cdn.modrinth.com/data/BTPIo2lP/images/a18b0afe37d1ca8d2eec1603577ed1df02148140.jpeg)

Creeper Beams Solver

![Creeper Beams Solver](https://cdn.modrinth.com/data/BTPIo2lP/images/98e45b35fb708338da5a9fdf2b822ddaec8b23e6.jpeg)

Tic Tac Toe Solver

![Tic Tac Toe Solver](https://cdn.modrinth.com/data/BTPIo2lP/images/9dee52f5c1257fea4c3e84f2f5b80d52f4887e03.jpeg)

Teleport Maze Solver

![Teleport Maze Solver](https://cdn.modrinth.com/data/BTPIo2lP/images/d069be5e30080be9fa2b49d470f622553f3865c5.jpeg)

Three Weirdos Solver

![Three Weirdos Solver](https://cdn.modrinth.com/data/BTPIo2lP/images/1d5a7f37ca1dfc879801834ba2eb06b03d6596df.jpeg)

Etherwarp Highlight

![The targeted block of your etherwarp is highlighted. If it is a valid block it is highlighted in green, otherwise in red.](https://cdn.modrinth.com/data/BTPIo2lP/images/2b70e46a8c77dc401fed2366a09876508f3fb9bf.jpeg)

</details>


## Installation
To use this mod you will need a Fabric 1.21.11 installation. 
To get that just follow the instructions on [their website](https://fabricmc.net/use/installer/).

You will also need the following additional libraries for the mod to function.
 - [Fabric API](https://github.com/FabricMC/fabric/releases/)
 - [Kotlin Language Support](https://github.com/FabricMC/fabric-language-kotlin/releases/)

Simply put the jar files of both of those together with the one for this mod  in to your mods folder.
