# Fumei — plano de redesign

## Por que parece "app genérico"

A tela atual usa exatamente o kit padrão do Material Design sem nenhuma escolha própria:

- Card verde-menta com cantos arredondados + número grande centralizado = o template de "app de bem-estar" que qualquer gerador de UI produz para qualquer coisa (hábitos, água, meditação, sono). Não fala especificamente sobre cigarro.
- Lista com ícone de relógio cinza, lápis verde e lixeira vermelha em todas as linhas — paleta semântica genérica (editar = verde, apagar = vermelho) que não tem relação com o assunto.
- FAB verde "+ Fumei" ocupando a largura toda: um botão de ação primária padrão, sem personalidade.
- Bottom nav com os 3 itens clássicos (Home / Stats / Info) em ícones de biblioteca, sem nada que ancore no tema.
- Tipografia: uma única família sans do sistema, um peso, sem hierarquia real além de tamanho.

Nada disso está "errado", mas também não poderia ser distinguido de um app de beber água. O app trata de um hábito bem específico e carregado (cigarro), e o visual não usa nada desse universo — fumaça, brasa, cinza, tempo entre cigarros.

## Direção nova

**Metáfora**: cinza e brasa. Em vez de "wellness mint", o app assume o material do próprio hábito — cinza escuro de fundo, laranja de brasa como único acento, e o registro do dia vira uma **linha do tempo de brasa** (uma trilha vertical com um ponto por cigarro), não uma lista de cards com dois ícones.

### Tokens de cor

| Token | Hex | Uso |
|---|---|---|
| `ash950` | `#1B1918` | fundo |
| `ash850` | `#24211F` | superfície / cards |
| `ash750` | `#322D29` | superfície elevada, divisores |
| `ember500` | `#E8734A` | acento primário (número do dia, botão, marcador ativo) |
| `ember300` | `#F2A387` | glow / estados sutis |
| `paper100` | `#F5F1EA` | texto primário |
| `smoke400` | `#8C8478` | texto secundário, linha da timeline |

### Tipografia

- **Display** — *Fraunces* (serifada, com personalidade, ótima em tamanho grande): usada só no número do dia e no título "Hoje". É o único lugar com serifa — carrega a "voz" do app.
- **Corpo** — *Manrope*: toda a UI funcional, botões, labels.
- **Utilitária/mono** — *JetBrains Mono*: horários dos registros. Data/hora em mono dá cara de "registro/ficha", reforça que isso é um log, não uma lista de tarefas decoradas.

### Layout — conceito

```
┌───────────────────────────────┐
│                                │
│        ╭─────────╮            │
│       ╱     6     ╲   ← anel de brasa (progresso
│      │   HOJE      │    vs. média pessoal, não decoração)
│       ╲___________╱           │
│     DOM · 30 AGO               │
│                                │
│  REGISTROS                     │
│  │                             │
│  ●─ 21:57  ································· │
│  │                             │
│  ●─ 20:42  ································· │
│  │                             │
│  ●─ 18:10  ································· │
│  │  (linha de brasa vertical liga os pontos) │
│                                │
├────────────────────────────────┤
│        ●  Fumei agora          │  ← botão chapado, ponto
└────────────────────────────────┘    de brasa no lugar do "+"
```

- O card verde vira um **anel de progresso** (arco), não um retângulo: o número do dia é desenhado dentro de um anel que se preenche conforme os cigarros aumentam, referenciando literalmente "queimar" ao longo do dia — informação real (progresso), não decoração.
- A lista vira **timeline vertical com trilha**: uma linha fina de `smoke400` desce ligando os pontos (marcadores `ember500`), cada ponto = 1 cigarro. Isso comunica *sequência no tempo do dia* de forma mais honesta do que uma lista de cards soltos.
- Editar/apagar deixam de ser dois ícones coloridos sempre visíveis; viram ações reveladas ao tocar/segurar o item (menos ruído visual, a timeline respira).
- Botão inferior chapado em `ember500`, ícone substituído por um ponto simples (●) — o mesmo símbolo usado nos marcadores da timeline, reforçando o sistema visual em vez de usar um ícone de "+" genérico de qualquer app.

### Elemento-assinatura

A **trilha de brasa vertical** (timeline com linha + pontos) é o elemento único do app: aparece no registro do dia e pode se estender para as Estatísticas (semana = trilha mais longa, com "respiros" — gaps maiores entre pontos — como indicador visual direto de melhora, sem precisar de gráfico de barras genérico).

## Restrição

Um único acento de cor (`ember500`) em toda a tela. Sem gradientes, sem sombras decorativas, sem emojis. O anel de progresso é a única forma "orgânica"; todo o resto é reto e alinhado a uma grade simples — para não competir com o elemento-assinatura.

---

Implementação em `ui/theme/` e `ui/HomeScreen.kt`. Para fontes customizadas, baixe Fraunces, Manrope e JetBrains Mono (Google Fonts) em `res/font/`. Atualmente o app usa Serif/SansSerif/Monospace do sistema como fallback.
