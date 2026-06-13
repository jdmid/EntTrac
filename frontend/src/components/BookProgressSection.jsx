import { useState, useEffect } from 'react'

function BookProgressSection({ currentChapter, currentPage, theme, onUpdate, onReset }) {
  const [chapterInput, setChapterInput] = useState(currentChapter ?? '')
  const [pageInput, setPageInput] = useState(currentPage ?? '')
  const [updateLabel, setUpdateLabel] = useState('Update')

  useEffect(() => {
    setChapterInput(currentChapter ?? '')
    setPageInput(currentPage ?? '')
  }, [currentChapter, currentPage])

  function handleReset() {
    setChapterInput('')
    setPageInput('')
    onReset()
        .then(() => {
        setUpdateLabel('✓ Updated')
        setTimeout(() => setUpdateLabel('Update'), 2000)
        })
        .catch(console.error)
  }

  function handleUpdate() {
    const chapter = chapterInput !== '' ? parseInt(chapterInput) : null
    const page = pageInput !== '' ? parseInt(pageInput) : null
    onUpdate(chapter, page)
      .then(() => {
        setUpdateLabel('✓ Saved')
        setTimeout(() => setUpdateLabel('Save progress'), 2000)
      })
      .catch(console.error)
  }

  return (
    <div
        className="rounded-lg p-3"
        style={{
        background: theme.topBar,
        border: `0.5px solid ${theme.cardBorder}`,
        }}
    >
        <div className="flex gap-2 mb-2">
        <div className="flex-1">
            <p className="text-[11px] text-[#555566] m-0 mb-1">Chapter #</p>
            <input
            type="number"
            min={0}
            value={chapterInput}
            onChange={(e) => setChapterInput(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter') handleUpdate() }}
            placeholder="—"
            className="w-full text-[12px] rounded-lg outline-none"
            style={{
                background: theme.background,
                border: `0.5px solid ${theme.cardBorder}`,
                color: '#e2e2f0',
                padding: '7px 12px',
                MozAppearance: 'textfield',
            }}
            />
        </div>

        <div className="flex-1">
            <p className="text-[11px] text-[#555566] m-0 mb-1">Page #</p>
            <input
            type="number"
            min={0}
            value={pageInput}
            onChange={(e) => setPageInput(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter') handleUpdate() }}
            placeholder="—"
            className="w-full text-[12px] rounded-lg outline-none"
            style={{
                background: theme.background,
                border: `0.5px solid ${theme.cardBorder}`,
                color: '#e2e2f0',
                padding: '7px 12px',
                MozAppearance: 'textfield',
            }}
            />
        </div>

        <div className="flex flex-col justify-end">
            <p className="text-[11px] text-[#555566] m-0 mb-1 invisible">_</p>
            <button
            onClick={handleUpdate}
            className="text-[12px] font-medium px-4 py-[7px] rounded-lg transition-colors whitespace-nowrap"
            style={{
                background: updateLabel === '✓ Updated' ? '#1f4a32' : theme.accent,
                border: `0.5px solid ${updateLabel === '✓ Updated' ? '#2a5a3a' : theme.accent}`,
                color: updateLabel === '✓ Updated' ? '#4ade80' : '#000000',
                cursor: 'pointer',
            }}
            >
            {updateLabel}
            </button>
        </div>
        </div>

        <button
        onClick={handleReset}
        className="w-full py-[7px] text-[12px] font-medium rounded-lg transition-colors"
        style={{
            background: '#2e1212',
            border: '0.5px solid #501c1c',
            color: '#f87171',
            cursor: 'pointer',
        }}
        >
        Reset progress
        </button>
    </div>
    )
}

export default BookProgressSection