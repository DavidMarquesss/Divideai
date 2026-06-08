package com.example.divideai.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.divideai.R

/**
 * Categorias suportadas para classificar despesas. O [id] é persistido no
 * Firestore como uma string estável (não traduzir).
 */
enum class ExpenseCategory(
    val id: String,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
) {
    FOOD("food", R.string.category_food, R.drawable.ic_category_food),
    TRANSPORT("transport", R.string.category_transport, R.drawable.ic_category_transport),
    GROCERIES("groceries", R.string.category_groceries, R.drawable.ic_category_groceries),
    ENTERTAINMENT("entertainment", R.string.category_entertainment, R.drawable.ic_category_entertainment),
    HOUSING("housing", R.string.category_housing, R.drawable.ic_category_housing),
    HEALTH("health", R.string.category_health, R.drawable.ic_category_health),
    EDUCATION("education", R.string.category_education, R.drawable.ic_category_education),
    SHOPPING("shopping", R.string.category_shopping, R.drawable.ic_category_shopping),
    OTHER("other", R.string.category_other, R.drawable.ic_category_other);

    companion object {
        /** Categoria padrão usada quando uma despesa não tem categoria salva. */
        val DEFAULT: ExpenseCategory = OTHER

        /** Resolve uma categoria pelo [id] persistido; cai em [DEFAULT] quando vazio/desconhecido. */
        fun fromId(id: String?): ExpenseCategory =
            entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}
