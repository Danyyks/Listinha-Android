"""Ajudante de automação: localiza por texto/descrição (nunca por coordenada fixa) e confere o banco do emulador via REST."""
import os, re, subprocess, time, json, urllib.request
ADB = os.path.expandvars(r"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe")
PKG = "com.danyks.listinha"
def adb(*a, check=True):
    return subprocess.run([ADB, *a], check=check, stdout=subprocess.PIPE, stderr=subprocess.PIPE).stdout.decode("utf-8", "replace")
def dump():
    adb("shell", "uiautomator", "dump", "/data/local/tmp/ui.xml")
    xml = adb("exec-out", "cat", "/data/local/tmp/ui.xml")
    nos = []
    for no in re.findall(r"<node [^>]*>", xml):
        g = lambda k: (re.search(k + r'="([^"]*)"', no) or [None, ""])[1]
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', no)
        nos.append(dict(text=g("text"), desc=g("content-desc"), id=g("resource-id"), clickable=g("clickable") == "true", cls=g("class"),
                        b=tuple(map(int, b.groups())) if b else (0,0,0,0), enabled=g("enabled") == "true", checked=g("checked")=="true"))
    return nos
def textos():
    return [n["text"] or n["desc"] for n in dump() if (n["text"] or n["desc"])]
def achar(txt, exato=True, nos=None):
    for n in (nos or dump()):
        for v in (n["text"], n["desc"]):
            if v and ((v == txt) if exato else (txt.lower() in v.lower())):
                return n
    return None
def centro(n):
    a, b, c, d = n["b"]; return (a + c) // 2, (b + d) // 2
def tocar(txt, exato=True, espera=1.0):
    n = achar(txt, exato)
    if not n: raise RuntimeError(f"nao achei: {txt!r}. Tela: {textos()[:25]}")
    x, y = centro(n); adb("shell", "input", "tap", str(x), str(y)); time.sleep(espera); return n
def digitar(txt, espera=0.6):
    adb("shell", "input", "text", txt.replace(" ", "%s")); time.sleep(espera)
def tecla(k, espera=0.6): adb("shell", "input", "keyevent", str(k)); time.sleep(espera)
def foto(nome, pasta="."):
    p = os.path.join(pasta, nome + ".png")
    with open(p, "wb") as f: f.write(subprocess.run([ADB, "exec-out", "screencap", "-p"], stdout=subprocess.PIPE).stdout)
    return p
def abrir(extra=()):
    adb("shell", "am", "start", "-S", "-n", PKG + "/com.listinha.app.MainActivity", *extra); time.sleep(6)
def aberto(): return PKG in adb("shell", "dumpsys", "activity", "activities") .split("topResumedActivity")[-1][:300] if True else False
def pid(): return adb("shell", "pidof", PKG).strip()
def crashes():
    return [l for l in adb("logcat", "-d", "-b", "crash").splitlines() if PKG in l or "FATAL" in l]
# --- banco (emulador) lido com o token "owner", que ignora as regras: é a verdade do que foi gravado ---
def _fs(path):
    req = urllib.request.Request(f"http://127.0.0.1:8080/v1/projects/demo-listinha/databases/(default)/documents/{path}", headers={"Authorization": "Bearer owner"})
    try: return json.load(urllib.request.urlopen(req))
    except urllib.error.HTTPError as e: return {"erro": e.code}
def val(v):
    k, x = next(iter(v.items()))
    if k == "mapValue": return {a: val(b) for a, b in x.get("fields", {}).items()}
    if k == "arrayValue": return [val(i) for i in x.get("values", [])]
    if k in ("integerValue",): return int(x)
    if k == "doubleValue": return float(x)
    return x
def docs(path):
    r = _fs(path); return [dict(id=d["name"].split("/")[-1], **{k: val(v) for k, v in d.get("fields", {}).items()}) for d in r.get("documents", [])]
def uid():
    r = _fs("users"); ds = r.get("documents", [])
    return ds[0]["name"].split("/")[-1] if ds else None
def itens(): u = uid(); return docs(f"users/{u}/items") if u else []
def receitas(): u = uid(); return docs(f"users/{u}/recipes") if u else []
def perfil(): u = uid(); return {k: val(v) for k, v in _fs(f"users/{u}").get("fields", {}).items()} if u else {}
def checkbox_de(nome):
    nos = dump()
    for t in nos:
        if t["text"] != nome and t["desc"] != nome: continue
        cy = (t["b"][1] + t["b"][3]) // 2
        for n in nos:
            if n["cls"].endswith("CheckBox") and n["b"][1] <= cy <= n["b"][3]: return n
    raise RuntimeError("sem checkbox de " + nome)
def marcar(nome, espera=1.5):
    x, y = centro(checkbox_de(nome)); adb("shell", "input", "tap", str(x), str(y)); time.sleep(espera)
def item(nome):
    for i in itens():
        if i["name"] == nome: return i
def na_tela(txt): return achar(txt, True) is not None
def limpar_campo(n=40):
    adb("shell", "input", "keyevent", *(["22"] * n), *(["67"] * n))
def _del(url):
    req = urllib.request.Request(url, method="DELETE"); urllib.request.urlopen(req).read()
def reset(email="teste@listinha.test", nome="Teste Silva", abrir_app=True):
    """Limpa app + emuladores (Firestore e Auth) e reabre logado como usuario falso."""
    adb("shell", "am", "force-stop", PKG)
    adb("shell", "pm", "clear", PKG)
    _del("http://127.0.0.1:8080/emulator/v1/projects/demo-listinha/databases/(default)/documents")
    _del("http://127.0.0.1:9099/emulator/v1/projects/demo-listinha/accounts")
    adb("shell", "run-as", PKG, "sh", "-c", f"'mkdir -p files; echo \"{email}\" > files/emulator_mode'")
    if nome is not None:  # cria a conta ja com displayName, direto no Auth Emulator (nunca producao)
        body = json.dumps({"email": email, "password": "senha-de-teste-local-123", "displayName": nome, "returnSecureToken": False}).encode()
        urllib.request.urlopen(urllib.request.Request("http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/accounts:signUp?key=fake", data=body, headers={"Content-Type": "application/json"})).read()
    if abrir_app: abrir()
def add(nome, preco=None, mais=0):
    tocar("Adicionar item"); digitar(nome)
    for _ in range(mais): tocar("Aumentar", espera=0.4)
    if preco: tocar("Preço", espera=0.4); digitar(preco); tecla(4)
    tocar("Adicionar à lista", espera=2)
def semear_itens(n, prefixo="Item", nome_longo=None):
    """Grava n itens direto no Firestore Emulator (token owner) para testar lista grande."""
    u = uid(); cats = ["frutas", "verduras", "carnes", "laticinios", "padaria", "mercearia", "outros"]
    for i in range(n):
        nome = nome_longo if (nome_longo and i == 0) else f"{prefixo} {i+1}"
        body = {"fields": {"name": {"stringValue": nome}, "category": {"stringValue": cats[i % len(cats)]}, "price": {"doubleValue": (i % 20) + 0.99},
                           "quantity": {"doubleValue": 1.0}, "bought": {"booleanValue": i % 3 == 0}, "createdAt": {"timestampValue": f"2026-11-09T10:{(i//60)%60:02d}:{i%60:02d}Z"}}}
        req = urllib.request.Request(f"http://127.0.0.1:8080/v1/projects/demo-listinha/databases/(default)/documents/users/{u}/items?documentId=seed{i}", data=json.dumps(body).encode(), headers={"Authorization": "Bearer owner", "Content-Type": "application/json"}, method="POST")
        urllib.request.urlopen(req).read()
def teclado(): return "mInputShown=true" in adb("shell", "dumpsys", "input_method")
def fechar_teclado():
    if teclado(): tecla(4, 0.8)
