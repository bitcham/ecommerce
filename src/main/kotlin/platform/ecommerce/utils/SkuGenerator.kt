package platform.ecommerce.utils

import java.util.Locale

object SkuGenerator {

    /**
     * Generates a normalized option SKU.
     * * Improvements:
     * 1. Normalizes to UPPERCASE to prevent "Black" vs "black" duplicates.
     * 2. Uses Regex to collapse ANY separator sequence (spaces, slashes, tabs) into a single dash.
     */
    fun generateOptionSku(productSku: String, optionName: String): String {
        val normalizedOption = optionName
            // 1. Collapse multiple non-alphanumeric chars into one dash
            //    Example: "Red  /  L" -> "Red-L"
            .replace(Regex("[^a-zA-Z0-9\\p{L}]+"), "-")
            // 2. Remove leading/trailing dashes
            .trim('-')
            // 3. Standardize casing
            .uppercase(Locale.getDefault())

        return "${productSku.uppercase()}-$normalizedOption"
    }
}