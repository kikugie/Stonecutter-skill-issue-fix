import { PageData, SiteConfig } from "vitepress";

function addTag(pageData: PageData, name: string, content: string) {
  pageData.frontmatter.head ??= [];
  pageData.frontmatter.head.push([
    "meta",
    {
      name,
      content,
    },
  ]);
}

export function applySEO(pageData: PageData) {
  addTag(
    pageData,
    "og:title",
    pageData.title === "Stonecutter"
      ? `Stonecutter`
      : `${pageData.title} | Stonecutter`
  );

  addTag(pageData, "og:type", "website");
  addTag(pageData, "og:url", `https://stonecutter.kikugie.dev/${pageData.relativePath}`);
  addTag(pageData, "og:description", pageData.description);
  addTag(pageData, "og:image", "/logo.webp");
  addTag(pageData, "og:image:width", "128");
  addTag(pageData, "og:image:height", "128");

  addTag(pageData, "twitter:card", "summary");

  addTag(pageData, "theme-color", "#1c6ced");

  // Dont index the page if it's a versioned page.
  const path = pageData.filePath;
  if (path.includes("versions")) {
    addTag(pageData, "robots", "noindex");
  }
}

export function removeVersionedItems(items: any[]): any[] {
  const config = globalThis.VITEPRESS_CONFIG as SiteConfig;
  const inverseRewrites = config.rewrites.inv;

  const itemsCopy = [...items];
  for (const item of items) {
    const path = item.url.replace("https://stonecutter.kikugie.dev/", "") + ".md";

    // Remove the item if it's a versioned item
    if (inverseRewrites[path]?.includes("versions")) {
      itemsCopy.splice(itemsCopy.indexOf(item), 1);
    }
  }

  return itemsCopy;
}
