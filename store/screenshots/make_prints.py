"""
make_prints.py  —  monta os prints da Play Store / README
------------------------------------------------------------
Pega capturas cruas do emulador (1080x2400, tiradas com
`adb exec-out screencap -p` e a barra de status em "modo demo")
e coloca cada uma numa arte 1080x2160: fundo lavanda, título em
Fredoka e a tela do app numa moldura arredondada com sombra —
o mesmo padrão dos prints da v1.

Uso:
    python make_prints.py <pasta_com_as_capturas_cruas>

A pasta precisa ter os arquivos listados em PRINTS (coluna "raw").
O HTML de cada print é renderizado pelo Chrome em modo headless.
"""

import pathlib
import subprocess
import sys

AQUI = pathlib.Path(__file__).resolve().parent
FONTES = (AQUI / ".." / ".." / "app" / "src" / "main" / "res" / "font").resolve()
CHROME = r"C:\Program Files\Google\Chrome\Application\chrome.exe"

# (arquivo final, captura crua, título linha 1, título linha 2, tema)
PRINTS = [
    ("02_receitas.png", "raw_grade.png", "Suas receitas,", "lindas como um álbum", "claro"),
    ("03_detalhe.png", "raw_detalhe.png", "Só o que falta", "vai pra lista", "claro"),
    ("04_colar_receita.png", "raw_colar.png", "Cole a receita,", "o Listinha organiza", "claro"),
    ("05_modo_cozinhar.png", "raw_cozinhar.png", "Modo cozinhar", "com timer", "claro"),
    ("08_modo_escuro.png", "raw_escuro.png", "Clara ou escura,", "do seu jeito", "escuro"),
]

TEMAS = {
    "claro": {"fundo": "linear-gradient(180deg,#F1ECF8 0%,#E6DDF2 100%)", "titulo": "#574471",
              "sombra": "rgba(87,68,113,.28)", "borda": "rgba(255,255,255,.9)"},
    "escuro": {"fundo": "linear-gradient(180deg,#2A2433 0%,#1B1722 100%)", "titulo": "#FFFFFF",
               "sombra": "rgba(0,0,0,.55)", "borda": "rgba(255,255,255,.08)"},
}

HTML = """<!doctype html><html><head><meta charset="utf-8"><style>
@font-face {{ font-family: Fredoka; font-weight: 700; src: url("{fonte}"); }}
html,body {{ margin:0; padding:0; }}
.arte {{ width:1080px; height:2160px; position:relative; overflow:hidden; background:{fundo}; }}
.titulo {{ position:absolute; top:96px; left:0; right:0; text-align:center; font-family:Fredoka;
          font-weight:700; font-size:76px; line-height:1.0; letter-spacing:-.5px; color:{titulo}; }}
.tela {{ position:absolute; left:134px; top:276px; width:812px; height:1804px; border-radius:56px;
        overflow:hidden; box-shadow:0 40px 90px {sombra}, 0 0 0 3px {borda}; }}
.tela img {{ width:100%; height:100%; display:block; }}
</style></head><body><div class="arte">
<div class="titulo">{l1}<br>{l2}</div>
<div class="tela"><img src="{img}"></div>
</div></body></html>"""


def main(pasta_raw: str) -> None:
    raw = pathlib.Path(pasta_raw).resolve()
    tmp = raw / "_html"
    tmp.mkdir(exist_ok=True)
    for final, captura, l1, l2, tema in PRINTS:
        t = TEMAS[tema]
        html = HTML.format(
            fonte=(FONTES / "fredoka_bold.ttf").as_uri(),
            img=(raw / captura).as_uri(),
            l1=l1, l2=l2, **t,
        )
        arquivo_html = tmp / (final + ".html")
        arquivo_html.write_text(html, encoding="utf-8")
        subprocess.run([
            CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars",
            "--force-device-scale-factor=1", "--window-size=1080,2160",
            "--allow-file-access-from-files",
            f"--screenshot={AQUI / final}", arquivo_html.as_uri(),
        ], check=True, capture_output=True)
        print("ok", final)


if __name__ == "__main__":
    main(sys.argv[1])
