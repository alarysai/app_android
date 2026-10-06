# alarysai · app Android

App Android da alarysai. Mostra o conteúdo cadastrado no [painel web](https://web-admin-theta-sage.vercel.app): categorias, questionários com passos, dicas e anunciantes.

- **Backend:** Cloud Firestore do projeto Firebase **`alarysai-b6e85`**. Não existe API REST: o app lê o Firestore direto com o SDK, e as regras de segurança garantem que ele só enxerga o que está ativo ou publicado.
- **Contrato dos dados:** [`web_admin/docs/android-integration.md`](../../web_admin/docs/android-integration.md) (consultas, formato dos documentos, fluxo do questionário) e [`web_admin/docs/data-model.md`](../../web_admin/docs/data-model.md) (fonte de verdade do schema).
- **Roteiro:** [TASKS.md](TASKS.md). **Padrões de engenharia:** [CLAUDE.md](CLAUDE.md).

## Configuração

| Item | Valor |
| --- | --- |
| `applicationId` | `com.alarysai.alarysai` |
| App no Firebase | `1:401658602082:android:5e4d6f50ce7f59b96e273b` ("alarysai") |
| minSdk / targetSdk | 26 / 35 |
| Toolchain | AGP 8.6.0, Kotlin 2.0.20, Gradle 8.11.1, JDK 17 |

O AGP fica em 8.6.0 porque é a versão mais nova que o Android Studio Koala sincroniza. Ela limita `compileSdk`/`targetSdk` a 35. Suba os três juntos (ver `gradle/libs.versions.toml`).

**Firebase Auth fixado em 23.2.1.** O `firebase-auth` 24.x do BoM 34 é compilado com metadados do Kotlin 2.3, que o Kotlin 2.0.20 não lê. O `core:firebase` declara `strictly(23.2.1)` para o BoM não subir a versão. Confirmado no aparelho que inicializa junto com o Firestore e o `firebase-common` 22.x. Ao atualizar a toolchain (Kotlin 2.3+), remova o `strictly` e volte a usar a versão do BoM.

**Login com Google:** usa o cliente OAuth web do `google-services.json` (`default_web_client_id`) e precisa do SHA-1 da chave que assina o app cadastrado no app Android do Firebase. O SHA-1 de debug desta máquina já está cadastrado; o de release, não. O provedor Google precisa estar ativo em Authentication → Sign-in method.

### `google-services.json`

Fica em `app/` e **é versionado**: contém só identificadores públicos. Para baixar de novo:

```bash
firebase apps:sdkconfig ANDROID 1:401658602082:android:5e4d6f50ce7f59b96e273b --project alarysai-b6e85 --out app/google-services.json
```

Sempre passe `--project alarysai-b6e85`. Existe um projeto `alarysai` (sem o sufixo) criado por engano que **não** deve ser usado.

### Rodar

```bash
./gradlew installDebug
```

Crie um `local.properties` com `sdk.dir=<caminho do Android SDK>` (não versionado). Nenhuma chave é necessária: o conteúdo é público e não exige login.

## Arquitetura

MVVM com Clean Architecture pragmática, modularizado por feature (detalhes em [CLAUDE.md](CLAUDE.md)).

| Módulo | Responsabilidade |
| --- | --- |
| `app` | `Application` (Hilt), `MainActivity`, `AppRoot`/`AppRootViewModel` (abertura, depois login obrigatório: login, primeiro acesso ou o app), `AlarysNavHost` (barra inferior e destinos), `TopLevelTab` (abas), `ClubScreen` (aba Club AI: Dicas + Anunciantes), `GoogleSignInModule` (cliente OAuth web) |
| `core:common` | `Language`, `LocalizedText` (fallback para PT), `LanguageProvider` (idioma do perfil, senão o do aparelho) e `PreferredLanguageHolder`, `ContentList`, `ContentLoadError`/`ContentLoadException`, busca sem acento (`matchesSearch`), contratos de sessão (`SessionRepository`, `SessionUser`) e perfil (`UserProfileRepository`, `UserProfile`) |
| `core:designsystem` | `AlarysTheme` (escuro, paleta do mockup), `AlarysBackground` (fundo com brilho azul/roxo), `GlassCard`, `AlarysLogo` (imagem `ic_alarys` nos `mipmap` do próprio módulo), `AlarysBottomBar` |
| `core:ui` | Estados de tela reutilizáveis (`LoadingContent`, `MessageContent`, `ContentLoadErrorContent`, `OfflineNotice`) e entrada por voz (`rememberSpeechInput`) |
| `core:navigation` | Contratos de rota (`AppRoute`): abas `home`, `history`, `plans`, `club`, `profile`, `questionnaires/{categoryId}?categoryName=` e `questionnaire/{questionnaireId}?title=` |
| `core:firebase` | Instância do Firestore (Hilt), `FirestoreContract` (campos e status), DTOs comuns (`LocalizedTextDto`, `ImageRefDto`), leitura defensiva em tempo real (`observeDocuments`) e única (`getRemoteDocumentOrNull`, `getRemoteDocuments`), documento em tempo real (`observeRemoteDocument`), exclusão (`deleteDocument`), `Throwable.toContentLoadError()`, sessão do Firebase Auth (`FirebaseSessionRepository`) e perfil em `users/{uid}` (`FirestoreUserProfileRepository`) |
| `core:testing` | `MainDispatcherRule`, JUnit, coroutines-test, MockK, Turbine |
| `feature:home` | Tela Início: cabeçalho, busca, saudação, uso do plano, categorias de questionário (Firestore) e chat |
| `feature:questionnaires` | Questionários publicados de uma categoria e a execução de um questionário (passos e fluxo) |
| `feature:tips` | Dicas com filtro por categoria (aba Club AI) |
| `feature:advertisers` | Anunciantes agrupados por tipo, link em Custom Tab (aba Club AI) |
| `feature:history` | Aba Histórico: saldo de créditos, gerações e extrato do usuário logado |
| `feature:auth` | Login (e-mail/senha e Google), cadastro, recuperação de senha, primeiro acesso e aba Perfil |
| `feature:plans` | Aba Planos: assinaturas e pacotes de créditos pela Google Play (Play Billing 7) |
| `feature:splash` | Abertura do app: vídeo de abertura (Media3/ExoPlayer) ou, sem vídeo, o logo animado |

### Como uma tela de conteúdo lê o Firestore

```
FirestoreXDataSource ──Flow<RemoteDocumentList<Dto>>──▶ XRepositoryImpl ──Flow<ContentList<Model>>──▶ XViewModel ──StateFlow<UiState>──▶ Screen
 (consulta da seção 4)        documento malformado          filtra, mapeia, ordena;          resolve o idioma;
                              é descartado e logado         erro → ContentLoadException      loading/sucesso/vazio/erro
```

Regras que valem para toda consulta nova (resumo da seção 3 do guia):

1. **Sempre filtre por `status`** (`active`, ou `published` para questionários). Sem o filtro, o Firestore recusa a consulta inteira com `PERMISSION_DENIED`.
2. **Use só as combinações `where` + `orderBy` documentadas.** Cada uma tem índice composto no `firestore.indexes.json` do painel. Uma combinação nova precisa de índice novo lá.
3. **Texto traduzido:** `LocalizedText.resolve(idioma)`, que cai para o português.
4. **Mapeamento defensivo:** DTOs com valores padrão; mappers `toDomainOrNull` devolvem `null` para o que não deve aparecer (status desconhecido, sem nome em PT). Nunca derrube a tela por um documento.
5. **Ordem** pelo campo `order`, com desempate por ID (igual ao painel).
6. **Imagens** vêm `null` por enquanto: trate como "sem imagem".

### Offline e erros

O Firestore mantém cache em disco (ligado por padrão). Quando a lista vem do cache (`isFromCache`), a tela mostra uma faixa discreta "Sem conexão. Mostrando a última versão salva." e se atualiza sozinha quando a rede volta.

| `ContentLoadError` | Origem (`FirebaseFirestoreException.Code`) | Na tela |
| --- | --- | --- |
| `OFFLINE` | `UNAVAILABLE`, `DEADLINE_EXCEEDED` | "Sem conexão" + Tentar de novo |
| `UNAVAILABLE` | `PERMISSION_DENIED`, `NOT_FOUND` | "Conteúdo indisponível" + Tentar de novo |
| `UNKNOWN` | qualquer outro erro | "Não foi possível carregar" + Tentar de novo |

"Tentar de novo" assina a consulta outra vez, porque um listener com erro para de emitir.

### Idioma

Textos do app em `values` (pt, padrão), `values-en` e `values-es`. O conteúdo usa o idioma do aparelho (`DeviceLanguageProvider`); idiomas fora de pt/en/es caem para pt. Quando o perfil do usuário existir, o campo `language` dele deve ter prioridade.

## Funcionalidades

### Abertura (`feature:splash`)

É a primeira tela ao abrir o app. Enquanto ela aparece, o `AppRootViewModel` já verifica a sessão; quando termina, o `AppRoot` faz um fade para login, primeiro acesso ou o app.

- **Com vídeo:** coloque o arquivo em `app/src/main/assets/splash/intro.mp4` e gere o app de novo. Não precisa mudar código: `AssetSplashVideoRepository` encontra o arquivo, e sem ele a abertura volta a ser o logo. O vídeo ocupa a tela inteira, cortado para preencher (prefira vídeo vertical 9:16, H.264, curto e leve, porque vai dentro do APK). O som do arquivo é tocado. Tem botão **Pular**.
- **Sem vídeo:** o `AlarysLogo` aparece com fade e um leve zoom por 1,8 s.
- **Nunca prende o usuário:** termina quando o vídeo acaba, ao tocar em Pular, se o vídeo não puder ser tocado (arquivo ruim, codec), ou após 15 s mesmo que o vídeo não avise o fim. Os tempos ficam em `SplashTiming` (`SplashModule`).
- Em segundo plano o vídeo pausa; ao girar o aparelho ou voltar ao app a abertura não se repete (`rememberSaveable` no `AppRoot`).
- **Android 12+** sempre mostra antes a splash do sistema (ícone do app sobre uma cor). A cor dela é a mesma do fundo (`values-v31/themes.xml`), para não piscar.
- Para trocar a origem do vídeo (por exemplo, baixar do Storage), basta outra implementação de `SplashVideoRepository`.

### Início (`feature:home`)

Segue o mockup da tela principal. Cada seção vem de um campo do `HomeUiState`:

| Seção | Fonte hoje | Comportamento |
| --- | --- | --- |
| Cabeçalho (logo + sino) | — | Logo provisório desenhado em código. O sino mostra "As notificações chegam em breve." (sem ponto de não lido: não há notificações) |
| Busca "Buscar recursos" | local | Filtra as categorias pelo nome, sem diferenciar maiúsculas nem acentos. O microfone preenche a busca por voz e some se o aparelho não tiver reconhecedor |
| Saudação | primeiro nome do perfil (`users/{uid}.displayName`) ou do provedor | "Olá, Marina"; "Olá!" sem nome |
| Uso do seu plano | `planUsage` (sempre `null`: falta a franquia do plano no servidor) | Sem dados: explica onde os créditos aparecem. Com dados: barra de uso, % usado/disponível e créditos. "Adquirir créditos" abre a aba Planos |
| Categorias | Firestore `questionnaireCategories` (ativas, por `order`, em tempo real) | Linhas de 3 cards; a última linha estica (5 categorias = 3 + 2). Cor do card pela posição na lista completa. Sem ícone, mostra a inicial. Estados: carregando, lista, sem resultado na busca, vazio, erro com "Tentar de novo", faixa de offline. Tocar abre os questionários da categoria |
| Converse com a Alarys | local | Guarda o texto digitado; enviar uma mensagem não vazia avisa que o chat chega em breve. Microfone dita a mensagem |

As categorias não têm campo de descrição no Firestore, então os cards mostram só o nome (o mockup mostra também uma frase, como "Crie e edite textos com IA").

### Questionários de uma categoria (`feature:questionnaires`)

- Rota `questionnaires/{categoryId}?categoryName=`, aberta ao tocar numa categoria da Início. O nome vai na rota (codificado com `Uri.encode`) e vira o título, sem reler a categoria. Sem `categoryId`, a tela mostra "Conteúdo indisponível" e não consulta nada.
- Consulta `questionnaires` com `status == "published"` e `categoryId`, ordenada por `order` (índice `status, categoryId, order`), em tempo real. O repositório desempata por ID e descarta o que não deve aparecer: rascunho, status desconhecido, sem título em PT, sem categoria.
- **Idioma:** mostra **todos** os publicados. Os que não têm tradução completa no idioma do usuário (`languages` não contém o idioma) aparecem com o texto em português e o selo "Em português", em vez de sumirem. Para esconder esses questionários, basta filtrar por `isInUserLanguage` no `QuestionnairesViewModel`. Códigos desconhecidos em `languages` são ignorados, e `pt` sempre conta como completo.
- Card com imagem (ou a inicial), título, descrição em até 2 linhas e o selo. Tocar abre o questionário.
- Estados: carregando, lista, vazio ("Nenhum questionário nesta categoria ainda"), erro com "Tentar de novo" e faixa de offline. Voltar pela seta ou pelo botão do sistema.

### Executar um questionário (`presentation/run`)

Segue os mockups "Criar imagem" com as cores da marca. **Cada tela é montada a partir do passo no Firestore**; nada do conteúdo é fixo no app.

- Rota `questionnaire/{questionnaireId}?title=`, em **tela cheia** (o `AlarysNavHost` esconde a barra inferior nessa rota). O título da rota aparece enquanto carrega e depois vira o título do questionário no idioma do usuário.
- **Leitura única** (seção 4.3): o questionário e **todos** os passos (`steps`, por `order`), com `get().await()`. As dicas ligadas aos passos e às opções são lidas logo depois, em paralelo. Daí em diante o fluxo roda só no aparelho e funciona offline.
- **Indisponível:** despublicado (`PERMISSION_DENIED`), apagado, rascunho ou sem passo que o app consiga mostrar: "Questionário indisponível" com "Voltar aos questionários". Sem rede: "Sem conexão" com "Tentar de novo".

**Campos propostos.** Tipo de resposta, texto de apoio, "pular", limite e exemplo da resposta aberta, dica por opção e custo **ainda não existem no Firestore**. A proposta para a equipe Web está em [docs/proposta-questionario-v2.md](docs/proposta-questionario-v2.md) (cópia em `alarysai/doc/`). O app já lê esses campos com os nomes propostos; **sem eles, toda pergunta é de escolha única, em lista e obrigatória** (o comportamento de antes).

**Tela de cada passo**

- Topo: X (sair), título do questionário e "Pular" (só em pergunta com `required: false`). Barra segmentada com um segmento por pergunta do caminho, "Pergunta N de M" e a porcentagem.
  - M é uma **estimativa**: perguntas já respondidas + o caminho que falta seguindo a opção selecionada e os saltos padrão (`AdvanceQuestionnaireUseCase.countRemaining`). Muda quando uma resposta leva a um ramo mais curto ou mais longo. A porcentagem vai de 0% na primeira a 100% na última.
- Ilustração do passo (quando houver), selo do tipo de resposta, título (`text`) e texto de apoio (`helpText`).
- Respostas por tipo (`answerType`):

| Tipo | Tela | Válido para "Continuar" | Próximo passo |
| --- | --- | --- | --- |
| `single_choice` (padrão) | lista com rádio; **grade de cards com imagem** quando todas as opções têm imagem | 1 opção | salto da opção → do passo → `order` |
| `multiple_choice` | lista (ou grade) com caixa de seleção | 1 ou mais | salto do passo → `order` (salto por opção é ignorado) |
| `open_text` | campo de texto com ditado por voz, contador "N/limite", exemplo (`placeholder`) e "Opcional — você pode pular." | texto não vazio (ou vazio, se opcional), até `maxLength` | salto do passo → `order` |
| `yes_no` | duas opções lado a lado (exige 2 opções; senão vira escolha única) | 1 opção | salto da opção → do passo → `order` |
| vídeo | "Assistir ao vídeo" (app do YouTube/Vimeo ou navegador) | sempre | salto do passo → `order` |

- "Pular" registra a pergunta sem resposta e segue o salto do passo. Pergunta de escolha que ficou sem opção válida mostra só "Continuar".
- **Dica:** a dica da opção selecionada (`options[].tipId`, proposto) tem prioridade; senão, a do `infoFlag` do passo, como "Dica: <rótulo>". Dica inativa ou com erro não aparece. O `infoFlag.value` não é mostrado no design novo.
- Rodapé: seta para voltar e "Continuar" (desabilitado até a resposta ser válida). Na última pergunta do caminho, o botão vira "Revisar pedido".
- Voltar (seta ou botão do sistema) reabre o passo anterior **com a resposta preenchida**; no primeiro passo, sai. O X pede confirmação ("Sair do questionário?") quando já há resposta.

**Revisão ("Revise seu pedido")**

- Uma linha por passo respondido: pergunta, resposta (opções separadas por vírgula, texto digitado, "Vídeo assistido" ou "Pulou") e o selo **"No prompt"** (`partOfPrompt`) ou **"Só contexto"**. Tocar reabre aquele passo para editar; as respostas seguintes são refeitas, porque o caminho pode mudar (`QuestionnaireProgress.reopen`).
- **Prévia do prompt** (`BuildPromptPreviewUseCase`): uma linha por parte, instruções seguidas da resposta. É só uma prévia: o texto final é montado pelo serviço de geração (ou por um `promptTemplate`, se a equipe Web adotar a proposta).
- "Custo: N créditos" quando o questionário tem `creditCost` (proposto). O saldo ainda não aparece: depende do login (Task 9).
- "Gerar com Alarys" mostra "A geração de conteúdo chega em breve.": o serviço de geração ainda não existe. Quando existir, é esse botão que envia `promptParts`.

**Domínio**

- `ResolveNextStepUseCase`: porte do `resolveNext` do painel (opção → passo → `order`; `"__end__"` encerra).
- `AdvanceQuestionnaireUseCase`: valida a resposta por tipo (`StepResponse` → `StepAnswer(stepId, optionIds, text)`), aplica "Pular" só em pergunta opcional e **encerra** quando o salto aponta para passo inexistente, para passo já visto (ciclo) ou depois de **200** respostas.
- `CollectPromptPartsUseCase`: porte do `promptParts` do painel, estendido: múltipla escolha junta os textos com ", " e todas as instruções das opções escolhidas; resposta aberta vai como foi digitada (não traduzida); pulado e vídeo só levam a instrução do passo. As partes ficam em `QuestionnaireRunViewModel.promptParts`. O app **nunca** chama a IA direto.
- `StepMapper`: descarta passo de tipo desconhecido, sem texto e sem imagem, ou vídeo sem link `https://`; descarta opção sem ID, sem texto e sem imagem, ou repetida. `answerType` ausente ou desconhecido = escolha única; `required` ausente = `true`; `maxLength` ausente = 500 (aceita 1 a 5000).

### Aba Club AI (`app/navigation/ClubScreen.kt`)

Título "Club AI" e duas abas no topo: **Dicas** (padrão) e **Anunciantes**. A aba escolhida sobrevive à rotação. O `ClubScreen` fica no `app` porque junta duas features, que não podem depender uma da outra; as telas de cada feature são só conteúdo, sem barra de título própria.

### Dicas (`feature:tips`)

- Primeira aba do Club AI (rota `club`).
- **Chips de categoria:** "Todas" e cada categoria ativa (`tipCategories`, `status == "active"`, por `order`, em tempo real). Se as categorias falharem ou não houver nenhuma, a fileira de chips some e as dicas continuam.
- **Dicas:** em tempo real, por `order` com desempate por ID.
  - "Todas": consulta `tips` com `status == "active"` (índice `status, order`). Cada card mostra o nome da categoria acima do texto.
  - Uma categoria: consulta com `status == "active"` e `categoryId` (índice `status, categoryId, order`). O nome da categoria não se repete no card.
  - Se a categoria escolhida for desativada no painel enquanto a tela está aberta, a seleção volta para "Todas".
- **Idioma:** mostra todas as dicas ativas. As que não têm tradução completa no idioma do usuário aparecem em português com o selo "Em português" (mesmo critério dos questionários).
- Card: ícone de lâmpada, categoria (em "Todas"), texto e imagem (quando houver). Descartadas: inativas, sem categoria, sem texto em PT.
- Estados: carregando, lista, vazio ("Nenhuma dica por aqui ainda"), erro com "Tentar de novo" (assina de novo as duas consultas) e faixa de offline.

### Anunciantes (`feature:advertisers`)

- Segunda aba do Club AI.
- Consulta `advertisers` com `status == "active"`, por `order` (índice `status, order`), em tempo real; desempate por ID.
- Descartados: inativos, sem nome e sem imagem, e link que não seja `https://` (o app só abre links seguros). O `type` é arrumado como no painel (`tidyType`: sem espaços nas pontas, espaços simples).
- **Agrupamento por tipo** (`GroupAdvertisersByTypeUseCase`, mesma regra do `advertiserTypes` do painel): "Patrocínio", "patrocinio " e "PATROCINIO" são um tipo só, mostrado com a primeira grafia encontrada. Grupos em ordem alfabética (colação pt-BR); dentro do grupo, a ordem do painel. Sem tipo vai para o grupo "Outros", no fim.
- Card: logo (ou a inicial), nome e ícone de abrir. Sem nome (só imagem), mostra o domínio do link. Tocar abre o link numa **Custom Tab** (`androidx.browser`); sem navegador compatível, abre com qualquer app que trate o link.
- Estados: carregando, lista, vazio ("Nenhum anunciante por enquanto"), erro com "Tentar de novo" e faixa de offline.

### Histórico e créditos (`feature:history`, aba Histórico)

- **Depende de login.** `users/{uid}` e as subcoleções só podem ser lidos pelo dono. A tela segue o `SessionRepository`: sem ninguém logado, mostra "Entre na sua conta" e não faz consulta nenhuma. Ao entrar, começa a ler; ao sair, para.
- A sessão vem do Firebase Auth (`FirebaseSessionRepository`, Task 9). Como o login é obrigatório, o convite para entrar só aparece se a sessão cair com a aba aberta.
- **Saldo:** `users/{uid}.creditBalance` em tempo real. Documento ou campo ausente (nunca creditado) e valores negativos contam como 0. Se a leitura falhar, o saldo aparece como "—".
- **Gerações** (`users/{uid}/history`, por `createdAt` decrescente, índice automático): título copiado no idioma usado (sem título: "Questionário"), tipo de saída, data, créditos gastos, prévia do texto (4 linhas) e "Abrir resultado" quando há link `https://`. `answers` e `prompt` não são mostrados.
- **Apagar:** o dono pode apagar itens do próprio histórico (única gravação permitida). Pede confirmação ("Os créditos usados não voltam"). A consulta em tempo real tira o item da lista sozinha; se falhar, aparece "Não foi possível apagar".
- **Extrato** (`users/{uid}/credits`, por `createdAt` decrescente): motivo (compra, uso, créditos do mês, bônus, estorno), data, valor com sinal (verde para entrada, vermelho para saída) e saldo depois.
- **Mapeamento defensivo:** dados escritos pelo servidor nunca são descartados; valores estranhos viram neutros (tipo de saída ou motivo desconhecido = "Outro"/"Ajuste", link inseguro = sem link, créditos negativos = 0). Registro ainda sem `createdAt` (timestamp pendente) vai para o topo.
- Estados por aba: carregando, lista, vazio, erro com "Tentar de novo" e faixa de offline.
- O card "Uso do seu plano" da Início ainda não usa o saldo: ele precisa da franquia do plano para calcular a porcentagem, e esse dado ainda não existe no schema.

### Login e perfil (`feature:auth`)

**O login é obrigatório.** O `AppRoot` mostra uma de três coisas, conforme `ObserveAuthGateUseCase`:

| Situação | Tela |
| --- | --- |
| Ninguém logado | `AuthFlow`: login, cadastro e recuperação de senha |
| Logado, sem `users/{uid}` (primeiro acesso) | "Seu perfil foi criado": nome e idioma |
| Logado, com perfil | o app (Início, abas…) |

Se o perfil não puder ser lido (ex.: sem rede num aparelho novo), o app abre com os dados do provedor em vez de travar o usuário.

- **Login ("Bem-vindo de volta"):**
  - e-mail e senha, com o olho para mostrar a senha;
  - "Manter conectado": salvo localmente. Se desmarcado, a sessão é encerrada na próxima abertura do app (`ApplyKeepSignedInUseCase`);
  - "Esqueci a senha", "Continuar com Google" (Credential Manager) e "Criar conta";
  - o login com Apple não existe, por decisão do produto;
  - erros: e-mail inválido, "E-mail ou senha incorretos" (o Firebase não diz qual dos dois), sem rede, muitas tentativas e Google indisponível neste aparelho. Fechar o seletor de contas do Google não mostra erro.
- **Cadastro ("Criar conta"):**
  - nome, e-mail, senha (mínimo de **8** caracteres) e confirmação;
  - aceite da Política de Privacidade e dos Termos de Uso (LGPD), obrigatório;
  - o botão fica cinza até tudo estar preenchido e o aceite marcado;
  - os erros de campo aparecem depois da primeira tentativa e somem enquanto o usuário corrige;
  - o nome vai para o perfil do Firebase Auth e preenche a tela de primeiro acesso;
  - o **opt-in de novidades** do mockup ficou de fora, porque as regras de `users/{uid}` não permitem guardá-lo. O aceite também ainda não é registrado no banco, e os textos aparecem **sem link** até a política e os termos terem URL. Proposta: [docs/proposta-usuarios-login.md](docs/proposta-usuarios-login.md).
- **Recuperar senha:**
  - envia o link do Firebase no idioma do usuário;
  - a confirmação ("Verifique seu e-mail") **nunca diz se a conta existe** ("Se existir uma conta para…");
  - "Abrir app de e-mail", "Reenviar" com espera de **30 s** e "Voltar ao login".
- **Primeiro acesso ("Seu perfil foi criado"):**
  - mostra o e-mail confirmado, as iniciais, o nome (vindo do cadastro ou do Google) e o idioma (Português (Brasil), English, Español);
  - "Começar" cria `users/{uid}` só com `displayName`, `photoUrl` (null), `language`, `createdAt` e `updatedAt`, que são os campos permitidos pelas regras;
  - "Adicionar foto" fica de fora até o Storage existir.
- **Aba Perfil:**
  - a mesma tela, em modo edição: salvar atualiza `displayName`, `language` e `updatedAt`;
  - tem o botão "Sair da conta".
- **Idioma do perfil:** passa a valer para os **conteúdos** (`LocalizedText`) no lugar do idioma do aparelho (`PreferredLanguageHolder`). Os textos fixos do app continuam no idioma do aparelho.
- **Saudação da Início:** "Olá, <primeiro nome>", vindo do perfil (ou do provedor enquanto o perfil carrega).
- **Histórico e créditos:** com login, a aba Histórico passa a ler os dados do usuário. Eles ficam vazios até o serviço de geração existir.

### Planos e créditos (`feature:plans`, aba Planos)

Vende pela **Google Play** (Play Billing 7.1.1) **assinaturas** (planos mensais com créditos) e **pacotes avulsos de créditos**. Configuração do Play Console, servidor e próximos passos: [docs/proposta-pagamentos-play.md](docs/proposta-pagamentos-play.md).

- **Catálogo:** os IDs ficam em `BillingCatalog` (`alarys_plano_basico`, `alarys_plano_pro`, `alarys_creditos_50`, `alarys_creditos_200`). Nome, descrição e preço vêm do Play Console, já traduzidos e na moeda do usuário. ID inexistente ou inativo simplesmente não aparece.
- **Tela:**
  - "Assinaturas": nome, descrição e preço recorrente do plano base ("R$ 19,90 por mês"), com o selo "Seu plano" na assinatura ativa;
  - "Pacotes de créditos";
  - nota de renovação automática e link para gerenciar assinaturas na Google Play.
- **Estados:**
  - carregando;
  - "Google Play indisponível": sem Play Store, ou o app não foi instalado por ela;
  - "Planos em breve": nenhum produto ativo;
  - erro com "Tentar de novo".
- **Compra:**
  - abre a folha de pagamento da Play com `obfuscatedAccountId = sha256(uid)`, para o servidor ligar a compra à conta;
  - o resultado passa por `ProcessPurchaseUseCase`: pagamento pendente espera, compra já confirmada conta como entregue, e o resto vai ao **servidor** (`PurchaseVerifier`).
- **O app nunca dá créditos nem confirma compras.**
  - Hoje o verificador é `ServerlessPurchaseVerifier` ("servidor indisponível"). A compra fica sem confirmação e a Google estorna em 3 dias.
  - O usuário vê: "Compra recebida, mas a liberação de créditos ainda não está ativa…".
  - Ao abrir a aba, compras sem entrega são reenviadas ao servidor sem aviso.
- **Habilitado só no debug:** `PURCHASES_ENABLED` é `true` no build de debug (contas de teste da Play) e `false` no release. No release, os botões mostram "Em breve", até o servidor existir.
- **Botão "Adquirir créditos" da Início:** abre a aba Planos.
- **Para testar:** o app precisa estar instalado **pela Play Store** (faixa de teste interno), com uma conta de teste de licença. Instalado pelo Android Studio, a Play não responde com os produtos.

### Navegação principal

Barra inferior com Início, Histórico, Planos, Club AI e Perfil (`TopLevelTab`). A troca de aba mantém uma cópia de cada tela e restaura o estado. Todas as abas têm conteúdo: Início, Histórico, Planos, Club AI (Dicas e Anunciantes) e Perfil. Telas abertas a partir de uma aba (como os questionários) mantêm a aba Início marcada.

O tema é escuro sempre (a marca é escura), com ícones claros nas barras do sistema.

## Testes

```bash
./gradlew testDebugUnitTest            # unitários (JVM)
./gradlew connectedDebugAndroidTest    # UI (Compose), precisa de aparelho/emulador em português
```

| Teste | Cobre |
| --- | --- |
| `LanguageTest`, `LocalizedTextTest` | código de idioma (regional, desconhecido, versão estrita), fallback para PT |
| `ContentMappersTest` | `LocalizedTextDto`/`ImageRefDto` → domínio, campos em branco |
| `ContentLoadErrorMapperTest` | códigos do Firestore → `ContentLoadError` |
| `QuestionnaireCategoryMapperTest` | status ativo/inativo/desconhecido, sem nome, ícone, `order` fora do intervalo |
| `QuestionnaireCategoryRepositoryImplTest` | ordenação (order + ID), descarte de inválidos, flag de cache, erro do Firestore |
| `SearchTextTest` | normalização (acento, caixa, espaços), busca vazia |
| `HomeViewModelTest` | loading, idioma e fallback, cor estável, offline, vazio, busca (sem acento, sem resultado, antes da lista chegar, com atualização ao vivo), erros, retry, chat, aviso de "em breve" nas notificações, "Adquirir créditos" abre Planos, abrir categoria, saudação com o primeiro nome |
| `HomeScreenTest` (androidTest) | saudação com e sem nome, créditos com e sem dados, cards, busca, sem resultado, "Tentar de novo", enviar no chat, tocar numa categoria |
| `DeviceLanguageProviderTest` | idioma do aparelho, idioma do perfil tem prioridade |
| `UserProfileMapperTest` | campos do perfil, nome vazio, foto insegura e idioma desconhecido |
| `ValidationTest` (auth) | e-mail, formulário completo, todos os erros, tamanho mínimo da senha |
| `SessionUseCasesTest` | login → primeiro acesso → app → saída, perfil ilegível abre o app, "Manter conectado" |
| `AuthErrorMapperTest` | exceções do Firebase Auth → `AuthError` |
| `LoginViewModelTest` | preferência salva, login, e-mail inválido, credenciais erradas, Google (token, cancelado, erro), eventos |
| `SignUpViewModelTest` | botão habilitado, criação, erros após a tentativa, e-mail em uso, voltar |
| `ForgotPasswordViewModelTest` | envio, confirmação e contagem de 30 s, e-mail inválido, erro, eventos |
| `ProfileViewModelTest` | primeiro acesso com nome do provedor, criação com idioma, nome vazio, edição sem sobrescrever o que foi digitado, falha ao salvar, sair |
| `SplashViewModelTest` | logo sem vídeo e tempo dele, vídeo até o fim, Pular, vídeo com erro, limite de 15 s, `Finished` uma vez só e guardado até a tela coletar |
| `AssetSplashVideoRepositoryTest` | vídeo presente, outro arquivo, pasta ausente, erro ao ler os assets |
| `SplashScreenTest` (androidTest) | logo sem Pular, vídeo com Pular |
| `AppRootViewModelTest` | sessão mantida abre o app no idioma do perfil, sessão não mantida é encerrada antes, saída limpa o idioma, primeiro acesso |
| `AuthScreensTest` (androidTest) | login, botão desabilitado, erros do cadastro e aceite, confirmação da recuperação, primeiro acesso |
| `HistoryMappersTest` | geração, valores estranhos viram neutros, todos os tipos de saída, transação e motivos, saldo ausente/negativo = 0 |
| `UserActivityRepositoriesTest` | histórico mais recente primeiro (pendente no topo), apagar e erro, saldo com documento ausente e sem repetições, extrato e erro |
| `HistoryViewModelTest` | sem login não consulta, com login mostra saldo/gerações/extrato, vazio e erro por aba, troca de aba, confirmar/cancelar/falhar ao apagar, abrir resultado, sair da conta, retry |
| `HistoryScreenTest` (androidTest) | convite para entrar, saldo e geração com ações, diálogo de apagar, extrato com sinal e troca de aba |
| `BillingDomainTest` | períodos da Play (P1M…), códigos do Play Billing → `BillingError`, compra nova vai ao servidor, pendente e já entregue não vão |
| `PlansViewModelTest` | planos e pacotes com "Seu plano", vazio/sem Play/erro, retry, abrir compra com oferta, release sem compra, compra aguardando servidor, assinatura entregue vira plano atual, pendente/falha/erro ao abrir, reenvio do que não foi entregue, gerenciar assinaturas |
| `PlansScreenTest` (androidTest) | preços, "Seu plano", assinar e comprar, aviso de "em breve" no release |
| `AdvertiserMapperTest` | ativo/inativo, `type` arrumado, só `https://`, nome ou imagem |
| `GroupAdvertisersByTypeUseCaseTest` | mesmo tipo sem caixa/acento/espaço com a primeira grafia, ordem alfabética pt, ordem interna, sem tipo no fim, lista vazia |
| `AdvertiserRepositoryImplTest` | ordenação, descarte de inativos e links inseguros, cache, erro do Firestore |
| `AdvertisersViewModelTest` | loading, grupos, vazio e offline, abrir link, erro e retry |
| `AdvertisersScreenTest` (androidTest) | grupos com título e "Outros", domínio para anunciante só com imagem, toque, "Tentar de novo", vazio |
| `TipMappersTest` | categoria ativa/inativa/sem nome; dica ativa/inativa/sem categoria/sem texto; imagem; idiomas desconhecidos |
| `TipRepositoryImplTest` | ordenação de categorias e dicas, descarte de inválidos, "Todas" sem categoria, descarte de outra categoria, cache, erro do Firestore |
| `TipsViewModelTest` | loading, chips e dicas no idioma do usuário com nome da categoria, nomes que chegam depois, troca de categoria, mesma categoria sem nova consulta, categoria desativada volta para "Todas", vazio e offline, categorias com erro, retry |
| `TipsScreenTest` (androidTest) | "Todas", dica com categoria e selo, toque nos chips, sem chips, "Tentar de novo" |
| `QuestionnaireMapperTest` | publicado/rascunho, sem título ou categoria, descrição vazia, idiomas desconhecidos, imagem |
| `QuestionnaireRepositoryImplTest` | lista: categoria pedida, ordenação, descarte de inválidos e de outra categoria, cache, erro. Carga: passos ordenados e inválidos descartados; ausente, rascunho e sem passos = indisponível; `PERMISSION_DENIED` = indisponível; sem rede = offline |
| `QuestionnairesViewModelTest` | título pela rota, idioma e selo "Em português", offline, vazio, erro, sem `categoryId`, retry, abrir questionário |
| `QuestionnairesScreenTest` (androidTest) | título e loading, lista com selo, vazio, "Tentar de novo", voltar, tocar num questionário |
| `StepMapperTest` | pergunta e vídeo, tipo desconhecido, sem texto e imagem, só imagem, link de vídeo inválido, opções inválidas e repetidas, campos em branco, `infoFlag`, campos propostos (padrões, tipos, sim/não com 2 opções, limite de texto, dica por opção) |
| `TipMapperTest` | dica ativa, inativa ou status desconhecido, sem texto em PT |
| `StepTipRepositoryImplTest` | dica ativa; desativada (`PERMISSION_DENIED`), ausente, inativa e sem rede viram "sem dica" sem falhar |
| `ResolveNextStepUseCaseTest` | prioridade opção > passo > ordem, último passo, `__end__` na opção e no passo |
| `CollectPromptPartsUseCaseTest` | mesmos casos do painel, múltipla escolha, resposta aberta, pulada, ordem das respostas |
| `BuildPromptPreviewUseCaseTest` | instruções + resposta por linha, partes incompletas, prévia vazia |
| `AdvanceQuestionnaireUseCaseTest` | início, vídeo, caminho pela opção, respostas em ordem, inválidas, múltipla escolha, resposta aberta (obrigatória, opcional, limite), sim/não, pular, pergunta sem opções, salto inexistente, ciclo, limite de 200, caminho estimado, voltar e reabrir |
| `QuestionnaireRunViewModelTest` | primeira pergunta com apoio e progresso, seleção e caminho estimado, única × múltipla, resposta aberta opcional e limite, dica do `infoFlag` × dica da opção, revisão (respostas, selos, prévia, custo, partes), vídeo e pulada na revisão, editar pela revisão, voltar com resposta preenchida, sair com confirmação, vídeo e "Gerar", indisponível/sem ID/retry |
| `QuestionnaireRunScreenTest` (androidTest) | topo, progresso e seleção, múltipla escolha, resposta aberta opcional com contador e "Pular", sim/não com dica e "Revisar pedido", fechar e voltar, diálogo de saída, revisão completa, indisponível |

Os testes unitários de módulos que criam `FirebaseFirestoreException` usam `unitTests.isReturnDefaultValues = true`, porque o construtor toca em stubs do Android.
