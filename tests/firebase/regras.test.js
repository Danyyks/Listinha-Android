// Testes das regras do Firestore (firestore.rules) no Emulator Suite.
// Projeto falso "demo-listinha": impossível falar com produção.
import { readFileSync } from 'node:fs';
import { afterAll, beforeAll, beforeEach, describe, test } from 'vitest';
import {
  assertFails, assertSucceeds, initializeTestEnvironment,
} from '@firebase/rules-unit-testing';
import {
  addDoc, collection, collectionGroup, deleteDoc, doc, getDoc, getDocs, query, setDoc, updateDoc, where,
} from 'firebase/firestore';

let env;
const DONO = 'dono123';
const OUTRO = 'outro456';

beforeAll(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-listinha',
    firestore: { rules: readFileSync(new URL('../../firestore.rules', import.meta.url), 'utf8'), host: '127.0.0.1', port: 8080 },
  });
});
afterAll(async () => { await env.cleanup(); });
beforeEach(async () => {
  await env.clearFirestore();
  // Dados do dono, semeados sem passar pelas regras.
  await env.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await setDoc(doc(db, `users/${DONO}`), { budget: 100, theme: 'auto' });
    await setDoc(doc(db, `users/${DONO}/items/i1`), { name: 'Leite', price: 5, quantity: 1, bought: false });
    await setDoc(doc(db, `users/${DONO}/priceHistory/p1`), { nameKey: 'leite', price: 5 });
    await setDoc(doc(db, `users/${DONO}/recipes/r1`), { name: 'Bolo', servings: 4 });
  });
});

// Coleções realmente usadas pelo app (ShoppingRepository, RecipeRepository, AuthViewModel.deleteAccount).
const CAMINHOS = [
  ['perfil', `users/${DONO}`],
  ['items', `users/${DONO}/items/i1`],
  ['priceHistory', `users/${DONO}/priceHistory/p1`],
  ['recipes', `users/${DONO}/recipes/r1`],
];

describe.each(CAMINHOS)('coleção %s', (nome, caminho) => {
  const pasta = caminho.split('/').slice(0, -1).join('/') || null;
  test('deve permitir ao dono ler, atualizar, criar e apagar', async () => {
    const db = env.authenticatedContext(DONO).firestore();
    await assertSucceeds(getDoc(doc(db, caminho)));
    await assertSucceeds(setDoc(doc(db, caminho), { x: 1 }, { merge: true }));
    await assertSucceeds(updateDoc(doc(db, caminho), { y: 2 }));
    await assertSucceeds(deleteDoc(doc(db, caminho)));
    if (nome !== 'perfil') await assertSucceeds(setDoc(doc(db, caminho + '_novo'), { z: 3 }));
  });
  test('deve negar leitura e escrita a outro usuário logado', async () => {
    const db = env.authenticatedContext(OUTRO).firestore();
    await assertFails(getDoc(doc(db, caminho)));
    await assertFails(setDoc(doc(db, caminho), { hack: true }, { merge: true }));
    await assertFails(updateDoc(doc(db, caminho), { hack: true }));
    await assertFails(deleteDoc(doc(db, caminho)));
    if (nome !== 'perfil') await assertFails(setDoc(doc(db, caminho + '_novo'), { hack: true }));
  });
  test('deve negar leitura e escrita sem login', async () => {
    const db = env.unauthenticatedContext().firestore();
    await assertFails(getDoc(doc(db, caminho)));
    await assertFails(setDoc(doc(db, caminho), { hack: true }, { merge: true }));
    await assertFails(updateDoc(doc(db, caminho), { hack: true }));
    await assertFails(deleteDoc(doc(db, caminho)));
  });
});

describe('listagens e consultas', () => {
  test('dono lista os próprios itens e lê de volta o que gravou', async () => {
    const db = env.authenticatedContext(DONO).firestore();
    const snap = await assertSucceeds(getDocs(collection(db, `users/${DONO}/items`)));
    if (snap.size !== 1) throw new Error('esperava 1 item, veio ' + snap.size);
    await assertSucceeds(getDocs(query(collection(db, `users/${DONO}/items`), where('bought', '==', true))));
    await assertSucceeds(addDoc(collection(db, `users/${DONO}/recipes`), { name: 'Pão' }));
  });
  test('outro usuário não lista itens, receitas nem histórico do dono', async () => {
    const db = env.authenticatedContext(OUTRO).firestore();
    for (const c of ['items', 'recipes', 'priceHistory'])
      await assertFails(getDocs(collection(db, `users/${DONO}/${c}`)));
  });
  test('anônimo não lista nada nem escreve em coleção do dono', async () => {
    const db = env.unauthenticatedContext().firestore();
    await assertFails(getDocs(collection(db, `users/${DONO}/items`)));
    await assertFails(addDoc(collection(db, `users/${DONO}/items`), { name: 'x' }));
  });
  test('ninguém lista a coleção raiz users (nem o dono)', async () => {
    await assertFails(getDocs(collection(env.authenticatedContext(DONO).firestore(), 'users')));
    await assertFails(getDocs(collection(env.authenticatedContext(OUTRO).firestore(), 'users')));
    await assertFails(getDocs(collection(env.unauthenticatedContext().firestore(), 'users')));
  });
  test('consulta collectionGroup é negada (vazaria itens de todos)', async () => {
    const db = env.authenticatedContext(OUTRO).firestore();
    await assertFails(getDocs(collectionGroup(db, 'items')));
    await assertFails(getDocs(collectionGroup(db, 'recipes')));
  });
});

describe('caminhos fora da gaveta do usuário', () => {
  test('qualquer outra coleção raiz fica negada, mesmo logado', async () => {
    const db = env.authenticatedContext(DONO).firestore();
    await assertFails(setDoc(doc(db, 'publico/x'), { a: 1 }));
    await assertFails(getDoc(doc(db, 'publico/x')));
    await assertFails(setDoc(doc(db, 'admin/config'), { a: 1 }));
  });
  test('dono não escreve na gaveta de outro uid', async () => {
    const db = env.authenticatedContext(DONO).firestore();
    await assertFails(setDoc(doc(db, `users/${OUTRO}`), { a: 1 }));
    await assertFails(setDoc(doc(db, `users/${OUTRO}/items/i9`), { a: 1 }));
  });
  test('subcoleção desconhecida do dono é permitida só a ele (futuro)', async () => {
    const dono = env.authenticatedContext(DONO).firestore();
    await assertSucceeds(setDoc(doc(dono, `users/${DONO}/novaColecao/a`), { a: 1 }));
    await assertFails(getDoc(doc(env.authenticatedContext(OUTRO).firestore(), `users/${DONO}/novaColecao/a`)));
  });
  test('token com uid vazio ou parecido não passa', async () => {
    const db = env.authenticatedContext(DONO + 'x').firestore();
    await assertFails(getDoc(doc(db, `users/${DONO}`)));
  });
});

describe('fluxo de exclusão de conta (AuthViewModel.deleteAccount)', () => {
  test('dono apaga itens, histórico, receitas e o próprio perfil', async () => {
    const db = env.authenticatedContext(DONO).firestore();
    for (const c of ['items', 'priceHistory', 'recipes']) {
      const snap = await assertSucceeds(getDocs(collection(db, `users/${DONO}/${c}`)));
      for (const d of snap.docs) await assertSucceeds(deleteDoc(d.ref));
    }
    await assertSucceeds(deleteDoc(doc(db, `users/${DONO}`)));
  });
});
