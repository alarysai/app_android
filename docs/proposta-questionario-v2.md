# Proposta para a equipe Web — questionário v2 (campos novos no Firestore)

**De:** app Android · **Para:** painel web (`web_admin`) · **Data:** 2026-10-03

As telas novas do questionário no app (mockups "Criar imagem") precisam de algumas informações que o schema atual não tem. Este documento lista os campos propostos, o valor padrão quando eles não existem e o que o painel precisa validar.

**O app Android já lê todos esses campos**, com os nomes abaixo. Enquanto o painel não grava nenhum deles, o app se comporta como hoje (toda pergunta é de escolha única, em lista e obrigatória). Por isso a mudança é **compatível**: nenhum documento existente precisa ser migrado. Se algum nome mudar na implementação do painel, avisem o app antes de publicar.

Fonte de verdade atual: [`web_admin/docs/data-model.md`](../../../web_admin/docs/data-model.md). Contrato do app: [`android-integration.md`](../../../web_admin/docs/android-integration.md).

---

## 1. O que o schema atual já cobre

| Elemento do mockup | Campo atual |
| --- | --- |
| Título da pergunta ("Para que é a imagem?") | `steps.text` |
| Ilustração no topo | `steps.image` (só depois do Storage/Blaze) |
| Opções em lista | `steps.options[].text` |
| Grade de estilos com imagem ("Fotorrealista", "Aquarela"…) | opções com `image`: o app mostra em grade quando **todas** as opções do passo têm imagem |
| Vídeo ("Como descrever bem uma cena") | `steps.type == "video"` + `videoUrl` |
| Dica "isso é ético?" | `steps.infoFlag.label` + `infoFlag.tipId` (dica ativa) |
| Selos "No prompt" / "Só contexto" na revisão | `steps.partOfPrompt` |
| Partes do prompt | `steps.promptInstruction` + `options[].promptInstruction` |

## 2. Campos propostos

### 2.1 Em `questionnaires/{id}/steps/{stepId}`

| Campo | Tipo | Padrão se ausente | Para quê (mockup) |
| --- | --- | --- | --- |
| `answerType` | `"single_choice" \| "multiple_choice" \| "open_text" \| "yes_no"` | `"single_choice"` | Selo "Escolha uma opção" / "Escolha uma ou mais" / "Resposta aberta" / "Sim ou não", rádio × caixa de seleção, campo de texto. Só vale para `type == "question"`. |
| `helpText` | `LocalizedText \| null` | `null` | Linha menor abaixo do título ("Isso ajuda a Alarys a escolher formato e composição."). |
| `required` | `boolean` | `true` | `false` mostra "Pular" no topo e "Opcional — você pode pular." na resposta aberta. |
| `maxLength` | `number` (inteiro) | `500` | Limite e contador "94/500" da resposta aberta. O app aceita de 1 a 5000. |
| `placeholder` | `LocalizedText \| null` | `null` | Texto de exemplo dentro do campo da resposta aberta. |

### 2.2 Em cada item de `steps.options[]`

| Campo | Tipo | Padrão | Para quê |
| --- | --- | --- | --- |
| `tipId` | `string \| null` | `null` | Dica mostrada **enquanto a opção está selecionada** (ex.: "Sim" em "A imagem retrata pessoas reais?" mostra a dica de consentimento). Tem prioridade sobre a dica do `infoFlag`. Dica inativa = sem dica. |

### 2.3 Em `questionnaires/{id}`

| Campo | Tipo | Padrão | Para quê |
| --- | --- | --- | --- |
| `creditCost` | `number` (inteiro ≥ 0) | `null` (não mostra) | "Custo: 4 créditos" na revisão. O débito real continua no servidor; este campo só informa. |
| `promptTemplate` | `string \| null` | `null` | **Opcional, para discutir.** Modelo do prompt final com marcadores `{{stepId}}` (ex.: `"Crie uma imagem {{formato}}, em estilo {{estilo}}…"`). Sem ele, o app mostra uma prévia simples (instruções + resposta, uma linha por passo) e avisa que o texto final é montado pela Alarys. O app **ainda não lê** este campo. |

## 3. Regras que o painel precisa validar

1. **`multiple_choice`:** saltos por opção (`options[].nextStepId`) **não valem**, porque não dá para saber qual opção decide. O app segue `steps.nextStepId` (ou a ordem). Sugestão: o formulário esconde o salto por opção nesse tipo, e a validação do fluxo (`flow.ts`) passa a ignorá-lo.
2. **`open_text`:** o passo não tem opções. O formulário esconde o editor de opções, e o salto vem só de `steps.nextStepId`.
3. **`yes_no`:** exatamente **2 opções** (texto livre, ex.: "Sim" / "Não", traduzidas). Com outra quantidade, o app mostra como escolha única comum. Saltos por opção continuam valendo.
4. **`required: false`** só faz sentido em `type == "question"`. Pular segue o salto do passo (nunca o de uma opção).
5. **`maxLength`:** inteiro entre 1 e 5000.
6. **`languages`** (tradução completa) deve passar a considerar também `helpText` e `placeholder`.
7. **`flow.ts`** (`resolveNext`/`transitionsOf`) precisa das regras 1, 2 e 4 para o mapa e o simulador de fluxo baterem com o app.
8. Nenhuma mudança em **regras de segurança** nem **índices**: são campos novos nos mesmos documentos.

## 4. Como o app monta o prompt com os tipos novos

Mesmo algoritmo do `prompt-preview.ts`, estendido. Para cada passo respondido com `partOfPrompt == true`, na ordem respondida:

| Tipo | `answer` | `instructions` |
| --- | --- | --- |
| `single_choice` / `yes_no` | texto da opção (idioma usado) | `step.promptInstruction`, `option.promptInstruction` |
| `multiple_choice` | textos das opções na ordem do passo, separados por ", " | `step.promptInstruction` + a instrução de **cada** opção escolhida |
| `open_text` | o texto digitado, como está (não traduzido) | `step.promptInstruction` |
| pulado / vídeo | `null` | `step.promptInstruction` |

Sugestão: atualizar o `prompt-preview.ts` e o futuro serviço de geração com esta mesma tabela.

## 5. Pontos para decidir juntos

- **`infoFlag.value`:** no design novo a informação booleana aparece só como **dica** ("Dica: isso é ético?" + texto). O `value` (Sim/Não definido pelo admin) não é mostrado. Ainda faz sentido manter esse campo?
- **`promptTemplate`:** vale ter o modelo do prompt no painel (prévia igual ao resultado final) ou o servidor monta tudo?
- **Saldo na revisão** ("Seu saldo: 4 créditos"): depende do login no app (`users/{uid}.creditBalance`); não precisa de campo novo.
- **Título do topo** ("Criar imagem"): o app usa o título do questionário. Se a ideia for um rótulo por tipo de saída, seria um campo `outputType` em `questionnaires` (o histórico já tem `outputType`).

## 6. Checklist para o painel

- [ ] `steps`: campos `answerType`, `helpText` (PT/EN/ES), `required`, `maxLength`, `placeholder` (PT/EN/ES) no schema Zod, no formulário e no mapper defensivo.
- [ ] `options[]`: campo `tipId` (seletor de dica, como o do `infoFlag`), com a mesma checagem de dica existente ao salvar/publicar.
- [ ] `questionnaires`: campo `creditCost`.
- [ ] Validações da seção 3 ao salvar e ao publicar.
- [ ] `flow.ts` e simulador com as regras de salto por tipo.
- [ ] `prompt-preview.ts` com a tabela da seção 4.
- [ ] `languages` considerando `helpText` e `placeholder`.
- [ ] `docs/data-model.md` e `docs/android-integration.md` atualizados (o app ajusta a própria documentação junto).
