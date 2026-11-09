import traceback, json, os
RES = []
def passo(nome):
    def deco(f):
        try:
            f(); RES.append((nome, "OK", ""))
        except AssertionError as e: RES.append((nome, "FALHOU", str(e)))
        except Exception as e: RES.append((nome, "ERRO", repr(e)[:300]))
        print(RES[-1][1], "-", nome, RES[-1][2])
        return f
    return deco
def salvar(arq):
    json.dump(RES, open(arq, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
