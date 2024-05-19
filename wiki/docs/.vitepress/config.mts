import { defineConfig } from 'vitepress'
import { tabsMarkdownPlugin } from 'vitepress-plugin-tabs'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "Stonecutter Wiki",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: 'Home', link: '/' },
    ],
    outline: {
      level: "deep"
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
      { icon: 'github', link: 'https://github.com/vuejs/vitepress' }
    ]
  },
  markdown: {
    config(md) {
      md.use(tabsMarkdownPlugin)
    }
  }
})
