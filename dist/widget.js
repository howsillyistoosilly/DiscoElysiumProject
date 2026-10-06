const API_BASE = globalThis.DISCO_API_BASE || "http://localhost:8080";

async function onWidgetTap() {
  const response = await fetch(`${API_BASE}/api/roll`);
  if (!response.ok) {
    throw new Error(`Disco backend returned ${response.status}`);
  }
  const data = await response.json();
  return {
    title: data.header,
    titleColor: data.is_critical ? "#D71921" : "#FFFFFF",
    outlineColor: data.accent_color,
    quote: `“${data.quote}”`,
    dice: [`${API_BASE}/assets/${data.die1_asset}`, `${API_BASE}/assets/${data.die2_asset}`],
    background: `${API_BASE}/assets/skills/${data.asset_path}`
  };
}

if (typeof module !== "undefined") module.exports = { onWidgetTap };
