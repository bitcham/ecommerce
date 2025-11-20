package platform.ecommerce.domain

import jakarta.persistence.*
import jakarta.persistence.GenerationType.IDENTITY
import org.hibernate.annotations.SQLRestriction
import platform.ecommerce.domain.vo.Money
import platform.ecommerce.enums.ProductStatus
import platform.ecommerce.utils.SkuGenerator.generateOptionSku

@Entity
@SQLRestriction("deleted_at IS NULL")
class Product(
    @Column(nullable = false, unique = true, length = 100)
    var sku: String,

    @Column(nullable = false)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String,

    @Embedded
    private var price: Money,

    @Column(length = 500, name = "image_url")
    var imageUrl: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "product_status")
    private var productStatus: ProductStatus = ProductStatus.AVAILABLE
): SoftDeletableEntity() {

    @Id @GeneratedValue(strategy = IDENTITY)
    var id: Long? = null
        protected set

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val productOptions: MutableList<ProductOption> = mutableListOf()

    val options: List<ProductOption>
        get() = productOptions.toList()

    fun getPrice(): Money = price

    fun getProductStatus(): ProductStatus = productStatus

    fun changePrice(newPrice: Money) {
        require(productStatus != ProductStatus.DISCONTINUED) {
            "Cannot change price of discontinued product"
        }
        this.price = newPrice
    }

    fun updateInfo(name: String, description: String, imageUrl: String) {
        require(name.isNotBlank()) { "Name cannot be blank" }
        this.name = name
        this.description = description
        this.imageUrl = imageUrl
    }

    fun discontinue() {
        this.productStatus = ProductStatus.DISCONTINUED
    }

    fun isAvailable(): Boolean {
        return productStatus == ProductStatus.AVAILABLE && hasAvailableStock()
    }

    fun addOption(optionName: String, stockQuantity: Int) {
        val optionSku = generateOptionSku(this.sku, optionName)
        val option = ProductOption.create(
            product = this,
            sku = optionSku,
            optionName = optionName,
            stockQuantity = stockQuantity
        )
        productOptions.add(option)
        checkAndUpdateStatus()
    }

    fun removeOption(optionId: Long) {
        val option = productOptions.find { it.id == optionId }
            ?: throw IllegalArgumentException("Product option with ID $optionId not found")
        productOptions.remove(option)
        checkAndUpdateStatus()
    }

    internal fun checkAndUpdateStatus() {
        if (productStatus == ProductStatus.DISCONTINUED) {
            return
        }

        productStatus = if (hasAvailableStock()) {
            ProductStatus.AVAILABLE
        } else {
            ProductStatus.OUT_OF_STOCK
        }
    }

    private fun hasAvailableStock(): Boolean {
        return productOptions.any { it.isOrderable() }
    }

    companion object {
        fun create(
            sku: String,
            name: String,
            description: String,
            price: Double,
            imageUrl: String
        ): Product {
            require(sku.isNotBlank()) { "SKU cannot be blank" }
            require(name.isNotBlank()) { "Name cannot be blank" }
            require(price >= 0) { "Price must be non-negative" }

            return Product(
                sku = sku,
                name = name,
                description = description,
                price = Money.of(price),
                imageUrl = imageUrl
            )
        }
    }
}