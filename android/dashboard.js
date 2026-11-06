(function () {
  const HISTORY_KEY = "disco-check-history-v1";
  const skillGroups = {
    INTELLECT: ["LOGIC", "ENCYCLOPEDIA", "RHETORIC", "DRAMA", "CONCEPTUALIZATION", "VISUAL CALCULUS"],
    PSYCHE: ["VOLITION", "INLAND EMPIRE", "EMPATHY", "AUTHORITY", "SUGGESTION", "ESPRIT DE CORPS"],
    PHYSIQUE: ["ENDURANCE", "PAIN THRESHOLD", "PHYSICAL INSTRUMENT", "ELECTROCHEMISTRY", "SHIVERS", "HALF LIGHT"],
    MOTORICS: ["HAND/EYE COORDINATION", "PERCEPTION", "REACTION SPEED", "SAVOIR FAIRE", "INTERFACING", "COMPOSURE"]
  };

  function applyNothingTheme() {
    if (!window.Android || typeof window.Android.widgetColors !== "function") return;
    try {
      const colors = JSON.parse(window.Android.widgetColors());
      const root = document.documentElement;
      const cssNames = {
        surface: "--nothing-surface",
        raised: "--nothing-raised",
        text: "--nothing-text",
        muted: "--nothing-muted",
        accent: "--nothing-accent",
        secondary: "--nothing-secondary",
        border: "--nothing-border"
      };
      for (const [key, cssName] of Object.entries(cssNames)) {
        if (colors[key]) root.style.setProperty(cssName, colors[key]);
      }
      document.querySelector('meta[name="theme-color"]')?.setAttribute("content", colors.surface);
    } catch (error) {
      console.warn("Nothing palette unavailable; using dashboard fallbacks", error);
    }
  }

  function readHistory() {
    try {
      const value = JSON.parse(localStorage.getItem(HISTORY_KEY) || "[]");
      return Array.isArray(value) ? value.filter(item => item && item.header && Number.isFinite(item.createdAt)) : [];
    } catch (_) {
      return [];
    }
  }

  function saveCheck(data) {
    const entry = {
      header: String(data.header || "UNKNOWN CHECK"),
      quote: String(data.quote || ""),
      die1: Number(data.die1) || 0,
      die2: Number(data.die2) || 0,
      total: Number(data.total) || 0,
      is_critical: Boolean(data.is_critical),
      createdAt: Date.now()
    };
    const history = [entry, ...readHistory()].slice(0, 100);
    try { localStorage.setItem(HISTORY_KEY, JSON.stringify(history)); } catch (_) {}
    render(history);
  }

  function el(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
  }

  function renderMetrics(history) {
    document.querySelector("#metric-total").textContent = String(history.length);
    document.querySelector("#metric-success").textContent = String(history.filter(item => item.header === "CRITICAL SUCCESS").length);
    document.querySelector("#metric-failure").textContent = String(history.filter(item => item.header === "CRITICAL FAILURE").length);
    const best = history.reduce((max, item) => Math.max(max, Number(item.total) || 0), 0);
    document.querySelector("#metric-best").textContent = best ? String(best) : "—";
  }

  function renderActivity(history) {
    const chart = document.querySelector("#activity-chart");
    chart.replaceChildren();
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const days = [];
    for (let offset = 6; offset >= 0; offset--) {
      const date = new Date(today);
      date.setDate(today.getDate() - offset);
      days.push({ date, count: 0 });
    }
    for (const item of history) {
      const date = new Date(item.createdAt);
      date.setHours(0, 0, 0, 0);
      const day = days.find(candidate => candidate.date.getTime() === date.getTime());
      if (day) day.count++;
    }
    const max = Math.max(1, ...days.map(day => day.count));
    for (const day of days) {
      const column = el("div", "activity-day");
      const track = el("div", "bar-track");
      const bar = el("div", "bar");
      const height = day.count ? Math.max(12, Math.round(day.count / max * 88)) : 3;
      bar.style.height = `${height}px`;
      bar.setAttribute("aria-hidden", "true");
      track.append(bar);
      column.append(track, el("span", "bar-count", String(day.count)), el("span", "day-label", day.date.toLocaleDateString(undefined, { weekday: "short" })));
      chart.append(column);
    }
    const total = days.reduce((sum, day) => sum + day.count, 0);
    chart.setAttribute("aria-label", `${total} checks in the last seven days`);
  }

  function groupFor(header) {
    const normalized = String(header || "").toUpperCase();
    return Object.keys(skillGroups).find(group => skillGroups[group].includes(normalized));
  }

  function renderGroups(history) {
    const container = document.querySelector("#group-list");
    container.replaceChildren();
    const counts = Object.fromEntries(Object.keys(skillGroups).map(group => [group, 0]));
    for (const item of history) {
      const group = groupFor(item.header);
      if (group) counts[group]++;
    }
    const max = Math.max(1, ...Object.values(counts));
    for (const [name, count] of Object.entries(counts)) {
      const row = el("div", "group-row");
      const track = el("div", "group-track");
      const fill = el("div", "group-fill");
      fill.style.width = `${count ? Math.max(6, count / max * 100) : 0}%`;
      track.append(fill);
      row.append(el("span", "group-name", name), track, el("span", "group-count", String(count)));
      container.append(row);
    }
  }

  function renderRecent(history) {
    const container = document.querySelector("#recent-list");
    container.replaceChildren();
    if (!history.length) {
      const empty = el("div", "empty-state");
      empty.append(el("strong", "", "Nothing on the board yet."), document.createTextNode("Run a check to start your local history."));
      container.append(empty);
      return;
    }
    for (const item of history.slice(0, 5)) {
      const row = el("div", "recent-row");
      const icon = el("div", "recent-icon", `${item.die1 || "–"}·${item.die2 || "–"}`);
      icon.setAttribute("aria-hidden", "true");
      const info = el("div", "recent-info");
      const title = el("div", "recent-title", item.header);
      const date = new Date(item.createdAt);
      const meta = el("div", "recent-meta", `${date.toLocaleDateString(undefined, { month: "short", day: "numeric" })} · ${date.toLocaleTimeString(undefined, { hour: "numeric", minute: "2-digit" })}`);
      info.append(title, meta);
      row.append(icon, info, el("div", "recent-total", item.total ? `${item.total}/12` : "—"));
      container.append(row);
    }
  }

  function render(history) {
    renderMetrics(history);
    renderActivity(history);
    renderGroups(history);
    renderRecent(history);
  }

  applyNothingTheme();
  render(readHistory());
  document.addEventListener("disco:roll", event => saveCheck(event.detail || {}));
  document.querySelector("#roll-now").addEventListener("click", () => document.querySelector("#widget").click());
})();
