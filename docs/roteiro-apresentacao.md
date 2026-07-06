# DivideAi — Roteiro de Slides (Apresentação Técnica)

**Subtítulo:** App Android nativo (Kotlin) para dividir despesas em grupo de forma justa — Emilly Neves & David Marques · Projeto Final 2025/2 · Licença MIT

> Roteiro **sincronizado com [`slides.md`](slides.md)** (21 slides) e escrito para **quem nunca programou**: cada termo técnico vem com uma **analogia do dia a dia**. Estrutura de cada slide: **🧩 O que é** (explicação simples), **👉 Foco** (o que olhar) e a **🎤 Fala**. A linha-chave do código está marcada com **⭐**. Glossário simplificado no final.

## Tabela de tempo (~15 min)

| # | Slide | Tempo |
|--:|-------|------:|
| 1 | Capa | 0:30 |
| 2 | Propósito + público | 0:50 |
| 3 | Ferramentas & Frameworks (visão geral) | 0:45 |
| 4 | Modelo de domínio | 0:40 |
| 5 | Arquitetura + MVVM | 0:50 |
| 6 | ⭐ DebtSimplifier | 1:05 |
| 7 | Build: Gradle + Wrapper | 0:45 |
| 8 | Build: catálogo + repositórios | 0:45 |
| 9 | Build: dependências + BoM | 0:45 |
| 10 | Firebase: Auth + Firestore | 0:55 |
| 11 | Firebase: Cloud Messaging (FCM) | 0:40 |
| 12 | UI: Navigation + ViewBinding + Material | 0:40 |
| 13 | Bibliotecas: gráficos + QR Code | 0:40 |
| 14 | Jetpack: imagem (Base64) + i18n | 0:40 |
| 15 | Testes: estrutura e cobertura (4 áreas) | 0:50 |
| 16 | Testes: DebtSimplifierTest (5 cenários) | 1:00 |
| 17 | Opcionais: Issue tracking + CI/CD | 0:45 |
| 18 | Opcionais: containers + notifier | 0:40 |
| 19 | Demo ao vivo (opcional) | 1:00 |
| 20 | Encerramento | 0:40 |
| 21 | Obrigado / perguntas | 0:15 |
| | **Total** | **~15:40** |

> As falas abaixo são a versão "completa e explicada". Na hora, fale no seu ritmo — dá pra encurtar mantendo a analogia principal de cada slide.

---

## Slide 1 — Capa: DivideAi

**No slide:** título, subtítulo, autores, 2025/2, MIT, repositório e badges de stack.

🎤 **Fala:** "Boa noite! Somos a Emilly e o David, e este é o **DivideAi**. Pensem naquele perrengue depois de uma viagem em grupo: um pagou o hotel, outro o combustível, outro o jantar… e no fim ninguém sabe quem deve quanto a quem. O nosso app resolve exatamente isso. É um **aplicativo de Android** feito 'do zero' para o celular, na linguagem oficial do Android, o Kotlin. Nos próximos 15 minutos a gente explica o problema e mostra, com calma, como ele foi construído por dentro."

⏱ **Tempo:** ~0:30

---

## Slide 2 — Propósito + público

👉 **Foco:** não é só dividir a conta — o app **simplifica as dívidas** ao mínimo de transferências.

**No slide:** a dor (rateio vira bagunça), a solução (registrar + dividir + saldos claros) e o público.

🎤 **Fala:** "O problema é bem comum: sempre que um grupo divide gastos, as contas se espalham e vira aquela confusão de 'quem pagou o quê'. No DivideAi, cada pessoa só registra a despesa — quanto foi, quem pagou e entre quem dividir — e o app calcula os saldos de cada um. E aí vem o nosso pulo do gato: ele **simplifica** as dívidas. Um exemplo: se a Ana deve 10 ao Bruno, e o Bruno deve 10 ao Carlos, em vez de duas transferências, o app percebe que basta a **Ana pagar 10 direto ao Carlos**. Ele acha sempre o **menor número de pagamentos** pra todo mundo ficar quite. Serve pra qualquer grupo que divide contas: amigos de viagem, república, família."

⏱ **Tempo:** ~0:50

---

## Slide 3 — Ferramentas & Frameworks (visão geral)

🧩 **O que é:** um **mapa da nossa "caixa de ferramentas"**. Antes de abrir o código, mostramos tudo o que foi usado, agrupado em 6 caixinhas — pra ninguém se perder depois.

**No slide (6 cards):**
- **💻 Linguagem & Build:** Kotlin · Gradle (Kotlin DSL) + Wrapper · Version Catalog · minSdk 26 / targetSdk 36
- **🤖 Android Jetpack:** ViewModel · LiveData · Navigation · ViewBinding · SwipeRefresh · Photo Picker · ExifInterface
- **🎨 UI & i18n:** Material 3 · tema DayNight (modo escuro) · Per-App Language (pt-BR/en)
- **🔥 Backend (Firebase):** Authentication · Cloud Firestore (NoSQL) · Cloud Messaging (FCM)
- **📚 Bibliotecas:** MPAndroidChart (gráficos) · ZXing (QR Code) · notifier (Node.js)
- **🧪 Testes & DevOps:** JUnit · Espresso · Git/GitHub · CI (GitHub Actions)

🎤 **Fala:** "Antes de mergulhar no código, este é o panorama da nossa 'caixa de ferramentas'. Não precisam decorar nada agora — a ideia é só mostrar que reaproveitamos muita coisa pronta em vez de reinventar tudo. A base é a linguagem Kotlin. A parte visual e a estrutura vêm de kits oficiais do Google, o **Jetpack** e o **Material**. Tudo que é 'servidor' — login, banco de dados e notificações — é o **Firebase**, do Google. Usamos duas bibliotecas prontas, uma de gráficos e uma de QR Code. E fechamos com testes automáticos e um robô que confere o projeto. Agora vamos ver as principais peças funcionando de verdade."

⏱ **Tempo:** ~0:45

---

## Slide 4 — Modelo de domínio (o que o app guarda)

🧩 **O que é:** o **"formulário" dos nossos dados**. Assim como uma ficha de cadastro tem campos (nome, e-mail…), aqui definimos os campos de uma **despesa**. Em Kotlin, essa ficha é uma `data class`.

👉 **Foco:** `payerId` (quem pagou) + `participants` (quem deve quanto) — é o que o algoritmo e os testes usam.

```kotlin
// data/model/Expense.kt — a "ficha" de uma despesa
data class ExpenseShare(
    val userId: String = "",
    val amountOwed: Double = 0.0,   // quanto esta pessoa deve ao pagador
    val paid: Boolean = false       // já quitou?
)
data class Expense(
    val id: String = "", val groupId: String = "", val title: String = "",
    val amount: Double = 0.0, val date: String = "",
    val payerId: String = "",                            // ⭐ quem pagou
    val participants: List<ExpenseShare> = emptyList(),  // ⭐ entre quem dividir
    val category: String = "", val receipt: String = ""  // receipt = foto (em texto)
)
```

🎤 **Fala:** "Toda despesa no app é como uma ficha de cadastro. A ficha guarda o valor, a data, **quem pagou** e uma lista de participantes — cada um com quanto deve e se já pagou. Não precisam ler campo por campo; o importante são esses dois marcados: quem pagou e entre quem dividir. É essa ficha simples que alimenta todo o cálculo que vem a seguir."

⏱ **Tempo:** ~0:40

---

## Slide 5 — Arquitetura: camadas + MVVM

🧩 **O que é:** um jeito de **separar tarefas**, como num restaurante: a **tela** é o garção (só mostra e anota) e o **ViewModel** é a cozinha (faz o trabalho nos bastidores). O **LiveData** é como um painel de pedidos que **se atualiza sozinho** — quando a cozinha termina, o garção vê na hora.

👉 **Foco:** a tela só **observa** o estado; a parte "pensante" fica separada → dá pra testar sozinha.

```kotlin
// ui/auth/LoginViewModel.kt — o "cérebro" da tela de login
class LoginViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = AuthRepository()
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState        // ⭐ painel que a tela observa

    fun login(email: String, password: String) = viewModelScope.launch {
        _loginState.value = LoginState.Loading                // "carregando…"
        try { repository.signIn(email, password); _loginState.value = LoginState.Success }
        catch (e: Exception) { _loginState.value = LoginState.Error(/* mensagem */) }
    }
}
// A tela só "fica de olho": loginState.observe(this) { mostrar(it) }
```

🎤 **Fala:** "Uma boa prática é separar 'o que aparece na tela' de 'quem faz a conta'. A gente usa um padrão chamado MVVM pra isso. Pensem num restaurante: a **tela** é o garçom, que só mostra as coisas e anota o pedido; o trabalho de verdade acontece na **cozinha**, que aqui é o ViewModel. E o **LiveData** é aquele painel de pedidos que acende sozinho quando algo fica pronto — então a tela se atualiza automaticamente, sem a gente ficar mandando. A vantagem prática: como a 'cozinha' é separada da tela, conseguimos testá-la sozinha, que é o que fazemos com o nosso algoritmo."

⏱ **Tempo:** ~0:50

---

## Slide 6 — ⭐ O algoritmo estrela: DebtSimplifier

🧩 **O que é:** a receita que **simplifica as dívidas**. Ela é "gulosa": a cada passo faz a jogada mais óbvia — pega **quem tem mais a receber** e **quem tem mais a pagar** e acerta os dois — repetindo até zerar. É como aquele momento no fim da viagem em que todo mundo se acerta de uma vez.

👉 **Foco:** o laço pega sempre o **maior credor × maior devedor** (⭐) — por isso dá o mínimo de pagamentos.

```kotlin
// 1) Saldo de cada um: + (tem a receber)  /  − (tem a pagar)
for (expense in expenses) for (share in expense.participants) {
    if (share.paid || share.userId == expense.payerId) continue
    balance.merge(expense.payerId, share.amountOwed, Double::plus)  // quem pagou: +
    balance.merge(share.userId, -share.amountOwed, Double::plus)    // quem deve: −
}
```

```kotlin
// 2) Acerta o maior credor com o maior devedor, até todos zerarem
while (true) {
    val creditor = balance.maxByOrNull { it.value } ?: break   // ⭐ tem MAIS a receber
    val debtor   = balance.minByOrNull { it.value } ?: break   // ⭐ tem MAIS a pagar
    if (creditor.value <= EPSILON || debtor.value >= -EPSILON) break
    val amount = min(creditor.value, -debtor.value)            // acerta o menor dos dois
    transfers += SettlementTransfer(debtor.key, creditor.key, amount)
    balance[creditor.key] = creditor.value - amount
    balance[debtor.key]   = debtor.value + amount              // quem zerou, sai
}
```

🎤 **Fala:** "Este é o coração do app, e funciona em duas etapas. Na **primeira**, a gente calcula o saldo de cada pessoa: quem bancou os outros fica positivo, com dinheiro a receber; quem consumiu fica negativo, com dinheiro a pagar. Na **segunda**, o app faz igual a gente faria na mão no fim de uma viagem: pega quem tem **mais a receber** e quem tem **mais a pagar** e acerta os dois; repete isso até todos ficarem zerados. É por isso que, no exemplo da Ana, Bruno e Carlos, o intermediário desaparece e sobra um pagamento só. Não precisam entender linha por linha — o essencial são essas duas linhas com estrela: sempre o maior com o maior."

⏱ **Tempo:** ~1:05

---

## Slide 7 — Build ⚙️ Gradle + Wrapper

🧩 **O que é:** *build* é **montar o app**, como uma fábrica que pega as peças e devolve o produto embalado — o **APK**, o arquivo que instala no celular. O **Gradle** é essa "linha de montagem". O **Wrapper** é um detalhe esperto: ele **trava a versão** da ferramenta dentro do projeto, como uma receita que já vem com a xícara de medida certa.

👉 **Foco:** com o Wrapper (⭐), qualquer um baixa o projeto e monta igual, **sem instalar nada** antes.

```properties
# gradle/wrapper/gradle-wrapper.properties — trava a versão da ferramenta
distributionUrl=https\://services.gradle.org/distributions/gradle-8.11.1-bin.zip  # ⭐
```

```kotlin
// build.gradle.kts (raiz) — liga os "complementos" que o projeto usa
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}
```

🎤 **Fala:** "Antes do código em si: o que é 'fazer o build'? É pegar tudo o que a gente escreveu e montar o aplicativo final, aquele arquivo que instala no celular. Quem faz essa montagem é uma ferramenta chamada **Gradle** — pensem nela como a linha de montagem da fábrica: ela junta as peças, monta e embala. Em vez de a gente apertar botões na mão, escrevemos em arquivos o que o app precisa e o Gradle executa. E tem um truque importante, o **Wrapper**: ele guarda dentro do projeto a versão exata da ferramenta. Assim, qualquer colega baixa o projeto e monta exatamente igual, sem precisar instalar nada antes — o projeto já traz a receita completa."

⏱ **Tempo:** ~0:45

---

## Slide 8 — Build ⚙️ dependências: catálogo + repositórios

🧩 **O que é:** *dependências* são **peças prontas** (bibliotecas) que a gente usa em vez de fabricar. Os *repositórios* são as **lojas** de onde o Gradle baixa essas peças. E o *Version Catalog* é a nossa **lista de compras central**, com a versão de cada peça anotada num lugar só.

👉 **Foco:** versões centralizadas (⭐ mudar 1 linha muda tudo); os repositórios dizem **de que loja** baixar.

```toml
# gradle/libs.versions.toml — a "lista de compras" com as versões
[versions]
agp = "8.10.1"
kotlin = "2.0.21"   # ⭐ muda aqui → vale p/ todo o projeto
coreKtx = "1.17.0"
junit = "4.13.2"
[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
```

```kotlin
// settings.gradle.kts — as "lojas" de onde baixamos as peças
dependencyResolutionManagement {
    repositories {
        google(); mavenCentral()
        maven { url = uri("https://jitpack.io") }   // loja da lib de gráficos
    }
}
```

🎤 **Fala:** "Quando eu digo 'dependência', é uma peça pronta que a gente reaproveita em vez de fabricar do zero — como comprar uma peça em vez de torneá-la. E o Gradle precisa saber de **qual loja** baixar cada peça: essas lojas são os repositórios, aqui o Google, o Maven Central e o JitPack. Pra não ficar bagunçado, anotamos a versão de cada peça numa **lista de compras central**, esse arquivo de catálogo. A vantagem: se eu quiser trocar a versão de uma peça, mudo **uma linha** na lista e o projeto inteiro passa a usar a nova."

⏱ **Tempo:** ~0:45

---

## Slide 9 — Build ⚙️ dependências do módulo + Firebase BoM

🧩 **O que é:** aqui a gente **lista as peças que o app usa** (`implementation`). O *Firebase BoM* é um **kit combinado**: em vez de escolher a versão de cada peça do Firebase e arriscar que não combinem, pegamos um "kit da mesma linha" que já vem com tudo compatível.

👉 **Foco:** com o `platform(firebase-bom)` (⭐), as peças do Firebase entram **sem precisar dizer a versão**.

```kotlin
// app/build.gradle.kts — as peças que este app usa
android {
    namespace = "com.example.divideai"; compileSdk = 36
    defaultConfig { minSdk = 26; targetSdk = 36 }    // roda do Android 8 pra cima
    buildFeatures { viewBinding = true }
}
dependencies {
    implementation(libs.material)                  // visual (Material 3)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))  // ⭐ o "kit"
    implementation("com.google.firebase:firebase-auth-ktx")       // login  — sem versão
    implementation("com.google.firebase:firebase-messaging-ktx")  // push   — sem versão
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")    // gráficos
    implementation("com.journeyapps:zxing-android-embedded:4.3.0") // QR Code
    testImplementation(libs.junit)                 // ferramenta de teste
}
```

🎤 **Fala:** "Aqui é onde a gente lista, uma por uma, as peças que o app usa. Reparem no destaque, o **Firebase BoM**. O Firebase é dividido em várias peças — login, banco, notificações — e elas precisam ser de versões que combinam. Em vez de escolher versão por versão e correr o risco de dar briga entre elas, pegamos um 'kit combinado' que já garante que tudo funciona junto. É por isso que o login e as notificações aparecem **sem número de versão**: o kit resolve isso pra gente."

⏱ **Tempo:** ~0:45

---

## Slide 10 — Firebase 🔌 Auth + Firestore

🧩 **O que é:** *Firebase Auth* é um **porteiro terceirizado** que cuida do login (a gente nem guarda as senhas). *Firestore* é o **banco de dados na nuvem**: em vez de uma planilha, ele guarda **fichas** (documentos) em **gavetas** (coleções). O `suspend` deixa o app **esperar a resposta sem travar** — como pedir um lanche e continuar conversando enquanto ele fica pronto.

👉 **Foco:** o login espera sem travar, e o banco devolve os dados já viram uma ficha Kotlin com `toObject` (⭐).

```kotlin
// AuthRepository.kt — login (o "porteiro" é o Firebase)
suspend fun signIn(email: String, pass: String): Result<AuthResult> =
  suspendCancellableCoroutine { cont ->
    auth.signInWithEmailAndPassword(email, pass)
      .addOnCompleteListener { t ->
        if (t.isSuccessful) cont.resume(Result.success(t.result!!))
        else cont.resumeWithException(t.exception!!)
      }
  }
```

```kotlin
// ExpenseRepository.kt — o banco na nuvem (Firestore)
val doc = db.collection("expenses").document()
doc.set(e.copy(id = doc.id))                        // guarda a ficha
    .addOnSuccessListener { cb(true, null) }
    .addOnFailureListener { cb(false, it.message) }
// ler: get() → it.toObject(Expense::class.java)    // ⭐ ficha do banco → objeto do app
```

🎤 **Fala:** "Duas peças do Firebase. Pro login usamos o **Authentication**: pensem num porteiro terceirizado que confere quem entra — a gente nem chega a guardar as senhas, quem cuida disso é o Google. E os dados ficam no **Firestore**, um banco de dados na nuvem: em vez de uma planilha com linhas, ele guarda fichas dentro de gavetas. Guardar é o `set`; e, na hora de ler, o `toObject` pega a ficha do banco e transforma de volta na ficha que o app entende. Ah, e reparem no `suspend`: é o jeito de esperar a resposta da internet sem o app congelar, como pedir um lanche e seguir conversando enquanto fica pronto."

⏱ **Tempo:** ~0:55

---

## Slide 11 — Firebase 🔌 Cloud Messaging (FCM)

🧩 **O que é:** *FCM* é o serviço que faz aquele **aviso aparecer no celular** (notificação push). O *token* é o **"endereço" do aparelho** — o servidor precisa dele pra saber pra qual celular entregar o aviso, como um CEP.

👉 **Foco:** guardamos o "endereço" do aparelho em `users/{uid}.fcmToken` (⭐).

```kotlin
// MainActivity — guarda o "endereço" (token) deste celular
FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
    FirebaseFirestore.getInstance()
        .collection("users").document(uid).update("fcmToken", token)   // ⭐ guarda o "CEP"
}
```

```kotlin
// DivideAiMessagingService.kt — o que fazer quando o aviso chega
class DivideAiMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) = saveTokenToFirestore(token)  // "endereço" mudou
    override fun onMessageReceived(message: RemoteMessage) {
        showNotification(message.notification?.title, message.notification?.body) // mostra
    }
}
```

🎤 **Fala:** "Aqui é a notificação — aquele aviso que aparece na tela do celular, mesmo com o app fechado. Pra isso usamos o Cloud Messaging. Pensem assim: pra alguém te enviar uma carta, precisa do seu endereço. O 'endereço' do celular aqui é o **token**. Então, quando o app abre, a gente pega esse token e guarda no cadastro do usuário. Depois, quando o aviso chega, essa classe é a responsável por montar e mostrar a notificação."

⏱ **Tempo:** ~0:40

---

## Slide 12 — UI 🔌 Navigation + ViewBinding + Material 3

🧩 **O que é:** *Navigation* é o **GPS de telas** — controla ir de uma tela pra outra. *ViewBinding* dá **atalhos prontos** pra cada elemento da tela (antes, a gente procurava cada botão pelo nome, o que dava erro). *Material 3* é o **kit de peças visuais** do Google (botões, cores, tema claro/escuro).

👉 **Foco:** um comando (⭐) liga a barra de baixo ao "mapa" de telas.

```kotlin
// MainActivity — liga a barra de baixo ao mapa de telas
binding = ActivityMainBinding.inflate(layoutInflater)   // atalhos prontos (ViewBinding)
setContentView(binding.root)
val navHost = supportFragmentManager
    .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
navView.setupWithNavController(navHost.navController)    // ⭐ barra ↔ mapa
```

```xml
<!-- res/navigation/mobile_navigation.xml — o "mapa" das telas -->
<navigation app:startDestination="@+id/navigation_groups">
    <fragment android:id="@+id/navigation_groups"  android:name="....GroupsFragment" />
    <fragment android:id="@+id/navigation_expenses" android:name="....MyExpensesFragment" />
</navigation>
```

🎤 **Fala:** "Três peças da parte visual. O **Navigation** é como um GPS das telas: a gente desenha um mapa dizendo quais telas existem, e um único comando liga a barrinha de baixo a esse mapa — sem a gente programar cada troca de tela. O **ViewBinding** cria atalhos diretos pra cada elemento da tela; antes disso, a gente tinha que procurar cada botão pelo nome, e às vezes errava. E o visual em si — os botões, as cores, o modo escuro — vem pronto do **Material 3**, o kit de design do Google."

⏱ **Tempo:** ~0:40

---

## Slide 13 — Bibliotecas 🔌 gráficos + QR Code

🧩 **O que é:** *MPAndroidChart* é uma **máquina de fazer gráfico** — a gente entrega os números e ela desenha a pizza. *ZXing* é a biblioteca de **QR Code**: o convite do grupo é um "endereço" nosso (`divideai://group/<id>`) que vira um QR pra outra pessoa escanear.

👉 **Foco:** damos os dados e a lib desenha (⭐); o mesmo "endereço" é gerado e depois lido no QR.

```kotlin
// MPAndroidChart — a "máquina de gráfico": dados entram, pizza sai
val entries = state.breakdown.map {
    PieEntry(it.total.toFloat(), getString(it.category.labelRes))   // ⭐ cada gasto → 1 fatia
}
binding.pieChart.data = PieData(PieDataSet(entries, "")); binding.pieChart.animateY(800)
```

```kotlin
// ZXing — gerar o QR do convite e depois ler
val bmp = BarcodeEncoder().encodeBitmap(
    GroupInviteCode.encode(groupId), BarcodeFormat.QR_CODE, 640, 640)   // gera o QR
registerForActivityResult(ScanContract()) { r ->                       // lê o QR
    val id = GroupInviteCode.decode(r?.contents) ?: return@registerForActivityResult
    joinGroupFromInvite(id)                                            // entra no grupo
}
```

🎤 **Fala:** "Aqui usamos duas ferramentas prontas. A primeira desenha o gráfico de pizza do painel de gastos: a gente não desenha nada na mão — só entrega os números, cada categoria vira uma fatia, e a biblioteca cuida do resto. A segunda é o **QR Code**, pra convidar alguém pro grupo. Funciona como um convite com um código: de um lado a gente gera o QR a partir de um 'endereço' do grupo; do outro, a pessoa aponta a câmera, o app lê esse endereço e entra no grupo automaticamente."

⏱ **Tempo:** ~0:40

---

## Slide 14 — Jetpack 🔌 imagem (Base64) + idiomas

🧩 **O que é:** *Photo Picker* é a **telinha de escolher uma foto** sem dar acesso ao álbum inteiro. *Base64* é **transformar a foto em texto** pra ela caber dentro do cadastro (assim não precisamos de um serviço de fotos pago). *i18n* é ter o **app em vários idiomas**.

👉 **Foco:** a foto vira **texto** e cabe direto no banco (⭐) → não precisamos do Firebase Storage.

```kotlin
// Escolher a foto (Photo Picker) e transformá-la em texto (Base64)
private val pick = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
    uri -> if (uri != null) handlePickedReceipt(uri) }

fun encodeFromUri(context, uri, maxSize = 720, quality = 70): String? {
    val menor = decodeScaled(context, uri, maxSize)          // diminui a foto
    val certo = applyExifOrientation(context, uri, menor)    // desvira, se preciso
    return Base64.encodeToString(toJpegBytes(certo, quality), Base64.NO_WRAP)  // ⭐ foto → texto
}
```

**No slide:** idiomas (pt-BR/en) — troca **dentro do app** com `AppCompatDelegate.setApplicationLocales(...)`, e a escolha fica salva.

🎤 **Fala:** "Duas comodidades. Pra escolher foto de perfil ou comprovante, usamos o **Photo Picker**, aquela telinha do Android que deixa pegar só a foto escolhida, sem liberar o álbum todo. E aqui tem um truque de economia: guardar imagens num serviço de fotos costuma ser pago, então a gente **transforma a foto em texto** — é isso que o Base64 faz — e guarda esse texto direto no cadastro, no próprio banco. Antes, a gente diminui a foto e corrige a orientação, pra não vir deitada. E o app ainda é bilíngue: dá pra trocar o idioma só do aplicativo, sem mexer no do celular."

⏱ **Tempo:** ~0:40

---

## Slide 15 — Testes 🧪 estrutura e cobertura

🧩 **O que é:** *teste automatizado* é um **robô conferente**: um código que testa o nosso código sozinho, pra não termos que conferir tudo na mão. *JUnit* testa uma **peça isolada** na bancada (rápido, sem emulador); *Espresso* testa **apertando botões** numa tela de verdade (precisa de celular/emulador).

👉 **Foco:** montamos uma suíte de **23 testes** em **4 áreas** da lógica do app, todos passando (⭐) — rodam em segundos com `./gradlew test`.

```kotlin
// app/build.gradle.kts — as ferramentas de teste
dependencies {
    testImplementation(libs.junit)                          // ⭐ testa peça isolada (rápido)
    androidTestImplementation(libs.androidx.espresso.core)  // testa apertando botões (tela)
}
```

**No slide — o que a suíte cobre** (tudo em `app/src/test/…`, roda com `./gradlew test`):

| Classe testada | Parte do app | Casos |
|---|---|--:|
| `DebtSimplifier` | acerto de contas — quem paga quem | 5 |
| `ExpenseSplit` | divisão da despesa (pagador + participantes) | 6 |
| `GroupInviteCode` | convite de grupo por QR Code | 6 |
| `ExpenseCategory` | categorias da despesa | 6 |

Estratégia: testar a **regra de negócio** (onde um erro dói mais), mantida separada da tela pra ser testável. Testes de interface (Espresso) ficam como **próximo passo**.

🎤 **Fala:** "Teste automatizado é ter um robô que confere o nosso trabalho sozinho, sem a gente clicar em tudo na mão toda vez. Existem dois tipos: um testa uma peça isolada, na bancada, e é super rápido; o outro simula uma pessoa apertando botões na tela e precisa de emulador. A gente montou uma suíte de **vinte e três testes**, todos passando, cobrindo **quatro partes** da lógica do app: o algoritmo que acerta as contas; a divisão de uma despesa entre quem pagou e os participantes; o convite de grupo por QR Code; e as categorias de despesa. A ideia é testar a regra de negócio — que é onde um erro machuca mais — mantida separada da tela justamente pra dar pra testar sozinha, em segundos, sem abrir emulador. Testes de interface com o Espresso a gente deixou como próximo passo."

⏱ **Tempo:** ~0:50

---

## Slide 16 — Testes 🧪 `DebtSimplifierTest` (5 cenários)

🧩 **O que é:** cada `@Test` é **um cenário** que a gente inventa: monta uma **entrada** conhecida, roda o algoritmo e o `assertEquals` **afirma o resultado esperado** ("tem que dar isso; se der outra coisa, o robô apita erro"). São **5 cenários** — o caminho principal e os casos-limite.

👉 **Foco:** o cenário estrela prova que Ana→Bruno→Carlos vira **1** pagamento Ana→Carlos (⭐) — o do meio some.

```kotlin
private fun debt(quemPagou: String, quemDeve: String, valor: Double) =
    Expense(payerId = quemPagou, participants = listOf(ExpenseShare(quemDeve, valor)))

@Test fun `cadeia A deve B e B deve C vira transferencia A para C`() {
    val transfers = DebtSimplifier.simplify(listOf(
        debt("B", "A", 10.0),   // A ("Ana") deve 10 a B ("Bruno")
        debt("C", "B", 10.0)))  // B deve 10 a C ("Carlos")
    assertEquals(1, transfers.size)              // sobra 1 pagamento só
    assertEquals("A", transfers[0].debtorId)     // A paga...
    assertEquals("C", transfers[0].creditorId)   // ⭐ ...direto pro C (o do meio some)
    assertEquals(10.0, transfers[0].amount, 0.01)
}
```

**Os 5 cenários e o que cada um garante:**

| Cenário (o caso montado) | O que o teste prova |
|---|---|
| ⭐ **Cadeia** — A deve a B **e** B deve a C | vira **1** só pagamento A→C — o intermediário some |
| **Dívida direta** — A pagou R$25 por B | **1** transferência: B→A, R$25 |
| **Rateio** — jantar de R$30 dividido entre A, B e C | B e C devem R$10 cada a A (a parte de quem pagou é ignorada) |
| **Parcela já paga** (`paid = true`) | **nenhuma** transferência — sai do cálculo |
| **Lista de despesas vazia** | **nenhuma** transferência — não quebra |

🎤 **Fala:** "Aqui está o robô conferente na prática. Cada bloco marcado com `@Test` é um cenário que a gente monta: eu invento uma situação de entrada e **afirmo** o que tem que sair — é isso que o `assertEquals` faz, 'confere se é isto, senão apita erro'. O cenário estrela é a cadeia: no código são A, B e C, mas pensem em Ana, Bruno e Carlos — a Ana deve ao Bruno e o Bruno deve ao Carlos, e o teste prova que isso vira um **único** pagamento da Ana direto pro Carlos; o intermediário some. Os outros quatro cobrem o resto do comportamento: a **dívida direta** de uma pessoa pra outra; o **rateio** de uma conta entre várias pessoas, onde a parte de quem pagou não é cobrada dele mesmo; uma **parcela já marcada como paga**, que tem que sumir do cálculo; e a **lista vazia**, que não pode quebrar o app. São cinco cenários, todos passando — e é exatamente esse comando que o robô do CI executa a cada envio."

⏱ **Tempo:** ~1:00

---

## Slide 17 — Opcionais ✅ Issue tracking + CI/CD

🧩 **O que é:** *Issue tracking* é ter um **quadro de tarefas e bugs** (usamos o do GitHub). *CI/CD* é um **funcionário-robô** que, toda vez que a gente envia código, roda os testes e monta o app sozinho, avisando se algo quebrou. Esse robô é o *GitHub Actions*.

👉 **Foco:** todo envio de código roda os testes (⭐) → se o algoritmo quebrar, o robô avisa antes de juntar.

```yaml
# .github/workflows/android.yml — a "receita" do robô
on:
  push: { branches: [ main ] }          # a cada envio de código...
jobs:
  build-and-test:
    runs-on: ubuntu-latest              # ...num computador na nuvem:
    steps:
      - uses: actions/checkout@v4       # pega o código
      - uses: actions/setup-java@v4     # prepara o ambiente
      - uses: android-actions/setup-android@v3
      - run: ./gradlew test             # ⭐ roda os 23 testes
      - run: ./gradlew assembleDebug    # monta o app (APK)
```

🎤 **Fala:** "A tarefa pedia itens opcionais, e fizemos dois. O primeiro é ter um **quadro de tarefas e bugs**, que é o GitHub Issues. O segundo é o que chamam de **integração contínua**: imaginem um funcionário-robô que fica de plantão. Toda vez que a gente envia uma mudança no código, ele acorda, roda todos os testes e monta o aplicativo sozinho, num computador na nuvem. Se alguém quebrar aquele nosso algoritmo, o robô avisa na hora — antes da mudança entrar de vez no projeto. Essa é a receita dele."

⏱ **Tempo:** ~0:45

---

## Slide 18 — Opcionais ✅ Containers + o extra: notifier

🧩 **O que é:** *Container* (tipo o Docker) é uma **"marmita" de programa**: leva tudo o que ele precisa dentro, pra rodar igual em qualquer lugar. O *notifier* é um programinha **separado, que roda num servidor** e fica **de vigia** no banco: quando entra uma despesa nova, ele dispara o aviso.

👉 **Foco:** o vigia (⭐) percebe a despesa nova **na hora** e avisa os participantes — de fora do app.

```javascript
// notifier/watch.js — um "vigia" que fica de olho no banco
db.collection('expenses').where('createdAt', '>=', startedAt)
  .onSnapshot((snap) => {                                   // ⭐ percebe algo novo na hora
    for (const change of snap.docChanges()) {
      if (change.type !== 'added') continue;                // só despesa NOVA
      for (const p of (change.doc.data().participants || [])) {
        if (p.userId === change.doc.data().payerId) continue;   // não avisa quem pagou
        sendToUser(p.userId, 'Nova despesa no grupo', 'Você foi incluído.');
      }
    }
  });
```

🎤 **Fala:** "O terceiro opcional era **containers** — que é tipo uma marmita: empacotar o programa com tudo dentro pra rodar igual em qualquer servidor. Esse a gente **não** fez, porque um app de celular não roda nesse formato; mas deixamos indicado como um próximo passo. Em compensação, fizemos um **extra**: um programinha separado, o notifier, que fica como um **vigia** de plantão olhando o banco de dados. Quando entra uma despesa nova, ele percebe na hora e dispara a notificação pra cada participante, menos pra quem pagou. E ele é escrito em outra linguagem, o que mostra o projeto conversando entre tecnologias diferentes."

⏱ **Tempo:** ~0:40

---

## Slide 19 — 🎬 Demonstração ao vivo (opcional)

👉 **Foco:** o ponto alto é mostrar **Saldos** virarem **Saldos Simplificados**.

**No slide (passo a passo):**
1. Login → Criar grupo ("Viagem Praia")
2. Convidar alguém por **QR Code**
3. Lançar despesa: **categoria + quem pagou + participantes + comprovante**
4. Ver **Saldos** → **SALDOS SIMPLIFICADOS**
5. **Painel**: gráfico de pizza por categoria
6. *(opcional)* rodar o notifier → aviso chegando

> **Plano B:** se o emulador falhar, os prints em `docs/screenshots/` (01→05) seguem o mesmo passo a passo.

🎤 **Fala:** "Agora mostrando funcionando: faço login, crio um grupo, convido alguém pelo QR Code e lanço uma despesa com foto do comprovante. O momento mais legal é ver os saldos normais virarem os **saldos simplificados** — aquela mágica de reduzir os pagamentos —, e o painel com o gráfico de gastos."

**Nota do apresentador:** abrir o emulador e **já estar logado ANTES**; deixar na tela de Grupos. Se faltar tempo, pular o aviso. Se o emulador travar, usar os prints.

⏱ **Tempo:** ~1:00

---

## Slide 20 — Encerramento

**No slide:**
1. **Problema real:** dividir contas em grupo e saber "quem deve a quem" gera confusão
2. **Solução:** registrar, dividir e **SIMPLIFICAR** as dívidas ao mínimo de pagamentos (algoritmo próprio, **testado**)
3. **Base sólida:** app Android nativo + kits do Google (Jetpack, Material) + Firebase; montagem confiável e **23 testes em 4 áreas + robô de CI**
- **Diferenciais:** testes + CI · QR Code · foto em texto (Base64) · modo escuro · dois idiomas · notifier
- **Próximos passos:** testes de tela · empacotar o notifier em container · marcar dívida como paga

🎤 **Fala:** "Recapitulando de forma bem direta: o DivideAi resolve uma dor que todo grupo conhece — transforma um monte de despesas em saldos claros e, principalmente, simplifica as dívidas ao mínimo de pagamentos, com um algoritmo nosso que é conferido por testes automáticos e por um robô a cada mudança. Tudo sobre uma base sólida de Android com Firebase. Como próximos passos, queremos testes de tela e empacotar o vigia num container. Muito obrigada!"

⏱ **Tempo:** ~0:40

---

## Slide 21 — Obrigado / Perguntas

**No slide:** "Obrigado! 🐮 · Perguntas?" — Emilly Neves e David Marques · Projeto final 2025/2 (MIT) · `github.com/DavidMarquesss/Divideai`

🎤 **Fala:** "Obrigada pela atenção! Ficamos abertos para perguntas."

⏱ **Tempo:** ~0:15

---

## Anexo — Glossário em palavras simples

- **Build / APK** — "montar" o app; o APK é o arquivo pronto que instala no celular.
- **Gradle** — a linha de montagem que junta as peças e monta o app.
- **Wrapper** — trava a versão da ferramenta no projeto (todo mundo monta igual).
- **Dependência / biblioteca** — peça pronta que reaproveitamos em vez de fabricar.
- **Repositório** — a loja de onde baixamos essas peças.
- **Version Catalog** — lista de compras central com a versão de cada peça.
- **BoM** — kit combinado que já traz as peças (do Firebase) compatíveis entre si.
- **MVVM / ViewModel / LiveData** — separar "tela" de "cozinha"; um painel que se atualiza sozinho.
- **Firebase Auth** — porteiro terceirizado que cuida do login (não guardamos senhas).
- **Firestore** — banco de dados na nuvem que guarda "fichas" em "gavetas".
- **FCM / token** — aviso no celular (push); token = o "endereço" do aparelho.
- **suspend / coroutine** — esperar a resposta sem travar o app.
- **ViewBinding** — atalhos prontos pra cada elemento da tela.
- **Navigation** — o GPS que leva de uma tela a outra.
- **Base64** — transformar uma foto em texto (pra caber no banco).
- **JUnit / Espresso** — robô que testa uma peça isolada / que testa apertando botões na tela.
- **assertEquals** — afirmar o resultado esperado ("tem que dar isso, senão apita").
- **CI/CD / GitHub Actions** — funcionário-robô que testa e monta o app a cada mudança.
- **Container (Docker)** — "marmita" que leva o programa com tudo dentro pra rodar em qualquer lugar.
- **notifier / onSnapshot** — vigia que fica de olho no banco e avisa quando entra algo novo.
