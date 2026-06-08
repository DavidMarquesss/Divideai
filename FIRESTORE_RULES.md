# Regras de segurança do Firestore

O arquivo `firestore.rules` na raiz contém as regras que restringem o acesso ao
banco. **Hoje** seu projeto está em "test mode" — qualquer pessoa autenticada
(ou até anônima, dependendo do modo) pode ler e escrever tudo. Antes da
apresentação do TCC vale aplicar as regras deste arquivo.

## Como aplicar — via Firebase Console (mais fácil)

1. Abra https://console.firebase.google.com/ e selecione o projeto **divideai**.
2. Menu lateral → **Build → Firestore Database**.
3. Aba **Rules** (Regras).
4. Cole o conteúdo de `firestore.rules` por cima do que estiver lá.
5. Clique em **Publish** (Publicar).

A propagação leva ~30 s. Pode testar logo em seguida no app.

## Como aplicar — via Firebase CLI (opcional, se quiser versionar)

```bash
npm install -g firebase-tools          # uma vez só
firebase login                          # uma vez por máquina
firebase init firestore                 # selecione o projeto divideai;
                                        # quando perguntar o arquivo, aponte para firestore.rules
firebase deploy --only firestore:rules
```

## O que as regras garantem

- Toda leitura/escrita exige usuário autenticado.
- **/users**: qualquer logado lê (precisa pra mostrar avatar/nome de outros);
  só o dono escreve.
- **/groups**: só membros do grupo (`memberIds` contendo o uid) leem,
  atualizam e apagam. Qualquer logado pode criar.
- **/members** e **/expenses**: só membros do grupo correspondente acessam.
- **/friends**: só remetente e destinatário acessam aquele pedido/amizade.

## Como verificar se quebrou algo

Depois de publicar, faça login no app e:

1. Veja a sua tela inicial — grupos devem aparecer normalmente.
2. Entre em um grupo — despesas e membros devem aparecer.
3. Vá em **Buscar usuários** e tente abrir o perfil de alguém — tem que abrir.
4. Crie uma despesa nova num grupo — tem que salvar.

Se algo falhar, abra a aba **Logs** do Firestore no Console: vai listar a
regra que negou e o caminho do documento. Aí dá pra ajustar a regra
correspondente.
