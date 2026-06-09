# DivideAi

## Documentação de Funcionalidades

### Propósito

O DivideAi é um aplicativo Android projetado para simplificar a divisão de despesas entre amigos, familiares e grupos. Ele permite que os usuários registrem despesas, dividam os custos de forma justa e acompanhem quem deve a quem, tornando a gestão financeira de atividades em grupo mais fácil e transparente.

### Público Alvo

Este aplicativo é ideal para:

- Amigos que viajam juntos ou saem com frequência.
- Colegas de quarto que compartilham despesas domésticas.
- Famílias que precisam organizar os gastos.
- Qualquer grupo de pessoas que precise de uma maneira simples de dividir custos.

### Telas do App

<table>
  <tr>
    <td align="center" width="33%">
      <img src="docs/screenshots/01-grupos.png" alt="Lista de grupos" width="240" /><br/>
      <sub><b>Lista de grupos</b><br/>com avatar de perfil e foto do grupo</sub>
    </td>
    <td align="center" width="33%">
      <img src="docs/screenshots/02-grupo-despesas.png" alt="Despesas do grupo" width="240" /><br/>
      <sub><b>Despesas do grupo</b><br/>com categorias e status de pagamento</sub>
    </td>
    <td align="center" width="33%">
      <img src="docs/screenshots/03-despesa-detalhes.png" alt="Detalhes da despesa" width="240" /><br/>
      <sub><b>Detalhes da despesa</b><br/>com comprovante, pagador e participantes</sub>
    </td>
  </tr>
  <tr>
    <td align="center" width="33%">
      <img src="docs/screenshots/04-dashboard.png" alt="Dashboard" width="240" /><br/>
      <sub><b>Dashboard de gastos</b><br/>gráfico de pizza por categoria</sub>
    </td>
    <td align="center" width="33%">
      <img src="docs/screenshots/05-saldos-simplificados.png" alt="Saldos simplificados" width="240" /><br/>
      <sub><b>Saldos simplificados</b><br/>compensação mínima entre membros</sub>
    </td>
    <td></td>
  </tr>
</table>

### Funcionalidades

- **Autenticação de Usuário:** Crie uma conta e faça login para acessar suas informações de forma segura.
- **Gestão de Grupos:** Crie grupos, adicione membros e organize as despesas por evento ou categoria. Compartilhe o convite por **QR Code** para que outros usuários entrem no grupo apenas escaneando.
- **Registro de Despesas:** Adicione novas despesas, especifique o valor, a descrição, quem pagou e quem são os participantes. Cada despesa pode ter uma **categoria** (Comida, Transporte, Compras, Saúde, Lazer, etc.) com ícone próprio e um **comprovante** (foto do recibo).
- **Divisão de Contas:** O aplicativo calcula automaticamente quanto cada participante deve, com base nas despesas registradas.
- **Acompanhamento de Dívidas:** Visualize de forma clara quem te deve dinheiro e para quem você deve.
- **Simplificação de Dívidas:** Um algoritmo guloso encontra o conjunto mínimo de transferências necessárias para zerar todos os saldos do grupo (ex.: se A deve a B e B deve a C, sugere que A pague direto C).
- **Dashboard de Gastos:** Tela com gráfico de pizza mostrando a distribuição das despesas por categoria.
- **Gestão de Amigos:** Adicione amigos à sua rede para facilitar a inclusão em grupos e despesas.
- **Fotos:** Cadastre uma foto de perfil para sua conta, uma foto para o grupo e anexe o comprovante de cada despesa. As imagens são armazenadas em Base64 dentro do próprio documento do Firestore (até 1 MB cada), evitando a dependência do Firebase Storage.
- **Notificações Push:** Notificações via Firebase Cloud Messaging (FCM). Disparo opcional por um script Node.js (pasta `notifier/`).
- **Modo Escuro:** Tema claro, escuro ou automático (segue o sistema), com persistência local da escolha.
- **Internacionalização:** O app está disponível em **pt-BR** e **en**, com seletor dentro do app (Per-App Language Preferences).
- **Pull-to-refresh e Empty States:** Listas principais (grupos, despesas, amigos) recarregam ao arrastar pra baixo e mostram ilustração + mensagem quando estão vazias.

### Possíveis atualizações

- **Visualizar Despesas de um Membro em um Grupo:** Poder dentro de um grupo selecionar um membro e visualizar despesas dele relacionadas ao usuário.
- **Métodos Customizados de Divisão de Despesas:** Ser possível dividir a despesa por porcentagem, por valor, customizada por usuário. Atualmente a despesa é dividida de maneira igualitária.
- **Marcar Pagamento de Dívida:** Hoje o cálculo considera o campo `paid` das shares, mas ainda não há fluxo de UI para que o credor confirme o recebimento.
- **Histórico/Auditoria:** Log de quem criou ou editou cada despesa para facilitar correções.

### Como Usar

1. **Crie sua Conta:** Baixe o aplicativo e registre-se com seu e-mail e senha.
2. **Crie um Grupo:** Na tela principal, acesse a aba "Grupos" e crie um novo grupo para sua viagem, moradia ou qualquer outra finalidade.
3. **Adicione Membros:** Convide seus amigos para o grupo (pela busca ou compartilhando o QR Code) para que todos possam visualizar e adicionar despesas.
4. **Registre uma Despesa:** Sempre que alguém pagar por algo, adicione uma nova despesa no grupo, informando o valor, a categoria, quem participou e (opcional) o comprovante.
5. **Acompanhe os Saldos:** O aplicativo mostrará os saldos atualizados, indicando quem precisa pagar e quem tem dinheiro a receber, com a opção de exibir a versão simplificada das dívidas.

---

## Detalhes Técnicos e Arquitetura

### Diagrama de Classes do Domínio do Problema

![Diagrama de Classes](out/teste/teste.png)

O arquivo-fonte fica em [`teste.puml`](teste.puml) (PlantUML). Para regerar:

```bash
java -jar plantuml.jar -tpng -o out/teste teste.puml
java -jar plantuml.jar -tsvg teste.puml
```

> Se aparecer “Dot Executable” na imagem, falta o Graphviz. Como alternativa, o `.puml` já habilita o layout nativo Java via `!pragma layout smetana` — basta usar o `plantuml.jar` oficial sem instalar nada extra.

### Ferramentas Escolhidas

- **Controle de Versão:** Git (hospedado no GitHub).
- **Ferramenta de Build:** Gradle (padrão em projetos Android).
- **Testes:** JUnit para testes unitários e Espresso para testes de interface (UI) (configurados no `build.gradle.kts`).
- **Issue Tracking:** GitHub Issues (para acompanhamento de bugs, tarefas e novas funcionalidades).
- **CI/CD:** Devido ao escopo atual do projeto, o processo de build e deploy é manual. Pipelines de CI/CD (ex.: GitHub Actions) podem ser implementados em evoluções futuras para automação de testes e geração de APKs.
- **Containerização:** Não aplicável. O projeto é um aplicativo Android nativo, sendo executado diretamente em dispositivos físicos ou emuladores através do Android Virtual Device (AVD).

### Frameworks Reutilizados

- **Android SDK:** Desenvolvimento nativo utilizando a linguagem **Kotlin**.
- **Android Jetpack:**
  - **ViewModel & LiveData:** Para gerenciamento de estado da UI e reatividade.
  - **Navigation Component:** Para navegação fluida entre os fragmentos do aplicativo.
  - **ViewBinding:** Para interagir de forma segura com os componentes de interface definidos em XML.
  - **AppCompat + Per-App Language:** Suporte ao seletor de idioma do próprio app (`AppCompatDelegate.setApplicationLocales`).
  - **SwipeRefreshLayout:** Pull-to-refresh nas listas.
  - **Activity Result API (Photo Picker):** Seleção de imagens da galeria sem precisar de permissão de Storage.
  - **ExifInterface:** Corrige a orientação de fotos enviadas pela câmera/galeria.
- **Material 3:** Componentes (ShapeableImageView, Chips, TextInputLayout, AlertDialog) e tema DayNight com modo escuro.
- **Firebase:**
  - **Firebase Authentication:** Para registro e login de usuários.
  - **Cloud Firestore:** Banco de dados NoSQL para armazenar usuários, grupos, despesas, membros, amigos e as próprias imagens em Base64.
  - **Firebase Cloud Messaging (FCM):** Notificações push para o cliente Android. As regras de envio ficam num script Node.js externo na pasta `notifier/`.
- **MPAndroidChart:** Gráficos do dashboard (pizza por categoria).
- **ZXing (`journeyapps:zxing-android-embedded`):** Geração e leitura do QR Code de convite de grupo.

### Estrutura de Pastas Relevantes

- `app/src/main/java/com/example/divideai/data/model/` — entidades persistidas (User, Group, Member, Expense, ExpenseShare, FriendRequest) + categorias (`ExpenseCategory`).
- `app/src/main/java/com/example/divideai/data/repository/` — camada de acesso ao Firestore.
- `app/src/main/java/com/example/divideai/data/image/` — utilitários de imagem: `Base64Image` (encode/decode + resize/EXIF), `UserAvatarCache` (cache em memória das fotos de perfil), `AvatarBinding` (extensão `ImageView.loadUserAvatar`).
- `app/src/main/java/com/example/divideai/data/balance/` — `DebtSimplifier` (algoritmo guloso de simplificação de dívidas).
- `app/src/main/java/com/example/divideai/data/invite/` — `GroupInviteCode` (encode/decode da URI `divideai://group/<id>`).
- `app/src/main/java/com/example/divideai/notifications/` — `DivideAiMessagingService` (handler FCM no cliente).
- `notifier/` — scripts Node.js (`send.js`, `watch.js`) que usam o Firebase Admin SDK para disparar notificações de fora do app.
- `firestore.rules` + `FIRESTORE_RULES.md` — regras de segurança do Firestore e como aplicá-las pelo Console.

### Configuração do Firebase

O `app/google-services.json` já está versionado para facilitar a execução em ambiente acadêmico. Antes da apresentação em produção:

1. Aplicar as regras em [`firestore.rules`](firestore.rules) seguindo o passo a passo de [`FIRESTORE_RULES.md`](FIRESTORE_RULES.md). Por padrão o Firestore é criado em "test mode" (acesso aberto por ~30 dias).
2. Habilitar **Firebase Cloud Messaging** (já vem ativo em projetos novos).
3. **Não é necessário ativar o Firebase Storage** — as imagens são salvas em Base64 dentro do próprio Firestore.

### Como Gerar a Documentação do Código

A ferramenta oficial para documentação de código Kotlin é o **Dokka** (o equivalente ao JavaDoc). Como alternativa usando as ferramentas integradas da IDE:

1. Abra o projeto no **Android Studio**.
2. No menu superior, vá em **Tools** > **Generate JavaDoc...**.
3. Selecione o escopo da documentação (ex.: o projeto inteiro ou um pacote específico).
4. Especifique o diretório de saída (Output directory) e clique em **OK**.

*(Nota: Para suporte completo às funcionalidades Kotlin e exportação em formatos HTML modernos, recomenda-se adicionar o plugin do Dokka no arquivo `build.gradle.kts` do projeto).*

### Como Executar o Sistema

Para rodar o projeto localmente, siga os passos abaixo:

1. **Pré-requisitos:** Certifique-se de ter o [Android Studio](https://developer.android.com/studio) instalado na sua máquina.
2. **Clonar o Repositório:**
   ```bash
   git clone <URL_DO_REPOSITORIO>
   ```
3. **Abrir o Projeto:** Inicie o Android Studio, clique em **Open** e selecione a pasta raiz do projeto clonado.
4. **Sincronização:** Aguarde o Android Studio realizar o download das dependências e a sincronização do Gradle (*Sync Project with Gradle Files*).
5. **Configuração do Firebase:** O projeto já contém o arquivo `google-services.json` configurado na pasta `app/`. Em caso de problemas de comunicação com o banco, verifique se as regras do Firestore permitem leitura/escrita ou configure um novo projeto no Firebase Console e substitua o arquivo.
6. **Execução:**
   - Configure um **Emulador (AVD)** no Android Studio ou conecte um dispositivo físico via USB (com o modo de depuração ativado).
   - Clique no botão **Run** (ícone de "Play" verde na barra superior) ou pressione `Shift + F10`.
   - O Gradle construirá o aplicativo e o instalará no dispositivo selecionado.
