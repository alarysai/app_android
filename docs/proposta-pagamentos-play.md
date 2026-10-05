# Pagamentos pela Google Play — Play Console, servidor e app

**Data:** 2026-10-04 · App: `com.alarysai.alarysai` · Firebase: `alarysai-b6e85`

A aba **Planos** do app vende **assinaturas** (planos com créditos por mês) e **pacotes avulsos de créditos** pela Google Play (Play Billing 7). O app já está pronto. Falta configurar o Play Console e construir o **servidor que valida as compras e dá os créditos**.

## 1. Por que precisa de servidor

- As regras do Firestore só deixam o **servidor** escrever `creditBalance` e `users/{uid}/credits`. Um cliente nunca pode se dar créditos.
- A Google exige que toda compra seja **confirmada** (acknowledge, ou consume nos pacotes) em até **3 dias**. Sem isso, a compra é **estornada automaticamente**.
- A confirmação deve acontecer **depois** de validar o `purchaseToken` na Google Play Developer API e entregar o que foi comprado. Fazer isso no app abriria espaço para fraude.

**Até o servidor existir:** o app recebe a compra, chama `PurchaseVerifier` (hoje `ServerlessPurchaseVerifier`, que responde "indisponível") e **não confirma nada**. A Google estorna o valor em 3 dias. Por isso a compra só fica habilitada no **build de debug**, para testadores. Na versão de produção, os botões mostram "Em breve".

## 2. Play Console (você)

1. **Produtos com estes IDs exatos** (estão em `feature/plans/.../data/billing/BillingCatalog.kt`; se preferir outros, me avise):

| Tipo | ID do produto | Sugestão |
| --- | --- | --- |
| Assinatura | `alarys_plano_basico` | plano base mensal (ex.: 40 créditos/mês) |
| Assinatura | `alarys_plano_pro` | plano base mensal (ex.: 150 créditos/mês) |
| Produto no app (consumível) | `alarys_creditos_50` | 50 créditos |
| Produto no app (consumível) | `alarys_creditos_200` | 200 créditos |

   O **nome, a descrição e o preço** que o app mostra vêm do Play Console, já traduzidos e na moeda do usuário. Use a descrição para dizer quantos créditos o produto dá.
2. **Plano base** em cada assinatura: o app usa o plano base (sem oferta promocional) e mostra o preço recorrente. Os períodos reconhecidos são semana, mês, 3 meses, 6 meses e ano.
3. **Testes:**
   - publique um AAB assinado numa faixa de **teste interno**;
   - adicione as contas de teste em **Configuração → Testes de licença**;
   - instale o app **pela Play Store** com uma dessas contas.

   A cobrança só funciona com o app instalado pela Play. Com o app instalado direto pelo Android Studio, a aba mostra "Google Play indisponível" ou "Planos em breve".
4. **Chave de release:** cadastre também o SHA-1 da chave de upload/assinatura da Play no app Android do Firebase. O login com Google em produção depende disso.
5. **Notificações em tempo real (RTDN):** crie um tópico Pub/Sub e configure-o em Monetização → Configuração, para o servidor saber de renovações, cancelamentos e estornos.

## 3. Servidor (equipe Web / Cloud Functions)

### 3.1 Validar a compra (chamada pelo app)

Endpoint autenticado (callable function ou rota no painel com o ID token do Firebase):

```
verifyPlayPurchase({ productId, purchaseToken, isSubscription }) → { result: "granted" | "rejected" }
```

1. Conferir o usuário pelo ID token.
2. Validar na **Google Play Developer API**:
   - `purchases.subscriptionsv2.get` (assinatura) ou `purchases.products.get` (pacote);
   - estado pago;
   - `obfuscatedExternalAccountId == sha256(uid)`, que o app envia em toda compra;
   - token **ainda não usado**: guardar os tokens processados.
3. Na **mesma transação do Firestore**:
   - criar `users/{uid}/credits/{id}` com `kind: "purchase"` (pacote) ou `"monthly_grant"` (assinatura), `amount`, `balanceAfter` e `purchaseRef: orderId`;
   - atualizar `creditBalance`.
4. Confirmar na API: `acknowledge` (assinatura) ou `consume` (pacote).
5. Responder `granted`. Se o token já tiver sido processado para o mesmo usuário, responder `granted` de novo, sem creditar outra vez (idempotente).

### 3.2 Renovações e estornos (RTDN)

- Renovação mensal: novo `monthly_grant`.
- Cancelamento ou expiração: parar os créditos mensais.
- Estorno (`voidedPurchases`): estornar os créditos (`kind: "refund"`, valor negativo).

### 3.3 Plano atual (proposta de campo)

Hoje o app descobre o plano atual pela própria Play Store. Para o resto do sistema (painel, card "Uso do seu plano" na Início), sugerimos que o servidor grave em `users/{uid}`:

| Campo | Tipo | Para quê |
| --- | --- | --- |
| `plan` | `{ productId, renewsAt, monthlyCredits } \| null` | plano ativo e franquia do mês |
| `creditsUsedThisCycle` | `number` | o "60% utilizado" do card da Início |

## 4. O que muda no app quando o servidor existir

1. Implementar `PurchaseVerifier` chamando `verifyPlayPurchase` e trocar o binding em `PlansModule` (hoje `ServerlessPurchaseVerifier`).
2. Ligar `PURCHASES_ENABLED` também no release (`feature/plans/build.gradle.kts`).
3. Ler `plan` e `creditsUsedThisCycle` para o card "Uso do seu plano".

O app já reenvia ao servidor, ao abrir a aba Planos, as compras que ficaram sem entrega (por exemplo, quando o app foi fechado no meio da compra).
