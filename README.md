<div align="center">

<img src="brand/app_icon.png" width="110" alt="Ícone do Listinha" />

# Listinha

**Suas receitas viram lista de compras num toque.**

App Android nativo de lista de compras e receitas — organização automática por categoria, controle de gastos em tempo real e um caderno de receitas que faz a compra por você.

<br />

![Em breve na Google Play](https://img.shields.io/badge/Google_Play-Em_breve-2B0B3A?style=for-the-badge&logo=google-play&logoColor=white)

<sub>🚧 Versão 2.0 em validação final na Google Play — link de download em breve.</sub>

<br />

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material_3-2B0B3A?style=flat-square&logo=materialdesign&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black)
![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)
![Versão](https://img.shields.io/badge/versão-2.0-2B0B3A?style=flat-square)

</div>

<br />

## Sobre o projeto

O **Listinha** é um app Android publicado na **Google Play Store**, feito para quem faz feira e mercado. A ideia nasceu de um problema simples: anotar item por item toda semana é chato e fácil de esquecer algo.

O diferencial está em unir **receitas e lista de compras** no mesmo lugar. Você guarda suas receitas favoritas com foto, ingredientes e modo de preparo — e, quando bate a vontade de cozinhar, um toque em **"Adicionar à lista"** joga todos os ingredientes na sua lista de compras, já organizados por categoria e prontos para o supermercado.

Salvar receita também é fácil: cole o texto de um blog ou compartilhe direto do Instagram, do navegador ou do WhatsApp. Quando os ingredientes e os passos vêm em linhas, o Listinha separa quantidades, categorias e passos sozinho; depois é só revisar. Na hora de cozinhar, o modo passo a passo tem timer e mantém a tela acesa.

Grátis, sem anúncios, com login pelo Google e sincronização na nuvem.

<br />

## Telas

<div align="center">

<table>
  <tr>
    <td align="center"><img src="store/screenshots/01_capa.png" width="190" /></td>
    <td align="center"><img src="store/screenshots/02_vontade.png" width="190" /></td>
    <td align="center"><img src="store/screenshots/03_feed.png" width="190" /></td>
    <td align="center"><img src="store/screenshots/04_so_o_que_falta.png" width="190" /></td>
  </tr>
  <tr>
    <td align="center"><img src="store/screenshots/05_cola_aqui.png" width="190" /></td>
    <td align="center"><img src="store/screenshots/06_mao_na_massa.png" width="190" /></td>
    <td align="center"><img src="store/screenshots/07_orcamento.png" width="190" /></td>
  </tr>
</table>

</div>

<br />

## Identidade visual

**"Deu vontade? Virou *lista.*"** A lista de compras é uma notinha de papel picotada, com o total como cupom e o item riscado de caneta. Cada área tem um sabor chapado: manteiga na lista, pistache no mercado, morango nas receitas, mirtilo na receita nova, açaí no perfil e noite no modo cozinhar. Tudo em tema claro e escuro. O guia completo está em [`brand/README.md`](brand/README.md).

<br />

## Funcionalidades

- 🧾 **Receitas viram lista** — um toque transforma os ingredientes de uma receita em itens da lista de compras.
- 📖 **Caderno de receitas** — grade estilo Pinterest com fotos grandes; receita sem foto ganha capa automática.
- 📋 **Salvar sem esforço** — cole uma receita (blog, WhatsApp, Instagram) ou compartilhe de outro app: com o texto em linhas, o Listinha separa nome, ingredientes, quantidades, categorias e passos, e você revisa antes de salvar.
- 🍳 **Modo cozinhar** — um passo por tela, letra grande, timer em um toque e tela sempre acesa.
- ⚖️ **Porções ajustáveis** — mude as porções e as quantidades se recalculam; marque o que já tem em casa e só o que falta vai pra lista.
- 🗂️ **Organização automática** — itens agrupados por categoria (frutas, carnes, mercearia, limpeza e mais).
- 💰 **Controle de gastos** — defina um orçamento e acompanhe o total da compra em tempo real.
- 🛒 **Modo compra** — marque os itens enquanto anda pelo supermercado.
- 🔐 **Login com Google** — dados seguros e sincronizados na nuvem.
- 🌗 **Tema claro e escuro** — visual leve, sem propaganda.

<br />

## Tecnologias

| Camada | Stack |
|--------|-------|
| **Linguagem** | Kotlin |
| **UI** | Jetpack Compose · Material 3 (Material You) |
| **Arquitetura** | MVVM · ViewModel · StateFlow |
| **Autenticação** | Firebase Auth · Credential Manager (login com Google) |
| **Banco de dados** | Cloud Firestore (nuvem, por usuário) |
| **Assíncrono** | Kotlin Coroutines |
| **Imagens** | Coil |
| **Testes** | JUnit · Firebase Emulator Suite (regras de segurança e fluxos reais) · uiautomator |
| **Outros** | Splash Screen API · R8 · ícone adaptativo e temático · fontes Fraunces + Fredoka + Nunito |

<br />

## Arquitetura

O app segue **MVVM** com uma separação clara entre dados, lógica de tela e interface. A UI é 100% declarativa em Jetpack Compose, e cada tela observa o estado exposto pelo seu `ViewModel`.

```
app/src/main/java/com/listinha/app/
├── auth/            # AuthViewModel — login com Google
├── data/            # Modelos e repositórios (Item, Recipe, Profile, Firestore)
├── list/            # ListViewModel — lista de compras e orçamento
├── recipes/         # RecipeViewModel — receitas
├── ui/
│   ├── components/   # Componentes e peças da marca (BrandKit: notinha, títulos, botões)
│   ├── screens/      # Telas (Login, Home, Lista, Receitas, Detalhe, Modo cozinhar, Perfil)
│   └── theme/        # Sabores por área, tipografia e tema (claro/escuro)
└── util/            # Moeda, imagem, leitura de receita colada (RecipeText) e contas de cozinha (Cooking)
```

### Segurança dos dados

Como o app fala direto com o Firestore, a proteção fica nas **regras de segurança**: cada pessoa só acessa a própria "gaveta" (`users/{uid}/...`) e nada além disso.

```js
// firestore.rules
function isOwner(userId) {
  return request.auth != null && request.auth.uid == userId;
}

match /users/{userId} {
  allow read, write: if isOwner(userId);
  match /{document=**} {
    allow read, write: if isOwner(userId);
  }
}
// Qualquer outro caminho fica NEGADO por padrão.
```

<br />

## Como rodar localmente

> O projeto usa Firebase, então precisa das suas próprias credenciais para compilar.

```bash
# 1. Clone o repositório
git clone https://github.com/Danyyks/Listinha-Android.git

# 2. Abra a pasta no Android Studio
```

3. Crie um projeto no [Firebase](https://console.firebase.google.com/), ative **Authentication (Google)** e **Cloud Firestore**.
4. Baixe o `google-services.json` e coloque em `app/`.
5. Publique as regras do arquivo `firestore.rules`.
6. Rode o app (`Shift + F10`).

### Testes

As regras que leem receita colada, escalam porções e detectam o timer têm testes que rodam no computador, sem emulador. No Android Studio: clique com o botão direito em `app/src/test` → **Run 'Tests in test'**. Pela linha de comando:

```bash
gradle testDebugUnitTest
```

Regras de segurança, fluxos contra o Firebase Emulator Suite (projeto falso, sem produção) e a vitrine de telas estão em [`docs/testes/README.md`](docs/testes/README.md).

> **Atenção:** o Android usa outro motor de expressões regulares (ICU) que o Java do computador. Uma regra pode passar no teste e quebrar no celular — por isso, depois de mexer em `util/RecipeText.kt` ou `util/Cooking.kt`, abra também a aba Receitas no emulador.

<br />

## Roadmap

- [ ] **Premium com IA** — geração de receitas e sugestões de compra com IA.
- [ ] Compartilhar listas entre pessoas da casa.
- [ ] Histórico de preços e comparação entre compras.

<br />

## Autor

Feito por **Dany Jonathan Bueno** — estudante de Análise e Desenvolvimento de Sistemas e desenvolvedor em formação.

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/danyyjonathan)
[![Instagram](https://img.shields.io/badge/Instagram-E4405F?style=for-the-badge&logo=instagram&logoColor=white)](https://instagram.com/danyyjonathan)
[![Gmail](https://img.shields.io/badge/Gmail-EA4335?style=for-the-badge&logo=gmail&logoColor=white)](https://mail.google.com/mail/?view=cm&fs=1&to=danyy.jonathan@gmail.com)

<br />

<div align="center">
<sub>Listinha · app Android publicado na Google Play · Kotlin + Jetpack Compose + Firebase</sub>
</div>
