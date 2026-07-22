export function stripMarkdown(text) {
  if (!text) return text
  return text
    .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')  // [text](url) -> text
    .replace(/^---+$/gm, '')                    // horizontal rules
    .replace(/\*\*([^*]+)\*\*/g, '$1')          // **bold** -> bold
    .replace(/\n{3,}/g, '\n\n')                 // collapse extra blank lines left behind
    .trim()
}