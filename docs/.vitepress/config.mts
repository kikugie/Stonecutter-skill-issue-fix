import {PageData, TransformPageContext} from 'vitepress';
import {tabsMarkdownPlugin} from 'vitepress-plugin-tabs'
import defineVersionedConfig from "vitepress-versioning-plugin";
import {applySEO, removeVersionedItems} from './seo';

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
    latestVersion: '0.5-project-trees.5',
  },
  cleanUrls: true,
  appearance: 'dark',

  head: [[
    'link',
    {rel: 'icon', sizes: '32x32', href: '/assets/logo.webp'},
  ]],

  // @ts-ignore
  transformPageData: (pageData: PageData, _ctx: TransformPageContext) => {
    applySEO(pageData);
  },

  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      {text: 'Home', link: '/'},
      {text: 'Stonecutter Wiki', link: '/stonecutter/introduction'},
      {text: 'Stonecutter KDoc', link: '/dokka/', target: '_self'},
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
            {text: 'Introduction', link: '/stonecutter/introduction'},
            {text: 'Setting up Stonecutter', link: '/stonecutter/setup'},
            {
              text: 'Configuration', items: [
                {text: 'Project Settings', link: '/stonecutter/settings'},
                {text: 'Project Controller', link: '/stonecutter/controller'},
                {text: 'Project Build', link: '/stonecutter/build'},
              ]
            },
            {
              text: 'Details', items: [
                {text: 'Comment Syntax', link: '/stonecutter/comments'},
                {text: 'Project Trees', link: '/stonecutter/project-tree'},
              ]
            },
          ]
        }
      ]
    },

    socialLinks: [
      {icon: 'github', link: 'https://github.com/kikugie/stonecutter'},
      {icon: 'discord', link: 'https://discord.gg/TBgNUCfryS'},
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
