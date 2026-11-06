// Admin page: list, add, edit and delete medicines.
const API = location.protocol === "file:" ? "http://localhost:8080/api" : "/api";
const GROUPS = [
  ["child", "Child (under 18)"],
  ["adult", "Adult (18 to 59)"],
  ["elderly", "Senior (60 and above)"]
];
const el = (id) => document.getElementById(id);
let editingId = null;
let allRows = [];

function esc(text) {
  const d = document.createElement("div");
  d.textContent = String(text);
  return d.innerHTML;
}

function showMsg(text, ok) {
  const m = el("msg");
  m.textContent = text;
  m.className = "msg " + (ok ? "ok" : "err");
  m.hidden = false;
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function backendDown() {
  el("banner").hidden = false;
  el("banner").textContent =
    "Cannot reach the backend. Please start it in VS Code (open DrugApplication.java and click Run), then refresh this page.";
}

// Every Admin call goes through here, so the admin key is always sent.
async function call(url, method, body) {
  const headers = { "X-Admin-Key": el("adminKey").value };
  if (body) headers["Content-Type"] = "application/json";
  const res = await fetch(url, {
    method: method,
    headers: headers,
    body: body ? JSON.stringify(body) : undefined
  });
  if (res.status === 401) throw new Error("KEY");
  return res;
}

function handleError(e) {
  if (e.message === "KEY") {
    el("list").textContent = "Type the correct admin key in the box above.";
    showMsg("Wrong or missing admin key. Type it in the Admin key box above.", false);
  } else {
    backendDown();
  }
}

// ---------- the 3 age group boxes ----------
function buildRules() {
  el("rules").innerHTML = GROUPS.map((g) => `
    <div class="rule">
      <div class="rt">${g[1]}</div>
      <select id="r-${g[0]}-status">
        <option value="safe">Safe</option>
        <option value="careful">Be careful</option>
        <option value="avoid">Avoid</option>
      </select>
      <input id="r-${g[0]}-note" placeholder="Safety note, e.g. Safe when taken as directed.">
      <input id="r-${g[0]}-amount" placeholder="Amount, e.g. 1 tablet at a time">
      <input id="r-${g[0]}-limit" placeholder="Daily limit, e.g. Up to 4 tablets in 24 hours">
    </div>`).join("");
}

// ---------- the list ----------
async function loadList() {
  try {
    const res = await call(API + "/admin/medicines", "GET");
    if (!res.ok) throw new Error("bad response");
    const rows = await res.json();
    allRows = rows;
    el("banner").hidden = true;

    if (!rows.length) {
      el("list").textContent = "No medicines yet.";
      return;
    }
    el("list").innerHTML = '<table class="tbl"><tr><th>Name</th><th>Used for</th><th></th></tr>' +
      rows.map((r) => `<tr>
        <td>${esc(r.name)}</td>
        <td>${esc(r.usedFor)}</td>
        <td class="act">
          <button class="small" data-act="edit" data-id="${r.id}">Edit</button>
          <button class="small del" data-act="del" data-id="${r.id}">Delete</button>
        </td></tr>`).join("") + "</table>";
  } catch (e) {
    handleError(e);
  }
}

el("list").addEventListener("click", (e) => {
  const b = e.target.closest("button[data-act]");
  if (!b) return;
  const id = b.dataset.id;
  if (b.dataset.act === "edit") startEdit(id);
  if (b.dataset.act === "del") {
    const row = allRows.find((r) => String(r.id) === String(id));
    removeMedicine(id, row ? row.name : "this medicine");
  }
});

// ---------- edit ----------
async function startEdit(id) {
  try {
    const res = await call(`${API}/admin/medicines/${id}`, "GET");
    if (!res.ok) throw new Error("bad response");
    const d = await res.json();

    editingId = id;
    el("formTitle").textContent = "Edit: " + d.name;
    el("f-name").value = d.name;
    el("f-used").value = d.usedFor;
    el("f-ing").value = d.ingredients.map((i) => `${i.name} | ${i.strength}`).join("\n");
    el("f-se").value = d.sideEffects.map((s) => `${s.name} | ${s.severity}`).join("\n");
    el("f-steps").value = d.howToUse.join("\n");

    GROUPS.forEach((g) => {
      const r = d.ageRules.find((x) => x.ageGroup === g[0]);
      el(`r-${g[0]}-status`).value = r ? r.status : "safe";
      el(`r-${g[0]}-note`).value = r ? r.note : "";
      el(`r-${g[0]}-amount`).value = r ? r.amount : "";
      el(`r-${g[0]}-limit`).value = r ? r.dailyLimit : "";
    });
    el("formCard").scrollIntoView({ behavior: "smooth" });
  } catch (e) {
    handleError(e);
  }
}

// ---------- delete ----------
async function removeMedicine(id, name) {
  if (!confirm(`Delete "${name}"? This cannot be undone.`)) return;
  try {
    const res = await call(`${API}/admin/medicines/${id}`, "DELETE");
    if (!res.ok) throw new Error("bad response");
    showMsg(`"${name}" was deleted.`, true);
    if (String(editingId) === String(id)) clearForm();
    loadList();
  } catch (e) {
    handleError(e);
  }
}

// ---------- save (add or edit) ----------
const lines = (text) => text.split("\n").map((l) => l.trim()).filter((l) => l);

async function save() {
  const body = {
    name: el("f-name").value.trim(),
    usedFor: el("f-used").value.trim(),
    ingredients: lines(el("f-ing").value).map((l) => {
      const p = l.split("|");
      return { name: p[0].trim(), strength: (p[1] || "").trim() };
    }),
    sideEffects: lines(el("f-se").value).map((l) => {
      const p = l.split("|");
      return { name: p[0].trim(), severity: (p[1] || "mild").trim().toLowerCase() };
    }),
    howToUse: lines(el("f-steps").value),
    ageRules: []
  };

  // An age group box that is completely empty is skipped
  GROUPS.forEach((g) => {
    const note = el(`r-${g[0]}-note`).value.trim();
    const amount = el(`r-${g[0]}-amount`).value.trim();
    const limit = el(`r-${g[0]}-limit`).value.trim();
    if (note || amount || limit) {
      body.ageRules.push({
        ageGroup: g[0],
        status: el(`r-${g[0]}-status`).value,
        note: note,
        amount: amount,
        dailyLimit: limit
      });
    }
  });

  const url = editingId ? `${API}/admin/medicines/${editingId}` : `${API}/admin/medicines`;
  try {
    const res = await call(url, editingId ? "PUT" : "POST", body);
    if (!res.ok) {
      let text = "Could not save. Please check the form.";
      try {
        const j = await res.json();
        if (j.message) text = j.message;
      } catch (e) { /* keep the default text */ }
      showMsg(text, false);
      return;
    }
    const j = await res.json();
    showMsg(`${j.message}: ${body.name}`, true);
    clearForm();
    loadList();
  } catch (e) {
    handleError(e);
  }
}

function clearForm() {
  editingId = null;
  el("formTitle").textContent = "Add a new medicine";
  ["f-name", "f-used", "f-ing", "f-se", "f-steps"].forEach((id) => { el(id).value = ""; });
  GROUPS.forEach((g) => {
    el(`r-${g[0]}-status`).value = "safe";
    el(`r-${g[0]}-note`).value = "";
    el(`r-${g[0]}-amount`).value = "";
    el(`r-${g[0]}-limit`).value = "";
  });
}

// ---------- start ----------
el("adminKey").value = sessionStorage.getItem("adminKey") || "";
el("adminKey").addEventListener("change", () => {
  sessionStorage.setItem("adminKey", el("adminKey").value);
  loadList();
});
buildRules();
el("saveBtn").addEventListener("click", save);
el("clearBtn").addEventListener("click", clearForm);
loadList();
