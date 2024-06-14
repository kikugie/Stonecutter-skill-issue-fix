---
# https://vitepress.dev/reference/default-theme-home-page
layout: home

titleTemplate: Stonecutter

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
]
</script>

## Projects using Stonecutter

<VPTeamMembers size="small" :members="members" />

*Projects are selected as one per author. If you want your project to be included or excluded, open a GitHub issue or contact on Discord.*