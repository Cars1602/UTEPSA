let presetsData = { url_presets: [], qr_wallet_presets: [], wifi_scenarios: [] };
let currentLesson = 1;
let completedLessons = 1;
let mobileShowingLessonDetail = false;

const LESSONS_DATA = [
  {
    index: 1,
    badge: "VISTA · EJEMPLO",
    duration: "2 min",
    title: "¿Qué es un QR?",
    subtitle: "Mira el destino antes de abrir.",
    icon: "qr",
  },
  {
    index: 2,
    badge: "SIGUIENTE",
    duration: "3 min",
    title: "Tu wallet, sin misterios",
    subtitle: "Qué compartir y qué guardar.",
    icon: "wallet",
  },
  {
    index: 3,
    badge: "EJERCICIO",
    duration: "3 min",
    title: "¿Detectas la estafa?",
    subtitle: "Practica con un premio ficticio.",
    icon: "chat",
  },
  {
    index: 4,
    badge: "GUÍA PRÁCTICA",
    duration: "4 min",
    title: "¿Sospechas una estafa?",
    subtitle: "Pasos para actuar con calma.",
    icon: "shield",
  },
];

document.addEventListener("DOMContentLoaded", async () => {
  renderLessonsRoute();
  openLesson(1, false);
  await loadPresets();
  await simulateWifi("mitm_arp");
  await analyzeQrWallet("wallet");
});

function switchBottomTab(tab) {
  ["inicio", "qr", "wallet", "proteccion"].forEach((t) => {
    document.getElementById(`tab-view-${t}`).classList.toggle("hidden", t !== tab);
    document.getElementById(`nav-${t}`).classList.toggle("active", t === tab);
  });

  if (tab === "inicio") {
    showMobileRouteList();
  } else {
    const titles = {
      qr: "Verificador de QR",
      wallet: "Tu Wallet Segura",
      proteccion: "Protección Wi-Fi y Enlaces",
    };
    document.getElementById("topBarTitle").textContent = titles[tab] || "Aprende a protegerte";
  }
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function showMobileRouteList() {
  mobileShowingLessonDetail = false;
  const routePanel = document.getElementById("mobileRoutePanel");
  const detailPanel = document.getElementById("lessonDetailCard");
  routePanel.classList.remove("hidden");
  detailPanel.classList.add("hidden");
  detailPanel.classList.add("lg:block");
  document.getElementById("topBarTitle").textContent = "Aprende a protegerte";
}

function handleBackClick() {
  if (mobileShowingLessonDetail) {
    showMobileRouteList();
    window.scrollTo({ top: 0, behavior: "smooth" });
  } else {
    switchBottomTab("inicio");
  }
}

function renderLessonsRoute() {
  const container = document.getElementById("lessonsListContainer");
  container.innerHTML = LESSONS_DATA.map((l) => {
    const isActive = l.index === currentLesson;
    const cardBg = isActive ? "bg-cardteal border-borderteal" : "bg-cardslate border-borderslate";
    const badgeColor = isActive ? "text-mint" : "text-slate-500";
    return `
      <div onclick="openLesson(${l.index}, true)" class="cursor-pointer rounded-2xl ${cardBg} border p-3.5 sm:p-4 flex items-center justify-between gap-3 hover:border-mint/60 active:scale-[0.99] transition">
        <div class="w-10 h-10 sm:w-11 sm:h-11 rounded-xl bg-[#0e2927] border border-borderteal flex items-center justify-center text-mint shrink-0 font-mono text-xs font-bold">
          ${l.index === 1 ? "[ - ]" : l.index === 2 ? "💳" : l.index === 3 ? "💬" : "🛡️"}
        </div>
        <div class="flex-1 min-w-0">
          <div class="flex items-center justify-between text-[10px] font-bold tracking-wider uppercase ${badgeColor}">
            <span class="truncate">${l.badge}</span>
            <span class="text-slate-500 font-normal lowercase shrink-0 ml-2">${l.duration}</span>
          </div>
          <h4 class="text-sm font-bold text-white mt-0.5 truncate">${l.title}</h4>
          <p class="text-xs text-slate-400 truncate">${l.subtitle}</p>
        </div>
        <span class="text-slate-400 text-lg shrink-0">›</span>
      </div>
    `;
  }).join("");

  const pct = Math.min(100, completedLessons * 25);
  document.getElementById("progressText").textContent = `${completedLessons} de 4 · ${pct} %`;
  const segs = document.getElementById("progressSegments");
  segs.innerHTML = [1, 2, 3, 4]
    .map(
      (i) =>
        `<div class="h-1.5 rounded-full ${
          i <= completedLessons ? "bg-mint" : "bg-[#1d3b38]"
        }"></div>`
    )
    .join("");
}

function openLesson(index, switchMobileView = false) {
  currentLesson = index;
  completedLessons = Math.max(completedLessons, index);
  renderLessonsRoute();

  const routePanel = document.getElementById("mobileRoutePanel");
  const card = document.getElementById("lessonDetailCard");

  if (switchMobileView && window.innerWidth < 1024) {
    mobileShowingLessonDetail = true;
    routePanel.classList.add("hidden", "lg:block");
    card.classList.remove("hidden");
    document.getElementById("topBarTitle").textContent = "Aprendizaje";
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  if (index === 1) {
    card.innerHTML = `
      <div>
        <span class="text-[10px] sm:text-[11px] font-bold uppercase tracking-widest text-mint">LECCIÓN 1 DE 4 · 2 MIN</span>
        <h2 class="text-2xl sm:text-3xl font-bold text-white mt-1.5">¿Qué es un QR?</h2>
        <p class="text-xs sm:text-sm text-slate-400 mt-2 leading-relaxed">
          Es un código que guarda información, como un texto o un enlace a una web.
        </p>
      </div>

      <!-- Escanear no es confiar -->
      <div class="rounded-2xl bg-cardteal border border-borderteal p-4 flex items-start gap-3">
        <div class="w-9 h-9 rounded-lg bg-[#0e2927] border border-borderteal flex items-center justify-center text-mint font-mono text-xs shrink-0 mt-0.5">
          [ - ]
        </div>
        <div class="min-w-0 flex-1">
          <h3 class="text-sm sm:text-base font-bold text-white">Escanear no es confiar</h3>
          <p class="text-xs text-slate-300 mt-1 leading-relaxed">
            Leer un QR no demuestra que su contenido sea seguro.
          </p>
        </div>
      </div>

      <!-- Ejemplo Ficticio menu-cafe.example -->
      <div class="rounded-2xl bg-cardslate border border-borderslate p-4 sm:p-5 space-y-3">
        <span class="text-[10px] font-bold uppercase tracking-widest text-slate-500 block">EJEMPLO FICTICIO · NO ESCANEABLE</span>
        <div class="flex items-center gap-3.5">
          <div class="w-14 h-14 sm:w-16 sm:h-16 rounded-2xl bg-[#1a2530] border border-[#2b3b4b] flex items-center justify-center font-mono text-slate-300 text-xs sm:text-sm leading-tight text-center shrink-0">
            回 ▪<br/>▪ 回
          </div>
          <div class="min-w-0 flex-1">
            <span class="text-xs text-slate-400 block">El QR podría llevarte a</span>
            <span class="text-sm sm:text-base font-bold font-mono text-white block mt-0.5 truncate">menu-cafe.example</span>
            <span class="text-xs font-mono text-slate-400">/carta</span>
          </div>
        </div>
        <p class="text-xs text-slate-400 leading-relaxed">
          El dominio es el nombre de la web: aquí, <code class="text-slate-200">menu-cafe.example</code>. Que parezca conocido no basta.
        </p>
      </div>

      <!-- Antes de abrir, haz una pausa -->
      <div class="space-y-3.5">
        <h3 class="text-sm sm:text-base font-bold text-white">Antes de abrir, haz una pausa</h3>

        <div class="flex items-start gap-3">
          <span class="w-6 h-6 sm:w-7 sm:h-7 rounded-full bg-[#0e2927] border border-borderteal text-mint text-xs font-bold flex items-center justify-center shrink-0 mt-0.5">1</span>
          <div class="min-w-0 flex-1">
            <h4 class="text-sm font-bold text-white">Revisa el dominio</h4>
            <p class="text-xs text-slate-400 mt-0.5 leading-relaxed">Mira la vista previa antes de abrir. Busca letras cambiadas o un nombre que no coincida.</p>
          </div>
        </div>

        <div class="flex items-start gap-3">
          <span class="w-6 h-6 sm:w-7 sm:h-7 rounded-full bg-[#0e2927] border border-borderteal text-mint text-xs font-bold flex items-center justify-center shrink-0 mt-0.5">2</span>
          <div class="min-w-0 flex-1">
            <h4 class="text-sm font-bold text-white">Mira también el QR físico</h4>
            <p class="text-xs text-slate-400 mt-0.5 leading-relaxed">¿Tiene una pegatina encima? Podrían haber sustituido el código original.</p>
          </div>
        </div>

        <div class="flex items-start gap-3">
          <span class="w-6 h-6 sm:w-7 sm:h-7 rounded-full bg-[#0e2927] border border-borderteal text-mint text-xs font-bold flex items-center justify-center shrink-0 mt-0.5">3</span>
          <div class="min-w-0 flex-1">
            <h4 class="text-sm font-bold text-white">Confirma por otra vía</h4>
            <p class="text-xs text-slate-400 mt-0.5 leading-relaxed">Pregunta al local o entra por su app o web oficial, sin usar el enlace del QR.</p>
          </div>
        </div>
      </div>

      <!-- Si no te cuadra, no lo abras -->
      <div class="rounded-2xl bg-cardslate border border-borderslate p-4">
        <h4 class="text-sm font-bold text-white">Si no te cuadra, no lo abras</h4>
        <p class="text-xs text-slate-400 mt-1 leading-relaxed">
          No pasa nada por detenerte y pedir ayuda. La prisa no tiene que decidir por ti.
        </p>
      </div>

      <div class="space-y-2.5 pt-1">
        <div class="text-center text-xs text-slate-400">Siguiente · Tu wallet, sin misterios</div>
        <button onclick="openLesson(2, true)" class="w-full min-h-[48px] py-3.5 rounded-2xl bg-mint hover:bg-[#3cd1a3] active:scale-[0.99] text-mintdark font-bold text-sm transition">
          Continuar &nbsp; →
        </button>
        <button onclick="showMobileRouteList()" class="w-full py-1.5 text-center text-xs font-semibold text-mint hover:underline">
          Volver al aprendizaje
        </button>
      </div>
    `;
  } else if (index === 2) {
    card.innerHTML = `
      <div>
        <span class="text-[10px] sm:text-[11px] font-bold uppercase tracking-widest text-mint">LECCIÓN 2 DE 4 · 3 MIN</span>
        <h2 class="text-2xl sm:text-3xl font-bold text-white mt-1.5">Tu wallet, sin misterios</h2>
        <p class="text-xs sm:text-sm text-slate-400 mt-2 leading-relaxed">
          Qué puedes compartir con tranquilidad y qué datos nunca debes entregar ni firmar.
        </p>
      </div>

      <div class="rounded-2xl bg-cardteal border border-borderteal p-4 sm:p-5 space-y-2">
        <span class="text-xs font-bold text-mint uppercase">Lo que SÍ puedes compartir</span>
        <h3 class="text-sm sm:text-base font-bold text-white">Tu Dirección Pública (0x71C8...89A2)</h3>
        <p class="text-xs text-slate-300 leading-relaxed">
          Funciona como tu número de cuenta para recibir transferencias. Aun así, al copiarla revisa también los caracteres del centro para evitar el <strong>Address Poisoning</strong>.
        </p>
      </div>

      <div class="rounded-2xl bg-cardslate border border-rose-500/40 p-4 sm:p-5 space-y-3">
        <span class="text-xs font-bold text-rose-400 uppercase">Lo que NUNCA debes compartir ni aprobar</span>
        <div class="space-y-2 text-xs text-slate-300">
          <p>• <strong>Tu frase semilla (12 o 24 palabras):</strong> Quien la tiene es dueño total de tus fondos.</p>
          <p>• <strong>Permisos 'Approve Ilimitado' en un QR:</strong> Para recibir un pago o premio jamás debes firmar un permiso de retiro.</p>
        </div>
      </div>

      <div class="space-y-2.5 pt-2">
        <div class="text-center text-xs text-slate-400">Siguiente · ¿Detectas la estafa?</div>
        <button onclick="openLesson(3, true)" class="w-full min-h-[48px] py-3.5 rounded-2xl bg-mint hover:bg-[#3cd1a3] active:scale-[0.99] text-mintdark font-bold text-sm transition">
          Continuar &nbsp; →
        </button>
        <button onclick="showMobileRouteList()" class="w-full py-1.5 text-center text-xs font-semibold text-mint hover:underline">
          Volver al aprendizaje
        </button>
      </div>
    `;
  } else if (index === 3) {
    card.innerHTML = `
      <div>
        <span class="text-[10px] sm:text-[11px] font-bold uppercase tracking-widest text-mint">LECCIÓN 3 DE 4 · 3 MIN · EJERCICIO</span>
        <h2 class="text-2xl sm:text-3xl font-bold text-white mt-1.5">¿Detectas la estafa?</h2>
        <p class="text-xs sm:text-sm text-slate-400 mt-2 leading-relaxed">
          Observa este mensaje ficticio y elige qué señal revela el engaño:
        </p>
      </div>

      <div class="rounded-2xl bg-cardslate border border-amber-500/40 p-4 sm:p-5 space-y-2">
        <span class="text-[10px] font-bold uppercase text-amber-400">MENSAJE DE PRÁCTICA</span>
        <p class="text-xs sm:text-sm text-white font-mono leading-relaxed break-words">
          "¡Últimas 2 horas! Reclama tu bono universitario de $500 escaneando este QR y aprobando el permiso de tu billetera en: http://bono-utepsa-verificar.xyz/claim"
        </p>
      </div>

      <div class="space-y-2.5">
        <button onclick="document.getElementById('quizFeedback').innerHTML='<span class=\\'text-mint font-bold\\'>✓ ¡Correcto!</span> La prisa artificial + dominio extraño (.xyz) + pedir firma para recibir dinero es una estafa clásica.'" class="w-full text-left p-3.5 sm:p-4 rounded-2xl bg-cardslate hover:bg-cardteal border border-borderslate hover:border-mint text-xs text-white transition">
          <strong>Opción A:</strong> Mete prisa ("Últimas 2 horas"), usa un dominio no oficial (<code>.xyz</code>) y pide aprobar un permiso para supuestamente recibir dinero.
        </button>
        <button onclick="document.getElementById('quizFeedback').innerHTML='<span class=\\'text-amber-400 font-bold\\'>⚠️ Observa el dominio:</span> Cualquier atacante puede poner el nombre de tu universidad o banco como texto, pero el dominio real termina en .xyz.'" class="w-full text-left p-3.5 sm:p-4 rounded-2xl bg-cardslate border border-borderslate text-xs text-white transition">
          <strong>Opción B:</strong> Es confiable porque menciona el bono universitario en el enlace.
        </button>
        <div id="quizFeedback" class="text-xs text-slate-300 pt-1"></div>
      </div>

      <div class="space-y-2.5 pt-2">
        <div class="text-center text-xs text-slate-400">Siguiente · ¿Sospechas una estafa?</div>
        <button onclick="openLesson(4, true)" class="w-full min-h-[48px] py-3.5 rounded-2xl bg-mint hover:bg-[#3cd1a3] active:scale-[0.99] text-mintdark font-bold text-sm transition">
          Continuar &nbsp; →
        </button>
        <button onclick="showMobileRouteList()" class="w-full py-1.5 text-center text-xs font-semibold text-mint hover:underline">
          Volver al aprendizaje
        </button>
      </div>
    `;
  } else {
    card.innerHTML = `
      <div>
        <span class="text-[10px] sm:text-[11px] font-bold uppercase tracking-widest text-mint">LECCIÓN 4 DE 4 · 4 MIN · GUÍA PRÁCTICA</span>
        <h2 class="text-2xl sm:text-3xl font-bold text-white mt-1.5">¿Sospechas una estafa?</h2>
        <p class="text-xs sm:text-sm text-slate-400 mt-2 leading-relaxed">
          Pasos claros para actuar con calma si abriste un QR dudoso o te conectaste a un Wi-Fi intervenido.
        </p>
      </div>

      <div class="space-y-4">
        <div class="flex items-start gap-3">
          <span class="w-6 h-6 sm:w-7 sm:h-7 rounded-full bg-[#0e2927] border border-borderteal text-mint text-xs font-bold flex items-center justify-center shrink-0">1</span>
          <div class="min-w-0 flex-1">
            <h4 class="text-sm font-bold text-white">Corta la conexión Wi-Fi sospechosa</h4>
            <p class="text-xs text-slate-400 mt-0.5">Si estás en una red pública abierta, desconéctate para detener cualquier lectura de tráfico (MITM).</p>
          </div>
        </div>
        <div class="flex items-start gap-3">
          <span class="w-6 h-6 sm:w-7 sm:h-7 rounded-full bg-[#0e2927] border border-borderteal text-mint text-xs font-bold flex items-center justify-center shrink-0">2</span>
          <div class="min-w-0 flex-1">
            <h4 class="text-sm font-bold text-white">Verifica el enlace, QR o Wallet en esta herramienta</h4>
            <p class="text-xs text-slate-400 mt-0.5">Revisa si solicitó un permiso 'approve' o si la dirección fue suplantada.</p>
          </div>
        </div>
        <div class="flex items-start gap-3">
          <span class="w-6 h-6 sm:w-7 sm:h-7 rounded-full bg-[#0e2927] border border-borderteal text-mint text-xs font-bold flex items-center justify-center shrink-0">3</span>
          <div class="min-w-0 flex-1">
            <h4 class="text-sm font-bold text-white">Cierra sesiones vinculadas y avisa a tu entidad</h4>
            <p class="text-xs text-slate-400 mt-0.5">Revisa 'Dispositivos vinculados' en tu mensajería y cambia tu contraseña desde una red segura.</p>
          </div>
        </div>
      </div>

      <div class="space-y-2.5 pt-2">
        <button onclick="switchBottomTab('qr')" class="w-full min-h-[48px] py-3.5 rounded-2xl bg-mint hover:bg-[#3cd1a3] active:scale-[0.99] text-mintdark font-bold text-sm transition">
          Probar el Verificador de QR y Wallets &nbsp; →
        </button>
        <button onclick="showMobileRouteList()" class="w-full py-1.5 text-center text-xs font-semibold text-mint hover:underline">
          Volver al aprendizaje
        </button>
      </div>
    `;
  }
}

async function loadPresets() {
  const res = await fetch("/api/presets");
  presetsData = await res.json();

  const qrContainer = document.getElementById("qrPresetsContainer");
  qrContainer.innerHTML = (presetsData.qr_wallet_presets || [])
    .map(
      (q) => `
      <div onclick="selectQrPreset('${q.id}')" class="shrink-0 w-60 sm:w-auto cursor-pointer rounded-2xl bg-cardslate hover:bg-cardteal border border-borderslate hover:border-mint p-3.5 transition flex flex-col justify-between">
        <div>
          <span class="text-[10px] font-bold uppercase text-mint block mb-1">${q.badge}</span>
          <h4 class="text-xs font-bold text-white leading-snug">${q.title}</h4>
        </div>
        <span class="text-[11px] text-mint font-semibold mt-2 block">Cargar ejemplo →</span>
      </div>
    `
    )
    .join("");

  const urlContainer = document.getElementById("urlPresetsContainer");
  urlContainer.innerHTML = presetsData.url_presets
    .map(
      (p) => `
      <div onclick="selectUrlPreset('${p.id}')" class="shrink-0 w-56 sm:w-auto cursor-pointer rounded-2xl bg-cardslate hover:bg-cardteal border border-borderslate hover:border-mint p-3 transition">
        <span class="text-[10px] font-bold uppercase text-mint block mb-1">${p.badge}</span>
        <h4 class="text-xs font-bold text-white leading-snug">${p.title}</h4>
      </div>
    `
    )
    .join("");

  const wifiContainer = document.getElementById("wifiScenariosContainer");
  wifiContainer.innerHTML = presetsData.wifi_scenarios
    .map(
      (w) => `
      <div onclick="simulateWifi('${w.id}')" class="shrink-0 w-60 sm:w-auto cursor-pointer rounded-2xl bg-cardslate hover:bg-cardteal border border-borderslate hover:border-mint p-3.5 transition">
        <span class="text-[10px] font-bold uppercase ${w.risk_score > 50 ? "text-rose-400" : "text-mint"}">${w.badge}</span>
        <h4 class="text-xs font-bold text-white mt-1 leading-snug">${w.title}</h4>
      </div>
    `
    )
    .join("");

  if ((presetsData.qr_wallet_presets || []).length > 0) {
    selectQrPreset(presetsData.qr_wallet_presets[0].id);
  }
  if (presetsData.url_presets.length > 0) {
    selectUrlPreset(presetsData.url_presets[0].id);
  }
}

function selectQrPreset(id) {
  const item = (presetsData.qr_wallet_presets || []).find((x) => x.id === id);
  if (!item) return;
  document.getElementById("qrPayloadInput").value = item.payload;
  analyzeQrWallet("qr");
}

function loadWalletSample(type) {
  const wInput = document.getElementById("walletAddressInput");
  if (type === "poison") {
    wInput.value = "0x71C80000009999888877776666555500000089A2";
  } else if (type === "usdt") {
    wInput.value = "TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE";
  } else {
    wInput.value = "0x71C845aB912cF40192837465aBcDeF12345689A2";
  }
  analyzeQrWallet("wallet");
}

async function analyzeQrWallet(mode) {
  const payload =
    mode === "wallet"
      ? document.getElementById("walletAddressInput").value
      : document.getElementById("qrPayloadInput").value;
  const trusted_wallet = document.getElementById("trustedWalletInput").value;

  const res = await fetch("/api/analyze-qr-wallet", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ payload, trusted_wallet }),
  });
  const data = await res.json();
  const targetBox = document.getElementById(mode === "wallet" ? "walletResultBox" : "qrResultBox");

  const isDanger = data.risk_score >= 60;
  const poisonHtml = data.poisoning_comparison
    ? `
      <div class="p-3.5 sm:p-4 rounded-2xl bg-rose-950/30 border border-rose-500/50 space-y-2">
        <div class="text-xs font-bold uppercase text-rose-300">🚨 Alerta Visual de Address Poisoning:</div>
        <div class="font-mono text-[11px] bg-obsidian p-3 rounded-xl border border-borderslate overflow-x-auto space-y-1.5">
          <div class="whitespace-nowrap"><span class="text-slate-400">Confiable: </span><span class="text-mint font-bold">${data.poisoning_comparison.matched_prefix}</span><span class="text-slate-300">${data.poisoning_comparison.trusted_wallet.slice(6, -4)}</span><span class="text-mint font-bold">${data.poisoning_comparison.matched_suffix}</span></div>
          <div class="whitespace-nowrap"><span class="text-slate-400">Copiada:   </span><span class="text-mint font-bold">${data.poisoning_comparison.matched_prefix}</span><span class="bg-rose-500/30 text-rose-300 font-bold px-1 rounded">${data.poisoning_comparison.fake_middle}</span><span class="text-mint font-bold">${data.poisoning_comparison.matched_suffix}</span></div>
        </div>
      </div>
    `
    : "";

  targetBox.innerHTML = `
    <div class="rounded-2xl ${isDanger ? "bg-rose-950/25 border-rose-500/50" : "bg-cardteal border-borderteal"} border p-4 sm:p-5 flex items-center justify-between gap-3">
      <div class="space-y-1 min-w-0 flex-1">
        <span class="text-[10px] font-bold uppercase tracking-wider text-mint block truncate">${data.detected_type} · ${data.network_or_bank}</span>
        <h3 class="text-base sm:text-lg font-bold ${isDanger ? "text-rose-400" : "text-white"}">${data.verdict}</h3>
        <p class="text-xs text-slate-300">${data.recommendation}</p>
      </div>
      <div class="text-right shrink-0">
        <span class="text-[10px] uppercase text-slate-400 block">Riesgo</span>
        <span class="text-xl sm:text-2xl font-black ${isDanger ? "text-rose-400" : "text-mint"}">${data.risk_score}/100</span>
      </div>
    </div>

    ${poisonHtml}

    <div class="grid grid-cols-1 md:grid-cols-3 gap-2.5 text-xs">
      <div class="p-3.5 rounded-2xl bg-cardslate border border-borderslate">
        <span class="text-slate-400 block">Destino / Cuenta:</span>
        <span class="font-mono text-white font-bold break-all">${data.destination}</span>
      </div>
      <div class="p-3.5 rounded-2xl bg-cardslate border border-borderslate">
        <span class="text-slate-400 block">Monto / Alcance:</span>
        <span class="font-mono text-mint font-bold break-words">${data.amount_detected}</span>
      </div>
      <div class="p-3.5 rounded-2xl bg-cardslate border border-borderslate">
        <span class="text-slate-400 block">Acción / Función:</span>
        <span class="font-mono text-white font-bold break-words">${data.action_function}</span>
      </div>
    </div>

    <div class="space-y-2.5">
      ${data.indicators
        .map(
          (i) => `
          <div class="p-3.5 sm:p-4 rounded-2xl bg-cardslate border border-rose-500/40 space-y-1">
            <div class="flex justify-between gap-2">
              <span class="text-xs sm:text-sm font-bold text-white">${i.title}</span>
              <span class="text-[10px] font-mono text-mint shrink-0">${i.mitre}</span>
            </div>
            <div class="text-[11px] font-mono text-amber-300 break-all">Evidencia: ${i.evidence}</div>
            <p class="text-xs text-slate-400">${i.explanation}</p>
          </div>
        `
        )
        .join("")}
    </div>
  `;
}

function selectUrlPreset(id) {
  const item = presetsData.url_presets.find((x) => x.id === id);
  if (!item) return;
  document.getElementById("urlInput").value = item.text;
  analyzeUrl();
}

async function analyzeUrl() {
  const text = document.getElementById("urlInput").value;
  const res = await fetch("/api/analyze-url", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ text }),
  });
  const data = await res.json();
  const isDanger = data.risk_score >= 60;

  document.getElementById("urlResultContainer").innerHTML = `
    <div class="rounded-2xl ${isDanger ? "bg-rose-950/25 border-rose-500/50" : "bg-cardteal border-borderteal"} border p-4 flex items-center justify-between gap-3">
      <div class="min-w-0 flex-1">
        <span class="text-xs font-bold ${isDanger ? "text-rose-400" : "text-mint"} block">${data.verdict}</span>
        <p class="text-xs text-slate-300 font-mono mt-0.5 break-all">${data.extracted_url}</p>
      </div>
      <span class="text-xl font-black ${isDanger ? "text-rose-400" : "text-mint"} shrink-0">${data.risk_score}/100</span>
    </div>
  `;
}

async function scanRealWifi() {
  const res = await fetch("/api/wifi/scan-real");
  const data = await res.json();
  renderWifi(data);
}

async function simulateWifi(scenario_id) {
  const res = await fetch("/api/wifi/simulate", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ scenario_id }),
  });
  const data = await res.json();
  renderWifi(data);
}

function renderWifi(data) {
  const isDanger = data.risk_score >= 50;
  const w = data.wifi_info;
  document.getElementById("wifiResultContainer").innerHTML = `
    <div class="rounded-2xl ${isDanger ? "bg-rose-950/25 border-rose-500/50" : "bg-cardteal border-borderteal"} border p-4 sm:p-5 space-y-3">
      <div class="flex items-center justify-between gap-2">
        <div class="min-w-0 flex-1">
          <span class="text-[10px] font-bold uppercase text-mint block">${data.mode}</span>
          <h4 class="text-sm sm:text-base font-bold text-white break-words">${data.status} — SSID: <span class="font-mono text-mint">${w.ssid}</span></h4>
        </div>
        <span class="text-xl sm:text-2xl font-black ${isDanger ? "text-rose-400" : "text-mint"} shrink-0">${data.risk_score}/100</span>
      </div>
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-2 text-xs font-mono text-slate-300">
        <div class="truncate">BSSID: ${w.bssid}</div>
        <div class="truncate">Cifrado: ${w.cipher}</div>
        <div class="truncate">Gateway: ${w.gateway}</div>
        <div class="truncate">DNS: ${(w.dns_servers || []).join(", ")}</div>
      </div>
      <div class="space-y-2">
        ${(data.threats || [])
          .map(
            (t) => `
            <div class="p-3 rounded-xl bg-obsidian border border-rose-500/40 text-xs">
              <strong class="text-rose-400 block">${t.type} (${t.mitre})</strong>
              <span class="text-amber-300 font-mono break-all">${t.evidence}</span>
              <p class="text-slate-400 mt-1">${t.explanation}</p>
            </div>
          `
          )
          .join("")}
      </div>
    </div>
  `;
}
