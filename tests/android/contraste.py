def L(h):
    r,g,b=[(h>>s)&255 for s in (16,8,0)]
    f=lambda v:(v/255/12.92) if v/255<=0.03928 else (((v/255)+0.055)/1.055)**2.4
    return 0.2126*f(r)+0.7152*f(g)+0.0722*f(b)
def cr(a,b):
    la,lb=sorted((L(a),L(b)),reverse=True); return (la+0.05)/(lb+0.05)
fl={ # bg,title,titleOnPaper,secondary,text,on,soft
'claro LISTA':(0xFFDE85,0x3A1670,0x3A1670,0x5B4A1E,0x2A1050,0xFFFDF6,0xFFEDB8),
'claro MERCADO':(0xDDEBA8,0x1F3D14,0x1F3D14,0x3F5530,0x1F3D14,0xFFFDF6,0xEEF5D3),
'claro RECEITAS':(0xFFD0D8,0xC81E12,0xC81E12,0x7A2A30,0x4A0D0A,0xFFFDF6,0xFFE6EA),
'claro NOVA':(0xCFDBFF,0x16246E,0x16246E,0x3A4580,0x16246E,0xFFFDF6,0xE6ECFF),
'claro ACAI':(0x2B0B3A,0xFFC4E4,0x3A1670,0xD9B8CF,0xFFE3F1,0x2B0B3A,0x43205A),
'claro COZINHAR':(0x17121E,0xFFC49B,0xFFC49B,0xC9BFD2,0xF3ECE2,0x17121E,0x3A3142),
'esc LISTA':(0x2A2110,0xFFDE85,0xFFDE85,0xD9CBA6,0xFFF1C7,0x2A2110,0x3D3118),
'esc MERCADO':(0x162110,0xDDEBA8,0xDDEBA8,0xB9C7A0,0xEEF5D3,0x162110,0x24331B),
'esc RECEITAS':(0x2E0F15,0xFFB8C4,0xFFB8C4,0xE3B9C0,0xFFE1E6,0x2E0F15,0x4A1A23),
'esc NOVA':(0x0F1534,0xCFDBFF,0xCFDBFF,0xAEB9DD,0xE6ECFF,0x0F1534,0x1C2550),
'esc ACAI':(0x170A1F,0xFFC4E4,0xFFC4E4,0xD9B8CF,0xFFE3F1,0x170A1F,0x3A1A4A),
}
print("fundo x titulo / secundario / texto  |  (titulo sobre a 'soft')")
for k,(bg,ti,tp,se,tx,on,so) in fl.items():
    print(f"{k:14} titulo {cr(bg,ti):5.2f}  sec {cr(bg,se):5.2f}  texto {cr(bg,tx):5.2f}  titulo/soft {cr(so,ti):5.2f}  texto/soft {cr(so,tx):5.2f}")
P={'claro':(0xFFFDF6,0x2B2233,0x6B6178,0xB3261E,0x2E6B2A,0xB3261E,0x3A1670),'escuro':(0x2A2430,0xF3ECE2,0xBDB2C6,0xF2B8B5,0xA8E0A0,0xFFB4A8,0xFFC4E4)}
for k,(pa,ink,mu,da,ok,bad,br) in P.items():
    print(k,'papel: ink',round(cr(pa,ink),2),'muted',round(cr(pa,mu),2),'ok',round(cr(pa,ok),2),'bad',round(cr(pa,bad),2),'brand',round(cr(pa,br),2))
# titleOnPaper de cada sabor claro sobre papel claro
for k in ('claro LISTA','claro MERCADO','claro RECEITAS','claro NOVA','claro ACAI'):
    print(k,'titleOnPaper/paper',round(cr(0xFFFDF6,fl[k][2]),2),'botao: on/bg',round(cr(fl[k][0],fl[k][5]),2))
print('botao + branco sobre #A58B7B?')
