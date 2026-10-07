const API_BASE = window.DISCO_API_BASE || "";
const ASSET_BASE = window.location.protocol === "file:"
  ? "file:///android_asset/assets"
  : "/assets";
let hasShownOpeningRoll = false;
let isRolling = false;
const montageImages = [
  "/assets/skills/Intellect/Logic.jpg",
  "/assets/skills/Intellect/Rhetoric.jpg",
  "/assets/skills/Psyche/Volition.jpg",
  "/assets/skills/Psyche/Empathy.jpg",
  "/assets/skills/Physique/Half_Light.jpg",
  "/assets/skills/Physique/Shivers.jpg",
  "/assets/skills/Motorics/Perception.jpg",
  "/assets/skills/Motorics/Reaction_Speed.jpg",
  "/assets/skills/Psyche/Inland_Empire.jpg",
  "/assets/skills/Intellect/Conceptualization.jpg"
];

async function rollCheck() {
  if (window.Android && typeof window.Android.roll === "function") {
    return JSON.parse(window.Android.roll());
  }
  const response = await fetch(`${API_BASE}/api/roll`);
  if (!response.ok) throw new Error(`roll failed (${response.status})`);
  return response.json();
}

function renderRoll(data) {
  const widget = document.querySelector("#widget");
  widget.style.setProperty("--accent", data.accent_color || "#777");
  const header = document.querySelector("#header");
  header.textContent = data.header;
  header.className = data.is_critical ? "critical" : "";
  const quote = document.querySelector("#quote");
  quote.textContent = `“${data.quote}”`;
  const length = data.quote.length;
  const size = length > 500 ? ".46rem" : length > 360 ? ".62rem" : ".82rem";
  quote.style.setProperty("--quote-size", size);
  const isLongQuote = length > 360;
  const contentHeight = isLongQuote
    ? Math.min(178, 112 + Math.ceil((length - 360) / 100) * 14)
    : 132;
  const gradientStart = isLongQuote ? "18%" : "35%";
  const gradientMid = isLongQuote ? "62%" : "72%";
  widget.style.setProperty("--content-height", `${contentHeight}px`);
  widget.style.setProperty("--gradient-start", gradientStart);
  widget.style.setProperty("--gradient-mid", gradientMid);
  document.querySelector("#portrait").style.backgroundImage =
    `linear-gradient(#0000, #000c), url("${ASSET_BASE}/skills/${data.asset_path}")`;
  document.querySelector("#dice").innerHTML = [data.die1_asset, data.die2_asset]
    .map(asset => `<img alt="die" src="${ASSET_BASE}/${asset}">`).join("");
}

async function handleWidgetTap() {
  if (isRolling) return;
  isRolling = true;
  const widget = document.querySelector("#widget");
  const photoToggle = document.querySelector("#photo-toggle");
  const flash = document.querySelector("#roll-flash");
  const portrait = document.querySelector("#portrait");
  widget.classList.remove("photo-only");
  widget.setAttribute("aria-label", "Roll Disco Check");
  photoToggle.setAttribute("aria-label", "View photo");
  photoToggle.setAttribute("title", "View photo");
  widget.setAttribute("aria-busy", "true");
  try {
    if (!hasShownOpeningRoll) {
      hasShownOpeningRoll = true;
      widget.classList.add("montage");
      const frames = [...montageImages].sort(() => Math.random() - 0.5).slice(0, 10);
      for (const image of frames) {
        portrait.style.backgroundImage = `url("${ASSET_BASE}/skills/${image.split("/assets/skills/").pop()}")`;
        await wait(190);
      }
      renderRoll(await rollCheck());
      widget.classList.remove("montage");
      flash.className = "roll-flash flash";
      await wait(950);
    } else {
      renderRoll(await rollCheck());
    }
  } catch (error) {
    console.error(error);
  } finally {
    flash.className = "roll-flash";
    widget.removeAttribute("aria-busy");
    isRolling = false;
  }
}

function wait(milliseconds) {
  return new Promise(resolve => setTimeout(resolve, milliseconds));
}

document.querySelector("#widget").addEventListener("click", handleWidgetTap);
document.querySelector("#widget").addEventListener("keydown", event => {
  if (event.key === "Enter" || event.key === " ") {
    event.preventDefault();
    handleWidgetTap();
  }
});

document.querySelector("#photo-toggle").addEventListener("click", event => {
  event.stopPropagation();
  const widget = document.querySelector("#widget");
  const photoOnly = widget.classList.toggle("photo-only");
  const label = photoOnly ? "Return to Disco Check" : "View photo";
  event.currentTarget.setAttribute("aria-label", label);
  event.currentTarget.setAttribute("title", label);
  widget.setAttribute("aria-label", label);
});
