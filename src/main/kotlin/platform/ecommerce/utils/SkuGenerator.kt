package platform.ecommerce.utils

object SkuGenerator {

    /**
     * Generates option SKU from product SKU and option name.
     * Format: {productSku}-{normalized-option-name}
     *
     * Examples:
     * - productSku="TSHIRT", optionName="빨강/M" -> "TSHIRT-빨강-M"
     * - productSku="SHOES", optionName="Black/270mm" -> "SHOES-Black-270mm"
     */
    fun generateOptionSku(productSku: String, optionName: String): String {
        val normalizedOption = optionName
            .replace("/", "-")
            .replace(" ", "-")
            .trim()

        return "$productSku-$normalizedOption"
    }
}
