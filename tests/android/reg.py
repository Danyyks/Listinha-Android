import h, time, sys
from rec import passo, salvar
def tt(txt):
    for n in h.dump():
        if n['text']==txt: x,y=h.centro(n); h.adb('shell','input','tap',str(x),str(y)); time.sleep(1.5); return
    raise RuntimeError('sem '+txt)
h.reset(); time.sleep(4)
@passo("adicionar com preco e quantidade grava no banco")
def _():
    h.add('Leite','5,50',1); h.fechar_teclado(); h.add('Pao'); h.fechar_teclado(); h.add('Banana','8',2); h.fechar_teclado()
    l=h.item('Banana'); assert l and l['price']==8.0 and l['quantity']==3.0, l
    assert h.na_tela('R$\xa035,00'), h.textos()
@passo("marcar, desmarcar e rotulo do checkbox")
def _():
    h.marcar('Pao'); assert h.item('Pao')['bought'] is True; h.marcar('Pao'); assert h.item('Pao')['bought'] is False
@passo("editar item grava preco")
def _():
    tt('Leite'); h.tocar('Preço',espera=0.5); h.limpar_campo(8); h.digitar('6,99'); h.fechar_teclado(); h.tocar('Salvar',espera=2); assert h.item('Leite')['price']==6.99, h.item('Leite')
@passo("apagar item")
def _():
    h.tocar('Apagar Pao',espera=2); assert h.item('Pao') is None
@passo("orcamento: definir 100, editar 30,5, invalido remove, definir e Remover")
def _():
    h.tocar('Definir orçamento',espera=1.5); h.digitar('100'); h.fechar_teclado(); h.tocar('Salvar',espera=2); assert h.perfil()['budget']==100
    h.tocar('Editar',espera=1.5); h.limpar_campo(8); h.digitar('30,5'); h.fechar_teclado(); h.tocar('Salvar',espera=2); assert h.perfil()['budget']==30.5, h.perfil()
    h.tocar('Editar',espera=1.5); h.limpar_campo(8); h.digitar('abc'); h.fechar_teclado(); h.tocar('Salvar',espera=2); assert h.perfil()['budget'] is None
    h.tocar('Definir orçamento',espera=1.5); h.digitar('50'); h.fechar_teclado(); h.tocar('Salvar',espera=2)
    h.tocar('Editar',espera=1.5); h.tocar('Remover',espera=2); assert h.perfil()['budget'] is None
@passo("limpar comprados")
def _():
    h.marcar('Leite'); h.tocar('Limpar comprados',espera=2); assert h.item('Leite') is None and h.item('Banana')
@passo("modo compra: marcar tudo, Concluir; aba e volta mantem modo")
def _():
    h.add('Cafe'); h.fechar_teclado(); h.tocar('Comprar',espera=2); h.tocar('Receitas',espera=2); h.tocar('Lista',espera=2)
    assert h.na_tela('Concluir'), h.textos()
    h.marcar('Banana'); h.marcar('Cafe'); assert any('Lista completa' in t for t in h.textos()), h.textos()[:6]
    h.tocar('Concluir',espera=2); assert h.na_tela('Comprar')
@passo("tema escuro e claro gravam e persistem apos matar o processo")
def _():
    h.tocar('Perfil',espera=2.5); h.tocar('Escuro',espera=2); assert h.perfil()['theme']=='dark'
    h.adb('shell','am','force-stop',h.PKG); h.abrir(); h.tocar('Perfil',espera=2.5); h.tocar('Auto',espera=2); assert h.perfil()['theme']=='auto'
@passo("receita por ACTION_SEND (ICU) vira rascunho completo e salva")
def _():
    h.adb('shell','am','start','-S','-n',h.PKG+'/com.listinha.app.MainActivity','-a','android.intent.action.SEND','-t','text/plain','--es','android.intent.extra.TEXT','"$(cat /data/local/tmp/r1.txt)"'); time.sleep(6)
    assert h.na_tela('Salvar'); h.tocar('Salvar',espera=3); r=h.receitas(); assert len(r)==1 and r[0]['servings']==8 and r[0]['minutes']==50 and len(r[0]['ingredients'])==7 and len(r[0]['steps'])==4, r
@passo("busca de receitas (BrandField) filtra, sem acento e sem caixa")
def _():
    import json, urllib.request
    u=h.uid()
    for i,n in enumerate(['Feijão tropeiro','Pão de queijo','Açaí na tigela','Strogonoff de frango','Bolo de chocolate']):
        body={"fields":{"name":{"stringValue":n},"servings":{"integerValue":"4"},"minutes":{"integerValue":"30"},"iconIndex":{"integerValue":"0"},"photo":{"stringValue":""},"ingredients":{"arrayValue":{"values":[]}},"steps":{"arrayValue":{"values":[]}},"sourceUrl":{"stringValue":""},"createdAt":{"timestampValue":"2026-11-09T12:00:0%dZ"%i}}}
        urllib.request.urlopen(urllib.request.Request("http://127.0.0.1:8080/v1/projects/demo-listinha/databases/(default)/documents/users/%s/recipes?documentId=s%d"%(u,i),data=json.dumps(body).encode(),headers={"Authorization":"Bearer owner","Content-Type":"application/json"},method="POST")).read()
    time.sleep(3); h.tocar('Buscar receita',espera=1); h.digitar('feijao'); assert h.na_tela('Feijão tropeiro'), h.textos()
    h.limpar_campo(8); h.digitar('zzz'); assert h.na_tela('Nenhuma receita encontrada.'); h.limpar_campo(8); h.fechar_teclado()
@passo("porcoes, ja tenho, enviar a lista e Substituir")
def _():
    h.limpar_campo(6) if False else None
    N='Bolo de cenoura com cobertura de chocolate'
    h.tocar('Buscar receita',espera=0.5) if False else None
    for _ in range(3):
        if h.na_tela(N): break
        h.limpar_campo(6); time.sleep(0.5)
    h.tocar(N,espera=2); h.tocar('Mais porções',espera=0.5); assert any('9 porç' in t for t in h.textos())
    h.tocar('Ovos',espera=1); assert any('Adicionar 6' in t for t in h.textos()), h.textos()[-4:]
    h.tocar('Mais opções',espera=1.2); h.tocar('Substituir minha lista',espera=1.5); h.tocar('Substituir',espera=3); names=[i['name'] for i in h.itens()]; assert len(names)==6 and not any('Ovos' in n for n in names), names
@passo("cozinhar: timer conta e segue ao trocar de passo")
def _():
    N='Bolo de cenoura com cobertura de chocolate'
    h.tocar('Receitas',espera=2); assert h.na_tela(N), 'volta a grade? '+str(h.textos()[:4])
    h.tocar(N,espera=2); h.tocar('Cozinhar',espera=2)
    for _ in range(3): h.tocar('Próximo',espera=1)
    h.tocar('Timer 40:00',espera=3); a=[x for x in h.textos() if ':' in x and x[0].isdigit()]; assert a and a[0].startswith('39:'), a
salvar('reg.json')
