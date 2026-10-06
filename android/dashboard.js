const defaults = [
  ["LOGIC", "Do it for the picture puzzle. Put it all together. Solve the world. One conversation at a time."],
  ["ENCYCLOPEDIA", "Your mangled brain would like you to know there is a boxer called Contact Mike."],
  ["RHETORIC", "Yes! Abject failure. Total, irreversible defeat on all fronts! Absolutely vanquished, beaten, curb-stomped and pissed on — until you came along!"],
  ["DRAMA", "Everyone knows you can't trust artists. They're nearly as bad as clowns."],
  ["CONCEPTUALIZATION", "The world’s most precious material, reserved for those she lets close enough to feel it."],
  ["VISUAL CALCULUS", "The man does not know the bullet has entered his brain. He never will."],
  ["VOLITION", "No. This is somewhere to be. This is all you have, but it’s still something. Streets and sodium lights."],
  ["INLAND EMPIRE", "A tremendous loneliness comes over you. Everybody in the world is doing something without you."],
  ["EMPATHY", "This is a very, very sad man who has just seen something that’s made him forget his sadness."],
  ["AUTHORITY", "And you? Is it an honour to work with you? Don't ask him, ask yourself."],
  ["SUGGESTION", "This was not about failure or success. This was always going to be horror."],
  ["ESPRIT DE CORPS", "If an assault were launched on this building right now, this man would hurl himself in death's way to save you."],
  ["ENDURANCE", "Think about the seagull's story. It's one of endurance — and adaptation."],
  ["PAIN THRESHOLD", "There's tenderness in the carabineer's look. Tenderness that's curdled into pain."],
  ["PHYSICAL INSTRUMENT", "Cold and heavy — like truth."],
  ["ELECTROCHEMISTRY", "The funk soul brother at the back of his head has gone dark. Forever."],
  ["SHIVERS", "I am a fragment of the world spirit, the genius loci of Revachol."],
  ["HALF LIGHT", "He whispers with such predatory hunger it borders on longing."],
  ["HAND/EYE COORDINATION", "A gun is a tool for putting holes into things far away. Keep your fingers steady."],
  ["PERCEPTION", "Listen closely. The silence here is not empty; it is crowded with things people decided not to say."],
  ["REACTION SPEED", "The second ticks by like a falling guillotine blade. Move, or be severed."],
  ["SAVOIR FAIRE", "Style is not an accident. It is a calculated declaration of war against the mundane."],
  ["INTERFACING", "The needle drops into the groove. You understand machines because they tell the simple truth."],
  ["COMPOSURE", "Straighten your collar. Swallow the bile. No one can know that your heart is coming apart."]
];

const storageKey = "disco-check-lines";
const nativeLines = window.Android && typeof window.Android.loadLines === "function"
  ? JSON.parse(window.Android.loadLines())
  : [];
let lines = nativeLines.length ? nativeLines : (JSON.parse(localStorage.getItem(storageKey) || "null") || defaults.map(([title, quote]) => ({ title, quote })));
const container = document.querySelector("#lines");
const status = document.querySelector("#status");

function render() {
  container.innerHTML = "";
  lines.forEach((line, index) => {
    const section = document.createElement("section");
    section.className = "line";
    section.innerHTML = `<label>${index + 1}. LINE</label>
      <input aria-label="Line title" value="${escapeHtml(line.title)}">
      <textarea aria-label="Line text">${escapeHtml(line.quote)}</textarea>`;
    section.querySelector("input").addEventListener("input", event => { line.title = event.target.value; });
    section.querySelector("textarea").addEventListener("input", event => { line.quote = event.target.value; });
    container.appendChild(section);
  });
}

function escapeHtml(value) {
  return value.replace(/[&<>"']/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[char]));
}

document.querySelector("#save").addEventListener("click", () => {
  localStorage.setItem(storageKey, JSON.stringify(lines));
  if (window.Android && typeof window.Android.saveLines === "function") {
    window.Android.saveLines(JSON.stringify(lines));
  }
  status.textContent = "Saved locally.";
  setTimeout(() => { status.textContent = ""; }, 1800);
});

render();
