# Proposta para a equipe Web — login e consentimento (LGPD) em `users/{uid}`

**De:** app Android · **Para:** painel web (`web_admin`) · **Data:** 2026-10-04

O app Android agora tem login com **e-mail e senha** e com **Google** (Firebase Auth do projeto `alarysai-b6e85`). O login com Apple ficou de fora por decisão do produto. No primeiro acesso, o app cria `users/{uid}` só com os campos que as regras permitem hoje (`displayName`, `photoUrl`, `language`, `createdAt`, `updatedAt`).

Fonte de verdade: [`web_admin/docs/data-model.md`](../../../web_admin/docs/data-model.md) → Usuários.

## 1. Configuração no console do Firebase (pendente)

| Item | Situação |
| --- | --- |
| Provedor **E-mail/senha** | Já ativo (o painel usa). |
| Provedor **Google** | **Precisa ser ativado** em Authentication → Sign-in method → Google. Sem isso, "Continuar com Google" falha. |
| SHA-1 de **debug** do app Android | Cadastrado em 2026-10-04 (`7F:D7:79:72:…:D7:53`). |
| SHA-1 de **release** (Play) | Cadastrar quando houver a chave de upload/assinatura da Play Store. |
| Modelo do e-mail de redefinição de senha | Revisar o texto e o remetente em Authentication → Templates (o app envia no idioma do usuário). |

## 2. Campos propostos em `users/{uid}`

Hoje as regras só aceitam campos de perfil. O cadastro do app exige o aceite da Política de Privacidade e dos Termos de Uso (LGPD), **mas não há onde registrar esse aceite**. O opt-in de novidades por e-mail do mockup foi **retirado do app** pelo mesmo motivo: coletar a escolha sem guardá-la seria enganoso.

| Campo | Tipo | Quem escreve | Para quê |
| --- | --- | --- | --- |
| `termsAcceptedAt` | `Timestamp` | usuário (só na criação) | Prova de quando aceitou a política e os termos. |
| `termsVersion` | `string` | usuário (só na criação) | Qual versão foi aceita (para pedir novo aceite quando mudar). |
| `marketingOptIn` | `boolean` | usuário | "Quero receber novidades e dicas por e-mail" (opcional, revogável). |
| `marketingOptInAt` | `Timestamp \| null` | usuário | Quando deu (ou tirou) o consentimento. |

### Regras sugeridas

- Criação: permitir também `termsAcceptedAt == request.time`, `termsVersion` (string curta) e `marketingOptIn`/`marketingOptInAt`.
- Atualização: `termsAcceptedAt` e `termsVersion` **imutáveis** pelo cliente (só uma nova aceitação, se o painel versionar os termos); `marketingOptIn`/`marketingOptInAt` podem mudar.
- Continuam proibidos: `creditBalance` e qualquer outro campo.

### Ainda falta definir

- **URLs** da Política de Privacidade e dos Termos de Uso. O app mostra o texto do aceite sem link até elas existirem.
- **Exclusão de conta** (LGPD): o app ainda não oferece. Precisa de uma Cloud Function ou rota no servidor que apague `users/{uid}`, `history` e `credits` e a conta do Auth.
- **Foto do perfil** ("Adicionar foto"): depende do Storage (plano Blaze) e de uma regra de Storage para `users/{uid}/avatar`.

## 3. Checklist

- [ ] Ativar o provedor Google no console.
- [ ] Campos e regras da seção 2 em `firestore.rules`, com testes em `rules-tests/`.
- [ ] `docs/data-model.md` com os campos novos.
- [ ] Publicar as páginas da política e dos termos e informar as URLs ao app.
- [ ] Definir o fluxo de exclusão de conta.
