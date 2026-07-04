# DivideAi — Roteiro de Slides (Apresentação Técnica)

**Subtítulo:** App Android nativo (Kotlin) para dividir despesas em grupo de forma justa — Emilly Neves & David Marques · Projeto Final 2025/2 · Licença MIT

> Roteiro **sincronizado com [`slides.md`](slides.md)** (24 slides) e escrito para **quem não participou do projeto**: cada slide traz uma linha **🧩 O que é** (o conceito em português simples), um **👉 Foco** (o que olhar / por que importa) e a **🎤 Fala** — que primeiro explica o conceito e depois mostra como usamos aqui. A linha-chave do código está marcada com **⭐**.

## Tabela de tempo (~15 min)

| # | Slide | Tempo |
|--:|-------|------:|
| 1 | Capa | 0:30 |
| 2 | Propósito + público | 0:50 |
| 3 | Modelo de domínio | 0:40 |
| 4 | Arquitetura + MVVM | 0:50 |
| 5 | ⭐ DebtSimplifier | 1:10 |
| 6 | Build: Gradle + Wrapper | 0:45 |
| 7 | Build: repositórios | 0:35 |
| 8 | Build: Version Catalog | 0:40 |
| 9 | Build: dependências + BoM | 0:45 |
| 10 | Firebase Authentication | 0:45 |
| 11 | Cloud Firestore | 0:45 |
| 12 | Cloud Messaging (FCM) | 0:40 |
| 13 | Navigation + ViewBinding | 0:40 |
| 14 | MPAndroidChart | 0:30 |
| 15 | ZXing (QR Code) | 0:35 |
| 16 | Photo Picker + Base64 | 0:35 |
| 17 | Per-App Language (i18n) | 0:25 |
| 18 | Testes: estrutura | 0:45 |
| 19 | Testes: DebtSimplifierTest | 0:55 |
| 20 | Opcionais: Issue tracking + CI/CD | 0:45 |
| 21 | Opcionais: containers + notifier | 0:40 |
| 22 | Demo ao vivo (opcional) | 1:00 |
| 23 | Encerramento | 0:40 |
| 24 | Obrigado / perguntas | 0:15 |
| | **Total** | **~15:30** |

> **Se apertar:** comprima os slides **14 (MPAndroidChart)**, **16 (Photo Picker)** e **17 (i18n)** — a demo já mostra esses recursos na prática.

---

## Slide 1 — Capa: DivideAi

**No slide:** título, subtítulo, autores, 2025/2, MIT, repositório e badges de stack.

🎤 **Fala:** "Boa noite! Somos a Emilly e o David; este é o DivideAi, nosso projeto final. É um **app Android nativo** — ou seja, feito especificamente para Android, na linguagem oficial da plataforma, o Kotlin — para dividir despesas em grupo sem confusão. Nos próximos 15 minutos vamos explicar o problema, e depois o build, os frameworks, os testes e os itens opcionais, sempre mostrando o código."

⏱ **Tempo:** ~0:30

---

## Slide 2 — Propósito + público

👉 **Foco:** não é só dividir a conta — o app **simplifica as dívidas** ao mínimo de transferências.

**No slide:** a dor (rateio vira bagunça), a solução (registrar + dividir + saldos claros) e o público (qualquer grupo que rateia custos).

🎤 **Fala:** "O problema é bem cotidiano: um grupo viaja ou divide um apê, as despesas se espalham e ninguém sabe ao certo quem deve a quem. O DivideAi resolve isso — você registra cada despesa dizendo quem pagou e entre quem dividir, e o app calcula os saldos. O nosso diferencial é **simplificar as dívidas**: imagine que o A deve 10 ao B e o B deve 10 ao C. Em vez de duas transferências, o app percebe que basta o A pagar 10 direto ao C. Ele encontra o **menor número de pagamentos** que zera todo mundo. Serve pra amigos de viagem, república, família — qualquer grupo que rateia custos."

⏱ **Tempo:** ~0:50

---

## Slide 3 — Modelo de domínio (o que é persistido)

🧩 **O que é:** *modelo de domínio* são as "fichas" de dados que o app guarda. `data class` é o jeito do Kotlin de criar um objeto que só carrega dados (como uma ficha).

👉 **Foco:** `payerId` (quem pagou) + `participants` (quem deve quanto) — é a **entrada do algoritmo e dos testes**.

```kotlin
// data/model/Expense.kt
data class ExpenseShare(
    val userId: String = "",
    val amountOwed: Double = 0.0,   // quanto este usuário deve ao pagador
    val paid: Boolean = false       // já quitado?
)

data class Expense(
    val id: String = "", val groupId: String = "", val title: String = "",
    val amount: Double = 0.0, val date: String = "",
    val payerId: String = "",                            // ⭐ quem pagou
    val participants: List<ExpenseShare> = emptyList(),  // ⭐ entre quem dividir
    val category: String = "", val receipt: String = ""  // receipt = Base64
)
```

🎤 **Fala:** "Antes do código, uma 'ficha' central: a despesa. No nosso modelo, uma **despesa** tem um pagador (`payerId`) e uma lista de participações; cada participação diz quanto aquela pessoa deve e se já pagou. Reparem que todo campo tem um valor padrão — isso é porque o banco de dados precisa conseguir criar o objeto vazio para preencher depois. É essa ficha que alimenta tanto o cálculo quanto os testes que vou mostrar."

⏱ **Tempo:** ~0:40

---

## Slide 4 — Arquitetura: camadas + MVVM

🧩 **O que é:** *MVVM* é uma forma de **organizar o código em camadas**: a tela (View) não faz conta; ela apenas observa um "gerente de estado" (o *ViewModel*). *LiveData* é uma caixinha que **avisa a tela** toda vez que o dado muda.

👉 **Foco:** a tela só **observa** o estado; a regra pesada fica fora dela → **testável**.

```kotlin
// ui/auth/LoginViewModel.kt — estado da tela exposto como LiveData
class LoginViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = AuthRepository()
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState        // ⭐ só-leitura p/ a UI

    fun login(email: String, password: String) = viewModelScope.launch {
        _loginState.value = LoginState.Loading
        try { repository.signIn(email, password); _loginState.value = LoginState.Success }
        catch (e: Exception) { _loginState.value = LoginState.Error(/* msg traduzida */) }
    }
}
// LoginActivity: viewModel.loginState.observe(this) { render(it) }
```

🎤 **Fala:** "Organizamos o projeto em camadas, num padrão chamado MVVM. A ideia é separar responsabilidades: a **tela** só mostra coisas e reage; quem guarda o estado e chama as regras é o **ViewModel**. Aqui, o login ViewModel expõe um `loginState` num **LiveData** — pensem numa caixinha que a tela fica 'de olho'. Quando o login vira Carregando, Sucesso ou Erro, a caixinha muda e a tela se atualiza sozinha, sem a gente escrever isso na mão. A vantagem: como a regra de negócio não está presa à tela, dá pra testá-la isolada — e é exatamente o que fazemos com o algoritmo."

⏱ **Tempo:** ~0:50

---

## Slide 5 — ⭐ Algoritmo estrela: DebtSimplifier

🧩 **O que é:** um *algoritmo guloso* é uma estratégia que, a cada passo, faz a "melhor jogada local" — aqui, quitar a maior dívida com o maior crédito — repetindo até acabar. É **Kotlin puro** (não depende de Android/Firebase).

👉 **Foco:** o loop pega sempre o **maior credor × maior devedor** (⭐) — é o que produz o mínimo de transferências.

```kotlin
// 1) Saldo líquido de cada usuário (quanto tem a receber, +, ou a pagar, −)
private fun computeNetBalances(expenses: List<Expense>): MutableMap<String, Double> {
    val balance = mutableMapOf<String, Double>()
    for (expense in expenses) for (share in expense.participants) {
        if (share.paid || share.userId == expense.payerId) continue
        balance.merge(expense.payerId, share.amountOwed, Double::plus)  // credor +
        balance.merge(share.userId, -share.amountOwed, Double::plus)    // devedor −
    }
    return balance
}
```

```kotlin
// 2) Loop guloso: maior credor × maior devedor, até zerar
while (true) {
    val creditor = balance.maxByOrNull { it.value } ?: break   // ⭐ maior credor
    val debtor   = balance.minByOrNull { it.value } ?: break   // ⭐ maior devedor
    if (creditor.value <= EPSILON || debtor.value >= -EPSILON) break
    val amount = min(creditor.value, -debtor.value)
    transfers += SettlementTransfer(debtor.key, creditor.key, amount)
    balance[creditor.key] = creditor.value - amount
    balance[debtor.key]   = debtor.value + amount   // ... remove quem zerou
}
```

🎤 **Fala:** "Esse é o coração do app. Ele funciona em dois passos. **Primeiro**, calculamos o 'saldo líquido' de cada pessoa: quem pagou por outros fica positivo (tem a receber), quem consumiu fica negativo (tem a pagar). **Segundo**, um laço 'guloso': a cada rodada ele pega quem tem **mais a receber** e quem tem **mais a pagar** e acerta o menor valor entre os dois, repetindo até todos zerarem. O resultado é a menor lista de pagamentos possível. É por isso que, no exemplo A→B→C, ele elimina o intermediário. E como é Kotlin puro, sem telas nem banco, conseguimos testá-lo facilmente."

⏱ **Tempo:** ~1:10

---

## Slide 6 — Build ⚙️ Gradle + Wrapper

🧩 **O que é:** *build* é transformar o código-fonte no **APK** (o arquivo que instala no celular). *Gradle* é a ferramenta que faz isso: baixa as bibliotecas, compila e empacota. *Kotlin DSL* só quer dizer que os arquivos de configuração são escritos em Kotlin. O *Wrapper* é um script que vem no projeto e **trava a versão** do Gradle.

👉 **Foco:** o Wrapper fixa a versão do Gradle no repo (⭐) → `git clone` + `./gradlew` compila em qualquer máquina, **sem instalar nada**.

```properties
# gradle/wrapper/gradle-wrapper.properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.11.1-bin.zip  # ⭐
```

```kotlin
// build.gradle.kts (raiz) — plugins declarados 1x, aplicados nos módulos
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}
```

🎤 **Fala:** "O que é 'build'? É pegar o nosso código e transformar no APK, o arquivo que instala no celular. Quem faz isso é o **Gradle** — uma ferramenta que baixa as bibliotecas de que dependemos, compila o código e empacota. Em vez de rodar comandos na mão, a gente **descreve** em arquivos o que o app precisa e o Gradle executa: isso é a automação de build. Um detalhe importante é o **Gradle Wrapper**: um script que vem junto no repositório e trava a versão do Gradle, a 8.11.1. Assim, qualquer pessoa baixa o projeto, roda `./gradlew` e compila com a versão exata — sem nem precisar instalar o Gradle. No arquivo da raiz, declaramos os **plugins** (extensões do Gradle, como o do Android) uma vez, e eles são aplicados nos módulos."

⏱ **Tempo:** ~0:45

---

## Slide 7 — Build ⚙️ repositórios (`settings.gradle.kts`)

🧩 **O que é:** *dependências* são bibliotecas de terceiros que reusamos. *Repositório* (aqui) é o **servidor de onde o Gradle baixa** essas bibliotecas — como uma "loja de pacotes". *JitPack* e *Maven Central* são duas dessas lojas.

👉 **Foco:** as origens ficam num só lugar; o **JitPack** (⭐) entra só por causa do MPAndroidChart.

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)  // sem repo solto nos módulos
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }   // ⭐ p/ MPAndroidChart
    }
}
rootProject.name = "DivideAi"
include(":app")
```

🎤 **Fala:** "Quando eu digo 'dependência', é uma biblioteca pronta que a gente reaproveita em vez de escrever do zero. E o Gradle precisa saber **de onde baixar** essas bibliotecas — esses são os repositórios, tipo lojas de pacotes. Aqui centralizamos tudo num lugar só: o Google, o Maven Central e o JitPack. O JitPack está aí só por causa da biblioteca de gráficos. Aquele `FAIL_ON_PROJECT_REPOS` é uma trava de organização: impede que alguém declare um repositório solto espalhado pelos módulos."

⏱ **Tempo:** ~0:35

---

## Slide 8 — Build ⚙️ Version Catalog (`libs.versions.toml`)

🧩 **O que é:** o *Version Catalog* é **um arquivo único** que lista todas as bibliotecas e suas versões, em vez de deixá-las espalhadas pelos vários arquivos de build.

👉 **Foco:** uma **fonte de verdade** — subir uma versão = mudar **uma linha** em `[versions]` (⭐).

```toml
# gradle/libs.versions.toml
[versions]
agp = "8.10.1"
kotlin = "2.0.21"          # ⭐ muda aqui → vale p/ todo o projeto
coreKtx = "1.17.0"
junit = "4.13.2"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
firebase-firestore = { module = "com.google.firebase:firebase-firestore" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
```

🎤 **Fala:** "Num projeto Android, os números de versão de cada biblioteca poderiam ficar espalhados. Para evitar isso, usamos um **catálogo de versões**: um arquivo único, dividido em versões, bibliotecas e plugins. A vantagem prática: se eu quiser atualizar o Kotlin, mudo **uma linha** em `[versions]` e o projeto inteiro passa a usar a nova. Depois, no build do módulo, a gente referencia essas bibliotecas por um apelido, com o autocomplete da IDE ajudando."

⏱ **Tempo:** ~0:40

---

## Slide 9 — Build ⚙️ dependências do módulo + Firebase BoM

🧩 **O que é:** `implementation(...)` **declara uma dependência** que o app usa. *BoM* (Bill of Materials, "lista de materiais") é uma **lista mestra** que já diz quais versões de um conjunto de bibliotecas combinam entre si — aqui, as do Firebase.

👉 **Foco:** o `platform(firebase-bom)` (⭐) faz `auth` e `messaging` entrarem **sem número de versão**.

```kotlin
// app/build.gradle.kts
android {
    namespace = "com.example.divideai"; compileSdk = 36
    defaultConfig { minSdk = 26; targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
    buildFeatures { viewBinding = true }          // habilita o ViewBinding
}
dependencies {
    implementation(libs.androidx.core.ktx)         // alias do catálogo
    implementation(libs.material)                  // Material 3
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))  // ⭐ alinha versões
    implementation("com.google.firebase:firebase-auth-ktx")       // sem versão (BoM)
    implementation("com.google.firebase:firebase-messaging-ktx")  // sem versão
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")    // gráficos
    implementation("com.journeyapps:zxing-android-embedded:4.3.0") // QR Code
    testImplementation(libs.junit)                 // JUnit 4 (testes)
}
```

🎤 **Fala:** "Aqui é onde listamos, com `implementation`, cada biblioteca que o app usa, além das configs do Android — como a versão mínima do Android que suportamos, a 26. Um destaque é o **Firebase BoM**. O Firebase são vários pacotes (login, banco, notificações) que precisam ter versões compatíveis entre si. Em vez de a gente escolher versão por versão e arriscar um conflito, importamos essa 'lista mestra', o BoM, e ela cuida disso: repare que o auth e o messaging entram **sem número de versão** — o BoM resolve por nós."

⏱ **Tempo:** ~0:45

---

## Slide 10 — Frameworks 🔌 Firebase Authentication

🧩 **O que é:** *Firebase Authentication* é um serviço pronto do Google que cuida de **cadastro e login** (e-mail/senha) — não precisamos gerenciar senhas nós mesmos. *Coroutine* / `suspend` é o jeito do Kotlin de escrever código que **espera** algo (a resposta do servidor) sem travar a tela.

👉 **Foco:** o `signIn` vira **`suspend`** (⭐) → o ViewModel usa `try/catch`, sem callback aninhado.

```kotlin
// data/repository/AuthRepository.kt
class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    suspend fun signIn(email: String, password: String): Result<AuthResult> =   // ⭐
        suspendCancellableCoroutine { cont ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) cont.resume(Result.success(task.result!!))
                    else cont.resumeWithException(task.exception!!)
                }
        }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser   // já tem alguém logado?
    fun logout() = auth.signOut()
}
```

🎤 **Fala:** "Para login e cadastro usamos o **Firebase Authentication**, um serviço do Google que faz esse trabalho pesado — a gente não guarda senha nenhuma. A chamada de login responde 'quando der', de forma assíncrona. Para deixar isso simples de usar, envolvemos essa resposta numa **coroutine** — um recurso do Kotlin para esperar sem congelar a tela. Assim, lá no ViewModel, o login vira quase uma linha com `try/catch`: deu certo, mostra sucesso; deu erro, mostra a mensagem. E o `getCurrentUser` diz se já tem alguém logado, o que decide se abrimos o login ou a tela principal."

⏱ **Tempo:** ~0:45

---

## Slide 11 — Frameworks 🔌 Cloud Firestore (banco de dados)

🧩 **O que é:** *Firestore* é o banco de dados **na nuvem** do Firebase. É *NoSQL*: em vez de tabelas com linhas, guarda **documentos** (parecidos com fichas JSON) dentro de **coleções**.

👉 **Foco:** `.set(...)` grava e `toObject(...)` (⭐) lê como objeto Kotlin — tudo assíncrono.

```kotlin
// data/repository/ExpenseRepository.kt
class ExpenseRepository {
    private val db = Firebase.firestore
    private val expenses = db.collection("expenses")

    fun addExpense(e: Expense, onComplete: (Boolean, String?) -> Unit) {
        val doc = expenses.document()
        doc.set(e.copy(id = doc.id))                       // grava (assíncrono)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { onComplete(false, it.message) }
    }

    fun getExpensesByGroup(groupId: String, onResult: (List<Expense>) -> Unit) {
        expenses.whereEqualTo("groupId", groupId).get()
            .addOnSuccessListener { r ->
                onResult(r.documents.mapNotNull { it.toObject(Expense::class.java) })  // ⭐
            }.addOnFailureListener { onResult(emptyList()) }
    }
}
```

🎤 **Fala:** "Os dados ficam no **Firestore**, o banco de dados na nuvem do Firebase. Ele é NoSQL: em vez de tabelas, guarda 'documentos' — cada despesa é um documento dentro da coleção 'expenses'. Toda essa conversa com o banco fica isolada numa camada de 'repositório'. Para gravar, chamamos `set`; para ler, fazemos uma consulta com `whereEqualTo` e o `toObject` **converte** o documento direto na nossa ficha Kotlin, a Expense. Tudo assíncrono, com retorno de sucesso ou falha."

⏱ **Tempo:** ~0:45

---

## Slide 12 — Frameworks 🔌 Cloud Messaging (FCM / push)

🧩 **O que é:** *FCM (Firebase Cloud Messaging)* é o serviço que entrega **notificações push**. O *token* é o "endereço" único de cada aparelho — o servidor precisa dele para saber para quem mandar.

👉 **Foco:** o token do aparelho é salvo em `users/{uid}.fcmToken` (⭐).

```kotlin
// MainActivity — registra o token FCM deste aparelho no Firestore
FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
    FirebaseFirestore.getInstance()
        .collection("users").document(uid).update("fcmToken", token)   // ⭐
}
```

```kotlin
// notifications/DivideAiMessagingService.kt — recebe o push
class DivideAiMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) = saveTokenToFirestore(token)
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: defaultTitle
        val body  = message.notification?.body  ?: message.data["body"].orEmpty()
        showNotification(title, body)          // NotificationCompat.Builder(...)
    }
}
```

🎤 **Fala:** "As notificações push usam o **Firebase Cloud Messaging**. Para o servidor conseguir avisar um usuário, ele precisa do endereço do aparelho dele — o **token**. Então, quando o app abre, pegamos esse token e salvamos no documento do usuário, no Firestore. Do outro lado, essa classe recebe o push: no `onNewToken` a gente atualiza o token, e no `onMessageReceived` monta a notificação que aparece na barra."

⏱ **Tempo:** ~0:40

---

## Slide 13 — Frameworks 🔌 Navigation + ViewBinding + Material 3

🧩 **O que é:** *Navigation Component* gerencia a **troca de telas**. *ViewBinding* gera, a partir do layout XML, variáveis já prontas para acessar os elementos da tela — evita o antigo `findViewById` (que buscava o elemento por um id e podia falhar). *Material 3* é o kit de componentes visuais do Google.

👉 **Foco:** **um** `setupWithNavController` (⭐) liga a barra inferior ao mapa de telas.

```kotlin
// MainActivity — ViewBinding + BottomNavigation ligada ao Navigation Component
binding = ActivityMainBinding.inflate(layoutInflater)   // ViewBinding (sem findViewById)
setContentView(binding.root)
val navHost = supportFragmentManager
    .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
navView.setupWithNavController(navHost.navController)    // ⭐ navView = barra inferior
```

```xml
<!-- res/navigation/mobile_navigation.xml — o "mapa" das telas -->
<navigation app:startDestination="@+id/navigation_groups">
    <fragment android:id="@+id/navigation_groups"
        android:name="com.example.divideai.ui.groups.GroupsFragment" />
    <fragment android:id="@+id/navigation_expenses" android:name="....MyExpensesFragment" />
</navigation>
```

🎤 **Fala:** "Três peças da UI. O **Navigation Component** cuida da navegação: a gente desenha um 'mapa' das telas num XML e um único comando liga a **barra inferior** a esse mapa — sem escrever transição na mão. O **ViewBinding** cria variáveis prontas para os elementos da tela; repare que não usamos aquele `findViewById` antigo, que buscava pelo id e às vezes dava erro. E o visual — botões, campos, tema claro e escuro — vem do **Material 3**, o kit de componentes do Google."

⏱ **Tempo:** ~0:40

---

## Slide 14 — Frameworks 🔌 MPAndroidChart (dashboard)

🧩 **O que é:** *MPAndroidChart* é uma **biblioteca externa de gráficos**. Em vez de desenharmos a pizza pixel a pixel, entregamos os dados e ela desenha.

👉 **Foco:** dados do ViewModel viram `PieEntry` → `PieData` (⭐); o resto é da lib.

```kotlin
// ui/dashboard/DashboardActivity.kt — gráfico de pizza por categoria
private fun renderPie(state: DashboardState) {
    val entries = state.breakdown.map {
        PieEntry(it.total.toFloat(), getString(it.category.labelRes))   // ⭐ dado → fatia
    }
    val dataSet = PieDataSet(entries, "").apply {
        colors = state.breakdown.map { it.colorArgb }
        valueFormatter = PercentFormatter(binding.pieChart)   // mostra %
    }
    binding.pieChart.data = PieData(dataSet)
    binding.pieChart.animateY(800, Easing.EaseInOutCubic)     // animação
    binding.pieChart.invalidate()
}
```

🎤 **Fala:** "O dashboard tem um gráfico de pizza com os gastos por categoria. Não reinventamos a roda: usamos uma biblioteca pronta, o **MPAndroidChart**. O nosso trabalho é só traduzir os dados — cada categoria vira uma fatia, a `PieEntry` — e entregar pra biblioteca, que cuida de desenhar, colorir e até animar."

⏱ **Tempo:** ~0:30

---

## Slide 15 — Frameworks 🔌 ZXing (QR Code de convite)

🧩 **O que é:** *ZXing* é uma **biblioteca de QR Code** (gerar e ler). O convite do grupo é uma "URL própria" nossa, `divideai://group/<id>`, que embutimos num QR.

👉 **Foco:** a mesma URI é **gerada** e **lida** (⭐); a Activity Result API entrega o que o scanner leu.

```kotlin
// Gerar o QR do convite (GroupDetailsActivity)
val bitmap = BarcodeEncoder().encodeBitmap(
    GroupInviteCode.encode(groupId),   // ⭐ "divideai://group/<id>"
    BarcodeFormat.QR_CODE, 640, 640)
dialogBinding.ivQrCode.setImageBitmap(bitmap)
```

```kotlin
// Ler o QR e entrar no grupo (GroupsFragment)
private val scanQrLauncher = registerForActivityResult(ScanContract()) { result ->
    val groupId = GroupInviteCode.decode(result?.contents)   // ⭐ mesma URI, decodificada
        ?: return@registerForActivityResult
    joinGroupFromInvite(groupId)
}
```

🎤 **Fala:** "Para entrar num grupo, a pessoa escaneia um **QR Code** — recurso da biblioteca ZXing. Como funciona: o convite é uma 'URL própria' nossa, `divideai://group` e o id do grupo. De um lado, geramos um QR a partir dessa URL; do outro, o scanner lê o QR, a gente decodifica de volta e entra no grupo. Os dois lados usam a mesma URL — um codifica, o outro decodifica."

⏱ **Tempo:** ~0:35

---

## Slide 16 — Frameworks 🔌 Photo Picker + Base64 + ExifInterface

🧩 **O que é:** *Photo Picker* é a telinha do Android para escolher **uma** foto sem dar acesso a todas. *Base64* é uma forma de representar uma imagem como **texto**, para guardá-la dentro do banco. *ExifInterface* lê a orientação da foto (para não vir deitada).

👉 **Foco:** a imagem vira **Base64** (⭐) e cabe no documento do Firestore → dispensa o Firebase Storage.

```kotlin
// Escolher imagem sem permissão de Storage (Jetpack Photo Picker)
private val pickReceiptLauncher = registerForActivityResult(
    ActivityResultContracts.PickVisualMedia()
) { uri -> if (uri != null) handlePickedReceipt(uri) }
```

```kotlin
// data/image/Base64Image.kt — reduz, corrige EXIF e vira Base64 (simplificado)
fun encodeFromUri(context, uri, maxSize = SIZE_AVATAR, quality = 70): String? {
    val scaled  = decodeScaled(context, uri, maxSize)          // reduz resolução
    val rotated = applyExifOrientation(context, uri, scaled)   // corrige orientação
    val baos = ByteArrayOutputStream()
    rotated.compress(Bitmap.CompressFormat.JPEG, quality, baos)
    return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)   // ⭐ vira Base64
}
```

🎤 **Fala:** "Fotos de perfil e comprovantes. Para escolher a imagem usamos o **Photo Picker**, uma tela padrão do Android que pega só a foto selecionada, sem pedir acesso à galeria inteira. Aí vem o truque: em vez de usar o Firebase Storage — que custa dinheiro —, a gente transforma a imagem em **Base64**, que é a imagem escrita como texto, e guarda direto no banco. Antes disso, reduzimos o tamanho e corrigimos a orientação com o ExifInterface, pra foto não vir deitada."

⏱ **Tempo:** ~0:35

---

## Slide 17 — Frameworks 🔌 Per-App Language (i18n pt-BR / en)

🧩 **O que é:** *i18n (internacionalização)* é deixar o app **em vários idiomas**. *Per-App Language* é o recurso que deixa o usuário escolher o idioma **só do app**, sem mexer no idioma do celular.

👉 **Foco:** `setApplicationLocales` (⭐) troca o idioma dentro do app e **persiste**.

```kotlin
// ui/profile/ProfileActivity.kt
val locales = if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList() // segue o sistema
              else LocaleListCompat.forLanguageTags(tag)               // "pt-BR" ou "en"
AppCompatDelegate.setApplicationLocales(locales)   // ⭐ aplica e persiste entre execuções
```

🎤 **Fala:** "O app é bilíngue, português e inglês. Isso se chama internacionalização. Usamos um recurso do Android chamado **Per-App Language**, que deixa o usuário escolher o idioma só do nosso app, sem precisar mudar o idioma do celular inteiro. Um comando aplica a escolha e ela fica salva; os textos ficam em pastas de recursos separadas, uma para cada idioma."

⏱ **Tempo:** ~0:25

---

## Slide 18 — Testes 🧪 estrutura e configuração

🧩 **O que é:** *teste automatizado* é um código que **verifica outro código** sozinho. *JUnit* roda testes **unitários** (uma função isolada) na própria máquina; *Espresso* roda testes de **interface**, que precisam de um celular ou emulador.

👉 **Foco:** `DebtSimplifier` é Kotlin puro → testa na **JVM, sem emulador** (⭐ `testImplementation`).

```kotlin
// app/build.gradle.kts
defaultConfig { testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
dependencies {
    testImplementation(libs.junit)                          // ⭐ JUnit 4.13.2 (unitário, JVM)
    androidTestImplementation(libs.androidx.junit)          // AndroidX Test
    androidTestImplementation(libs.androidx.espresso.core)  // Espresso (UI)
}
```

🎤 **Fala:** "Teste automatizado é um código que confere o nosso código sozinho, sem a gente testar na mão. Existem dois tipos: o **unitário**, que testa uma função isolada e roda direto na máquina, e o de **interface**, que simula toques na tela e precisa de um emulador. A nossa infraestrutura tem as duas — JUnit e Espresso — configuradas no Gradle. Escolhemos escrever testes unitários para o coração do app, o DebtSimplifier: como ele é Kotlin puro, roda em segundos, sem emulador. São cinco casos, todos passando."

⏱ **Tempo:** ~0:45

---

## Slide 19 — Testes 🧪 `DebtSimplifierTest` (JUnit)

🧩 **O que é:** `@Test` marca um caso de teste; `assertEquals(esperado, obtido)` **falha se** o resultado não for o esperado. A gente monta despesas fictícias e confere a saída do algoritmo.

👉 **Foco:** o `assertEquals` prova que A→B→C vira **1** transferência A→C (⭐) — o intermediário some.

```kotlin
private fun debt(payer: String, debtor: String, amount: Double) =
    Expense(payerId = payer, participants = listOf(ExpenseShare(debtor, amount)))

@Test fun `cadeia A deve B e B deve C vira transferencia A para C`() {
    val transfers = DebtSimplifier.simplify(listOf(
        debt("B", "A", 10.0),   // A deve 10 a B
        debt("C", "B", 10.0)))  // B deve 10 a C
    assertEquals(1, transfers.size)              // uma única transferência
    assertEquals("A", transfers[0].debtorId)
    assertEquals("C", transfers[0].creditorId)   // ⭐ o intermediário B some
    assertEquals(10.0, transfers[0].amount, 0.01)
}
```

🎤 **Fala:** "Aqui está o teste na prática. O `@Test` marca cada caso. Neste, eu monto duas despesas fictícias que representam 'A deve 10 ao B' e 'B deve 10 ao C', chamo o algoritmo e uso o `assertEquals` — que quer dizer 'confere se o resultado é este; se não for, o teste falha'. A gente afirma que sobra **uma única** transferência, direto de A para C. Ou seja, provamos automaticamente que o intermediário some. Temos mais quatro casos parecidos, e todos rodam com um comando — o mesmo que o nosso robô de CI executa."

⏱ **Tempo:** ~0:55

---

## Slide 20 — Opcionais ✅ Issue tracking + CI/CD

🧩 **O que é:** *Issue tracking* é usar uma ferramenta para registrar bugs e tarefas (aqui, o **GitHub Issues**). *CI/CD (Integração Contínua)* é **automatizar testes e build a cada mudança**. *GitHub Actions* é o "robô" do GitHub que faz isso; *artefato* é o arquivo que ele guarda no fim (o relatório e o APK).

👉 **Foco:** todo push roda `./gradlew test` (⭐) → se o algoritmo quebrar, o CI acusa **antes do merge**.

```yaml
# .github/workflows/android.yml
on:
  push: { branches: [ main ] }
  pull_request: { branches: [ main ] }
jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '17' }
      - uses: android-actions/setup-android@v3
      - run: ./gradlew test            # ⭐ roda o DebtSimplifierTest
      - run: ./gradlew assembleDebug   # gera o APK de debug
```

🎤 **Fala:** "A tarefa pedia itens opcionais; fizemos dois. O primeiro é **issue tracking** — usar o GitHub Issues para anotar bugs e tarefas. O segundo é **CI/CD**, ou integração contínua: a ideia é ter um robô que, toda vez que a gente envia código, roda os testes e monta o app automaticamente. Esse robô é o **GitHub Actions**; a receita está nesse arquivo. A cada envio para o repositório, ele prepara o ambiente, roda o `gradlew test` e gera o APK, guardando os dois como 'artefatos'. Na prática: se alguém quebrar o DebtSimplifier, o robô avisa na hora, antes de juntar o código."

⏱ **Tempo:** ~0:45

---

## Slide 21 — Opcionais ✅ Containers + o extra: notifier (Node.js)

🧩 **O que é:** *Container* (ex.: Docker) empacota um programa com tudo que ele precisa para rodar igual em qualquer servidor. *Admin SDK* é a biblioteca que acessa o Firebase pelo **lado do servidor**. `onSnapshot` "escuta" o banco e **avisa em tempo real** quando algo muda.

👉 **Foco:** `onSnapshot` (⭐) reage a despesa nova e notifica os participantes — de **fora** do app (Node).

```javascript
// notifier/watch.js — escuta o Firestore em tempo real e notifica
db.collection('expenses').where('createdAt', '>=', startedAt)
  .onSnapshot((snap) => {                                   // ⭐ reage em tempo real
    for (const change of snap.docChanges()) {
      if (change.type !== 'added') continue;
      const data = change.doc.data();
      for (const p of (data.participants || [])) {
        if (p.userId === data.payerId) continue;            // não avisa quem pagou
        sendToUser(p.userId, 'Nova despesa no grupo', `Incluído em "${data.title}".`);
      }
    }
  });
```

🎤 **Fala:** "O terceiro opcional era **containers** — que é empacotar um programa para rodar igual em qualquer servidor, tipo o Docker. Esse a gente **não** fez, porque um app Android não roda em container; mas indicamos que o próximo componente poderia. E esse componente é o nosso **extra**: o `notifier`, um programinha separado em **Node.js**. Usando o Admin SDK — a versão do Firebase para servidores — ele fica **escutando** o banco em tempo real com o `onSnapshot`; quando entra uma despesa nova, ele dispara a notificação para cada participante, menos quem pagou. Isso mostra o projeto usando duas linguagens, Kotlin e JavaScript."

⏱ **Tempo:** ~0:40

---

## Slide 22 — 🎬 Demonstração ao vivo (opcional)

👉 **Foco:** o clímax é mostrar o pulo de **Saldos** → **Saldos Simplificados**.

**No slide (fluxo feliz):**
1. Login → Criar grupo ("Viagem Praia")
2. Convidar membro por **QR Code**
3. Lançar despesa: **categoria + pagador + participantes + comprovante**
4. Ver **Saldos** → **SALDOS SIMPLIFICADOS**
5. **Dashboard**: gráfico de pizza por categoria
6. *(opcional)* rodar o `notifier` → push chegando

> **Plano B:** prints em `docs/screenshots/` (01→05) seguem o mesmo fluxo se o emulador falhar.

🎤 **Fala:** "Agora ao vivo: faço login, crio um grupo, convido alguém por QR Code e lanço uma despesa com categoria e comprovante. O ponto alto é ver os saldos normais virarem os **saldos simplificados** — aquela mágica de reduzir o número de pagamentos — e o dashboard com a pizza por categoria."

**Nota do apresentador:** abrir o emulador e **já estar logado ANTES**; deixar na tela de Grupos. Se o tempo apertar, cortar o push. Se o emulador falhar, usar os prints.

⏱ **Tempo:** ~1:00

---

## Slide 23 — Encerramento

**No slide:**
1. **Problema real:** dividir contas em grupo e saber "quem deve a quem" gera confusão
2. **Solução:** registrar despesas, dividir e **SIMPLIFICAR** as dívidas ao mínimo de transferências (`DebtSimplifier`, Kotlin puro, **testado**)
3. **Stack sólida:** Android nativo + Jetpack + Material 3 + Firebase; build reproduzível (Wrapper + Version Catalog); **testes + CI**
- **Diferenciais:** testes + CI · QR Code · Base64 no Firestore · modo escuro · i18n · notifier Node.js
- **Próximos passos:** testes de interface (Espresso) · containerizar o notifier · marcar dívida como paga

🎤 **Fala:** "Recapitulando: o DivideAi resolve uma dor comum de qualquer grupo que rateia custos — transforma despesas em saldos claros e simplifica as dívidas ao mínimo de transferências, com esse algoritmo central testado e rodando num CI. Tudo sobre uma base Android nativa com Firebase. Como próximos passos, queremos testes de interface e containerizar o notifier. Obrigada!"

⏱ **Tempo:** ~0:40

---

## Slide 24 — Obrigado / Perguntas

**No slide:** "Obrigado! 🐮 · Perguntas?" — Emilly Neves e David Marques · Projeto final 2025/2 (MIT) · `github.com/DavidMarquesss/Divideai`

🎤 **Fala:** "Obrigada pela atenção! Ficamos abertos para perguntas."

⏱ **Tempo:** ~0:15

---

## Anexo — Glossário rápido (cola de bolso)

- **Build / APK** — processo que vira o código no arquivo instalável do Android.
- **Gradle** — ferramenta que baixa bibliotecas, compila e monta o APK.
- **Gradle Wrapper** — script no repo que fixa a versão do Gradle (todos compilam igual).
- **Dependência / biblioteca** — código de terceiros que reaproveitamos.
- **Repositório (de pacotes)** — servidor de onde o Gradle baixa as bibliotecas (Maven Central, JitPack).
- **Version Catalog** — arquivo único com as versões de todas as bibliotecas.
- **BoM (Bill of Materials)** — lista mestra que alinha as versões de um grupo de libs (Firebase).
- **MVVM / ViewModel / LiveData** — padrão de camadas; a tela observa um estado que se atualiza sozinho.
- **Firebase Auth** — serviço de login/cadastro pronto.
- **Firestore (NoSQL)** — banco na nuvem que guarda "documentos" em "coleções".
- **FCM / token** — serviço de notificação push; token = endereço do aparelho.
- **Coroutine / suspend** — esperar uma resposta sem travar a tela.
- **ViewBinding** — acesso tipado às views (substitui o `findViewById`).
- **Navigation Component** — gerencia a troca de telas.
- **Base64** — imagem representada como texto (cabe no banco).
- **JUnit / Espresso** — testes unitários (na máquina) e de interface (no emulador).
- **CI/CD / GitHub Actions** — robô que roda testes e build a cada envio de código.
- **Container (Docker)** — empacota um programa para rodar igual em qualquer servidor.
