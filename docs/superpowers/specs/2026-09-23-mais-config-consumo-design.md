# Mais, configurações e consumo (gramas / R$)

**Status:** aprovado pelo usuário (2026-09-23), pendente só preço default da carteira  
**Versão alvo:** próximo release visível (Play + histórico no app)

## Objetivo

Reorganizar a aba **Mais**, separar **Configurações** e **Sobre**, e deixar o usuário escolher **Cannabis** ou **Cigarro** com defaults sensatos. A meta diária continua sendo **quantidade de toques** (sessões ou cigarros). Abaixo de `N de M` na Home, mostrar **gramas fumadas hoje** ou **R$ gastos hoje**, para screenshot e atualização da Play Store.

## Decisões fechadas

| # | Decisão |
|---|---------|
| Tipos v1 | Só **Cannabis** e **Cigarro** |
| Default produto | **Cannabis** |
| Gramas por sessão (bowl XL Solo 3) | **0,3 g** (editável) |
| Cigarros por carteira | **20** (editável) |
| Meta diária | Sempre **contagem de toques** (`N de M`), independente do tipo |
| 1 toque | 1 bowl (cannabis) ou 1 cigarro |
| Persistência de puffs | **Nunca apagar** `fumei.db` em update; sem `fallbackToDestructiveMigration` |
| Campos novos | Só **SharedPreferences** (ou DataStore futuro); na primeira leitura após update, **defaults**; histórico de eventos intacto |
| Home UI | Linha principal: `N de M`. Linha secundária: **≈ X,X g fumadas hoje** ou **R$ X,XX gastos hoje** |
| Play Store | Após implementação: novo screenshot da Home, revisar short/full description e notas (cannabis + cigarro, offline) |

## Navegação (aba Mais)

```
Bottom "Mais" → Hub (duas entradas)
  ├── Configurações
  │     ├── Tipo: Cannabis | Cigarro
  │     ├── Meta diária (+ / −, como hoje)
  │     ├── Se Cannabis: gramas por sessão (default 0,3)
  │     └── Se Cigarro: cigarros por carteira (20) + preço da carteira (R$)
  └── Sobre (como hoje, sem meta)
        Pix, versão, novidades
```

Implementação: pilha Compose dentro da aba (`remember` back stack ou `NavHost` aninhado), botão voltar no topo.

## Cálculos

### Cannabis

- `gramasHoje = registrosHoje × gramasPorSessão`
- Formato pt-BR: uma casa decimal se fizer sentido (`0,9 g fumadas hoje` ou `2,4 g fumadas hoje`)

### Cigarro

- `precoPorCigarro = precoCarteira / cigarrosPorCarteira`
- `gastoHoje = registrosHoje × precoPorCigarro`
- Formato: `R$ 3,60 gastos hoje` (moeda BRL, 2 decimais)

### Meta

- Não muda: `DailyProgress.label(count, dailyGoal)` → `3 de 8`

## Persistência e migração

### Room (`PuffEntity`)

- Manter **version 1** enquanto o evento for só timestamp (sem colunas novas obrigatórias).
- Testes instrumentados existentes: upgrade DB preserva puffs.

### Preferências (novas chaves)

| Chave | Tipo | Default |
|-------|------|---------|
| `product_type` | enum string | `CANNABIS` |
| `grams_per_session` | float | `0.3f` |
| `cigarettes_per_pack` | int | `20` |
| `pack_price_brl` | float | **TBD** (ver abaixo) |

Leitura: se chave ausente → default, nunca crash. Escrita: normalizar (gramas > 0, carteira 1..40, preço ≥ 0).

### Histórico vs troca de tipo

- **Eventos:** intocáveis (só contagem por dia).
- **Métricas derivadas (g / R$):** v1 calculadas com **configuração atual** × contagem de cada dia (limitação conhecida: trocar de cannabis para cigarro reinterpreta dias antigos na estatística derivada). Documentar na Config se necessário. Evolução futura: snapshot por registro ou por dia na Room v2.

## UI Home (`DiaHeader`)

Ordem vertical (centro):

1. Anel + número HOJE  
2. `N de M` (`daily_progress_label`)  
3. **Novo** `daily_secondary_metric` (testTag para E2E)  
4. Label do dia (ontem etc.)

Copy sugerido:

- Cannabis: `{gramas} g fumadas hoje`
- Cigarro: `R$ {valor} gastos hoje`

## Estatísticas

- Repetir totais derivados no período visível (dia/mês/ano): gramas ou R$ além da contagem, mesma fórmula × contagem do período.
- Escopo v1: incluir na mesma entrega que a Home (evita Play screenshot desalinhado do app).

## Google Play / conteúdo

- Permitido: app de **registro pessoal**, sem venda/entrega, tom neutro.
- Usar **“Cannabis”** na UI (confirmado).
- Atualizar: `docs/play-store/metadata/pt-BR/*`, screenshots (`docs/play-store/assets/`), `listing.md`, notas de release.
- Declaração **Health apps** no Console: acompanamento de hábito; sem claims médicos.

## Testes (TDD)

- Unit: funções de cálculo gramas/gasto + defaults prefs  
- E2E: hub Mais → Config; troca tipo; Home mostra linha secundária correta; `aboutTab` / meta movida para Config  
- Instrumentado: prefs ausentes após “upgrade” simulado → defaults  

## Fora de escopo v1

- Tipo “Outro”  
- Meta em gramas ou em R$  
- Contador “3 de 20 na carteira” na Home (opcional depois; gasto em R$ já responde parte do pedido)  
- Snapshot histórico por tipo (Room v2)

## Pendência única do product owner

**Preço default da carteira de cigarros (R$):** sugerido **R$ 12,00** (editável). Confirmar ou informar outro valor.

## Próximos passos (ordem)

1. Confirmar preço default da carteira  
2. `writing-plans` → plano de implementação  
3. Código + testes + emulador  
4. Screenshot Home → assets Play  
5. `play-release.ps1` / listing quando for publicar  
