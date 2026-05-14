import Navbar from '../../components/Navbar'

function MangaSearchPage() {
  return (
    <div>
      <Navbar activeMedia="manga" />
      <div className="p-5">
        <p className="text-white">Manga Search</p>
      </div>
    </div>
  )
}

export default MangaSearchPage