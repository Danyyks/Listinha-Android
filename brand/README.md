# Identidade Visual — Listinha

Documento da marca do app **Listinha** (lista de compras). Guarda o conceito,
o ícone, os sabores, as fontes, as fotos e o mapa dos ícones usados no app.

---

## Conceito

"Deu vontade? Virou *lista.*" A lista de compras vira uma **notinha de papel
picotada**, que é a assinatura do app: ela aparece na lista, com o total como
cupom, nos ingredientes das receitas e nos estados vazios. Cada área tem um
fundo chapado com nome de comida. Peças em `ui/components/BrandKit.kt`.

---

## Ícone do app: "A mordida"

A notinha picotada com uma mordida no canto: a lista deu tanta vontade que
alguém mordeu. Fundo açaí chapado.

**Arquivos-fonte** (nesta pasta `brand/`): `app_icon.svg` (fonte da verdade,
tela de 108 dp) e `app_icon.png` (1024 px). A loja usa `store/icon-512.png`,
enquadrado na área visível de 72 dp. Os arquivos `logo.*` são de um logo antigo,
**aposentado**.

| Recurso Android | O que é |
|-----------------|---------|
| `res/drawable/ic_launcher_foreground.xml` | A notinha: frente do ícone e da **splash** |
| `res/drawable/ic_launcher_background.xml` | Fundo do ícone: açaí `#2B0B3A` |
| `res/drawable/ic_launcher_monochrome.xml` | Ícone temático do Android 13+ (linhas são furos) |
| `res/mipmap-anydpi-v26/ic_launcher*.xml` | Junta as camadas no **ícone adaptativo** |

> Mantenha o desenho dentro do círculo seguro de 66 dp.

---

## Sabores

Definidos em `app/src/main/java/com/listinha/app/ui/theme/Color.kt`. Cada área
usa um par de fundo e título, com versão escura. Troque de sabor com `AreaTheme`.

| Sabor | Área | Fundo | Título |
|-------|------|-------|--------|
| Manteiga | Lista | `#FFDE85` | `#3A1670` |
| Pistache | Modo compra | `#DDEBA8` | `#1F3D14` |
| Morango | Receitas | `#FFD0D8` | `#C81E12` |
| Mirtilo | Nova receita | `#CFDBFF` | `#16246E` |
| Açaí | Boas-vindas e Perfil | `#2B0B3A` | `#FFC4E4` |
| Noite | Modo cozinhar (sempre escuro) | `#17121E` | `#FFC49B` |

Papel da notinha: `#FFFDF6` no claro e `#2A2430` no escuro. Uva `#3A1670` é a
tinta da marca.

---

## Fontes (Google Fonts, licença OFL)

Arquivos fixos por peso em `res/font/`, configurados em `ui/theme/Type.kt`.

| Fonte | Uso |
|-------|-----|
| **Fraunces** | Títulos, com uma palavra em itálico (`*palavra*` nos textos). Gerada com opsz 144, SOFT 100 e WONK 1; licença em `OFL-Fraunces.txt` |
| **Fredoka** | A marca "Listinha", o total e o timer |
| **Nunito** | Corpo e interface |

---

## Ícones (todos em `res/drawable/`, estilo linha/minimalista, sem emojis)

| Ícone | Onde é usado |
|-------|--------------|
| `ic_cart` | Botão "Comprar" (modo de compra) |
| `ic_google` | Botão "Continuar com Google" |
| `ic_lock` | Linha "entrar é rápido e seguro" (login) |
| `ic_add` / `ic_remove` | Seletor de quantidade, "adicionar", "definir orçamento" |
| `ic_delete` | Apagar item / limpar histórico |
| `ic_sun` / `ic_moon` / `ic_auto` | Seletor de tema (perfil) |
| `ic_star` | Avaliar o app (perfil) |
| `ic_share` | Compartilhar o app (perfil) |
| `ic_heart` | Saudação do perfil |
| `ic_chevron_right` | Setas ">" das opções do perfil |
| `ic_list` / `ic_recipes` / `ic_person` | Navegação inferior (Lista / Receitas / Perfil) |
| `ic_recipes` | Estado vazio das Receitas |
| `ic_food_*` (12: panela, panqueca, salada, camadas, macarrão, pizza, hambúrguer, pão, bolo, bebida, café, peixe) | Miniaturas que o usuário escolhe para cada receita |
| `ic_sparkle` | Botão "Gerar receita com IA" (Premium) |
| `ic_search` | Campo de busca (Receitas) |
| `ic_back` | Voltar (detalhe da receita) |
| `ic_camera` | Adicionar foto à receita |

**Fotos da interface** (licença Pexels, uso comercial permitido, sem atribuição
obrigatória; sem rostos nem marcas):

| Arquivo em `res/drawable/` | Onde | Autor | Link |
|---|---|---|---|
| `profile_cover.jpg` | Capa do Perfil | Alena Darmel | https://www.pexels.com/photo/7223306/ |
| `welcome_burger.jpg` | Boas-vindas | Lucas Andrade | https://www.pexels.com/photo/person-holding-burgers-with-bacon-5179783/ |
| `empty_cookies.jpg` | Lista vazia | Terrance Barksdale | https://www.pexels.com/photo/round-tin-can-with-assorted-cookies-5607399/ |
| `empty_strawberry.jpg` | Receitas vazias | Alesia Talkachova | https://www.pexels.com/photo/shallow-focus-of-chocolate-covered-strawberry-7619935/ |

## Vitrine de telas (só em desenvolvimento)

`app/src/debug/.../PreviewActivity.kt` mostra as telas com dados falsos, sem
Firebase e sem conta. Não entra na versão da loja.

```bash
adb shell am start -n com.danyks.listinha/com.listinha.app.debug.PreviewActivity --es tela lista --es tema claro
```

Telas: `boasvindas`, `lista`, `vazia`, `compra`, `receitas`, `semreceitas`,
`detalhe`, `nova`, `colada`, `cozinhar`, `perfil`. Temas: `claro`, `escuro`.
