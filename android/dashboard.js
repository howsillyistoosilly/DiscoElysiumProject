const defaults = [
  ["LOGIC", "Do it for the picture puzzle. Put it all together. Solve the world. One conversation at a time.", "Intellect/Logic.jpg"],
  ["ENCYCLOPEDIA", "Your mangled brain would like you to know there is a boxer called Contact Mike.", "Intellect/Encyclopedia.jpg"],
  ["RHETORIC", "Yes! Abject failure. Total, irreversible defeat on all fronts!", "Intellect/Rhetoric.jpg"],
  ["DRAMA", "Everyone knows you can't trust artists. They're nearly as bad as clowns.", "Intellect/Drama.jpg"],
  ["CONCEPTUALIZATION", "The world’s most precious material, reserved for those she lets close enough to feel it.", "Intellect/Conceptualization.jpg"],
  ["VISUAL CALCULUS", "The man does not know the bullet has entered his brain. He never will.", "Intellect/Visual_Calculus.jpg"],
  ["VOLITION", "No. This is somewhere to be. This is all you have, but it’s still something.", "Psyche/Volition.jpg"],
  ["INLAND EMPIRE", "A tremendous loneliness comes over you. Everybody in the world is doing something without you.", "Psyche/Inland_Empire.jpg"],
  ["EMPATHY", "This is a very, very sad man who has just seen something that’s made him forget his sadness.", "Psyche/Empathy.jpg"],
  ["AUTHORITY", "And you? Is it an honour to work with you? Don't ask him, ask yourself.", "Psyche/Authority.jpg"],
  ["SUGGESTION", "This was not about failure or success. This was always going to be horror.", "Psyche/Suggestion.jpg"],
  ["ESPRIT DE CORPS", "If an assault were launched on this building right now, this man would hurl himself in death's way.", "Psyche/Espirit_De_Corps.jpg"],
  ["ENDURANCE", "Think about the seagull's story. It's one of endurance — and adaptation.", "Physique/Endurance.jpg"],
  ["PAIN THRESHOLD", "There's tenderness in the carabineer's look. Tenderness that's curdled into pain.", "Physique/Pain_Threshold.jpg"],
  ["PHYSICAL INSTRUMENT", "Cold and heavy — like truth.", "Physique/Physical_Instrument.jpg"],
  ["ELECTROCHEMISTRY", "The funk soul brother at the back of his head has gone dark. Forever.", "Physique/Electrochemistry.jpg"],
  ["SHIVERS", "I am a fragment of the world spirit, the genius loci of Revachol.", "Physique/Shivers.jpg"],
  ["HALF LIGHT", "He whispers with such predatory hunger it borders on longing.", "Physique/Half_Light.jpg"],
  ["HAND/EYE COORDINATION", "A gun is a tool for putting holes into things far away. Keep your fingers steady.", "Motorics/Hand_Eye_Coordination.jpg"],
  ["PERCEPTION", "Listen closely. The silence here is not empty; it is crowded with things left unsaid.", "Motorics/Perception.jpg"],
  ["REACTION SPEED", "The second ticks by like a falling guillotine blade. Move, or be severed.", "Motorics/Reaction_Speed.jpg"],
  ["SAVOIR FAIRE", "Style is not an accident. It is a calculated declaration of war against the mundane.", "Motorics/Savoir_Faire.jpg"],
  ["INTERFACING", "The needle drops into the groove. You understand machines because they tell the simple truth.", "Motorics/Interfacing.jpg"],
  ["COMPOSURE", "Straighten your collar. Swallow the bile. No one can know that your heart is coming apart.", "Motorics/Composure.jpg"]
];

const storageKey = "disco-check-lines";
const nativeLines = window.Android && typeof window.Android.loadLines === "function" ? JSON.parse(window.Android.loadLines()) : [];
let lines = nativeLines.length ? nativeLines : JSON.parse(localStorage.getItem(storageKey) || "null") || defaults.map(([title, quote, asset]) => ({ title, quote, asset, lines: [] }));
lines = lines.map(line => ({ ...line, lines: Array.isArray(line.lines) && line.lines.length ? line.lines : [{ quote: line.quote || "Write a new thought." }] }));
let editingIndex = -1;
let activeSkillIndex = -1;
const list = document.querySelector("#lines");
const editor = document.querySelector("#editor");

function render() {
  const query = document.querySelector("#search").value.trim().toLowerCase();
  list.innerHTML = "";
  const visible = lines.map((line, index) => ({ line, index })).filter(({ line }) => `${line.title} ${line.quote}`.toLowerCase().includes(query));
  visible.forEach(({ line, index }) => {
    const row = document.createElement("button");
    row.className = "skill";
    row.innerHTML = line.asset
      ? `<img src="/assets/skills/${line.asset}" alt=""><span class="skill-name"></span>`
      : `<span class="skill-name"></span>`;
    row.querySelector(".skill-name").textContent = line.title;
    row.addEventListener("click", () => openSkill(index));
    list.appendChild(row);
  });
  const add = document.createElement("button");
  add.className = "skill skill-add";
  add.type = "button";
  add.innerHTML = '<span aria-hidden="true">+</span><span class="skill-name">ADD LINE</span>';
  add.addEventListener("click", addSkill);
  list.appendChild(add);
}

function addSkill() {
  lines.push({
    title: "NEW SKILL",
    quote: "Write a new thought.",
    asset: "",
    lines: [{ quote: "Write a new thought." }]
  });
  openSkill(lines.length - 1);
}

function openSkill(index) {
  activeSkillIndex = index;
  document.querySelector("#grid-view").classList.add("hidden");
  document.querySelector("#detail").classList.add("active");
  document.querySelector("#detail-title").textContent = lines[index].title;
  document.querySelector("#detail-image").src = lines[index].asset ? `/assets/skills/${lines[index].asset}` : "";
  renderDetail();
}

function renderDetail() {
  const detail = document.querySelector("#detail-lines");
  detail.innerHTML = "";
  lines[activeSkillIndex].lines.forEach((entry, index) => {
    const item = document.createElement("article");
    item.className = "detail-line";
    item.innerHTML = `<span></span><button type="button" aria-label="Edit line">⋮</button>`;
    item.querySelector("span").textContent = entry.quote;
    item.querySelector("button").addEventListener("click", () => openEditor(index));
    detail.appendChild(item);
  });
}

function openEditor(lineIndex) {
  document.querySelector("#delete-line").hidden = false;
  document.querySelector("#editor-heading").textContent = `Edit ${lines[activeSkillIndex].title}`;
  document.querySelector("#title").value = lines[activeSkillIndex].title;
  document.querySelector('label[for="title"]').hidden = true;
  document.querySelector("#title").hidden = true;
  document.querySelector("#text").value = lines[activeSkillIndex].lines[lineIndex].quote;
  editingIndex = lineIndex;
  editor.showModal();
}

function addLine() {
  lines[activeSkillIndex].lines.push({ quote: "Write a new thought." });
  openEditor(lines[activeSkillIndex].lines.length - 1);
}

document.querySelector("#search").addEventListener("input", render);
document.querySelector("#editor form").addEventListener("submit", event => {
  event.preventDefault();
  if (editingIndex < 0) return;
  lines[activeSkillIndex].lines[editingIndex].quote = document.querySelector("#text").value.trim();
  editor.close();
  renderDetail();
  saveLines();
});
document.querySelector("#delete-line").addEventListener("click", () => {
  if (editingIndex < 0) return;
  lines[activeSkillIndex].lines.splice(editingIndex, 1);
  editor.close();
  saveLines();
  renderDetail();
});
document.querySelector("#back").addEventListener("click", () => {
  document.querySelector("#detail").classList.remove("active");
  document.querySelector("#grid-view").classList.remove("hidden");
  activeSkillIndex = -1;
  render();
});
document.querySelector("#add-line").addEventListener("click", addLine);
function saveLines() {
  localStorage.setItem(storageKey, JSON.stringify(lines));
  if (window.Android && typeof window.Android.saveLines === "function") window.Android.saveLines(JSON.stringify(lines));
}
document.querySelector("#save-nav").addEventListener("click", saveLines);
document.querySelector("#roll-nav").addEventListener("click", () => window.location.href = "index.html");
render();
