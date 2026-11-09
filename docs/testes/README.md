# Como rodar os testes do Listinha

Tudo aqui roda **sem produção**: o Firebase Emulator Suite usa o projeto falso `demo-listinha`, e o app de debug
nunca lê o `google-services.json` real quando está em "modo emulador". Nada de conta do Dany, nada de `firebase deploy`.

## 1. Testes de unidade (JUnit, no PC)

```bash
GRADLE_BIN="/c/Users/danyy/.gradle/wrapper/dists/gradle-8.11.1-bin/bpt9gzteqjrbo1mjrsomdt32c/gradle-8.11.1/bin/gradle"
JAVA_HOME="/c/Users/danyy/.jdks/jbr-21.0.11" "$GRADLE_BIN" -Dorg.gradle.java.home="C:/Users/danyy/.jdks/jbr-21.0.11" -p . testDebugUnitTest --console=plain
```

Cobrem parser de receitas, culinária, `formatCurrency`, categorias, `Item.total` e `brandText`.
Atenção: teste JVM não prova regex no Android (ICU). A aba Receitas precisa ser conferida no aparelho (ver item 4).

## 2. Regras do Firestore (Emulator Suite)

Requer Node, `firebase-tools` e Java 21+ (já instalados). Rodar da raiz do repositório:

```bash
cd tests/firebase && npm install && cd ../..
firebase emulators:exec --project demo-listinha --only firestore "npm --prefix tests/firebase run test:rules"
```

`firebase.json` e `.firebaserc` na raiz só definem os emuladores e apontam `firestore.rules`.
Se der "port taken", sobrou um emulador antigo (processo `java` com `cloud-firestore-emulator`): encerre-o.

## 3. App real contra os emuladores (só debug)

1. Suba os emuladores: `firebase emulators:start --project demo-listinha --only auth,firestore`
2. Use um AVD **separado** da sua conta (ex.: `Listinha_Teste`, criado sem a conta do Dany). Nunca o `Pixel_8` com a conta real.
3. Instale o debug e ligue o modo emulador (o arquivo-marcador é o interruptor):

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell run-as com.danyks.listinha sh -c 'mkdir -p files; echo "teste@listinha.test" > files/emulator_mode'
adb shell am start -S -n com.danyks.listinha/com.listinha.app.MainActivity
```

Como é seguro: `app/src/debug/.../EmulatorBootProvider.kt` roda antes do Firebase, cria o `FirebaseApp` padrão com projeto
`demo-listinha` e chave falsa, aponta Auth e Firestore para `10.0.2.2` (o PC) e entra com um usuário falso. Sem o
arquivo `files/emulator_mode` o provider não faz nada. O log mostra `EMULADOR LIGADO projeto=demo-listinha firestore=10.0.2.2:8080`
(`adb logcat -s EmuBoot`). O provider está em `src/debug`: não entra no APK de release.

Automação dos fluxos (Python + `uiautomator`, localiza por texto, confere o banco do emulador por REST): `tests/android/h.py`.
Exige `adb` no PATH e `MSYS_NO_PATHCONV=1` no Git Bash.

## 4. Vitrine de telas com dados falsos (sem Firebase)

```bash
adb shell am start -S -n com.danyks.listinha/com.listinha.app.debug.PreviewActivity --es tela lista --es tema claro
```

Telas: `boasvindas, lista, vazia, compra, receitas, semreceitas, detalhe, nova, cozinhar, perfil`. Temas: `claro`, `escuro`.

## 5. Relatório da campanha

Ver `docs/testes/campanha-2.0.md`.
