import Navbar from '../../components/Navbar'

function MangaLibraryPage() {
  return (
    <div>
      <Navbar activeMedia="manga" />
      <div className="p-5">
        <p className="text-white">Manga Library</p>
      </div>
    </div>
  )
}

export default MangaLibraryPage