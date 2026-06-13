function AttributionFooter() {
  return (
    <div className="px-5 py-3 mt-8 border-t border-white/5">
      <p className="text-[10px] text-[#333344] m-0">
        Data provided by{' '}
        <a href="https://www.themoviedb.org" target="_blank" rel="noreferrer"
          style={{ color: '#444455' }}>TMDB</a>
        {' · '}
        <a href="https://www.omdbapi.com" target="_blank" rel="noreferrer"
          style={{ color: '#444455' }}>OMDb</a>
        {' · '}
        <a href="https://mangadex.org" target="_blank" rel="noreferrer"
          style={{ color: '#444455' }}>MangaDex</a>
        {' · '}
        {' · '}
        <a href="https://jikan.moe" target="_blank" rel="noreferrer"
          style={{ color: '#444455' }}>Jikan</a>
        {' · '}
        <a href="https://openlibrary.org" target="_blank" rel="noreferrer"
          style={{ color: '#444455' }}>Open Library</a>
      </p>
    </div>
  )
}

export default AttributionFooter