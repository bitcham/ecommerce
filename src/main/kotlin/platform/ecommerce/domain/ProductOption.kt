package platform.ecommerce.domain

import jakarta.persistence.*
import jakarta.persistence.GenerationType.IDENTITY

@Entity
@Table(name = "product_option")
class ProductOption(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(nullable = false, unique = true, length = 100)
    var sku: String,

    @Column(nullable = false, name = "option_name")
    var optionName: String,

    @Column(nullable = false, name = "stock_quantity")
    private var stockQuantity: Int
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    var id: Long? = null
        protected set

    fun getStockQuantity(): Int = stockQuantity

    fun decreaseStock(quantity: Int) {
        require(quantity > 0) { "Quantity must be positive" }
        require(stockQuantity >= quantity) { "Insufficient stock: requested=$quantity, available=$stockQuantity" }

        stockQuantity -= quantity
        product.checkAndUpdateStatus()
    }

    fun increaseStock(quantity: Int) {
        require(quantity > 0) { "Quantity must be positive" }

        stockQuantity += quantity
        product.checkAndUpdateStatus()
    }

    fun isOrderable(): Boolean {
        return stockQuantity > 0
    }

    fun canOrder(quantity: Int): Boolean {
        return stockQuantity >= quantity
    }

    companion object {
        fun create(
            product: Product,
            sku: String,
            optionName: String,
            stockQuantity: Int
        ): ProductOption {
            require(sku.isNotBlank()) { "SKU cannot be blank" }
            require(optionName.isNotBlank()) { "Option name cannot be blank" }
            require(stockQuantity >= 0) { "Stock quantity must be non-negative" }

            return ProductOption(
                product = product,
                sku = sku,
                optionName = optionName,
                stockQuantity = stockQuantity
            )
        }
    }
}
