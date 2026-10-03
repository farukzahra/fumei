# Aviso de atualização pelo Google Play

## Objetivo

Avisar quem instalou o Fumei pela Play Store que existe versão nova, e resolver
a atualização dentro do app, sem mandar a pessoa para a loja. Um cartão na aba
Mais dá o caminho manual para quem dispensou o aviso.

## Mecanismo

Play In-App Updates, biblioteca `com.google.android.play:app-update:2.1.0` e
`app-update-ktx:2.1.0`.

Quem responde se há atualização é a Play Store, por comunicação local com o app
da Play. O Fumei continua **sem permissão de internet**: nenhuma chamada de rede
sai do processo do app. Isso mantém válidos o "Tudo offline" do Sobre e o "sem
conta e sem internet" da descrição da loja.

O download e a instalação são sempre do Google Play. O Fumei dispara e
acompanha, nunca instala por conta própria.

## Comportamento

**Ao abrir o app**

Verificação silenciosa, uma vez por abertura. Nada de tela de carregando, nada
de aviso quando não há atualização. Enquanto a resposta não chega, nenhum dialog
aparece e o cartão fica escondido, para não piscar na tela.

**Dialog de atualização disponível**

Aparece quando a Play responde que existe versão nova e você não dispensou o
aviso antes.

- Título: "Tem atualização disponível"
- Texto: "Baixa em segundo plano, sem sair do app. Depois você reinicia quando
  quiser."
- Botões: "Atualizar" e "Agora não"

"Atualizar" dispara o fluxo flexível do Google. O download roda em segundo plano
e o app segue utilizável. "Agora não" fecha e marca a dispensa.

**Dialog de atualização pronta**

Quando o download termina, ou quando o app abre e encontra um download já
concluído:

- Título: "Atualização pronta"
- Texto: "O download terminou. Reinicie o app para usar a versão nova."
- Botões: "Reiniciar" e "Depois"

"Reiniciar" chama a instalação do Google, que reabre o app atualizado.

Se a Play recusar o início do fluxo flexível, o dialog continua aberto e o
cartão volta para "Versão nova disponível". Nenhuma mensagem de erro nova.

**Cartão na aba Mais**

Título "Atualizações", com o estado corrente:

| Estado | Texto | Ação |
|---|---|---|
| Sem informação da Play | cartão escondido | nenhuma |
| Sem atualização | "Você está na versão mais recente" | nenhuma |
| Atualização disponível | "Versão nova disponível" | "Atualizar" |
| Baixando | "Baixando 42%" | nenhuma |
| Baixado | "Pronta para instalar" | "Reiniciar" |

Quando a Play não responde (APK instalado por fora, aparelho sem Play), o cartão
fica escondido e nenhum dialog aparece. O app não mostra erro nem trava.

**Memória da dispensa**

A Play não informa qual versão está disponível, então não existe como comparar a
versão dispensada com a próxima. A dispensa é um sinalizador único, limpo quando
a Play responde que não há atualização pendente, ou seja, quando você atualizou.
Enquanto o sinalizador estiver ligado, o dialog automático não volta. O cartão
da aba Mais continua funcionando e é o caminho manual para atualizar.

## Arquitetura

Pacote novo `fumei.faruk.dev.br.update`, com os tipos do Google isolados em um
único arquivo:

- `AppUpdater` (interface): `suspend fun check(): UpdateSignals`,
  `suspend fun startFlexibleUpdate(activity: Activity): Boolean`,
  `fun observeInstallState(): Flow<UpdateSignals>`,
  `suspend fun completeUpdate()`.
- `PlayAppUpdater`: implementação com `AppUpdateManager`, mapeando
  `UpdateAvailability` e `InstallStatus` do Google para os enums próprios.
- `UpdateSignals`: `availability` (`Unknown`, `None`, `Available`, `Downloading`,
  `Downloaded`), `bytesDownloaded`, `totalBytes`.
- `UpdatePromptPolicy`: funções puras que decidem o dialog, o estado do cartão e
  quando limpar a dispensa. Sem imports de Android, testável em JVM.
- `UpdateViewModel`: combina `check()`, `observeInstallState()` e o sinalizador
  de dispensa em `UpdateUiState`; expõe `onDismiss`, `onUpdateClick(activity)`,
  `onRestartClick` e `onResume()`.
- `UpdateUiState`: `dialog: UpdateDialogKind?` e `card: UpdateCardState`.

O `UpdateViewModel` nunca guarda a `Activity`: quem chama `onUpdateClick` passa a
Activity no momento do toque, o que evita referência velha depois de recriar a
Activity.

`AppUpdaterProvider` (objeto em `main`) entrega a implementação real e permite
troca. Em `debug`, `FumeiDebugApplication` troca por `FakeAppUpdater` quando
`DebugDevSeeder.isAndroidEmulator()` for verdadeiro, para dar para ver os
estados no emulador. O fake vive em `src/debug` e é reaproveitado pelos testes
instrumentados.

**Interface**

- `UpdateDialogs.kt`: `UpdateAvailableDialog`, `RestartReadyDialog`. testTags
  `update_dialog`, `update_dialog_update_button`, `update_dialog_later_button`,
  `restart_dialog`, `restart_dialog_restart_button`, `restart_dialog_later_button`.
- `MoreScreen.kt`: cartão "Atualizações", testTags `update_card`,
  `update_card_status`, `update_card_action`.
- `FumeiApp`: recebe `updateState` e os callbacks, e desenha os dialogs por cima
  de qualquer aba.

## Persistência

`UpdatePromptStore` (interface) com `observePromptDismissed()` e
`setPromptDismissed(Boolean)`, implementada em `UserPreferencesRepository` sobre
o mesmo `SharedPreferences` existente. Sem banco, sem migração.

## Textos e páginas

- `docs/play-store/privacy-policy.md`: parágrafo curto dizendo que o app pergunta
  à Play Store se há versão nova, que quem acessa a rede é o Google Play e que
  nenhum registro de consumo sai do aparelho. Republicar com
  `.\scripts\deploy-privacy.ps1`.
- `docs/release-history.json` e `app/src/main/assets/release-history.json`:
  entrada de versão 1.2.0, tipo `feat`.
- `docs/play-store/release-notes-pt-BR.txt`: notas do próximo AAB.
- Sobre (`AboutScreen.kt`): hero continua "Tudo offline", porque segue verdade.

## Validação

- Unitários em `app/src/test`: `UpdatePromptPolicy` (dialog por estado, cartão
  por estado, limpeza da dispensa, percentual do download com `totalBytes` zero)
  e o mapeamento de `UpdateSignals` para `UpdateUiState`.
- E2E Compose em `app/src/androidTest/.../ui/UpdateFlowE2ETest.kt` com
  `FakeAppUpdater`: dialog aparece com atualização disponível e sumir com "Agora
  não" não volta na próxima abertura; "Atualizar" chama o fluxo e o cartão passa
  a "Baixando 50%"; download concluído mostra "Atualização pronta" e "Reiniciar"
  chama `completeUpdate`; sem resposta da Play o cartão fica escondido.
- Permissão: conferir no manifest mesclado do build debug que
  `android.permission.INTERNET` não aparece, e travar isso com checagem no CI no
  padrão de `scripts/check_release_history_encoding.py`.
- Verificação real depende de build instalado pela Play: AVD com Play ligado
  (imagem `google_apis_playstore` já instalada) com a 1.1.2/1.2.0 da faixa de
  teste interno. Fica fora da suíte automatizada e é passo manual do plano.
