# Segurança dos dados (Data Safety) — respostas prontas

Guia para preencher o formulário **"Segurança dos dados"** no Google Play Console
(Política do app → Segurança dos dados). É só seguir as respostas abaixo.

---

## Parte 1 — Visão geral

| Pergunta | Resposta |
|----------|----------|
| Seu app coleta ou compartilha algum dos tipos de dados exigidos? | **Sim** (coleta) |
| Todos os dados são **criptografados em trânsito**? | **Sim** |
| Você oferece uma forma de o usuário **solicitar a exclusão** dos dados? | **Sim** (dentro do app, em Perfil → "Excluir conta", e também por e-mail) |

> O app **não compartilha** dados com terceiros e **não** usa dados para anúncios.

---

## Parte 2 — Tipos de dados coletados

Para **cada** item abaixo, marque:
- **Coletado:** Sim · **Compartilhado:** Não
- **Processado de forma efêmera:** Não
- **Coleta obrigatória** (exceto onde indicado "opcional")

### Informações pessoais → **Nome**
- Coletado: **Sim** · Compartilhado: **Não**
- Obrigatório (vem do login com Google)
- Finalidades: **Funcionalidade do app**, **Gerenciamento da conta**

### Informações pessoais → **Endereço de e-mail**
- Coletado: **Sim** · Compartilhado: **Não**
- Obrigatório (vem do login com Google)
- Finalidades: **Funcionalidade do app**, **Gerenciamento da conta**

### Fotos e vídeos → **Fotos**
- Coletado: **Sim** · Compartilhado: **Não**
- **Opcional** (só se o usuário adicionar uma foto à receita)
- Finalidade: **Funcionalidade do app**

> As listas de compras, receitas (texto), orçamento e histórico de preços são
> conteúdo criado pelo próprio usuário e ficam guardados só na conta dele. Não se
> encaixam nas categorias sensíveis exigidas pelo formulário — não precisam ser
> declarados como tipo de dado pessoal. Se o formulário oferecer a categoria
> "Outros" para conteúdo do usuário, pode declarar como Funcionalidade do app.

---

## Parte 3 — Práticas de segurança

| Pergunta | Resposta |
|----------|----------|
| Dados criptografados em trânsito | **Sim** (HTTPS/TLS, via Firebase) |
| Usuário pode pedir exclusão dos dados | **Sim** (no app: Perfil → "Excluir conta") |
| O app segue a política para Famílias? | **Não** (não é voltado para crianças) |

---

## Onde declarar a URL da política de privacidade

No Play Console: **Política do app → Política de privacidade** → cole o link:

**https://danyyks.github.io/listinha-privacidade/**

(Página hospedada no GitHub Pages; a fonte fica em `store/privacy/index.html` e no
repositório `github.com/Danyyks/listinha-privacidade`.)
