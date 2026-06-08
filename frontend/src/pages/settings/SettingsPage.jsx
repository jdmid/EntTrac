import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  DndContext,
  closestCenter,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core'
import {
  SortableContext,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import Navbar from '../../components/Navbar'
import { useSettings } from '../../context/SettingsContext'
import { themes } from '../../theme/themes'

const theme = themes.brand

function SortableTabRow({ tab, onToggle, isLastVisible }) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: tab.id })

  const [showGuard, setShowGuard] = useState(false)

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.4 : 1,
    zIndex: isDragging ? 10 : 'auto',
    position: 'relative',
  }

  function handleToggle() {
    if (tab.visible && isLastVisible) {
      setShowGuard(true)
      setTimeout(() => setShowGuard(false), 2500)
      return
    }
    onToggle(tab.id)
  }

  return (
    <div ref={setNodeRef} style={style}>
      <div
        className="flex items-center gap-3 px-4 py-2.5"
        style={{
          borderBottom: `0.5px solid ${theme.cardBorder}`,
          background: isDragging ? theme.accentBg : 'transparent',
          borderRadius: isDragging ? '8px' : '0',
        }}
      >
        {/* Drag handle */}
        <button
          {...attributes}
          {...listeners}
          className="cursor-grab active:cursor-grabbing touch-none flex-shrink-0"
          style={{ color: isDragging ? theme.accent : '#555580', lineHeight: 0 }}
          aria-label={`Drag to reorder ${tab.label}`}
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="9" cy="5" r="1" fill="currentColor" stroke="none"/>
            <circle cx="9" cy="12" r="1" fill="currentColor" stroke="none"/>
            <circle cx="9" cy="19" r="1" fill="currentColor" stroke="none"/>
            <circle cx="15" cy="5" r="1" fill="currentColor" stroke="none"/>
            <circle cx="15" cy="12" r="1" fill="currentColor" stroke="none"/>
            <circle cx="15" cy="19" r="1" fill="currentColor" stroke="none"/>
          </svg>
        </button>

        {/* Color dot */}
        <div
          className="flex-shrink-0 rounded-full"
          style={{
            width: '7px',
            height: '7px',
            background: themes[tab.id]?.accent ?? theme.accent,
            opacity: tab.visible ? 1 : 0.25,
          }}
        />

        {/* Tab name */}
        <span
          className="flex-1 text-[13px]"
          style={{ color: tab.visible ? '#9090a8' : '#555580' }}
        >
          {tab.label}
        </span>

        {/* Toggle */}
        <button
          onClick={handleToggle}
          className="flex-shrink-0 rounded-full transition-colors"
          style={{
            width: '30px',
            height: '17px',
            background: tab.visible ? theme.accent : '#1e1e30',
            position: 'relative',
            cursor: tab.visible && isLastVisible ? 'not-allowed' : 'pointer',
            opacity: tab.visible && isLastVisible ? 0.5 : 1,
          }}
          aria-label={`${tab.visible ? 'Hide' : 'Show'} ${tab.label} tab`}
          role="switch"
          aria-checked={tab.visible}
        >
          <span
            className="absolute rounded-full"
            style={{
              width: '13px',
              height: '13px',
              background: tab.visible ? '#fff' : '#303045',
              top: '2px',
              left: tab.visible ? '15px' : '2px',
              transition: 'left 0.15s',
            }}
          />
        </button>
      </div>

      {/* Last tab guard hint */}
      {showGuard && (
        <div
          className="flex items-center gap-2 px-4 py-2"
          style={{ background: theme.accentBg }}
        >
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none"
            stroke={theme.accent} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
            aria-hidden="true">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <span className="text-[11px]" style={{ color: theme.accent }}>
            At least one tab must stay visible
          </span>
        </div>
      )}
    </div>
  )
}

function Section({ title, description, children, defaultOpen = true }) {
  const [open, setOpen] = useState(defaultOpen)

  return (
    <div
      className="rounded-[10px] overflow-hidden mb-3"
      style={{
        background: theme.cardBody,
        border: `0.5px solid ${theme.cardBorder}`,
      }}
    >
      <button
        onClick={() => setOpen((v) => !v)}
        className="w-full flex items-center justify-between px-4 py-3 transition-colors"
        style={{ background: 'transparent' }}
      >
        <div className="text-left">
          <p
            className="text-[11px] font-medium uppercase tracking-[0.06em] m-0"
            style={{ color: '#7070a0' }}
          >
            {title}
          </p>
          <p className="text-[11px] m-0 mt-0.5" style={{ color: '#555580' }}>
            {description}
          </p>
        </div>
        <svg
          width="13"
          height="13"
          viewBox="0 0 24 24"
          fill="none"
          stroke="#555580"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
          style={{
            transform: open ? 'rotate(180deg)' : 'rotate(0deg)',
            transition: 'transform 0.15s',
            flexShrink: 0,
          }}
        >
          <polyline points="6 9 12 15 18 9"/>
        </svg>
      </button>

      {open && (
        <div style={{ borderTop: `0.5px solid ${theme.cardBorder}` }}>
          {children}
        </div>
      )}
    </div>
  )
}

function SettingsPage() {
  const { tabs, reorderTabs, toggleTab } = useSettings()

  const visibleCount = tabs.filter((t) => t.visible).length

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    })
  )

  function handleDragEnd(event) {
    const { active, over } = event
    if (over && active.id !== over.id) {
      reorderTabs(active.id, over.id)
    }
  }

  return (
    <div className="min-h-screen" style={{ background: theme.background }}>
      <Navbar activeMedia="manga" />

      <div className="p-5">
        <h1 className="text-[18px] font-medium text-[#e2e2f0] m-0 mb-1">
          Settings
        </h1>
        <p className="text-[13px] text-[#555566] m-0 mb-5">
          Manage your EntTrac preferences
        </p>

        <Section
          title="Tabs"
          description="Reorder or hide media tabs"
          defaultOpen={true}
        >
          <div
            className="flex items-center justify-between px-4 py-2"
            style={{ borderBottom: `0.5px solid ${theme.cardBorder}` }}
          >
            <span className="text-[11px]" style={{ color: '#555580' }}>
              {visibleCount} of {tabs.length} tabs visible
            </span>
            <span
              className="text-[11px] rounded"
              style={{
                color: '#555580',
                background: theme.background,
                border: `0.5px solid ${theme.cardBorder}`,
                padding: '2px 7px',
                cursor: 'default',
                userSelect: 'none',
              }}
            >
              drag to reorder
            </span>
          </div>

          <DndContext
            sensors={sensors}
            collisionDetection={closestCenter}
            onDragEnd={handleDragEnd}
          >
            <SortableContext
              items={tabs.map((t) => t.id)}
              strategy={verticalListSortingStrategy}
            >
              {tabs.map((tab) => (
                <SortableTabRow
                  key={tab.id}
                  tab={tab}
                  onToggle={toggleTab}
                  isLastVisible={tab.visible && visibleCount === 1}
                />
              ))}
            </SortableContext>
          </DndContext>
        </Section>

        <Section
          title="Display"
          description="Appearance and layout preferences"
          defaultOpen={false}
        >
          <p className="text-[12px] px-4 py-3" style={{ color: '#555580', fontStyle: 'italic' }}>
            More options coming soon
          </p>
        </Section>

        <Section
          title="Data"
          description="Export or manage your library"
          defaultOpen={false}
        >
          <p className="text-[12px] px-4 py-3" style={{ color: '#555580', fontStyle: 'italic' }}>
            More options coming soon
          </p>
        </Section>
      </div>
    </div>
  )
}

export default SettingsPage