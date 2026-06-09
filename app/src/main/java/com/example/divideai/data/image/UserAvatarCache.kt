package com.example.divideai.data.image

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Cache em memória das fotos de perfil (Base64) dos usuários.
 *
 * Sem isso, listas com muitos membros disparariam uma leitura ao Firestore
 * por item a cada bind do RecyclerView. O cache agrega chamadas concorrentes
 * para o mesmo `uid` e devolve resultados instantâneos depois da primeira
 * busca.
 *
 * Não persiste entre execuções — propositadamente, para que o usuário sempre
 * comece com fotos atualizadas após login.
 */
object UserAvatarCache {

    private val cache = HashMap<String, String>()
    private val pending = HashMap<String, MutableList<(String?) -> Unit>>()

    /**
     * Devolve a foto Base64 do usuário [uid] ao [callback]. Retorna `null`
     * quando o documento não existe, não tem foto ou a leitura falha.
     */
    fun get(uid: String, callback: (String?) -> Unit) {
        if (uid.isEmpty()) {
            callback(null)
            return
        }
        cache[uid]?.let {
            callback(it.takeIf { v -> v.isNotEmpty() })
            return
        }
        val waiters = pending[uid]
        if (waiters != null) {
            waiters.add(callback)
            return
        }
        val newWaiters = mutableListOf(callback)
        pending[uid] = newWaiters
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val base64 = doc.getString("profilePhoto") ?: ""
                cache[uid] = base64
                pending.remove(uid)?.forEach { it(base64.takeIf { v -> v.isNotEmpty() }) }
            }
            .addOnFailureListener {
                cache[uid] = ""
                pending.remove(uid)?.forEach { it(null) }
            }
    }

    /** Limpa a entrada do [uid] — chame após o próprio usuário trocar a foto. */
    fun invalidate(uid: String) {
        cache.remove(uid)
    }

    /** Limpa tudo — útil ao deslogar. */
    fun clear() {
        cache.clear()
        pending.clear()
    }
}
