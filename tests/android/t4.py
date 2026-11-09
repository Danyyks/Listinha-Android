import h, time, json, urllib.request
h.abrir(); h.fechar_teclado(); u=h.uid(); print('uid',u)
nomes=['Feijão tropeiro','Bolo de cenoura','Pão de queijo','Açaí na tigela','Strogonoff de frango','Bolo de chocolate']
for i,n in enumerate(nomes):
    body={"fields":{"name":{"stringValue":n},"servings":{"integerValue":"4"},"minutes":{"integerValue":"30"},"iconIndex":{"integerValue":"0"},"photo":{"stringValue":""},"ingredients":{"arrayValue":{"values":[]}},"steps":{"arrayValue":{"values":[]}},"sourceUrl":{"stringValue":""},"createdAt":{"timestampValue":f"2026-11-09T12:00:0{i}Z"}}}
    urllib.request.urlopen(urllib.request.Request(f"http://127.0.0.1:8080/v1/projects/demo-listinha/databases/(default)/documents/users/{u}/recipes?documentId=s{i}",data=json.dumps(body).encode(),headers={"Authorization":"Bearer owner","Content-Type":"application/json"},method="POST")).read()
time.sleep(3)
h.tocar('Receitas',espera=2); print('barra de busca com varias receitas:', h.na_tela('Buscar receita'))
def vis(): return [t for t in h.textos() if t in nomes]
h.tocar('Buscar receita',espera=1); h.digitar('feijao'); print('feijao ->',vis())
h.limpar_campo(8); h.digitar('bolo'); print('bolo ->',vis())
h.limpar_campo(8); h.digitar('ACAI'); print('ACAI ->',vis())
h.limpar_campo(8); h.digitar('zzz'); print('zzz ->',vis(), [t for t in h.textos() if 'ncontr' in t or 'nenhuma' in t.lower()][:2])
h.limpar_campo(8); print('limpo ->',len(vis()))
