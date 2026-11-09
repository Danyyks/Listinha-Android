import h, time, json, urllib.request
def post_item(u,i,nome):
    body={"fields":{"name":{"stringValue":nome},"category":{"stringValue":"outros"},"price":{"doubleValue":1.0},"quantity":{"doubleValue":1.0},"bought":{"booleanValue":False},"createdAt":{"timestampValue":"2026-11-09T12:00:00Z"}}}
    urllib.request.urlopen(urllib.request.Request(f"http://127.0.0.1:8080/v1/projects/demo-listinha/databases/(default)/documents/users/{u}/items?documentId={i}",data=json.dumps(body).encode(),headers={"Authorization":"Bearer owner","Content-Type":"application/json"},method="POST")).read()
# estado atual: usuario teste@ com receita; adiciona item pela UI
h.abrir(); h.fechar_teclado(); h.add('ItemDoTeste'); h.fechar_teclado()
u=h.uid(); print('uid teste',u)
# sincronizacao mesmo usuario: grava direto no banco com app aberto
post_item(u,'ext1','Item gravado no banco'); time.sleep(3); print('sync item externo aparece na tela:', h.na_tela('Item gravado no banco'))
h.tocar('Receitas',espera=2); print('receitas listadas', [t for t in h.textos() if t.startswith('Bolo')][:1])
# apaga receita no banco -> some da grade
for r in h.receitas(): urllib.request.urlopen(urllib.request.Request(f"http://127.0.0.1:8080/v1/projects/demo-listinha/databases/(default)/documents/users/{u}/recipes/{r['id']}",headers={"Authorization":"Bearer owner"},method="DELETE")).read()
time.sleep(3); print('receita apagada no banco some da grade:', not any(t.startswith('Bolo') for t in h.textos()))
# sair do app
h.adb('logcat','-c'); h.tocar('Perfil',espera=2.5); h.adb('shell','input','swipe','540','1600','540','600','300'); time.sleep(1); h.tocar('Sair do app',espera=4); print('login:', h.na_tela('Continuar com Google'))
print('log erro listener:', [l[-90:] for l in h.adb('logcat','-d').splitlines() if 'Erro ao observar' in l][:2])
# outro usuario (processo novo)
h.reset('outro@listinha.test','Outro Silva'); time.sleep(5); t=h.textos(); print('OUTRO usuario ve lista vazia:', h.na_tela('Lista limpinha.') and not h.na_tela('ItemDoTeste'), t[:3])
h.tocar('Receitas',espera=2); print('OUTRO ve receitas vazias:', h.na_tela('Seu caderno de receitas'))
