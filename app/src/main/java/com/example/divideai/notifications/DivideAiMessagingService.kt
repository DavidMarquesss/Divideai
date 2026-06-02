package com.example.divideai.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.example.divideai.MainActivity
import com.example.divideai.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Recebe mensagens FCM (notificações push) e:
 *   - registra o token atual deste dispositivo no documento do usuário em Firestore;
 *   - exibe uma notificação local quando o app recebe um push em foreground.
 *
 * O canal de notificações é criado de forma idempotente em [ensureChannel].
 */
class DivideAiMessagingService : FirebaseMessagingService() {

    /**
     * Disparado quando o FCM gera um novo token para este dispositivo (instalação
     * recente, app data limpo, troca de Firebase project etc.). Persiste o token
     * no documento do usuário logado para que um servidor possa enviar pushes.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveTokenToFirestore(token)
    }

    /**
     * Recebe mensagens do FCM. Para mensagens **data-only** (sem o bloco
     * `notification`), o Android não exibe nada automaticamente — montamos
     * uma notificação local. Mensagens com bloco `notification` são exibidas
     * pelo sistema quando o app está em background.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(R.string.notification_default_title)
        val body = message.notification?.body ?: message.data["body"].orEmpty()

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        ensureChannel(this)

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName)
                ?: android.content.Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_DEFAULT)
            .setSmallIcon(R.drawable.ic_cow_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(openAppIntent)
            .build()

        val managerCompat = NotificationManagerCompat.from(this)
        // POST_NOTIFICATIONS é checado no Activity. Aqui apenas tentamos exibir.
        if (managerCompat.areNotificationsEnabled()) {
            managerCompat.notify(System.currentTimeMillis().toInt(), notification)
        }
    }

    private fun saveTokenToFirestore(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("fcmToken", token)
    }

    companion object {
        const val CHANNEL_ID_DEFAULT = "divideai_default"

        /**
         * Cria o canal de notificações default (no-op em APIs < 26, mas o
         * minSdk do app é 26 — então o NotificationManager sempre existe).
         */
        fun ensureChannel(context: Context) {
            val manager = context.getSystemService<NotificationManager>() ?: return
            if (manager.getNotificationChannel(CHANNEL_ID_DEFAULT) != null) return

            val channel = NotificationChannel(
                CHANNEL_ID_DEFAULT,
                context.getString(R.string.notification_channel_default_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_default_description)
            }
            manager.createNotificationChannel(channel)
        }
    }
}
