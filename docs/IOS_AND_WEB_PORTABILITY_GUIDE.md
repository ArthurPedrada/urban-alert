# Guia de Portabilidade para iOS (iPhone) e Web / PWA — Urban Alert

Este projeto foi construído nativamente para **Android** (`Kotlin + Jetpack Compose`), mas toda a arquitetura de dados, regras de segurança e identidade no **Firebase** já é multiplataforma e pode ser reutilizada diretamente no **iOS (SwiftUI)**, **Flutter / React Native** ou em uma versão **Web / PWA (funciona no Safari do iPhone sem precisar de Mac ou App Store)**.

---

## Opção Recomendada para Rodar Hoje no iPhone: Web / PWA no AI Studio

1. No Google AI Studio, crie um **novo projeto Web (React + TypeScript + Vite/Next.js)**.
2. Cole o **Prompt Pronto** abaixo no novo projeto.
3. Depois de publicado, abra o link no **Safari do iPhone**, toque em **Compartilhar** e depois em **Adicionar à Tela de Início** (*Add to Home Screen*) para usá-lo em tela cheia como aplicativo no iOS.

---

## Contrato Compartilhado do Backend (já ativo no Firebase)

- **Firebase Project ID:** `gen-lang-client-0306514623`
- **Firestore Database ID:** `ai-studio-android-urbanale-a04628a1-a243-4916-872d-5a08297c9f87`
- **Cidade-Piloto:** Salvador, BA (`cityCode: "ssa-ba"`, centro `-12.9714, -38.5014`, zoom `13`)
- **Coleções e Regras (`firestore.rules`):**
  - `/profiles/{userId}`: Perfil cidadão autenticado (`userId`, `displayName`, `email`, `photoUrl`, `homeCityCode`, `role`, `acceptedPrivacyVersion`, `createdAt`, `updatedAt`).
  - `/admins/{userId}`: Controle administrativo gerenciado via servidor/console (`role`, `organizationId`).
  - `/categories/{categoryId}`: Catálogo público (`slug`, `title`, `description`, `defaultSeverity`, `active`).
  - `/agencies/{agencyId}`: Órgãos públicos de Salvador (`SEMAN / SUCOP`, `LIMPURB`, `CODESAL` `199`, `TRANSALVADOR`, `SEDUR`, `EMBASA`).
  - `/incidents/{incidentId}`: Ocorrências públicas sem PII (`authorId`, `cityCode`, `categorySlug`, `title`, `description`, `severity`, `status`, `latitude`, `longitude`, `addressLabel`, `publicImageUrl`, `stillExistsCount`, `suggestResolvedCount`, `idempotencyKey`, `createdAt`, `updatedAt`). Consultas públicas exigem `limit <= 200`.
  - `/incident_confirmations/{incidentId}_{userId}_{confirmationType}`: Confirmação comunitária única (`still_exists` / `suggest_resolved`), sem alterar `status`.
  - `/incident_status_history/{historyId}`, `/audit_logs/{logId}`, `/ai_telemetry/{telemetryId}`: Trilha oficial imutável, logs administrativos e telemetria sem PII.

---

## Prompt Pronto para Colar em um Novo Projeto Web (PWA para iPhone / iOS)

```text
Crie a versão Web Responsiva e PWA (otimizada para iPhone/iOS Safari "Adicionar à Tela de Início", Android e Desktop) do aplicativo **Urban Alert AI** — plataforma cívica geolocalizada ("Waze de problemas urbanos"), com cidade-piloto Salvador, Bahia, Brasil (-12.9714, -38.5014).

Requisitos técnicos e funcionais obrigatórios:
1. Stack e Visual: React + TypeScript + Tailwind CSS (ou CSS tokens) com tema claro/escuro, cores grafite #20282B, verde cívico #25A66A e fundo cartográfico claro #DCE8DC, fontes Space Grotesk (títulos) e Plus Jakarta Sans (textos). Layout mobile-first com safe-area-inset do iOS (viewport-fit=cover), manifest.json PWA e ícones Apple Touch Icon.
2. Firebase Compartilhado: Conecte-se ao projeto Firebase existente (ID `gen-lang-client-0306514623`, Firestore Database ID `ai-studio-android-urbanale-a04628a1-a243-4916-872d-5a08297c9f87`) com Firebase Auth (Login Google) e App Check.
3. Mapa Público sem Login: Use Leaflet + OpenStreetMap centrado em Salvador (-12.9714, -38.5014), com clusterização por zoom, consulta paginada no Firestore por área visível (sempre usando limit <= 150), filtros por categoria e status, busca de endereços e botão de GPS do navegador (opcional).
4. Categorias e Semântica Acessível:
   - Slugs oficiais: buracos-em-vias, acumulo-de-lixo, iluminacao-publica, vazamentos, alagamentos, problemas-de-drenagem, sinalizacao-danificada, semaforos-quebrados, barreiras-de-acessibilidade.
   - Gravidade (low, medium, high, critical) e Status (open, under_review, in_progress, resolved, rejected) sempre exibidos com ícone + texto, nunca apenas cor.
5. Privacidade de Mídia (EXIF) e IA Responsável:
   - Ao selecionar/tirar foto no iPhone ou navegador, redesenhe a imagem em HTML5 Canvas no cliente antes do upload/análise para remover todos os metadados EXIF/GPS sensíveis.
   - Assistente conversacional com Gemini API retornando JSON estruturado (categoria, gravidade, título, descrição, confiança) com fallback manual e revisão humana obrigatória antes de publicar em /incidents usando chave de idempotência (`inc_<uuid>`).
6. Modo Offline e Comunidade:
   - Rascunhos locais e fila pendente em IndexedDB com sincronização automática quando voltar a internet.
   - Tela de detalhes com confirmações comunitárias únicas ("Ainda existe" / "Aparenta resolvida" em `/incident_confirmations/{incidentId}_{userId}_{type}` com incremento atômico sem alterar o status oficial) e exibição dos órgãos públicos verificados de Salvador (Fala Salvador 156, salvadordigital.salvador.ba.gov.br, CODESAL 199, EMBASA) com aviso transparente de que o registro não abre protocolo automático na prefeitura.
7. Conta LGPD e Painel Admin:
   - Aba Conta com perfil editável, logout e exclusão definitiva da conta e perfil (/profiles/{uid}).
   - Painel Administrativo visível apenas quando existir documento ativo em `/admins/{uid}`, com triagem oficial (atualizando status + /incident_status_history + /audit_logs em WriteBatch), gestão de categorias/órgãos e relatórios.
```

---

## Observação para App Nativo iOS (Swift / Xcode)

Se futuramente você quiser publicar na **Apple App Store** como app nativo Swift:
- Crie um projeto **SwiftUI** no **Xcode (macOS)** com `MapKit`, `PhotosPicker` (removendo EXIF com `ImageIO`/`CGImageDestination` sem copiar metadados GPS), `FirebaseFirestore` + `FirebaseAuth` (Google Sign-In + Sign in with Apple) apontando para o mesmo Database ID e usando exatamente o contrato de `firebase-blueprint.json` acima.
