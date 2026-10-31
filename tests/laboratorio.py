"""Casos de laboratorio: no visita los enlaces analizados ni envia alertas reales."""
import json
from pathlib import Path
from unittest.mock import patch, Mock
from app import app

CASES = [
 ('Suplantacion con @','url',{'text':'https://accounts.google.com@192.0.2.10/login'},True),
 ('Urgencia y dominio falso','url',{'text':'URGENTE: tu cuenta sera bloqueada. Verifica tu clave en http://bancounion-seguridad-bo.xyz/login'},True),
 ('Descarga APK falsa','url',{'text':'Ganaste una beca: instala http://becas-utepsa-bolivia.top/bono.apk'},True),
 ('Microsoft con cero','url',{'text':'Tu contraseña expira: https://login-micros0ft-office365.support/verify'},True),
 ('Portal universidad','url',{'text':'https://www.utepsa.edu/v2/'},False),
 ('Documentacion Microsoft','url',{'text':'https://learn.microsoft.com/es-es/security/'},False),
 ('GitHub OWASP','url',{'text':'https://github.com/OWASP/Top10'},False),
 ('Mensaje sin enlace','url',{'text':'Hola, nos vemos mañana en la universidad.'},False),
 ('Permiso ilimitado','wallet',{'payload':'ethereum:0x1111111111111111111111111111111111111111@1/approve?spender=0x2222222222222222222222222222222222222222&uint256=UNLIMITED'},True),
 ('Direccion envenenada','wallet',{'payload':'0x71C80000009999888877776666555500000089A2','trusted_wallet':'0x71C845aB912cF40192837465aBcDeF12345689A2'},True),
 ('Direccion coincidente','wallet',{'payload':'0x71C845aB912cF40192837465aBcDeF12345689A2','trusted_wallet':'0x71C845aB912cF40192837465aBcDeF12345689A2'},False),
 ('Vinculacion WhatsApp legitima posible','wallet',{'payload':'whatsapp://link-device?token=DEMO'},False),
 ('Approve limitado no necesariamente fraude','wallet',{'payload':'ethereum:0x1111111111111111111111111111111111111111@1/approve?uint256=1'},False),
 ('Direccion distinta a referencia','wallet',{'payload':'0x2222222222222222222222222222222222222222','trusted_wallet':'0x1111111111111111111111111111111111111111'},False),
]

def run():
 rows=[]
 with app.test_client() as c, patch.dict('os.environ',{'FARO_N8N_WEBHOOK':'','FARO_N8N_TOKEN':''}):
  for title,kind,data,expected in CASES:
   path='/api/analyze-'+('url' if kind=='url' else 'qr-wallet')
   r=c.post(path,json=data);d=r.get_json();score=d.get('risk_score',0)
   rows.append({'caso':title,'tipo':kind,'esperado_alerta':expected,'riesgo':score,'alerta_60':score>=60,'coincide':(score>=60)==expected and (score>=25 if title=='Direccion distinta a referencia' else True),'http':r.status_code,'veredicto':d.get('verdict'),'entrada':data})
  for title,data in [('vacio',{}),('tipo incorrecto',{'text':123})]:
   r=c.post('/api/analyze-url',json=data);rows.append({'caso':title,'http':r.status_code,'validacion_correcta':r.status_code==400})
 with app.test_client() as c,patch.dict('os.environ',{'FARO_N8N_WEBHOOK':'https://example.test/hook','FARO_N8N_TOKEN':'demo'}),patch('requests.post',return_value=Mock(status_code=200)) as send:
  d=c.post('/api/analyze-url',json=CASES[0][2]).get_json()
  rows.append({'caso':'Webhook simulado','estado':d.get('notification'),'correcto':d.get('notification')=='accepted' and set(send.call_args.kwargs['json'])=={'risk_score','category'}})
 return rows
if __name__=='__main__':
 import logging
 logging.disable(logging.CRITICAL)
 rows=run();Path('docs/pruebas-resultados.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding='utf-8')
 for r in rows: print(r['caso'],r.get('riesgo',''),r.get('coincide',r.get('validacion_correcta',r.get('correcto'))))
