# Roteiro do app Android

Uma task por vez: cada uma é testada no aparelho antes da próxima. Base: [`android-integration.md`](../../web_admin/docs/android-integration.md).

| # | Task | Seção do guia | Situação |
| --- | --- | --- | --- |
| 1 | Base do projeto (módulos, Hilt, Compose, Navigation, Firebase) + **Início: grade de categorias** | 2, 3, 4.1 | ✅ feita (2026-10-02) |
| 1b | **Tela Início conforme o mockup** (logo, busca, saudação, uso do plano, cards de categoria, chat) + **barra inferior** (Início, Histórico, Planos, Club AI, Perfil) | — | ✅ feita (2026-10-02), validada no aparelho |
| 2 | **Questionários de uma categoria**: tocar na categoria abre a lista de publicados (sem tradução completa: aparece em português com selo) | 4.2 | ✅ feita (2026-10-02) |
| 3 | **Questionário e passos**: domínio `Step`/`StepOption`, `ResolveNextStepUseCase` + `AdvanceQuestionnaireUseCase` (seção 6, limite de 200 passos), telas de pergunta e de vídeo, "questionário indisponível" | 4.3, 5, 6 | ✅ feita (2026-10-02); falta validar com um questionário publicado |
| 4 | **Informação booleana (`InfoFlag`) + dica** ligada ao passo (dica inativa = sem dica) | 4.4, 6 | ✅ feita (2026-10-02); falta validar com um questionário publicado |
| 5 | **Partes do prompt** (`CollectPromptPartsUseCase`) e tela de conclusão. O envio fica para quando o serviço de geração existir | 6 | ✅ feita (2026-10-03); "Gerar conteúdo" avisa que chega em breve |
| 5b | **Questionário v2 conforme os mockups** (tela cheia, progresso, tipos de resposta, pular, dica por opção, revisão com prévia do prompt e custo) | 4.3, 6 | ✅ feita (2026-10-03); os tipos novos dependem dos campos propostos à equipe Web ([docs/proposta-questionario-v2.md](docs/proposta-questionario-v2.md)); falta validar com um questionário publicado |
| 6 | **Dicas**: categorias de dicas e dicas por categoria, na aba Club AI | 4.4 | ✅ feita (2026-10-03); falta validar com dicas ativas no painel |
| 7 | **Anunciantes**: lista agrupada por tipo (sem maiúsculas/acentos), link em Custom Tabs, aba Club AI junto com Dicas | 4.5 | ✅ feita (2026-10-03); falta validar com anunciantes ativos no painel |
| 8 | **Planos**: assinaturas e pacotes de créditos pela Google Play (Play Billing), aba Planos, "Adquirir créditos" da Início | — | ✅ app pronto (2026-10-04), compra só no debug; falta configurar o Play Console e o servidor que valida as compras ([docs/proposta-pagamentos-play.md](docs/proposta-pagamentos-play.md)) |
| 9 | **Login e perfil**: e-mail/senha e Google (sem Apple), cadastro com aceite LGPD, recuperação de senha, primeiro acesso, aba Perfil, idioma do perfil, saudação | 8 | ✅ feita (2026-10-04); falta ativar o provedor Google no console e validar com uma conta real; campos de consentimento propostos em [docs/proposta-usuarios-login.md](docs/proposta-usuarios-login.md) |
| 10 | **Histórico e créditos** (só leitura; dono pode apagar itens do histórico) | 8 | ✅ feita (2026-10-03) sem login: a aba mostra "Entre na sua conta" até a Task 9; dados reais só depois do serviço de geração |
| 11 | **Abertura (splash)** preparada para vídeo: `assets/splash/intro.mp4` toca em tela cheia com Pular; sem o arquivo, logo animado | — | ✅ feita (2026-10-06); falta o vídeo oficial |

Pendências externas conhecidas:

- Logo oficial em vetor (hoje: imagens PNG `icon_app` e `ic_alarys`).
- Vídeo de abertura oficial (`app/src/main/assets/splash/intro.mp4`).
- Descrição das categorias ("Crie e edite textos com IA"): exige um campo novo em `questionnaireCategories` no painel.
- Imagens (`image`, `icon`) só depois que o Storage for ativado (plano Blaze).
- Serviço de geração (IA, créditos, histórico) ainda não existe.
