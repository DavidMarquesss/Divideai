# DivideAi

## Documentação de Funcionalidades

### Propósito

O DivideAi é um aplicativo Android projetado para simplificar a divisão de despesas entre amigos, familiares e grupos. Ele permite que os usuários registrem despesas, dividam os custos de forma justa e acompanhem quem deve a quem, tornando a gestão financeira de atividades em grupo mais fácil e transparente.
[Link da Apresentação do App](https://youtu.be/Qb-sMwQeXyg).

### Público Alvo

Este aplicativo é ideal para:

- Amigos que viajam juntos ou saem com frequência.
- Colegas de quarto que compartilham despesas domésticas.
- Famílias que precisam organizar os gastos.
- Qualquer grupo de pessoas que precise de uma maneira simples de dividir custos.

### Funcionalidades

- **Autenticação de Usuário:** Crie uma conta e faça login para acessar suas informações de forma segura.
- **Gestão de Grupos:** Crie grupos, adicione membros e organize as despesas por evento ou categoria.
- **Registro de Despesas:** Adicione novas despesas, especifique o valor, a descrição, quem pagou e quem são os participantes.
- **Divisão de Contas:** O aplicativo calcula automaticamente quanto cada participante deve, com base nas despesas registradas.
- **Acompanhamento de Dívidas:** Visualize de forma clara quem te deve dinheiro e para quem você deve.
- **Gestão de Amigos:** Adicione amigos à sua rede para facilitar a inclusão em grupos e despesas.
- **Internacionalização:** O app está disponível para os idiomas pt-BR e en.

### Possíveis atualizações
- **Upload de Fotos:** Adicionar funcionalidade de escolher avatar para o cadastro de grupos e usuários. Não implementado pois a versão gratuita do Firebase não comportava upload de fotos.
- **Visualizar Despesas de um Membro em um Grupo:** Poder dentro de um grupo selecionar um membro e visualizar despesas dele relacionadas ao usuário.
- **Métodos Customizados de Divisão de Despesas:** Ser possível dividir a despesa por porcentagem, por valor, customizada por usuário. Atualmente a despesa é dividida de maneira igualitária.

### Como Usar

1. **Crie sua Conta:** Baixe o aplicativo e registre-se com seu e-mail e senha.
2. **Crie um Grupo:** Na tela principal, acesse a aba "Grupos" e crie um novo grupo para sua viagem, moradia ou qualquer outra finalidade.
3. **Adicione Membros:** Convide seus amigos para o grupo para que todos possam visualizar e adicionar despesas.
4. **Registre uma Despesa:** Sempre que alguém pagar por algo, adicione uma nova despesa no grupo, informando o valor e quem participou.
5. **Acompanhe os Saldos:** O aplicativo mostrará os saldos atualizados, indicando quem precisa pagar e quem tem dinheiro a receber.

---

## Detalhes Técnicos e Arquitetura

### Diagrama de Classes do Domínio do Problema

![Diagrama de Classes](out/teste/teste.png)

### Ferramentas Escolhidas

- **Controle de Versão:** Git (hospedado no GitHub).
- **Ferramenta de Build:** Gradle (padrão em projetos Android).
- **Testes:** JUnit para testes unitários e Espresso para testes de interface (UI) (configurados no `build.gradle`).
- **Issue Tracking:** GitHub Issues (para acompanhamento de bugs, tarefas e novas funcionalidades).
- **CI/CD:** Devido ao escopo atual do projeto, o processo de build e deploy é manual. Pipelines de CI/CD (ex.: GitHub Actions) podem ser implementados em evoluções futuras para automação de testes e geração de APKs.
- **Containerização:** Não aplicável. O projeto é um aplicativo Android nativo, sendo executado diretamente em dispositivos físicos ou emuladores através do Android Virtual Device (AVD).

### Frameworks Reutilizados

- **Android SDK:** Desenvolvimento nativo utilizando a linguagem **Kotlin**.
- **Android Jetpack:**
  - **ViewModel & LiveData:** Para gerenciamento de estado da UI e reatividade.
  - **Navigation Component:** Para navegação fluida entre os fragmentos do aplicativo.
  - **ViewBinding:** Para interagir de forma segura com os componentes de interface definidos em XML.
- **Firebase:**
  - **Firebase Authentication:** Para registro e login de usuários.
  - **Cloud Firestore:** Banco de dados NoSQL para armazenar informações de usuários, grupos, despesas e relacionamentos.

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
5. **Configuração do Firebase:** O projeto já contém o arquivo `google-services.json` configurado na pasta `app/`. Em caso de problemas de comunicação com o banco, verifique se as regras do Firestore permitem leitura/escrita ou configure um novo projeto no Firebase console e substitua o arquivo.
6. **Execução:**
   - Configure um **Emulador (AVD)** no Android Studio ou conecte um dispositivo físico via USB (com o modo de depuração ativado).
   - Clique no botão **Run** (ícone de "Play" verde na barra superior) ou pressione `Shift + F10`.
   - O Gradle construirá o aplicativo e o instalará no dispositivo selecionado.
