/**
 * Detects the user's country code using the Intl API.
 * Returns a 2-letter country code (e.g. 'US', 'GB') or 'US' as fallback.
 */
export function detectRegion() {
  try {
    // Intl.Locale gives the most reliable region resolution
    const locale = new Intl.Locale(navigator.language)
    if (locale.region && locale.region.length === 2) {
      return locale.region.toUpperCase()
    }

    // Fallback: try resolving region from timezone
    // e.g. 'America/New_York' -> not directly a country code, so skip this
    // and fall through to US default
  } catch (e) {
    // Intl.Locale not supported or locale string malformed
  }

  return 'US'
}