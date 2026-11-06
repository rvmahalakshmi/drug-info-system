// Reports page: asks the backend for the numbers and draws them.
const API = location.protocol === "file:" ? "http://localhost:8080/api" : "/api";
const el = (id) => document.getElementById(id);

function esc(text) {
  const d = document.createElement("div");
  d.textContent = String(text);
  return d.innerHTML;
}

// A list of bars. items = [{name, count, severity?}]
function bars(items, useSeverity) {
  if (!items.length) return '<div class="sec-note">No data yet.</div>';
  const max = Math.max(...items.map((i) => i.count));
  return items.map((i) => {
    const pct = Math.round((i.count / max) * 100);
    const cls = useSeverity ? "bar-" + i.severity : "";
    return `<div class="rrow">
      <div class="rlabel">${esc(i.name)}</div>
      <div class="rtrack"><div class="rbar ${cls}" style="width:${pct}%"></div></div>
      <div class="rcount">${i.count}</div>
    </div>`;
  }).join("");
}

function safetyTable(rows) {
  const groups = [["child", "Child (under 18)"], ["adult", "Adult (18 to 59)"], ["elderly", "Senior (60+)"]];
  const status = [["safe", "Safe"], ["careful", "Be careful"], ["avoid", "Avoid"]];
  const num = {};
  rows.forEach((r) => { num[r.ageGroup + "|" + r.status] = r.count; });

  let html = '<table class="tbl"><tr><th>Age group</th>' +
    status.map((s) => `<th>${s[1]}</th>`).join("") + "</tr>";
  groups.forEach((g) => {
    html += `<tr><td>${g[1]}</td>` + status.map((s) =>
      `<td><span class="pill ${s[0]}">${num[g[0] + "|" + s[0]] || 0}</span></td>`).join("") + "</tr>";
  });
  return html + "</table>";
}

async function load() {
  try {
    const res = await fetch(API + "/reports");
    if (!res.ok) throw new Error("bad response");
    const d = await res.json();
    el("banner").hidden = true;

    el("totals").innerHTML = [
      [d.totals.medicines, "Medicines"],
      [d.totals.ingredients, "Chemicals (ingredients)"],
      [d.totals.sideEffects, "Side effects"]
    ].map((t) => `<div class="stat"><div class="big">${t[0]}</div><div class="small">${t[1]}</div></div>`).join("");

    el("effects").innerHTML = bars(d.commonSideEffects, true);
    if (d.commonSideEffects.length) {
      const top = d.commonSideEffects[0];
      el("effectsNote").textContent =
        `${top.name} is the most common. It appears in ${top.count} medicines. Green is mild, yellow is medium, red is serious.`;
    }
    el("most").innerHTML = bars(d.mostSideEffects, false);
    el("ingredients").innerHTML = bars(d.medicinesPerIngredient, false);
    el("safety").innerHTML = safetyTable(d.safetyByAge);
  } catch (e) {
    el("banner").hidden = false;
    el("banner").textContent =
      "Cannot reach the backend. Please start it in VS Code (open DrugApplication.java and click Run), then refresh this page.";
  }
}
load();
