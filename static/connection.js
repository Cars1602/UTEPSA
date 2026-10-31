document.getElementById('check-network').onclick=()=>{
 const c=navigator.connection||navigator.mozConnection||navigator.webkitConnection;
 let lines=[navigator.onLine?'El dispositivo informa conexión disponible.':'El dispositivo informa que está sin conexión.'];
 if(c?.type)lines.push('Medio informado: '+c.type+'.');
 if(c?.effectiveType)lines.push('Calidad estimada: '+c.effectiveType+'.');
 lines.push('El navegador no permite leer el nombre del Wi-Fi ni comprobar si es seguro.');
 document.getElementById('network-status').textContent=lines.join(' ');
};
