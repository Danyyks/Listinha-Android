"""
make_prints.py  —  gera prints de loja e banners a partir de HTML.

Cada peça é um arquivo .html numa pasta de origem, com o tamanho em
<meta name="print-size" content="540x1080@2">  (largura x altura @ escala).
A saída é um PNG por arquivo, renderizado pelo Chrome em modo headless.

Uso:
    python make_prints.py <pasta_com_html> <pasta_de_saida>
"""

import pathlib
import re
import subprocess
import sys

CHROME = r"C:\Program Files\Google\Chrome\Application\chrome.exe"


def main(src: str, out: str) -> None:
    src_dir = pathlib.Path(src).resolve()
    out_dir = pathlib.Path(out).resolve()
    out_dir.mkdir(parents=True, exist_ok=True)
    for page in sorted(src_dir.glob("*.html")):
        size = re.search(r'name="print-size" content="(\d+)x(\d+)@(\d+)"', page.read_text(encoding="utf-8"))
        if not size:
            print("sem print-size, pulando:", page.name)
            continue
        w, h, scale = size.groups()
        png = out_dir / f"{page.stem}.png"
        subprocess.run([
            CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars",
            f"--force-device-scale-factor={scale}", f"--window-size={w},{h}",
            "--virtual-time-budget=6000", "--allow-file-access-from-files",
            f"--screenshot={png}", page.as_uri(),
        ], check=True, capture_output=True)
        print("ok", png.name)


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
