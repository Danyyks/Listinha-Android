import h, time, sys
from PIL import Image
TELAS = "boasvindas,lista,vazia,compra,receitas,semreceitas,detalhe,nova,cozinhar,perfil".split(",")
def rodar(rotulo, tema):
    ims = []
    for t in TELAS:
        h.adb("shell", "am", "start", "-S", "-n", h.PKG + "/com.listinha.app.debug.PreviewActivity", "--es", "tela", t, "--es", "tema", tema)
        time.sleep(5)
        p = h.foto(f"vis/{rotulo}_{tema}_{t}"); ims.append(Image.open(p).convert("RGB"))
    W = 300; H = int(W * ims[0].height / ims[0].width)
    for k in range(0, 10, 5):
        sheet = Image.new("RGB", (5 * (W + 8) + 8, H + 16), (70, 70, 70))
        for i, im in enumerate(ims[k:k+5]): sheet.paste(im.resize((W, H)), (8 + i * (W + 8), 8))
        sheet.save(f"vis/sheet_{rotulo}_{tema}_{k//5}.jpg", quality=85)
if __name__ == "__main__":
    rotulo = sys.argv[1]
    for tema in ("claro", "escuro"): rodar(rotulo, tema)
