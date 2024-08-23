---
# https://vitepress.dev/reference/default-theme-home-page
layout: home

titleTemplate: Stonecutter
title: Stonecutter
description: Modern Gradle plugin for multi-version management

hero:
  name: Stonecutter
  tagline: Modern Gradle plugin for multi-version management
  image:
    src: /assets/logo.webp
    alt: Stonecutter

features:
  - title: Migrating to Stonecutter
    icon: 🛫
    details: Do you already have a project or want to start from scratch? Take a look on the detailed setup guide.
    link: /stonecutter/migration
    linkText: Get Started
  - title: Quick Start
    icon: ⏳
    details: Check out the Fabric mod template repository to start a new mod with multi-version support.
    link: https://github.com/kikugie/stonecutter-template-fabric
    linkText: Template Repository
  - title: Learn to use Stonecutter
    icon: 🖊
    details: Explore the rich feature set provided by the custom in-comment language used by Stonecutter - Stitcher.
    link: /stonecutter/comments
    linkText: Documentation
  - title: Code documentation
    icon: 🛠
    details: Do you want to know how the file parsing and transformation is accomplished? Then you're in luck - the code is very organized and documented.
    link: /dokka/
    linkText: KDoc
---

<!--suppress ES6UnusedImports, HtmlUnknownAttribute -->
<script setup>
import { VPTeamMembers } from 'vitepress/theme';
import modrinth from '/assets/modrinth.svg?raw';
import curseforge from '/assets/curseforge.svg?raw';

let start = "here";
const members = [
  {
    avatar: 'https://cdn.modrinth.com/data/XpzGz7KD/8ff6751948e096f540e320681742d0b3b918931e.png',
    name: 'Elytra Trims',
    title: 'Customizable elytra mod with trims, banner patterns and more!',
    links: [
      { icon: 'github', link: 'https://github.com/Kikugie/elytra-trims' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/elytra-trims' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/elytra-trims' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/1eAoo2KR/1e43d5714f87ac6b20622e73b3ba7209be5ebafb.png',
    name: 'YetAnotherConfigLib',
    title: 'A builder-based configuration library for Minecraft.',
    links: [
      { icon: 'github', link: 'https://github.com/isXander/YetAnotherConfigLib' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/yacl' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/yacl' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/w7ThoJFB/25d48c335340c12566044c8f35df5102e72dc06c.png',
    name: 'Zoomify',
    title: 'A zoom mod with infinite customizability.',
    links: [
      { icon: 'github', link: 'https://github.com/isXander/Zoomify' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/zoomify' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/zoomify' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/1s5x833P/b7e3cf809f07d3cad88a60834c3e4e7248021744.png',
    name: 'Neruina - Ticking Entity Fixer',
    title: 'A Mod that prevents ticking-related crashes from bricking worlds',
    links: [
      { icon: 'github', link: 'https://github.com/Benjamin-Norton/Neruina' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/neruina' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/neruina' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/SNVQ2c0g/cd0ac6b474ae39a347364a62a39ea04ce0e146d9.png',
    name: 'M.R.U',
    title: 'Mineblock\'s Repeated Utilities',
    links: [
      { icon: 'github', link: 'https://github.com/mineblock11/MRU' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/mru' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/mru' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/DOUdJVEm/4f8cdb3933f9efa0c5dfd5574d3ad6b101c7f3ef.png',
    name: 'Controlify',
    title: 'Adds the best controller support to Minecraft Java edition!',
    links: [
      { icon: 'github', link: 'https://github.com/isXander/Controlify' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/controlify' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/controlify' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/IwCkru1D/53eee5642c7c426729b8313628b83f8513322484.png',
    name: 'CICADA',
    title: 'Confusing, Interesting and Considerably Agnostic Development Aid',
    links: [
      { icon: 'github', link: 'https://github.com/enjarai/cicada-lib' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/cicada' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/cicada' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/ZouiUX7t/d20aa8a40008b3f027144e21c916c4a7229a0c78.png',
    name: 'Sounds',
    title: 'Upgrade your experience with this customizable sound mod! It introduces new sound effects and enhances the original ones. Enjoy a richer audio experience with new sounds for blocks, items, inventory, and the UI!',
    links: [
      { icon: 'github', link: 'https://github.com/IMB11/Sounds' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/sound' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/XVnUIUAQ/8165a80ccd1c58a9a0fd7505b4d27235a759bf28.png',
    name: 'Snow Under Trees (Fabric)',
    title: 'Adds snow under trees in snowy biomes, making the biomes more immersive.',
    links: [
      { icon: 'github', link: 'https://github.com/mineblock11/SnowUnderTrees' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/snow-under-trees-remastered' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/snow-under-trees-remastered' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/5srFLIaK/03454d120e13a3a25579bd74fe4bd761fed19431.png',
    name: 'Better Clouds',
    title: 'Beautiful clouds in touch with the vanilla style',
    links: [
      { icon: 'github', link: 'https://github.com/Qendolin/better-clouds' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/better-clouds' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/WuGVWUF2/3475344bb37e1e27c2a54b574284cf0240b1ab70.png',
    name: 'Fog',
    title: 'A total overhaul of Minecraft\'s fog, offering options to customize fog color, start, and end points. Enjoy a more immersive experience with enhanced depth and visuals, all while keeping the same view distance.',
    links: [
      { icon: 'github', link: 'https://github.com/IMB11/Fog' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/fog' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/fog' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/pnsUKrap/7bb6b50b2f8be66ea13e0cfd290a7c2e348d6074.png',
    name: 'All The Trims',
    title: 'Allows any item to be an armour trim material and makes all armour trimmable.',
    links: [
      { icon: 'github', link: 'https://github.com/Benjamin-Norton/AllTheTrims/' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/allthetrims' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/all-the-trims' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/Eoxi2LJd/481ae7705912ab3418955e5bd650d938d1261c59.png',
    name: 'Flow',
    title: 'Configurable ease in-out inventory UI transitions.',
    links: [
      { icon: 'github', link: 'https://github.com/mineblock11/Flow' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/flow' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/flow' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/3s19I5jr/e66d99492c9b25e88a614846cca6b154ec5309f2.png',
    name: 'Skin Shuffle',
    title: 'Easily change your skin in-game without having to leave the world.',
    links: [
      { icon: 'github', link: 'https://github.com/IMB11/SkinShuffle' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/skinshuffle' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/skinshuffle' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/FrZIkosK/914fbe1f142a3fbe7488d0064e252f08f10c4a93.png',
    name: 'Forgotten Graves',
    title: 'Minecraft (Fabric) mod that stores items and XP in a decaying grave upon death',
    links: [
      { icon: 'github', link: 'https://github.com/ginsm/forgotten-graves' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/forgotten-graves' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/k68glP2e/1cee94d3e17436d409839a79fa5d6bced5993023.png',
    name: 'AutoModpack',
    title: 'Enjoy a seamless modpack installation process and effortless updates with a user-friendly solution that simplifies management, making your gaming experience a breeze.',
    links: [
      { icon: 'github', link: 'https://github.com/Skidamek/AutoModpack' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/automodpack' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/automodpack' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/m5T5xmUy/c67c1f900e8344e462bb5c21fb512579f3b0be46.png',
    name: 'BetterGrassify',
    title: 'Gamers can finally touch grass!?  OptiFine\'s Fancy and Fast better grass implemented on Fabric and NeoForge!',
    links: [
      { icon: 'github', link: 'https://github.com/UltimatChamp/BetterGrassify' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/bettergrassify' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/bettergrassify' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/J5NAzRqK/43f9b135ef9ab49a67da667caa8b5987e1d5d864.png',
    name: 'FSit',
    title: 'Sit anywhere!',
    links: [
      { icon: 'github', link: 'https://github.com/rvbsm/fsit' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/fsit' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/fsit' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/9pubtjcn/7c702ddf0204753f221ab781f3f9360e071b988b.png',
    name: 'Blocky Bubbles',
    title: 'Ports the Fast Bubbles setting from Bedrock Edition!',
    links: [
      { icon: 'github', link: 'https://github.com/axialeaa/BlockyBubbles' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/blocky-bubbles' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/tr2Mv6ke/a98512fe0df192749fa001268dcf8dd96f99e587.png',
    name: 'Sushi Bar',
    title: 'A library mod for Chai\'s mods',
    links: [
      { icon: 'github', link: 'https://github.com/Chailotl/sushi-bar' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/sushi-bar' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/RCjGlCDj/icon.png',
    name: 'Mod-erate Loading Screen',
    title: 'An "alternative" to Mod Menu that\'s a lot less usable.',
    links: [
      { icon: 'github', link: 'https://github.com/enjarai/moderate-loading-screen' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/moderate-loading-screen' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/mod-erate-loading-screen' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/9dzLWnmZ/3a7ea28ca4aa82708c23d0f1f7189661eb2ea363.png',
    name: 'Camerapture',
    title: 'Take pictures using a working camera, show them to other players and hang them on your wall in picture frames!',
    links: [
      { icon: 'github', link: 'https://github.com/chrrs/camerapture' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/camerapture' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/camerapture' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/Kd03i2oU/5bfab0390b4655470b95b80824df9ffe6e280514.png',
    name: 'Enchantment Disabler',
    title: 'Disable enchantments you don\'t like, and nerf enchanting in multiple ways with an extensive configuration. Supports modded enchantments.',
    links: [
      { icon: 'github', link: 'https://github.com/pajicadvance/enchantmentdisabler' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/enchantment-disabler' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/enchantmentdisabler' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/kNtBiHzs/f83c432acee2a4bb87b09ed62374acdb017fc68c.png',
    name: 'OpenBoatUtils',
    title: 'Configurable boat physics.',
    links: [
      { icon: 'github', link: 'https://github.com/o7Moon/OpenBoatUtils' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/openboatutils' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/8CsGxc95/f1d33853af9a7c9a05f2562fc72750187b3ed988.png',
    name: 'Shared Resources',
    title: 'A mod for sharing game files like resource packs, shaders, saves and more between separate Minecraft instances.',
    links: [
      { icon: 'github', link: 'https://github.com/enjarai/shared-resources' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/shared-resources' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/ygYU16dG/cd7e06456a341a345a6d2be1e2a057745d293969.png',
    name: 'My Totem Doll',
    title: 'Simple Fabric mod which replaces all totems with player dolls. Rename your totem to player\'s nickname to use it\'s skin.',
    links: [
      { icon: 'github', link: 'https://github.com/LopyMine/My-Totem-Doll' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/my_totem_doll' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/LLfA8jAD/68480ea1745081e6ab88970f58c9b58c9fa3a7e5.jpeg',
    name: 'telekinesis',
    title: 'The Telekinesis enchantment automatically collects drops from mobs, vehicles and blocks',
    links: [
      { icon: 'github', link: 'https://github.com/btwonion/telekinesis' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/telekinesis' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/telekinesis' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/s9XXQTOb/b2ad6897ae0ae1277dc3fefe4d99ed6e7e3f9024.png',
    name: 'Chai\'s Inventory Sorter',
    title: 'An inventory sorter that complements the vanilla UI with fully configurable sorting',
    links: [
      { icon: 'github', link: 'https://github.com/Chailotl/inventory-sort' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/chais-inventory-sorter' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/9auOqb3o/a0abec247c17a55fb4826f9b641fefdebd794339.png',
    name: 'CyanSetHome',
    title: 'Adds the /sethome command and a system of trust between player to allow them to teleport to their respective homes',
    links: [
      { icon: 'github', link: 'https://github.com/Aeldit/CyanSetHome' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/cyansethome' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/wTfH1dkt/b9d1ed7933cbbad760cae996d8732c914a57fbd2.png',
    name: 'Better Boat Movement',
    title: 'Increases boat step height to move up water and blocks',
    links: [
      { icon: 'github', link: 'https://github.com/btwonion/better-boat-movement' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/better-boat-movement' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/Kt9bUp9L/9f4bfe0ec9fa7b4d55b9b6c926420aff7b542e36.png',
    name: 'NameFabric',
    title: 'Shows you public player data like past usernames, current skin, and more using LabyNet\'s and Mojang\'s API.',
    links: [
      { icon: 'github', link: 'https://github.com/not-coded/NameFabric' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/namefabric' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/namefabric' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/YS3ZignI/f8056e4ce6ac00a50c431eac915509d14a0e90b4.png',
    name: 'TT20 (TPS Fixer)',
    title: 'TT20 helps reduce lag by optimizing how ticks work when the server\'s TPS is low.',
    links: [
      { icon: 'github', link: 'https://github.com/snackbag/tt20' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/tt20' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/lg17V3i3/d88de184e364d8dd1da21933c7c82ce298b0fb98.png',
    name: 'autodrop',
    title: 'Simply drops specific items after pickup',
    links: [
      { icon: 'github', link: 'https://github.com/btwonion/autodrop' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/autodrop' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/hHVaPgFK/723c55a27d7d633024fdfe14464a44c84bf05d48.png',
    name: 'Mob Armor Trims',
    title: 'Makes mobs be able to spawn with naturally trimmed armor',
    links: [
      { icon: 'github', link: 'https://github.com/Imajo24I/Mob-Armor-Trims-1.20.1/' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/mob-armor-trims' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/6OpnBWtt/5bb148d10f81498a60f0498302743a39eadd6900.png',
    name: 'CTM Selector',
    title: 'This mod allows you to choose which blocks will have connected textures in every CTM resource pack you have loaded',
    links: [
      { icon: 'github', link: 'https://github.com/Aeldit/CTMSelector' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/ctm-selector' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/TBQr8ROh/b092cf15b0e51b27740df11220d4a8c51fd0b08e.png',
    name: 'MCC Island Nametag Mod',
    title: 'A mod that displays your own name tag on MCC Island in the exact way the server does it.',
    links: [
      { icon: 'github', link: 'https://github.com/anastarawneh/MCCINametagMod' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/mcc-island-nametag-mod' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/yXAvIk0x/a8e206afee8b866700008f18b57212f0d6ce17c6.png',
    name: 'Scribble',
    title: 'Expertly edit your books with rich formatting options, page utilities and more! And it\'s all client-side!',
    links: [
      { icon: 'github', link: 'https://github.com/chrrs/scribble' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/scribble' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/scribble' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/zGxxQr33/94087d290a929535845be488cde26de54c6826f0.png',
    name: 'Cyan',
    title: 'Adds a few commands for survival Minecraft server and client !',
    links: [
      { icon: 'github', link: 'https://github.com/Aeldit/Cyan' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/cyan' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/GON0Fdk5/047b7bfec30d245cd7d5972affe208e6b0f8da98.png',
    name: 'skin overrides',
    title: 'a simple mod for locally changing skins and capes.',
    links: [
      { icon: 'github', link: 'https://lumity.dev/orifu/skin-overrides' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/skin-overrides' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/BMaqFQAd/116458c672aadeb31856563eaff8ed7edd764753.png',
    name: 'AutoWhitelist',
    title: 'A way to automate the whitelist of a minecraft server based on discord roles',
    links: [
      { icon: 'github', link: 'https://github.com/Awakened-Redstone/AutoWhitelist' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/autowhitelist' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/autowhitelist' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/3KmV3g7F/7705715ccaa9306dcaf02ac7b7c121016bde8dbe.png',
    name: 'Florum Sporum',
    title: 'Breathing new life into the spore blossom without undoing any of Mojang\'s work!',
    links: [
      { icon: 'github', link: 'https://github.com/axialeaa/FlorumSporum' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/florum-sporum' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/MXwU9ODv/f984c9f3ddcc0d1bf9bd227406a540778b4932ff.png',
    name: 'skylper',
    title: 'Utility mod for Hypixel Skyblock focusing on mining',
    links: [
      { icon: 'github', link: 'https://github.com/btwonion/skylper' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/skylper' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/w9M3qI9U/e338d0dd4b2df5fbc8ad784d3c682f7f12bcacd2.png',
    name: 'Easy Rename',
    title: 'Easily Rename your Containers!',
    links: [
      { icon: 'github', link: 'https://github.com/GravityCY/EasyRename' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/easyrename' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/yItp8TXg/c0a28bc0b0027385ec15f694e341d683f3a24eb7.png',
    name: 'Fast Recipe',
    title: 'Ctrl + LMB on recipe for instantly crafting, according to it. Fabric.',
    links: [
      { icon: 'github', link: 'https://github.com/LopyMine/fast-recipe' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/fast-recipe' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/vEyyiUWA/4c3f1393e4885be462eaa0e984d9c2f08562d8cf.png',
    name: 'Random Fishing',
    title: 'A simple mod that adds an enchantment for fishing rods that makes fishing produce random items.',
    links: [
      { icon: 'github', link: 'https://github.com/Overcontrol1/RandomFishing' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/random-fishing' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/OKRes5Es/16c8870d72779019359fdf3780398b2925433b02.gif',
    name: 'CustomDurability',
    title: 'A Fabric Mod that allows for changing the durabilities of all items!',
    links: [
      { icon: 'github', link: 'https://github.com/GravityCY/CustomDurability' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/customdurability' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/customdurability' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/W6FxNQeL/32ac73deaeb08113876eae860eac387cdd9fe975.png',
    name: 'Saturative',
    title: 'Just a hunger and saturation overhaul',
    links: [
      { icon: 'github', link: 'https://github.com/EmilAhmaBoy/saturative/' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/saturative' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/PztDP6Mf/b4734849fa5b15e7bd86d0cad353cab356cef542.png',
    name: 'Player Statistics',
    title: 'Adds a command to your Fabric server that gives players insights into everyone\'s statistics.',
    links: [
      { icon: 'github', link: 'https://github.com/kr8gz/PlayerStatistics' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/playerstatistics' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/joSM3OBw/d678ad694601af535d957b535bce1510b2690dee.png',
    name: 'New Creative Inventory',
    title: 'This mod allows the ver1.19.3 creative inventory layout to be used with versions lower than ver1.19.3.',
    links: [
      { icon: 'github', link: 'https://github.com/Plastoid501/NewCreativeInventory' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/new-creative-inventory' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/UqXUT3DQ/e48c590ddc53b8c061eb5a428fa5786f6446b9b5.png',
    name: 'HudEnhancer',
    title: 'Display, customize and move elements in the HUD.',
    links: [
      { icon: 'github', link: 'https://github.com/sailex428/HudEnhancer' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/hudenhancer' }
    ]
  }
];
let end = "here";
</script>

## Projects using Stonecutter

*This list is autogenerated. If you find a mistake please report it to the [Issues page](https://github.com/kikugie/stonecutter/issues)*  
*If you want your project to be included or excluded, open a GitHub issue or contact on Discord.*
<VPTeamMembers size="small" :members="members" />