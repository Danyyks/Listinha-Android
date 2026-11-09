# Campanha de pré-lançamento: Listinha 2.0 (versionCode 3), ciclo 1

Data: 09/11/2026. Build: debug do working tree (rebranding sem commit) e `assembleRelease` não assinado.
Aparelho: AVD `Listinha_Teste` (Android 37.1, x86_64, sem conta Google, criado só para os testes). O `Pixel_8` do Dany **não foi ligado**.
Backend: Firebase Emulator Suite (Auth + Firestore), projeto falso `demo-listinha`. Nada tocou produção.
Como rodar: `docs/testes/README.md`.

## Resumo

| Camada | Resultado |
|---|---|
| 1. Regras do Firestore | 22 de 22 passaram (4 coleções x dono / outro usuário / sem login, listagens, collectionGroup, caminhos fora da gaveta, exclusão de conta) |
| 2. Lógica (JUnit) | 48 de 48 passaram (13 que já existiam + 35 novos) |
| 3. Fluxos reais contra o emulador | maioria passou; 4 falhas reais (bugs 1, 2, 3 e 4) |
| 4. Visual e acessibilidade | 3 problemas de layout em 360dp e fonte 200% (bugs 5 e 6), contrastes ok |
| 5. Robustez | monkey 7.000 eventos (2 sementes) sem crash nem ANR; 1 bug de rotação (bug 3); offline ok |
| 6. Compatibilidade | só Android 37.1 testado (único image do SDK) |
| 7. Release | compila; PreviewActivity e código de teste ausentes do APK; R8 desligado (bug 7) |

## Ambiente de teste e prova de que não alcança produção

- `app/src/debug/.../EmulatorBootProvider.kt`: só em debug, só liga se existir o arquivo `files/emulator_mode` dentro do app.
  Cria o `FirebaseApp` padrão com projeto `demo-listinha`, chave de API falsa, aponta Auth e Firestore para `10.0.2.2` e entra com usuário falso.
  O `google-services.json` real nem é lido. A sessão Google do aparelho nunca é usada.
- Provas: (a) o log `EMULADOR LIGADO projeto=demo-listinha firestore=10.0.2.2:8080 ssl=false auth=10.0.2.2:9099`; (b) o provider
  tem `check(projectId == "demo-listinha")`: se o app padrão não for o falso, o app nem abre; (c) os soquetes TCP do UID do app em
  `/proc/net/tcp` apontavam só para `10.0.2.2:8080` e `10.0.2.2:9099`; (d) os dados gravados pelo app aparecem no emulador (lidos por REST).
- **Nenhum arquivo de `app/src/main` foi alterado por mim.** Mudanças fora dele: `app/src/debug/` (provider,
  `network_security_config.xml` que libera HTTP só para 10.0.2.2, manifesto debug), `app/src/test/`, `tests/`, `docs/testes/`, `firebase.json`, `.firebaserc`, `.gitignore`.
- Para a automação de interface: `tests/android/h.py` (localiza por texto, nunca por coordenada fixa; confere o banco do emulador por REST).

## Bugs por gravidade

Nenhum bloqueante (crash, perda de dado ou vazamento) foi encontrado.

### Médios

**1. Excluir conta falhando não avisa nada.** `MainActivity.kt:84`: `authViewModel.errorMessage` só é passado ao `LoginScreen`.
Logado, a falha de `deleteAccount` (sem rede, usuário cancela, sem credencial) cai em `errorMessage` mas nada o mostra.
Passos: Perfil, Excluir conta, Excluir tudo, com o seletor do Google indisponível. Resultado: nada muda na tela, sem mensagem
(log: `AuthViewModel: Falha ao excluir conta ... NoCredentialException`). Os dados ficam intactos (bom), mas a pessoa não sabe que não foi apagada.
Fluxo exigido pela política da Play, vale corrigir.

**2. Girar a tela (ou mudar o idioma) joga para fora da receita e perde o timer.** `RecipesScreen.kt:116`
`var route by remember` (não `rememberSaveable`) e `HomeScreen.kt:92` `recipesImmersive` com `remember`.
Passos: Receitas, abrir uma receita, Cozinhar, iniciar o timer de 40:00, girar o aparelho para paisagem (ou trocar o idioma do app).
Resultado: volta para a grade de Receitas e o timer some. Na cozinha, com o celular girando na bancada, é uma falha real.

**3. Boas-vindas: a notinha cobre o título em telas pequenas.** `LoginScreen.kt:100` e seguintes (`PaperTicket` com `rotate(5f)` posicionada sobre a foto).
Em 360dp (1280x720 a 320dpi, fonte 100%) a notinha "Smash burger" cobre "Virou lista." Com fonte 200% cobre também "Deu vontade?" e o botão vira "Continuar com" (sem "Google").
Capturas: `capturas/sheet_small360_claro_0.jpg`, `sheet_small360_f200_claro_0.jpg`. É a primeira tela do app.

**4. Orçamento: ao tocar "Editar", o cursor fica no início do campo.** `BudgetSummary.kt` (folha `BudgetSheet`, valor pré-preenchido).
Passos: orçamento de R$ 50, Editar, digitar 9: o campo vira `950` (e gravaria R$ 950). A pessoa precisa apagar antes. Fix simples: cursor no fim / selecionar tudo.

### Baixos

**5. Fonte do sistema em 200% (a11y).** Barra de abas com rótulos cortados na base ("Lista", "Receitas", "Perfil"), nome do item quebrando no meio da palavra ("Mor/an...", "merca/do",
"orçament/o"), botões "Cozinhar" e "Adicionar à lista" viram "C..." e "Adicion..." (também a 360dp com fonte 100%: "Cozi..." e "Adicionar à lis..."), campo "Porções" quebra ("Porçõe/s")
em 360dp. Capturas em `docs/testes/capturas/`.

**6. Alvos e rótulos de acessibilidade na lista.** Cada `CheckBox` da linha não tem nome (11 de 11 sem rótulo: o leitor de tela lê só "caixa de seleção"; o nome do item está noutro elemento);
a linha de item tem 100px (38dp) de altura e o `CheckBox` 126x100px, abaixo dos 48dp quando o item não tem segunda linha. (`ShoppingItemRow.kt:70,83`.) Em Perfil existe um clicável de 81px (31dp) de altura.

**7. R8 desligado e DEX acima de 10 MB.** `app/build.gradle.kts` `isMinifyEnabled = false`. O DEX do release tem 10,4 MB compactado / 29,5 MB. Segundo a página do Android vitals (consultada em 09/11/2026),
apps com DEX acima de 10 MB precisam de pelo menos 25% de otimização/ofuscação/redução, e o impacto na visibilidade na loja começa em **fevereiro de 2027**. Não bloqueia o lançamento; ligar R8 exige nova rodada de testes do release.

**8. Saudação sem o mesmo tratamento do Perfil.** `ShoppingModeBanner.kt:44` não remove `*` do nome (`Vamos lá, Ana*!`; o Perfil remove) e usa `firstOrNull()` sem checar vazio (nome `""` daria "Vamos lá, !"; hoje o Firebase devolveu nulo e caiu em "você").
`ProfileScreen.kt:157`: avatar mostra a inicial `*` para nome `*Ana`.

**9. Cosmético.** O risco do item riscado ultrapassa o texto truncado (`Queijo mussarela ... sa…`). Contraste do título do sabor Receitas no claro é 4,18:1 (ok para título grande; não use em texto pequeno).

## Camada 1: regras do Firestore (22/22)

`tests/firebase/regras.test.js` (Vitest + `@firebase/rules-unit-testing` 6.0.0 + `firebase` 13.2.0).
Por coleção usada (`users/{uid}`, `items`, `priceHistory`, `recipes`): dono lê, atualiza, cria e apaga; outro usuário logado e sem login são negados em todas as operações.
Também: listar itens/receitas/histórico de outro, listar `users` (negado inclusive ao dono), `collectionGroup`, coleção raiz estranha, escrever na gaveta alheia, uid parecido, subcoleção futura só do dono, e a sequência da exclusão de conta (apaga itens, histórico, receitas e perfil).
Nota: as regras não validam campos (preço, nome, tamanho). Quem tem a conta pode gravar qualquer coisa na própria gaveta (aceitável; o cliente valida).

## Camada 2: lógica (48/48)

Novos: `FormatCurrencyTest` (zero, centavos, milhar, negativo, `0,1+0,2`), `ItemCategoryTest` (total, ordenação sem erro de contrato do Comparator, rótulos), `BrandTextTest`
(par de asteriscos, nome com um asterisco só, `*`, vazio, `a**b`, dois pares), `CategoriaPorIngredienteTest`. Todos passaram: `brandText` se comporta bem nos casos pedidos.
Limite: total e orçamento são calculados dentro de composables (`BudgetSummary`), sem função pura testável. Foram cobertos pelo fluxo real (camada 3).

## Camada 3: fluxos reais contra o emulador

Passaram (conferidos na tela **e** no banco do emulador):
adicionar item com preço e quantidade (`3 x R$ 12,50 = R$ 37,50`); editar item (preço); marcar e desmarcar (grava `bought`); apagar item; total e contagem "x de y itens marcados";
orçamento definir (R$ 100, "Restante: R$ 62,50"), editar com vírgula (30,5, "Passou em R$ 7,00"), valor inválido (`abc`, remove o orçamento sem travar) e Remover; limpar comprados; modo Comprar
(marcar, "Lista completa", Concluir, trocar de aba e voltar mantém o modo); receita colada pelo compartilhamento (ACTION_SEND com acentos, `&`, `1/2`, `°C`) vira rascunho com nome, 50 min, 8 porções, 7 ingredientes
categorizados e 4 passos (**regex ICU funcionando no aparelho**); salvar receita; escalar porções (3 a 3 3/4 cenouras, 0,4 em pt-BR); "Toque no que já tem" (botão vira "Adicionar 6 à lista");
enviar à lista; Substituir minha lista (diálogo, lista e orçamento zerados); modo cozinhar com timer (40:00 contando, segue contando ao trocar de passo); tema Claro, Escuro e Auto (grava e persiste após matar o app);
nomes de usuário com `*`, vazio e muito longo (Perfil ok).
Falharam: bugs 1, 2 e 4 acima.
Não cobertos: editar e excluir receita pelos botões, criar receita manual campo a campo, Sair do app (o botão existe; o login volta por reabertura do app), foto da receita pelo seletor (lacuna), excluir conta até o fim (exige o login Google real).

## Camada 4: visual e acessibilidade

Capturas de todas as 10 telas da vitrine em claro e escuro: aparelho normal (1080x2400), 360dp, 360dp com fonte 200% e normal com fonte 200%. Resumo em `docs/testes/capturas/`.
Ícones da barra de status legíveis em Perfil, boas-vindas e cozinhar no tema claro. Teclado sobre a lista vazia cobre parte da arte (esperado).
Contraste (`tests/android/contraste.py`): todos os textos de sabor e papel acima de 4,5:1, exceto título de Receitas no claro (4,18:1, aceitável como texto grande). Sem inspeção com o Accessibility Scanner (não instalável aqui).

## Camada 5: robustez

- Monkey restrito ao app: semente **20261109**, 3.000 eventos, throttle 150 ms; semente **7777**, 4.000 eventos, throttle 100 ms, 5% rotação. Sem crash, sem ANR (buffer `crash` vazio, processo vivo). As mensagens `IOException ... event0` são do próprio monkey (flip não suportado).
- Modo avião: com o item salvo offline, o item aparece na hora, sobrevive à morte do processo e sincroniza quando o app reabre com rede. Sem mensagem de erro durante o offline (comportamento do Firestore).
- Lista com 301 itens (nome de 120 caracteres): rolagem e modo compra sem crash; segmentos limitados a 30; nome longo cortado em 2 linhas com reticências.
- Receita com foto grande: não testada (ver lacunas). `compressImageToBase64` reduz para 800 px e JPEG 78% (~100 KB), abaixo do limite de 1 MiB do documento.

## Camada 6: compatibilidade

Só existe a imagem do Android 37.1 (API de pré-visualização do `Pixel_8`) no SDK: **minSdk 26, intermediárias e API 36 estável não foram testados**. Faltam `cmdline-tools` para instalar imagens; sem a Play Store, o Firebase Test Lab (cota Spark: 15 execuções/dia, 10 virtuais e 5 físicas, página oficial consultada em 09/11/2026) é o caminho.

## Camada 7: release

`assembleRelease` não assinado compilou (12,6 MB, debug 17,5 MB, 295 arquivos). `targetSdk 36`, `versionCode 3`, `versionName 2.0`, sem `debuggable`, permissões só INTERNET e ACCESS_NETWORK_STATE (+2 do Firebase/GMS).
Verificado no APK: `PreviewActivity`, `EmulatorBootProvider`, `demo-listinha`, `10.0.2.2`, `emulator_mode`, `network_security_config` **ausentes**; as 11 fontes (3 Fraunces, 3 Fredoka, 5 Nunito) presentes, com os mesmos tamanhos dos `.ttf` do projeto.
**Não executei o APK de release**: ele fala com o Firebase de produção. O tempo de abertura e a assinatura precisam ser medidos pelo Dany na faixa de teste.

## Fontes oficiais (consultadas em 09/11/2026)

- Target API (developer.android.com): a partir de 31/08/2026, apps novos e atualizações precisam mirar Android 16 (API 36) ou superior. O Listinha mira 36: **ok**. Extensão possível até 01/11/2026 já passou.
- Android vitals: limites de mau comportamento 1,09% (falha percebida) e 0,47% (ANR percebido) no geral, 8% por modelo: **inalterados**. Novidade: limites de memória e bitmap e de otimização de DEX passam a afetar a visibilidade a partir de fevereiro de 2027.
- Relatório de pré-lançamento (support.google.com): testa estabilidade, compatibilidade, desempenho e acessibilidade com um robô que navega por vários minutos; a página não informa data de atualização. A URL antiga em developer.android.com respondeu 404.
- Firebase Test Lab (Spark): 15 execuções/dia (10 virtuais, 5 físicas): mesmo número do arquivo do agente.

## Lacunas (não testado)

- Login com Google real (depende da instalação pela Play, aparelho real, feito pelo Dany); excluir conta de ponta a ponta.
- Execução do APK de release (assinatura, tempo de abertura); relatório de pré-lançamento e Android vitals (só existem depois do envio).
- minSdk 26 e API 36 estável; aparelho real; Firebase Test Lab.
- Foto grande da galeria; editar e excluir receita por clique; Accessibility Scanner e TalkBack de verdade.
- Compose UI Test (`androidTest`): não criado neste ciclo; a automação foi por `uiautomator`.
- Regras: não há teste de validação de campos porque as regras não os validam.

## Próximo ciclo (sugestão)

Rodar o mesmo roteiro após as correções (bugs 1 a 4), acrescentar edição/exclusão de receita e foto grande, repetir o monkey com novas sementes e fazer o fluxo de release pela faixa de teste interno com a conta real do Dany.

---

# Reteste do ciclo 1 (09/11/2026, mesma data, build com as correções aplicadas em `app/src/main`)

Aparelho `Listinha_Teste`, emuladores `demo-listinha`. `Pixel_8` não ligado. Debug rebuildado (contém `RecipesNavState`, conferido no DEX instalado), depois build debug com R8 (`-Pr8debug=true`).
Reproduzível com `tests/android/reg.py` (regressão) e `tests/android/rot.py` (rotação).

## Veredito por bug

| # | Resultado | Evidência |
|---|---|---|
| 1 Excluir conta silencioso | **Resolvido** | Perfil, Excluir conta, Excluir tudo sem credencial Google: Toast "Não foi possível excluir a conta. Tente novamente."; os dados continuam no banco. |
| 2 Girar perde receita/timer | **NÃO resolvido** (resolvido só no formulário) | Formulário (novo e edição) sobrevive à rotação e o texto digitado fica. Detalhe e Cozinhar continuam voltando à grade; o timer some. Enviar à lista e voltar à aba Receitas abre a grade: ok. Causa provável abaixo. |
| 3 Boas-vindas | **Resolvido em 360dp; regressão em tela normal (novo bug R1)** | 360dp 100% e 200%: sem sobreposição, botão "Continuar com Google" inteiro, logo menor. Em 1080x2400 a 420 dpi a notinha **não aparece** (deveria). |
| 4 Cursor do orçamento | **Resolvido** | Editar R$ 50 e digitar 9 dá `509`. Campos de adicionar item (limpa após adicionar), editar item (`CafeX`, cursor no fim) e orçamento sem regressão. Acento e teclado composto: não testado (`input text` só ASCII). Busca de receitas: não exercitada (a barra só aparece com várias receitas). |
| 5 Fonte 200% / 360dp | **Resolvido em 360dp a 100%; parcialmente a 200%** | 100%: botões do detalhe e rótulo "Porções" inteiros. 200%: abas e botões do detalhe ok (2 linhas), título "No mercado" sem quebra no meio. Ainda: nome do item e "unidades" quebram letra a letra ("Mor/an...", "unid/ades"), "Anterior" vira "Anterio/r" no Cozinhar, rótulos "Tem" e "Porç" cortados no formulário, "Sorvete de 2 ingredi...". |
| 6 CheckBox sem rótulo / alvos | **Resolvido** | Todos os CheckBox agora têm descrição com o nome do item e medem 126x126 px (48 dp). Nenhum clicável da lista abaixo de 48 dp. |
| 8 Asterisco e nome vazio | **Resolvido** | Modo compra: `Vamos lá, Ana!` com nome `Ana*` e `Lista completa, Ana!` com `*Ana Maria`; avatar `A` (antes `*`); nome vazio cai em "você". |
| 9 Risco do item riscado | **Resolvido** (resíduo de poucos px nas pontas) | Risco acompanha a largura do texto truncado. |

### Item 6: identificação dos nós
- **Os 100 px (38 dp)**: três nós do ciclo 1, todos de linhas de item **sem segunda linha** (sem "2 unidades"): o `CheckBox` (126x100 px), o `View` clicável do nome/linha para editar (433x100 px) e o `View` clicável do botão Apagar (126x99 px). Eram o hit-box das linhas de uma linha só. No reteste não aparece mais nenhum clicável abaixo de 125 px na lista (checkbox 126x126).
- **O clicável de 31 dp no Perfil** (974x82 px, sem texto nem descrição próprios): é o botão **"Sair do app"** (texto filho "Sair do app" em 427..654, 2121..2145). Os 82 px são só a fatia visível: a base do botão fica atrás da barra de abas (a tela precisa rolar). Não era um alvo pequeno, era um botão cortado pela viewport; **não é bug de alvo de toque**, mas o botão ficar meio escondido sob a barra de abas na rolagem inicial do Perfil é o que se vê na captura. Retiro o relato de "alvo de 31 dp".

### Bug 2: causa provável do que sobrou
`RecipesScreen.kt`: `LaunchedEffect(userId) { vm.start() }` roda de novo depois de girar a tela e `RecipeViewModel.start()` faz `recipes = emptyList()`. Na rota `Detail`, `vm.recipes.find { it.id == r.recipeId }` retorna `null` e o ramo "Receita sumiu" faz `route = Grid`. A rota até sobrevive no `RecipesNavState`, mas é descartada antes da lista voltar do Firestore. O formulário não depende da lista (carrega o objeto), por isso sobrevive. Cozinhar mora dentro do detalhe, então cai junto. Sugestão: só chamar `vm.start()` se o uid mudou de fato (ou não limpar `recipes` quando o uid é o mesmo) e/ou só tratar "receita sumiu" depois do primeiro carregamento.

## Novo problema encontrado no reteste

**R1 (médio): notinha escondida na tela normal.** `LoginScreen.kt`: `.onSizeChanged { alturaNota = it.height }` vem **antes** de `.padding(bottom = alturaPainel + 28.dp)` na cadeia de modificadores, então `alturaNota` inclui o espaçamento inferior (cerca de a altura do painel) e `cabe` fica falso. Em 1080x2400/420 dpi o resultado é a notinha invisível (`capturas/reteste_login_normal_sem_notinha.png`), na tela real (logout no app em modo emulador) e na vitrine. Sugestão: medir a nota depois dos paddings ou subtrair o padding no cálculo. Em 360dp esconder está certo.

## Regressão curta (build normal e build R8): 11 de 12 passos passaram

Passaram em ambas: adicionar com preço e quantidade, marcar e desmarcar, editar preço, apagar, orçamento (definir, editar com vírgula, inválido, Remover), limpar comprados, modo compra (marcar tudo, Concluir, aba e volta), tema (grava e persiste após matar o app), receita por ACTION_SEND com regex ICU (8 porções, 50 min, 7 ingredientes, 4 passos), porções, "já tenho" (Adicionar 6), enviar à lista, Substituir minha lista, cozinhar com timer.
Um passo não foi executado: busca de receitas (a barra de busca só existe com várias receitas; falha do roteiro, não do app).

## Monkey (build normal): semente 20261110, 3.000 eventos, throttle 100 ms
Sem crash, sem ANR, sem `ClassNotFound`/`NoSuchMethod` no logcat.

---

# Build com R8 ligado (debug, `-Pr8debug=true`)

Ainda com `EmulatorBootProvider`. APK debug 16,8 MB (DEX 13,1 MB em 2 arquivos; a release não assinada já compilada tem 4,3 MB, DEX único de 5,4 MB). Provider, 11 fontes (3 Fraunces, 3 Fredoka, 5 Nunito) e 4 fotos (`welcome_burger`, `empty_cookies`, `empty_strawberry`, `profile_cover`) presentes.
Ícone adaptativo: `ic_launcher` com `background`, `foreground` e `monochrome` (`ic_launcher_monochrome`) mantidos pelo encolhimento de recursos (conferido com `aapt2`).

| Teste na build R8 | Resultado |
|---|---|
| Regressão (12 passos, mesmo script) | 11 de 12 (o mesmo passo da busca) |
| Vitrine, 10 telas em claro | Fraunces, Fredoka e Nunito renderizando, fotos do hambúrguer, morangos, biscoitos e toranja presentes; sem diferença visual em relação à build normal (`capturas/r8_sheet_claro_*.jpg`) |
| Rotação (rota em ViewModel) | Igual à build normal: detalhe e Cozinhar voltam à grade (bug 2 continua; não é efeito do R8) |
| Monkey semente 31415, 3.000 eventos | Sem crash e sem ANR |
| Logcat (fluxos + monkey) | **0** ocorrências de `ClassNotFoundException`, `NoSuchMethodException`, `NoClassDefFoundError`, `FATAL EXCEPTION`, `Resources$NotFoundException` |

Release R8 não assinada (já compilada pelo coordenador, inspecionada sem recompilar): `PreviewActivity`, `EmulatorBootProvider`, `demo-listinha`, `10.0.2.2` e `emulator_mode` ausentes; 11 fontes e 4 fotos presentes; 221 arquivos. A release **não foi executada** (falaria com produção). O bug 7 (R8/DEX acima de 10 MB) fica resolvido do ponto de vista do tamanho (DEX 5,4 MB). Fica a lacuna: o `.aab`/APK de release R8 precisa ser exercitado na faixa de teste interna, pois só o debug com R8 foi executado.

---

# Reteste final (segunda leva de correções)

Mesmo ambiente (AVD `Listinha_Teste`, emuladores `demo-listinha`, `Pixel_8` não ligado). Scripts: `tests/android/t2.py` (rotação), `t3.py` (sincronização e troca de usuário), `t4.py` (busca), `reg.py` (regressão).

| Ponto | Resultado | Evidência |
|---|---|---|
| Bug 2 detalhe | **Resolvido** | Receita aberta, 9 porções e "já tenho" em Ovos (botão "Adicionar 6 à lista"): após girar para paisagem e voltar, continua no detalhe com 9 porções e "Adicionar 6 à lista". |
| Bug 2 Cozinhar | **Resolvido** | Timer 39:56 no passo 3: em paisagem 39:51, depois 39:44, de volta ao retrato 39:39; segue no passo 3. |
| Risco: sincronia do mesmo usuário | **OK** | Com o app aberto, item gravado direto no banco do emulador aparece na lista em 3 s; receita apagada no banco some da grade. |
| Risco: sair e entrar com outro usuário | **OK no que dá para testar** | Sair do app mostra o login; reabrir como `outro@listinha.test` mostra lista e receitas vazias (nada do usuário anterior). |
| Risco: sair e entrar com o mesmo usuário | **Não testável de ponta a ponta** | A volta dentro do mesmo processo exige o login Google real. Evidência indireta: ao sair, as duas assinaturas terminam com erro de permissão (`ListViewModel: Erro ao observar itens`, `RecipeViewModel: Erro ao observar receitas`), então `observeJob.isActive` fica falso e o `start(userId)` voltaria a assinar. Confirmar na faixa de teste com a conta real. |
| R1 notinha | **Resolvido** | 1080x2400/420dpi: "Smash burger" visível sobre a foto sem cobrir título nem painel; 360dp a 100% e 200%: oculta e sem sobreposição. |
| Bug 5 em 360dp e 200% | **Resolvido nos pontos pedidos** | Item "Morango" inteiro, "2 unidades" inteiro e preço abaixo do nome (em fonte 100% o preço fica à direita); "Anterior" inteiro no Cozinhar; "Tempo" legível e "Porções" empilhado abaixo no formulário (só "Tempo" aparece na captura; o "Porções" ficou abaixo da dobra); "Sorvete de 2 ingredientes" inteiro na grade. |
| Busca de receitas | **Resolvido** | Com 6 receitas: "feijao" acha "Feijão tropeiro"; "ACAI" acha "Açaí na tigela"; "bolo" acha as duas de bolo; "zzz" mostra "Nenhuma receita encontrada."; limpar volta a lista. |

Regressão nova: nenhuma.

## Build com R8 (`-Pr8debug=true`), segunda rodada

- Rotação (mesmo roteiro do detalhe e do Cozinhar): igual à build normal, **resolvido** (detalhe com 9 porções e "Adicionar 6", timer 39:56, 39:51, 39:43, 39:38).
- Regressão: **12 de 12** (a busca agora com várias receitas; "feijao" acha "Feijão tropeiro").
- Logcat dos fluxos: 0 ocorrências de `ClassNotFound`, `NoSuchMethod`, `NoClassDefFound`, `FATAL EXCEPTION`, `ANR`, `Resources$NotFound`.
- Monkey: semente 271828 (com troca de app) parou no evento 145 por um **crash do Pixel Launcher** (`SearchEditText.onKeyDown`, NullPointerException em `com.google.android.apps.nexuslauncher`), não do Listinha. Repeti com `--pct-appswitch 0 --pct-syskeys 0`, semente **161803**, 3.000 eventos: concluiu, 0 crash e 0 ANR, 0 erros de classe ou recurso.
- Observação: o APK de debug com R8 desta rodada tem exatamente os mesmos 16.817.667 bytes da rodada anterior; o comportamento novo (rotação) funcionou nele, então o código novo está dentro.
