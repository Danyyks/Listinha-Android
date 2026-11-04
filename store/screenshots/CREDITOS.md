# Créditos das fotos

Todas as fotos são da [Pexels](https://www.pexels.com/license/) (uso gratuito, edição permitida, crédito opcional).
Regra da licença a respeitar: não sugerir que pessoas ou marcas nas imagens endossam o app.

| Foto | Autor | Link |
|---|---|---|
| Smash burger | Lucas Andrade | https://www.pexels.com/photo/person-holding-burgers-with-bacon-5179783/ |
| Lasanha de frigideira | Laura oliveira | https://www.pexels.com/photo/delicious-cheesy-lasagna-bite-on-fork-34474031/ |
| Carbonara | Nano Erdozain | https://www.pexels.com/photo/creamy-spaghetti-carbonara-in-ceramic-dish-29039085/ |
| Espaguete na panela | Klaus Nielsen | https://www.pexels.com/photo/woman-putting-raw-spaghetti-in-hot-water-6294418/ |
| Donut de air fryer | JReyas place | https://www.pexels.com/photo/assorted-donuts-in-a-box-sweet-treat-variety-34491951/ |
| Cookie de frigideira | aziz 3z | https://www.pexels.com/photo/delicious-homemade-chocolate-chip-cookies-30759934/ |
| Sorvete de 2 ingredientes | Jahra Tasfia Reza | https://www.pexels.com/photo/delicious-vanilla-ice-cream-cone-close-up-38987559/ |
| Casquinha na mão (print 2) | Nick Mayer | https://www.pexels.com/photo/close-up-photo-of-a-person-s-hand-holding-a-delicious-ice-cream-7761650/ |
| Iced matcha | Nay Nyo | https://www.pexels.com/photo/refreshing-iced-matcha-latte-with-sunglasses-31599066/ |
| Morango com chocolate | Alesia Talkachova | https://www.pexels.com/photo/shallow-focus-of-chocolate-covered-strawberry-7619935/ |
| Burrata com tomate | Valeria Boltneva | https://www.pexels.com/photo/gourmet-burrata-with-tomato-sauce-dish-29380175/ |
| Tacos | ROMAN ODINTSOV | https://www.pexels.com/photo/man-holding-fresh-tacos-5837196/ |

## Como refazer os prints

As telas ficam em `src/img/tela_*.jpg` (capturas reais do app no emulador). O print 7 usa dados de exemplo.
As páginas ficam em `src/*.html`. Para gerar os PNGs:

```bash
python store/screenshots/make_prints.py store/screenshots/src store/screenshots
```

O `feature-graphic.png` gerado vai para `store/feature-graphic.png`.
