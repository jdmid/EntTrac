export const themes = {
  manga: {
    accent: '#9d7cff',
    accentBg: '#9d7cff18',
    accentBorder: '#9d7cff55',
    background: '#0d0b14',
    topBar: '#130f1f',
    subNav: '#100d1a',
    cardCover: '#221a35',
    cardBody: '#16102a',
    cardBorder: '#2a1f4a',
    cardIcon: '#3d2a5a', 
    unreadBadge: '#9d7cff33',
    name: 'Manga',
  },
  anime: {
    accent: '#2dd4bf',
    accentBg: '#2dd4bf18',
    accentBorder: '#2dd4bf55',
    background: '#080f0f',
    topBar: '#0b1515',
    subNav: '#091212',
    cardCover: '#0a2020',
    cardBody: '#0a1f1f',
    cardBorder: '#0f3535',
    cardIcon: '#0f3535',   
    unreadBadge: '#2dd4bf33',
    name: 'Anime',
  },
}

export const statusStyles = {
  CONSUMING: {
    bg: '#163326', border: '#2a5a3a', label: 'Reading',
    badge: { background: '#1f4a32', color: '#4ade80' }
  },
  PLANNED: {
    bg: '#101e38', border: '#1e3a60', label: 'Plan to Read',
    badge: { background: '#1a2f50', color: '#60a5fa' }
  },
  FINISHED: {
    bg: '#222228', border: '#3a3a44', label: 'Finished',
    badge: { background: '#2e2e38', color: '#9ca3af' }
  },
  DROPPED: {
    bg: '#2e1212', border: '#501c1c', label: 'Dropped',
    badge: { background: '#4a1c1c', color: '#f87171' }
  },
}