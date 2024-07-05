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
    avatar: 'https://cdn.modrinth.com/data/8CsGxc95/f1d33853af9a7c9a05f2562fc72750187b3ed988.png',
    name: 'Shared Resources',
    title: 'A mod for sharing game files like resource packs, shaders, saves and more between separate Minecraft instances.',
    links: [
      { icon: 'github', link: 'https://github.com/enjarai/shared-resources' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/shared-resources' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/shared-resources' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/ZouiUX7t/d20aa8a40008b3f027144e21c916c4a7229a0c78.png',
    name: 'Sounds',
    title: 'A highly configurable sound overhaul mod that adds new sound effects while improving vanilla sounds too.',
    links: [
      { icon: 'github', link: 'https://github.com/IMB11/Sounds' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/sound' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/sound-overhaul' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/1s5x833P/b7e3cf809f07d3cad88a60834c3e4e7248021744.png',
    name: 'Neruina',
    title: 'A Mod that prevents ticking-related crashes from bricking worlds.',
    links: [
      { icon: 'github', link: 'https://github.com/Bawnorton/Neruina' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/neruina' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/neruina' }
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
    avatar: 'https://cdn.modrinth.com/data/Kd03i2oU/5bfab0390b4655470b95b80824df9ffe6e280514.png',
    name: 'Enchantment Disabler',
    title: 'Disable enchantments you don\'t like, and nerf enchanting in multiple ways with an extensive configuration.',
    links: [
      { icon: 'github', link: 'https://github.com/pajicadvance/enchantmentdisabler' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/enchantment-disabler' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/enchantmentdisabler' }
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
    avatar: 'https://cdn.modrinth.com/data/BMaqFQAd/116458c672aadeb31856563eaff8ed7edd764753.png',
    name: 'AutoWhitelist',
    title: 'A way to automate the whitelist of a minecraft server based on discord roles.',
    links: [
      { icon: 'github', link: 'https://github.com/Awakened-Redstone/AutoWhitelist' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/autowhitelist' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/s9XXQTOb/b2ad6897ae0ae1277dc3fefe4d99ed6e7e3f9024.png',
    name: 'Chai\'s Inventory Sorter',
    title: 'An inventory sorter that complements the vanilla UI with fully configurable sorting.',
    links: [
      { icon: 'github', link: 'https://github.com/Chailotl/inventory-sort' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/chais-inventory-sorter' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/9dzLWnmZ/3a7ea28ca4aa82708c23d0f1f7189661eb2ea363.png',
    name: 'Camerapture',
    title: 'Take pictures using a working camera, show them to other players and hang them on your wall in picture frames!',
    links: [
      { icon: 'github', link: 'https://github.com/chrrs/camerapture' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/camerapture' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/wTfH1dkt/b9d1ed7933cbbad760cae996d8732c914a57fbd2.png',
    name: 'Better Boat Movement',
    title: 'Increases boat step height to move up water and blocks.',
    links: [
      { icon: 'github', link: 'https://github.com/btwonion/better-boat-movement' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/better-boat-movement' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/FrZIkosK/914fbe1f142a3fbe7488d0064e252f08f10c4a93.png',
    name: 'Forgotten Graves',
    title: 'Minecraft (Fabric) mod that stores items and XP in a decaying grave upon death.',
    links: [
      { icon: 'github', link: 'https://github.com/ginsm/forgotten-graves' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/forgotten-graves' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/forgotten-graves' }
    ]
  },
  {
    avatar: 'https://github.com/CallMeEchoCodes/CabinetAPI/blob/main/src/main/resources/assets/cabinetapi/icon.png',
    name: 'Cabinet API',
    title: 'A library mod with a bunch of useful utilities for modders. ',
    links: [
      { icon: 'github', link: 'https://github.com/CallMeEchoCodes/CabinetAPI' }
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
    avatar: 'https://cdn.modrinth.com/data/GON0Fdk5/047b7bfec30d245cd7d5972affe208e6b0f8da98.png',
    name: 'Skin Overrides',
    title: 'A simple mod for locally changing skins and capes.',
    links: [
      { icon: 'github', link: 'https://github.com/orifu/skin-overrides' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/skin-overrides' }
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
    avatar: 'https://cdn.modrinth.com/data/XKh6bbqf/220523ec73cb6172bb4e01c3368c5998d4470631.webp',
    name: 'Forgotten Graves',
    title: 'Brings back the old world loading progress bar to Modern Minecraft.',
    links: [
      { icon: 'github', link: 'https://github.com/FokshaWasTaken/loading-bar' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/old-loading-progress-bar' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/hHVaPgFK/723c55a27d7d633024fdfe14464a44c84bf05d48.png',
    name: 'Mob Armor Trims',
    title: 'Makes mobs be able to spawn with naturally trimmed armor.',
    links: [
      { icon: 'github', link: 'https://github.com/Imajo24I/Mob-Armor-Trims' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/mob-armor-trims' },
      { icon: { svg: curseforge }, link: 'https://www.curseforge.com/minecraft/mc-mods/mob-armor-trims' }
    ]
  },
  {
    avatar: 'https://cdn.modrinth.com/data/9auOqb3o/a0abec247c17a55fb4826f9b641fefdebd794339.png',
    name: 'CyanSetHome',
    title: 'Adds the /sethome command and a system of trust between player to allow them to teleport to their respective homes.',
    links: [
      { icon: 'github', link: 'https://github.com/Aeldit/CyanSetHome' },
      { icon: { svg: modrinth }, link: 'https://modrinth.com/mod/cyansethome' }
    ]
  },
]
</script>

## Projects using Stonecutter

<VPTeamMembers size="small" :members="members" />

*Projects are selected as one per author. If you want your project to be included or excluded, open a GitHub issue or contact on Discord.*