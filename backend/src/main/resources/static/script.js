// ------------------------------------------------------------
// Medicine Checker - frontend
// Talks to the backend that runs at http://localhost:8080
// ------------------------------------------------------------
const API = location.protocol === "file:" ? "http://localhost:8080/api" : "/api";

const el = (id) => document.getElementById(id);
let currentAge = null;

// Make text safe before putting it on the page
function esc(text) {
  const d = document.createElement("div");
  d.textContent = String(text);
  return d.innerHTML;
}

const STATUS_TEXT = { safe: "Safe", careful: "Be careful", avoid: "Avoid" };
const GROUP_TEXT = {
  child: "Child (under 18)",
  adult: "Adult (18 to 59)",
  elderly: "Senior (60 and above)"
};

// ---------- 1. Load names for the search box ----------
async function loadSuggestions() {
  try {
    const res = await fetch(API + "/suggestions");
    if (!res.ok) throw new Error("bad response");
    const names = await res.json();

    el("names").innerHTML = names.map((n) => `<option value="${esc(n)}">`).join("");

    el("chips").innerHTML = "";
    names.forEach((n) => {
      const b = document.createElement("button");
      b.className = "chip";
      b.textContent = n;
      b.addEventListener("click", () => {
        el("name").value = n;
        el("age").focus();
      });
      el("chips").appendChild(b);
    });
    el("banner").hidden = true;
  } catch (e) {
    showBackendDown();
  }
}

function showBackendDown() {
  el("banner").hidden = false;
  el("banner").textContent =
    "Cannot reach the backend. Please start it in VS Code (open DrugApplication.java and click Run), then refresh this page.";
}

// ---------- 2. Press Check ----------
async function check() {
  const name = el("name").value.trim();
  const age = parseInt(el("age").value, 10);
  el("error").textContent = "";

  if (!name) {
    el("error").textContent = "Step 1 is empty. Please type a medicine or chemical name.";
    return;
  }
  if (!age || age < 1 || age > 120) {
    el("error").textContent = "Step 2 is empty. Please type a valid age (1 to 120).";
    return;
  }
  currentAge = age;
  showMessage("Searching...");

  try {
    const res = await fetch(`${API}/medicine?name=${encodeURIComponent(name)}&age=${age}`);
    if (!res.ok) throw new Error("bad response");
    const data = await res.json();
    el("banner").hidden = true;

    if (data.status === "FOUND") {
      renderResult(data.result);
    } else if (data.status === "MULTIPLE") {
      renderChooser(data.matches, name);
    } else {
      showMessage(`Sorry, I do not have "${name}" in my list yet. Try one of the names shown in step 1.`);
    }
  } catch (e) {
    showMessage("Something went wrong.");
    showBackendDown();
  }
}

// ---------- 3. Several medicines match: let the user pick ----------
function renderChooser(matches, searchedText) {
  const box = el("result");
  box.className = "card muted";
  box.innerHTML = `<div style="margin-bottom:10px">${matches.length} medicines match
    <span class="pill chem-pill">${esc(searchedText)}</span>.
    Click one to see the details for age ${currentAge}:</div>`;

  matches.forEach((m) => {
    const b = document.createElement("button");
    b.className = "pick-btn";
    b.innerHTML = `${esc(m.name)}<small>${esc(m.ingredients)}</small>`;
    b.addEventListener("click", () => pick(m.id));
    box.appendChild(b);
  });
}

async function pick(id) {
  // Use the age that is typed in the box right now
  const typedAge = parseInt(el("age").value, 10);
  if (typedAge >= 1 && typedAge <= 120) currentAge = typedAge;
  showMessage("Loading...");
  try {
    const res = await fetch(`${API}/medicine/${id}?age=${currentAge}`);
    if (!res.ok) throw new Error("bad response");
    renderResult(await res.json());
  } catch (e) {
    showMessage("Something went wrong.");
    showBackendDown();
  }
}

// ---------- 4. Show the full answer ----------
function section(icon, title, content) {
  return `<div class="sec"><div class="ic">${icon}</div>
    <div><div class="t">${title}</div><div>${content}</div></div></div>`;
}

function renderResult(r) {
  const s = r.safety;

  const ingredients = r.ingredients
    .map((i) => `<span class="pill chem-pill">${esc(i.name)} ${esc(i.strength)}</span>`)
    .join("");

  const steps = "<ul>" + r.howToUse.map((h) => `<li>${esc(h)}</li>`).join("") + "</ul>";

  const effects = r.sideEffects
    .map((e) => `<span class="pill ${esc(e.severity)}">${esc(e.name)}</span>`)
    .join("");

  const warn = currentAge < 12
    ? `<div class="warn">For young children, always check the label and ask a doctor first.</div>`
    : "";

  const box = el("result");
  box.className = "card";
  box.innerHTML =
    `<div class="med-name">${esc(r.name)}</div>
     <div class="used">Used for: ${esc(r.usedFor)}</div>` +
    section("🧪", "What is inside (the drugs)", ingredients) +
    section("🛡️", `Is it safe for age ${currentAge}? (${GROUP_TEXT[s.ageGroup] || s.ageGroup})`,
      `<span class="pill big ${esc(s.status)}">${STATUS_TEXT[s.status] || esc(s.status)}</span>
       <div>${esc(s.note)}</div>`) +
    section("⏱️", "How much is safe",
      `<div><b>Amount:</b> ${esc(s.amount)}</div>
       <div><b>Limit:</b> ${esc(s.dailyLimit)}</div>`) +
    section("📋", "How to use it", steps) +
    section("⚠️", "Possible side effects", effects) +
    warn;
}

function showMessage(text) {
  const box = el("result");
  box.className = "card muted";
  box.textContent = text;
}

// ---------- Start ----------
el("checkBtn").addEventListener("click", check);
el("name").addEventListener("keydown", (e) => { if (e.key === "Enter") check(); });
el("age").addEventListener("keydown", (e) => { if (e.key === "Enter") check(); });
loadSuggestions();
