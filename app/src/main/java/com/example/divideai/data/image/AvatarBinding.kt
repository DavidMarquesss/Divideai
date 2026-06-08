package com.example.divideai.data.image

import android.widget.ImageView
import com.example.divideai.R

/**
 * Define o avatar de um [ImageView] usando a foto Base64 atual do usuário
 * [uid]. Se o usuário ainda não tem foto (ou a busca falha), mantém o
 * [placeholder].
 *
 * Seguro para uso em adapters de RecyclerView — a chamada assíncrona
 * verifica se o `View` ainda representa o mesmo `uid` antes de aplicar a
 * imagem, evitando trocas indevidas em itens reciclados.
 */
fun ImageView.loadUserAvatar(
    uid: String,
    placeholder: Int = R.drawable.ic_generic_avatar_gray
) {
    val originalScale = scaleType
    setImageResource(placeholder)
    scaleType = originalScale
    setTag(R.id.tag_avatar_uid, uid)
    UserAvatarCache.get(uid) { base64 ->
        if (getTag(R.id.tag_avatar_uid) != uid) return@get
        val bmp = Base64Image.decode(base64) ?: return@get
        scaleType = ImageView.ScaleType.CENTER_CROP
        setImageBitmap(bmp)
    }
}

/** Atalho para receber a foto já decodificada (ou null). Útil fora de adapters. */
fun ImageView.setBase64Image(base64: String?, placeholder: Int = R.drawable.ic_generic_avatar_gray) {
    val bmp = Base64Image.decode(base64)
    if (bmp != null) {
        scaleType = ImageView.ScaleType.CENTER_CROP
        setImageBitmap(bmp)
    } else {
        setImageResource(placeholder)
    }
}
