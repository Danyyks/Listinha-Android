# Identidade Visual — Listinha

Documento da marca do app **Listinha** (lista de compras). Guarda o logo
oficial, as cores, as fontes e o mapa dos ícones usados no app.

---

## Ícone do app — "lista feliz"

Uma folhinha de lista de compras que também é uma **carinha sorridente**: a
carinha no topo, três itens já **marcados** (checkboxes) e as linhas da lista.
Passa a ideia de compras organizadas e felizes. **Usado só como ícone do app**
(launcher + splash) — dentro do app a marca aparece só pelo nome "Listinha",
sem logo.

**Arquivos-fonte** (nesta pasta `brand/`): `app_icon.svg` (fonte da verdade,
`viewBox 0 0 1024 1024`) e `app_icon.png` (prévia). Os arquivos `logo.*` são do
logo antigo (carrinho), agora **aposentado**.

**No app**, virou o ícone adaptativo (já recolorido pro roxo do app):

| Recurso Android | O que é |
|-----------------|---------|
| `res/drawable/ic_launcher_foreground.xml` | A folha feliz — frente do ícone e da **splash** |
| `res/drawable/ic_launcher_background.xml` | Fundo do ícone: **degradê roxo** (`#8672AD` → `#574471`) |
| `res/mipmap-anydpi-v26/ic_launcher*.xml` | Junta frente + fundo = **ícone adaptativo** |

> Regra: o desenho já traz as próprias cores; para mudar de tamanho no ícone,
> mexa só na escala do `<group>`. **Não é mais usado dentro das telas.**

---

## Cores

Roxo pastel "dusty" (sóbrio, sem brilho excessivo). Tokens completos em
`app/src/main/java/com/listinha/app/ui/theme/Color.kt`.

**Claro**
- Primária: `#6E5A94` · Container: `#E7E0F2`
- Fundo: `#EDE7F5` · Superfície (cards): `#FFFFFF`
- Texto: `#2B2233` · Texto secundário: `#6B6178`
- Erro: `#D9604E`

**Escuro**
- Primária: `#C6B7E6` · Fundo: `#17131F` · Superfície: `#221C2E`

> O ícone do app usa o degradê `#8672AD → #574471` (mesma família), e o resto
> da interface usa o primário `#6E5A94`.

---

## Fontes (Google Fonts, gratuitas / OFL)

Arquivos em `res/font/`, configuradas em `ui/theme/Type.kt`.

| Fonte | Uso |
|-------|-----|
| **Fredoka** (`fredoka.ttf`) | A marca "Listinha", nomes e números em destaque |
| **Nunito** (`nunito.ttf`) | Corpo e interface (todos os outros textos) |

São fontes **variáveis** (um arquivo cobre vários pesos) — por isso o app exige
Android 8.0+ (`minSdk 26`).

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

**Foto de capa do perfil:** `res/drawable/supermarket_cover.jpg` — foto de uso
livre do Pexels (licença Pexels, uso comercial permitido, sem atribuição
obrigatória).
