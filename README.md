<div align="center">

<img src="brand/app_icon.png" width="110" alt="Ícone do Listinha" />

# Listinha

**Suas receitas viram lista de compras num toque.**

App Android nativo de lista de compras e receitas — organização automática por categoria, controle de gastos em tempo real e um caderno de receitas que faz a compra por você.

<br />

![Em breve na Google Play](https://img.shields.io/badge/Google_Play-Em_breve-6E5A94?style=for-the-badge&logo=google-play&logoColor=white)

<sub>🚧 Em fase de teste fechado na Google Play — link de download em breve.</sub>

<br />

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material_3-6E5A94?style=flat-square&logo=materialdesign&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black)
![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)
![Versão](https://img.shields.io/badge/versão-1.0-6E5A94?style=flat-square)

</div>

<br />

## Sobre o projeto

O **Listinha** é um app Android publicado na **Google Play Store**, feito para quem faz feira e mercado. A ideia nasceu de um problema simples: anotar item por item toda semana é chato e fácil de esquecer algo.

O diferencial está em unir **receitas e lista de compras** no mesmo lugar. Você guarda suas receitas favoritas com foto, ingredientes e modo de preparo — e, quando bate a vontade de cozinhar, um toque em **"Adicionar à lista"** joga todos os ingredientes na sua lista de compras, já organizados por categoria e prontos para o supermercado.

Grátis, sem anúncios, com login pelo Google e sincronização na nuvem.

<br />

## Telas

<div align="center">

<table>
  <tr>
    <td align="center"><img src="store/screenshots/02_receitas.png" width="220" /><br /><sub><b>Suas receitas</b></sub></td>
    <td align="center"><img src="store/screenshots/03_detalhe_brownie.png" width="220" /><br /><sub><b>Detalhe da receita</b></sub></td>
    <td align="center"><img src="store/screenshots/04_orcamento.png" width="220" /><br /><sub><b>Lista + orçamento</b></sub></td>
  </tr>
  <tr>
    <td align="center"><img src="store/screenshots/01_login.png" width="220" /><br /><sub><b>Login com Google</b></sub></td>
    <td align="center"><img src="store/screenshots/05_perfil.png" width="220" /><br /><sub><b>Perfil</b></sub></td>
    <td align="center"><img src="store/screenshots/06_modo_escuro.png" width="220" /><br /><sub><b>Modo escuro</b></sub></td>
  </tr>
</table>

</div>

<br />

## Funcionalidades

- 🧾 **Receitas viram lista** — um toque transforma os ingredientes de uma receita em itens da lista de compras.
- 📖 **Caderno de receitas** — foto, ingredientes e modo de preparo, tudo salvo na sua conta.
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
| **Outros** | Splash Screen API · fontes variáveis (Fredoka + Nunito) |

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
│   ├── components/   # Componentes reutilizáveis (linhas, formulários, resumos)
│   ├── screens/      # Telas (Login, Home, Lista, Receitas, Detalhe, Perfil)
│   └── theme/        # Cores, tipografia e tema (claro/escuro)
└── util/            # Formatação de moeda, utilitários de imagem
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
[![Gmail](https://img.shields.io/badge/Gmail-EA4335?style=for-the-badge&logo=gmail&logoColor=white)](mailto:danyy.jonathan@gmail.com)

<br />

<div align="center">
<sub>Listinha · app Android publicado na Google Play · Kotlin + Jetpack Compose + Firebase</sub>
</div>
