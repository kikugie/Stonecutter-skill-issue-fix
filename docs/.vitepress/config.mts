import { defineConfig } from 'vitepress'
import { tabsMarkdownPlugin } from 'vitepress-plugin-tabs'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  base: '/stonecutter-kt',
  lang: "en-US",
  title: "Stonecutter Wiki",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Stonecutter Docs', link: '/stonecutter/introduction' },
    ],
    outline: {
      level: "deep"
    },
    search: {
      provider: 'local'
    },
    sidebar: [
      {
        text: 'Stonecutter',
        items: [
          { text: 'Introduction', link: '/stonecutter/introduction' },
          { text: 'Migrating to Stonecutter', link: '/stonecutter/migration' },
          { text: 'Developing your mod', link: '/stonecutter/launch' },
          { text: 'Versioned comments', link: '/stonecutter/comments' },
          { text: 'Stonecutter configuration', link: '/stonecutter/configuration' },
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/kikugie/stonecutter-kt' },
      { icon: 'discord', link: 'https://discord.gg/TBgNUCfryS' },
    ]
  },
  markdown: {
    config(md) {
      md.use(tabsMarkdownPlugin)
    }
  }
})
