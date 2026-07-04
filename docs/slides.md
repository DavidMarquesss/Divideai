---
marp: true
theme: default
paginate: true
size: 16:9
footer: 'DivideAi · Emilly Neves & David Marques · 2025/2'
style: |
  section { font-size: 21px; padding: 34px 48px; background: #FBFDFB; }
  h1, h2 { color: #2B9A5B; margin: 0 0 .25em; }
  h1 { font-size: 38px; }
  h2 { font-size: 28px; }
  section.lead { background: #0E2A1B; justify-content: center; text-align: center; }
  section.lead h1 { color: #3DDC84; font-size: 54px; }
  section.lead h2, section.lead p, section.lead strong, section.lead code { color: #EAF7EF; }
  ul { margin: .2em 0; } li { margin: .1em 0; }
  code { font-size: .9em; }
  pre { font-size: 14px; line-height: 1.22; margin: .3em 0; }
  strong { color: #1E7A47; }
  section.lead strong { color: #3DDC84; }
  table { font-size: .82em; }
  footer { color: #9AA6A0; font-size: .5em; }
  .tk { background:#E8F6EE; border-left:6px solid #2B9A5B; padding:6px 13px;
        border-radius:6px; font-size:15px; margin:.1em 0 .4em; }
  .tk b { color:#1E7A47; }
  .cols { display:grid; grid-template-columns:1fr 1fr; gap:0 26px; }
  /* slide de visão geral (card grid) */
  section.grid { background:#3EA57A; }
  section.grid h1 { color:#FFFFFF; }
  .grid6 { display:grid; grid-template-columns:repeat(3,1fr); gap:15px; margin-top:.3em; }
  .card { background:#FFFFFF; border-radius:14px; padding:11px 16px;
          box-shadow:0 3px 9px rgba(0,0,0,.16); }
  .card h3 { margin:.1em 0 .25em; font-size:18px; color:#2B9A5B; }
  .card ul { margin:0; padding-left:1.05em; }
  .card li { font-size:13.5px; margin:.08em 0; color:#173d2b; }
---

<!-- _class: lead -->
<!-- _paginate: false -->

# DivideAi 💸

## Dividir despesas em grupo de forma justa, simples e transparente

**Emilly Neves** & **David Marques**
Projeto Final da disciplina · 2025/2 · Licença MIT
`github.com/DavidMarquesss/Divideai`

`Android` · `Kotlin 2.0.21` · `minSdk 26` · `Firebase (BoM 33.1.0)` · `Material 3`

<!--
Fala: Boa noite! Somos a Emilly e o David; este é o DivideAi, nosso projeto final — um app Android nativo, em Kotlin, para dividir despesas em grupo sem confusão. Vamos mostrar o problema, a stack, e o código do build, dos frameworks, dos testes e dos opcionais.
-->

---

## Propósito: que problema resolvemos?

<div class="tk">👉 <b>O ponto:</b> não é só dividir a conta — o app <b>simplifica as dívidas</b> ao mínimo de transferências.</div>

- **Dor:** dividir contas em grupo (viagem, república, família) vira bagunça — "quem pagou o quê" e "quem deve a quem" → **atrito**
- **Solução:** registrar cada despesa (categoria, pagador, participantes, comprovante) e **dividir automaticamente**; saldos claros: **a pagar** / **a receber**
- **Público-alvo:** amigos de viagem, colegas de república, famílias — **qualquer grupo que rateia custos**

> *Ex.:* A deve 10 a B e B deve 10 a C → em vez de 2 pagamentos, o app manda **A pagar 10 direto a C**.

<!--
Fala: O problema é cotidiano: um grupo viaja ou divide um apê, as despesas se espalham e ninguém sabe quem deve a quem. O DivideAi registra cada despesa e calcula os saldos. O diferencial é simplificar as dívidas: se A deve pro B e B deve pro C, basta A pagar direto pro C. Serve pra qualquer grupo que rateia custos.
-->

---

<!-- _class: grid -->

# 🧰 Ferramentas & Frameworks (visão geral)

<div class="grid6">
  <div class="card"><h3>💻 Linguagem &amp; Build</h3><ul>
    <li>Kotlin · JVM 11</li>
    <li>Gradle (Kotlin DSL) + Wrapper</li>
    <li>Version Catalog</li>
    <li>minSdk 26 · targetSdk 36</li>
  </ul></div>
  <div class="card"><h3>🤖 Android Jetpack</h3><ul>
    <li>ViewModel · LiveData</li>
    <li>Navigation · ViewBinding</li>
    <li>SwipeRefresh · Photo Picker</li>
    <li>ExifInterface · SplashScreen</li>
  </ul></div>
  <div class="card"><h3>🎨 UI &amp; i18n</h3><ul>
    <li>Material 3</li>
    <li>Tema DayNight (modo escuro)</li>
    <li>Per-App Language (pt-BR/en)</li>
  </ul></div>
  <div class="card"><h3>🔥 Backend (Firebase / BaaS)</h3><ul>
    <li>Authentication</li>
    <li>Cloud Firestore (NoSQL)</li>
    <li>Cloud Messaging (FCM)</li>
  </ul></div>
  <div class="card"><h3>📚 Bibliotecas</h3><ul>
    <li>MPAndroidChart (gráficos)</li>
    <li>ZXing (QR Code)</li>
    <li>notifier: Node.js + Admin SDK</li>
  </ul></div>
  <div class="card"><h3>🧪 Testes &amp; DevOps</h3><ul>
    <li>JUnit (unitários)</li>
    <li>Espresso (UI)</li>
    <li>Git/GitHub · Issues</li>
    <li>CI: GitHub Actions</li>
  </ul></div>
</div>

<!--
Fala: Antes de entrar no código, o panorama da nossa stack. A base é Kotlin com Gradle. Pra UI e estrutura usamos o Android Jetpack e o Material 3. O backend é todo Firebase — login, banco e notificações. Temos duas bibliotecas externas, de gráfico e de QR Code, mais um componente extra em Node. E fechamos com testes e um CI no GitHub Actions. Agora vamos ver as principais em código.
-->

---

## Modelo de domínio (o que é persistido)

<div class="tk">👉 <b>Olhe:</b> <code>payerId</code> (quem pagou) + <code>participants</code> (quem deve quanto) — é a <b>entrada do algoritmo e dos testes</b>.</div>

```kotlin
// data/model/Expense.kt — data class = objeto que só carrega dados
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
    val category: String = "", val receipt: String = ""  // receipt = foto em Base64
)
```

<!--
Fala: A ficha central é a despesa: tem um pagador e uma lista de participações, cada uma dizendo quanto a pessoa deve e se já pagou. Os valores padrão existem porque o Firestore precisa criar o objeto vazio pra preencher. É essa ficha que alimenta o cálculo e os testes.
-->

---

## Arquitetura: camadas + MVVM

<div class="tk">👉 <b>Olhe:</b> a tela só <b>observa</b> o estado (LiveData); a regra pesada fica fora dela → <b>testável</b>.</div>

```kotlin
// ui/auth/LoginViewModel.kt — ViewModel guarda o estado; a tela observa
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

<!--
Fala: Organizamos em camadas com MVVM. A tela não faz conta; ela observa um LiveData, uma caixinha que avisa a UI quando o dado muda. Aqui o estado é Loading, Success ou Error e a tela reage sozinha. Como a regra de negócio não fica presa à tela, dá pra testá-la isolada.
-->

---

## ⭐ Algoritmo estrela: DebtSimplifier (Kotlin puro)

<div class="tk">👉 <b>Olhe:</b> o loop pega sempre o <b>maior credor × maior devedor</b> (⭐). <b>Por quê:</b> é o que dá o mínimo de transferências.</div>

```kotlin
// 1) Saldo líquido de cada usuário (+ tem a receber, − tem a pagar)
for (expense in expenses) for (share in expense.participants) {
    if (share.paid || share.userId == expense.payerId) continue
    balance.merge(expense.payerId, share.amountOwed, Double::plus)  // credor +
    balance.merge(share.userId, -share.amountOwed, Double::plus)    // devedor −
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

<!--
Fala: O coração do app, em dois passos. Primeiro, o saldo líquido de cada um: quem pagou por outros fica positivo, quem consumiu fica negativo. Depois, um laço guloso: a cada rodada pega quem tem mais a receber e quem tem mais a pagar e acerta o menor valor, até zerar. Por isso, no A→B→C, o intermediário some. E, sendo Kotlin puro, é fácil de testar.
-->

---

## Build ⚙️ — Gradle + Wrapper (Kotlin DSL)

<div class="tk">👉 <b>Por que importa:</b> o Wrapper fixa a versão do Gradle no repo (⭐) → <code>git clone</code> + <code>./gradlew</code> compila em qualquer máquina, sem instalar nada.</div>

- **Build** = virar o código em **APK**; o **Gradle** baixa libs, compila e empacota (config em `.kts`, Kotlin)

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

<!--
Fala: Build é transformar o código no APK, o instalável. Quem faz é o Gradle: baixa as bibliotecas, compila e empacota, tudo descrito em arquivos .kts em vez de comandos na mão. O Wrapper trava a versão do Gradle no repositório, então qualquer um roda ./gradlew e compila igual, sem instalar nada.
-->

---

## Build ⚙️ — dependências: catálogo + repositórios

<div class="tk">👉 <b>Olhe:</b> versões centralizadas no catálogo (⭐ muda 1 linha); os repositórios dizem <b>de onde</b> o Gradle baixa as libs.</div>

<div class="cols">

```toml
# gradle/libs.versions.toml
[versions]
agp = "8.10.1"
kotlin = "2.0.21"   # ⭐ muda aqui
coreKtx = "1.17.0"
junit = "4.13.2"
[libraries]
androidx-core-ktx = { group =
  "androidx.core", name = "core-ktx",
  version.ref = "coreKtx" }
[plugins]
android-application = { id =
  "com.android.application",
  version.ref = "agp" }
```

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
  repositoriesMode.set(
    FAIL_ON_PROJECT_REPOS)  // sem repo solto
  repositories {
    google()
    mavenCentral()
    maven { url =
      uri("https://jitpack.io") } // MPAndroidChart
  }
}
include(":app")
```

</div>

<!--
Fala: Dependência é biblioteca de terceiros que reusamos. As versões ficam num catálogo único: quero subir o Kotlin, mudo uma linha e vale pra tudo. E os repositórios são as 'lojas' de onde o Gradle baixa: Google, Maven Central e o JitPack, esse só por causa do gráfico.
-->

---

## Build ⚙️ — dependências do módulo + Firebase BoM

<div class="tk">👉 <b>Olhe:</b> o <code>platform(firebase-bom)</code> (⭐) faz <code>auth</code> e <code>messaging</code> entrarem <b>sem número de versão</b>.</div>

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

<!--
Fala: Aqui listamos com implementation cada biblioteca, além das configs do Android. O destaque é o Firebase BoM: o Firebase são vários pacotes que precisam de versões compatíveis; a BoM é a lista mestra que alinha isso, por isso auth e messaging entram sem versão.
-->

---

## Frameworks 🔌 — Firebase: Auth + Firestore

<div class="tk">👉 <b>Olhe:</b> login vira <code>suspend</code> (esperar sem travar) e o banco lê com <code>toObject</code> (⭐), virando objeto Kotlin.</div>

<div class="cols">

```kotlin
// AuthRepository.kt — login (serviço pronto)
suspend fun signIn(email: String, pass: String)
  : Result<AuthResult> =
  suspendCancellableCoroutine { cont ->
    auth.signInWithEmailAndPassword(email, pass)
      .addOnCompleteListener { t ->
        if (t.isSuccessful)
          cont.resume(Result.success(t.result!!))
        else cont.resumeWithException(t.exception!!)
      }
  }
```

```kotlin
// ExpenseRepository.kt — Firestore (NoSQL)
private val db = Firebase.firestore
fun addExpense(e: Expense, cb:(Boolean,String?)->Unit){
  val doc = db.collection("expenses").document()
  doc.set(e.copy(id = doc.id))          // grava
    .addOnSuccessListener { cb(true, null) }
    .addOnFailureListener { cb(false, it.message) }
}
// leitura: get() → it.toObject(Expense::class.java) // ⭐
```

</div>

<!--
Fala: A autenticação é o Firebase Auth, serviço pronto do Google — não guardamos senha. A resposta é assíncrona, então envolvemos numa coroutine e o ViewModel usa try/catch. O banco é o Firestore, um NoSQL de documentos: o set grava e o toObject converte o documento direto na nossa ficha Kotlin.
-->

---

## Frameworks 🔌 — Firebase: Cloud Messaging (FCM)

<div class="tk">👉 <b>Olhe:</b> o <b>token</b> do aparelho é salvo em <code>users/{uid}.fcmToken</code> (⭐) — é o "endereço" pro servidor mandar o push.</div>

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

<!--
Fala: As notificações push usam o Firebase Cloud Messaging. Pro servidor avisar alguém, ele precisa do endereço do aparelho — o token —, que salvamos no documento do usuário. Do outro lado, essa classe recebe o push: no onNewToken atualiza o token, no onMessageReceived monta a notificação.
-->

---

## Frameworks 🔌 — UI: Navigation + ViewBinding + Material 3

<div class="tk">👉 <b>Olhe:</b> <b>um</b> <code>setupWithNavController</code> (⭐) liga a barra inferior ao "mapa" de telas; ViewBinding = sem <code>findViewById</code>.</div>

```kotlin
// MainActivity — ViewBinding + BottomNavigation ligada ao Navigation Component
binding = ActivityMainBinding.inflate(layoutInflater)   // ViewBinding (acesso tipado)
setContentView(binding.root)
val navHost = supportFragmentManager
    .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
navView.setupWithNavController(navHost.navController)    // ⭐ liga barra ↔ grafo
```

```xml
<!-- res/navigation/mobile_navigation.xml — o "mapa" das telas -->
<navigation app:startDestination="@+id/navigation_groups">
    <fragment android:id="@+id/navigation_groups"
        android:name="com.example.divideai.ui.groups.GroupsFragment" />
    <fragment android:id="@+id/navigation_expenses" android:name="....MyExpensesFragment" />
</navigation>
```

<!--
Fala: Três peças da UI. O Navigation Component cuida da troca de telas: desenhamos um mapa em XML e um comando liga a barra inferior a ele. O ViewBinding cria variáveis prontas pras views, sem o findViewById antigo. E o visual, com tema claro e escuro, vem do Material 3.
-->

---

## Frameworks 🔌 — Bibliotecas: gráficos + QR Code

<div class="tk">👉 <b>Olhe:</b> damos os dados e a lib desenha a pizza (⭐); a mesma URI do convite é gerada e lida no QR.</div>

<div class="cols">

```kotlin
// MPAndroidChart — pizza do dashboard
val entries = state.breakdown.map {
  PieEntry(it.total.toFloat(),
    getString(it.category.labelRes)) // ⭐ dado→fatia
}
val ds = PieDataSet(entries, "").apply {
  colors = state.breakdown.map { it.colorArgb }
  valueFormatter =
    PercentFormatter(binding.pieChart)   // %
}
binding.pieChart.data = PieData(ds)
binding.pieChart.animateY(800)
```

```kotlin
// ZXing — gerar e ler o QR do convite
val bmp = BarcodeEncoder().encodeBitmap(
  GroupInviteCode.encode(groupId),       // URI
  BarcodeFormat.QR_CODE, 640, 640)
// ler (Activity Result API):
registerForActivityResult(ScanContract()){ r->
  val id = GroupInviteCode.decode(r?.contents)
    ?: return@registerForActivityResult
  joinGroupFromInvite(id)                // entra
}
```

</div>

<!--
Fala: Duas bibliotecas externas. O MPAndroidChart desenha a pizza do dashboard: a gente só traduz cada categoria numa fatia e entrega. E o ZXing faz o QR Code de convite: geramos o QR a partir da nossa URI divideai://group/id, e do outro lado o scanner lê, a gente decodifica e entra no grupo.
-->

---

## Frameworks 🔌 — Jetpack: imagem (Base64) + i18n

<div class="tk">👉 <b>Por que importa:</b> a imagem vira <b>Base64</b> (texto) e cabe no Firestore (⭐) → dispensa o Firebase Storage.</div>

```kotlin
// Photo Picker (sem permissão de Storage) + Base64Image (reduz, EXIF, comprime)
private val pick = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
    uri -> if (uri != null) handlePickedReceipt(uri) }

fun encodeFromUri(context, uri, maxSize = 720, quality = 70): String? {
    val scaled  = decodeScaled(context, uri, maxSize)          // reduz resolução
    val rotated = applyExifOrientation(context, uri, scaled)   // corrige orientação
    return Base64.encodeToString(toJpegBytes(rotated, quality), Base64.NO_WRAP)  // ⭐
}
```

- **i18n (pt-BR/en):** seletor **dentro do app** com `AppCompatDelegate.setApplicationLocales(locales)` (persiste)

<!--
Fala: Fotos de perfil e comprovantes. O Photo Picker escolhe a imagem sem pedir acesso à galeria toda. E, em vez do Storage pago, transformamos a imagem em Base64 — a imagem como texto — e guardamos no próprio banco, reduzindo e corrigindo a orientação antes. E o app é bilíngue, com um seletor de idioma só do app.
-->

---

## Testes 🧪 — estrutura e configuração

<div class="tk">👉 <b>Por que importa:</b> <code>DebtSimplifier</code> é Kotlin puro → testa na <b>JVM, sem emulador</b> (⭐ <code>testImplementation</code>).</div>

- **Teste automatizado** = código que verifica o código sozinho. **JUnit** = unitário (na máquina) · **Espresso** = interface (emulador)
- `app/src/test/…` (sem device) vs `app/src/androidTest/…` · comandos `./gradlew test` e `connectedAndroidTest`

```kotlin
// app/build.gradle.kts
defaultConfig { testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
dependencies {
    testImplementation(libs.junit)                          // ⭐ JUnit 4.13.2 (unitário, JVM)
    androidTestImplementation(libs.androidx.junit)          // AndroidX Test
    androidTestImplementation(libs.androidx.espresso.core)  // Espresso (UI)
}
```

<!--
Fala: Teste automatizado é um código que confere o código sozinho. Tem o unitário, que roda na máquina, e o de interface, que precisa de emulador — temos os dois configurados. Escolhemos testar o coração do app, o DebtSimplifier: como é Kotlin puro, roda em segundos. São cinco casos, todos passando.
-->

---

## Testes 🧪 — `DebtSimplifierTest` (JUnit)

<div class="tk">👉 <b>Olhe:</b> o <code>assertEquals</code> prova que A→B→C vira <b>1</b> transferência A→C (⭐) — o intermediário some.</div>

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
// + 4 casos: dívida direta · rateio entre vários · parcelas pagas · lista vazia
```

<!--
Fala: O teste na prática. O @Test marca o caso; monto duas despesas que representam A deve ao B e B deve ao C, chamo o algoritmo e uso o assertEquals — 'confere se é isto, senão falha'. Afirmo que sobra uma única transferência, direto de A pra C. Temos mais quatro casos, todos rodando com o mesmo comando do CI.
-->

---

## Opcionais ✅ — Issue tracking + CI/CD (GitHub Actions)

<div class="tk">👉 <b>Por que importa:</b> todo push roda <code>./gradlew test</code> (⭐) → se o algoritmo quebrar, o CI acusa antes do merge.</div>

- **Issue tracking:** GitHub Issues · **CI** publica **relatório de testes** e **APK** como artefatos

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

<!--
Fala: Dos opcionais, fizemos dois. Issue tracking pelo GitHub. E CI/CD, integração contínua: um robô, o GitHub Actions, que a cada envio de código prepara o ambiente, roda os testes e monta o APK, guardando os dois como artefatos. Se alguém quebrar o DebtSimplifier, ele avisa na hora.
-->

---

## Opcionais ✅ — Containers + o extra: notifier (Node.js)

<div class="tk">👉 <b>Olhe:</b> <code>onSnapshot</code> (⭐) reage a despesa nova em tempo real e notifica os participantes — de <b>fora</b> do app (Node).</div>

- **Containers:** *não feito* — app Android não roda em container; o `notifier/` poderia ser containerizado (evolução)
- **Extra:** `notifier/` em **Node.js** com `firebase-admin` (Admin SDK) → projeto **poliglota** (Kotlin + JS)

```javascript
// notifier/watch.js — escuta o Firestore em tempo real e notifica
db.collection('expenses').where('createdAt', '>=', startedAt)
  .onSnapshot((snap) => {                                   // ⭐ reage em tempo real
    for (const change of snap.docChanges()) {
      if (change.type !== 'added') continue;
      for (const p of (change.doc.data().participants || [])) {
        if (p.userId === change.doc.data().payerId) continue;   // não avisa quem pagou
        sendToUser(p.userId, 'Nova despesa no grupo', 'Você foi incluído.');
      }
    }
  });
```

<!--
Fala: Containers a gente não fez — um app Android não roda em container —, mas indicamos como evolução do notifier. E esse notifier é o extra: um programa em Node.js, separado, que usa o Admin SDK pra escutar o banco em tempo real com onSnapshot e disparar push quando entra uma despesa nova. Isso deixa o projeto poliglota.
-->

---

## 🎬 Demonstração ao vivo (opcional)

<div class="tk">👉 <b>O clímax:</b> mostrar o pulo de <b>Saldos</b> → <b>Saldos Simplificados</b>.</div>

1. Login → Criar grupo ("Viagem Praia")
2. Convidar membro por **QR Code**
3. Lançar despesa: **categoria + pagador + participantes + comprovante**
4. Ver **Saldos** → **SALDOS SIMPLIFICADOS**
5. **Dashboard**: gráfico de pizza por categoria
6. *(opcional)* rodar o `notifier` → push chegando

> **Plano B:** prints em `docs/screenshots/` (01→05) seguem o mesmo fluxo se o emulador falhar.

<!--
Fala: Ao vivo: login, criar grupo, convidar por QR, lançar uma despesa com comprovante, e o ponto alto — ver os saldos virarem os simplificados —, além do dashboard. Nota: abrir já logado antes; se faltar tempo, cortar o push; se o emulador falhar, usar os prints.
-->

---

## Encerramento

1. **Problema real:** dividir contas em grupo e saber "quem deve a quem" gera confusão
2. **Solução:** registrar, dividir e **SIMPLIFICAR** as dívidas ao mínimo de transferências (`DebtSimplifier`, Kotlin puro, **testado**)
3. **Stack sólida:** Android nativo + Jetpack + Material 3 + Firebase; build reproduzível (Wrapper + Version Catalog); **testes + CI**

**Diferenciais:** testes + CI · QR Code · Base64 no Firestore · modo escuro · i18n · notifier Node.js
**Próximos passos:** testes de interface (Espresso) · containerizar o notifier · marcar dívida como paga

<!--
Fala: Recapitulando: o DivideAi resolve uma dor comum de quem rateia custos — vira despesas em saldos claros e simplifica as dívidas, com o algoritmo central testado e num CI. Base Android nativa com Firebase. Próximos passos: testes de interface e containerizar o notifier. Obrigada!
-->

---

<!-- _class: lead -->
<!-- _paginate: false -->

# Obrigado! 🐮

## Perguntas?

**Emilly Neves** & **David Marques**
Projeto Final 2025/2 · Licença MIT
`github.com/DavidMarquesss/Divideai`

<!--
Fala: Obrigada pela atenção! Ficamos abertos para perguntas.
-->
