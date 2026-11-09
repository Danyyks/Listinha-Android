import h, time
def rot(n): h.adb('shell','settings','put','system','user_rotation',str(n)); time.sleep(3.5)
N='Bolo de cenoura com cobertura de chocolate'
h.reset(); time.sleep(4)
h.adb('shell','am','start','-n',h.PKG+'/com.listinha.app.MainActivity','-a','android.intent.action.SEND','-t','text/plain','--es','android.intent.extra.TEXT','"$(cat /data/local/tmp/r1.txt)"'); time.sleep(5)
h.tocar('Salvar',espera=3)
h.tocar(N,espera=2); h.tocar('Mais porções',espera=0.5); h.tocar('Ovos',espera=1)
pre=[x for x in h.textos() if 'porç' in x and 'Mais' not in x and 'Menos' not in x]; bt=[x for x in h.textos() if x.startswith('Adicionar')]
print('antes',pre,bt)
rot(1); t=h.textos(); print('paisagem detalhe',[x for x in t if 'porç' in x and 'Mais' not in x and 'Menos' not in x],[x for x in t if x.startswith('Adicionar')]); h.foto('f2_det_land')
rot(0); t=h.textos(); print('retrato detalhe',[x for x in t if 'porç' in x and 'Mais' not in x and 'Menos' not in x],[x for x in t if x.startswith('Adicionar')])
h.tocar('Cozinhar',espera=2)
for _ in range(3): h.tocar('Próximo',espera=1)
h.tocar('Timer 40:00',espera=3); a=[x for x in h.textos() if ':' in x and x[0].isdigit()]
rot(1); b=[x for x in h.textos() if ':' in x and x[0].isdigit()]; print('timer antes/paisagem',a,b, 'passo3', any('PASSO 3' in x for x in h.textos()))
time.sleep(3); c=[x for x in h.textos() if ':' in x and x[0].isdigit()]; print('conta ainda',c)
rot(0); d=[x for x in h.textos() if ':' in x and x[0].isdigit()]; print('retrato',d)
