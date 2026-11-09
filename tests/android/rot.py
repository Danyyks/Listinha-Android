import h, time
def rot(n): h.adb('shell','settings','put','system','user_rotation',str(n)); time.sleep(3.5)
N='Bolo de cenoura com cobertura de chocolate'
h.adb('shell','settings','put','system','accelerometer_rotation','0')
h.tocar('Receitas',espera=2)
h.tocar(N,espera=2); rot(1); print('detalhe apos girar:', 'Cozinhar' in h.textos() or any('Adicionar' in t and 'lista' in t for t in h.textos()) and N in h.textos(), h.textos()[:3]); rot(0)
if not h.na_tela('Cozinhar'): h.tocar(N,espera=2)
h.tocar('Cozinhar',espera=2)
for _ in range(3): h.tocar('Próximo',espera=1)
h.tocar('Timer 40:00',espera=3); a=[x for x in h.textos() if ':' in x and x[0].isdigit()]; rot(1); b=[x for x in h.textos() if ':' in x and x[0].isdigit()]; print('timer antes/depois de girar',a,b); rot(0)
