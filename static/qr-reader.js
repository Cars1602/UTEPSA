import QrScanner from './vendor/qr-scanner.min.js';
const el=id=>document.getElementById(id);
let scanner=null,busy=false;
function stop(){scanner?.stop();el('camera-box').hidden=true;}
function accept(text){
 if(busy)return;
 if(typeof text!=='string'||!text.trim()||text.length>12000){el('qr-status').textContent='Contenido QR no válido o demasiado largo.';stop();return;}
 busy=true;stop();el('content').value=text;
 el('mode').value=/^(https?:\/\/|www\.)/i.test(text)||/https?:\/\//i.test(text)?'url':'wallet';
 el('mode').dispatchEvent(new Event('change'));
 el('qr-status').textContent='QR leído. Analizando sin abrir su contenido…';
 el('run').click();setTimeout(()=>{busy=false;el('qr-status').textContent='QR leído. Revisa el resultado.';},1500);
}
el('scan-camera').onclick=async()=>{
 busy=false;el('camera-box').hidden=false;el('qr-status').textContent='Apunta al QR y permite usar la cámara.';
 try{if(!scanner)scanner=new QrScanner(el('qr-video'),r=>accept(r.data),{preferredCamera:'environment',highlightScanRegion:true,returnDetailedScanResult:true});await scanner.start();}
 catch{stop();el('qr-status').textContent='No se pudo abrir la cámara. Revisa el permiso o usa Subir QR.';}
};
el('stop-camera').onclick=()=>{stop();el('qr-status').textContent='Cámara cerrada.';};
el('scan-photo').onclick=()=>el('qr-file').click();
el('qr-file').onchange=async()=>{
 stop();busy=false;const f=el('qr-file').files[0];if(!f)return;
 if(f.size>10*1024*1024){el('qr-status').textContent='Elige una imagen de menos de 10 MB.';return;}
 el('qr-status').textContent='Leyendo imagen…';
 try{const r=await QrScanner.scanImage(f,{returnDetailedScanResult:true});accept(r.data);}
 catch{el('qr-status').textContent='No se encontró un QR legible. Prueba una imagen más clara.';}
 finally{el('qr-file').value='';}
};
document.addEventListener('visibilitychange',()=>{if(document.hidden)stop();});
window.addEventListener('pagehide',()=>scanner?.destroy());
for(const a of document.querySelectorAll('[data-view]'))a.addEventListener('click',stop);
