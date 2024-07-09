import { PageData, TransformPageContext } from 'vitepress';
import { tabsMarkdownPlugin } from 'vitepress-plugin-tabs'
import defineVersionedConfig from "vitepress-versioning-plugin";
import { applySEO, removeVersionedItems } from './seo';

const req = await fetch(
    'https://raw.githubusercontent.com/nishtahir/language-kotlin/master/dist/Kotlin.JSON-tmLanguage'
)

const kotlin2 = JSON.parse(
    JSON.stringify(await req.json()).replace(/Kotlin/gi, 'kotlin2')
)

// https://vitepress.dev/reference/site-config
export default defineVersionedConfig(__dirname, {
  lang: 'en-US',
  title: 'Stonecutter',
  description: 'Modern Gradle plugin for multi-version management',
  versioning: {
    latestVersion: '0.4.2',
  },
  cleanUrls: true,
  appearance: 'dark',

  head: [[
    'link',
    { rel: 'icon', sizes: '32x32', href: '/assets/logo.webp' },
  ]],

  // @ts-ignore
  transformPageData: (pageData: PageData, _ctx: TransformPageContext) => {
    applySEO(pageData);
  },

  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Stonecutter Wiki', link: '/stonecutter/introduction' },
      { text: 'Stonecutter KDoc', link: '/dokka/', target: '_self' },
    ],
    outline: {
      level: "deep"
    },
    logo: "/assets/logo.webp",
    search: {
      provider: 'local'
    },

    sidebar: {
      '/': [
        {
          text: 'Stonecutter',
          items: [
            { text: 'Introduction', link: '/stonecutter/introduction' },
            { text: 'Migrating to Stonecutter', link: '/stonecutter/migration' },
            { text: 'Developing your mod', link: '/stonecutter/launch' },
            { text: 'Versioned comments', link: '/stonecutter/comments' },
            { text: 'Stonecutter configuration', link: '/stonecutter/configuration' },
            { text: 'Tips and tricks', link: '/stonecutter/tips' },
          ]
        }
      ]
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/kikugie/stonecutter' },
      { icon: 'discord', link: 'https://discord.gg/TBgNUCfryS' },
    ]
  },
  sitemap: {
    hostname: "https://stonecutter.kikugie.dev/",
    transformItems: items => removeVersionedItems(items)
  },
  markdown: {
    config(md) {
      // @ts-ignore
      md.use(tabsMarkdownPlugin)
    },
    languages: [kotlin2],
    languageAlias: {
      kotlin: 'kotlin2',
      kt: 'kotlin2',
      kts: 'kotlin2'
    }
  }
})
