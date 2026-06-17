function RatingCard({ value, label, color, theme }) {
  if (value == null) return null

  return (
    <div
      className="rounded-lg p-3 text-center flex flex-col justify-center"
      style={{
        background: theme.topBar,
        border: `0.5px solid ${theme.cardBorder}`,
        minWidth: '80px',
        alignSelf: 'stretch',
      }}
    >
      <p className="text-[20px] font-medium m-0 mb-0.5"
        style={{ color }}>
        {value}
      </p>
      <p className="text-[10px] text-[#555566] m-0">{label}</p>
    </div>
  )
}

export default RatingCard