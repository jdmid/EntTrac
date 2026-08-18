function AttributionFooter() {
  return (
    <div className="px-5 py-3 mt-8 border-t border-white/5">
      <p className="text-[10px] text-[#333344] m-0 flex items-center gap-1.5 flex-wrap">
        <img src="/tmdb-logo.svg" alt="TMDB" style={{ height: '10px', width: 'auto' }} />
        <span>This product uses the TMDB API but is not endorsed or certified by TMDB.</span>
      </p>
      <p className="text-[10px] text-[#333344] m-0 mt-1">
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
        <a href="https://openlibrary.org" target="_blank" rel="noreferrer"
          style={{ color: '#444455' }}>Open Library</a>
        {' · '}
        <a href="https://www.igdb.com" target="_blank" rel="noreferrer"
          style={{ color: '#444455' }}>IGDB</a>
      </p>
    </div>
  )
}

export default AttributionFooter