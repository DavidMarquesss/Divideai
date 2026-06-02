# DivideAi notifier

Script Node.js externo para enviar push notifications (FCM) aos usuários do DivideAi sem precisar do plano Blaze do Firebase. Roda na sua máquina (ou em qualquer servidor) e usa o **Firebase Admin SDK** para falar com o Firestore e o FCM.

## Setup (apenas uma vez)

1. **Gerar a chave de serviço** no Firebase Console:
   - `Project settings` → `Service accounts` → `Generate new private key`
   - Salvar o arquivo baixado **dentro desta pasta** com o nome `service-account.json`
   - Esse arquivo dá acesso administrativo ao projeto — **nunca commitar no git**

2. **Instalar dependências**:
   ```bash
   cd notifier
   npm install
   ```

## Envio único — `send.js`

Manda uma notificação pontual para um usuário específico. Útil pra testar.

```bash
# Por UID
node send.js --uid AbCdEfGhIj1234567890 --title "Lembrete" --body "Você ainda deve R$ 50 ao João."

# Por email
node send.js --email maria@exemplo.com --title "Oi!" --body "Sua despesa foi adicionada."
```

O script lê `users/{uid}.fcmToken` do Firestore (preenchido automaticamente pelo app DivideAi assim que o usuário abre o app logado) e usa o FCM Admin SDK pra enviar.

## Watcher contínuo — `watch.js`

Fica escutando o Firestore e dispara notificações automaticamente quando:
- **Solicitação de amizade nova** → notifica o destinatário
- **Despesa nova** → notifica todos os participantes (exceto o pagador)

Pré-requisito: as collections precisam ter um campo `createdAt` (Firestore Timestamp). Se ainda não tem, dá pra adicionar `createdAt: FieldValue.serverTimestamp()` ao criar os documentos no app.

```bash
node watch.js
```

Mantém o terminal aberto rodando. Pra desenvolvimento local, ideal. Pra produção, você pode hospedar em qualquer VPS / Cloud Run / Render free tier.

## Como funciona o lado do app

O app DivideAi:
- Já tem `firebase-messaging-ktx` adicionado
- O `DivideAiMessagingService` (em `app/src/main/java/com/example/divideai/notifications/`) recebe pushes e os exibe
- Ao abrir o app logado, salva o token FCM atual em `users/{uid}.fcmToken`
- No Android 13+, pede a permissão `POST_NOTIFICATIONS` na primeira execução

## Notas de segurança

- `service-account.json` é a "chave do reino" — **não commite**. Tem um `.gitignore` nesta pasta excluindo `service-account.json` e `node_modules/`.
- As regras de segurança do Firestore precisam permitir o app gravar o próprio `fcmToken` no documento do usuário (o `update` é feito pelo usuário autenticado).
