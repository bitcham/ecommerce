package platform.ecommerce.domain

import jakarta.persistence.*
import jakarta.persistence.GenerationType.IDENTITY
import platform.ecommerce.domain.vo.Money
import platform.ecommerce.enums.ProductStatus

@Entity
class Product(
    @Column(nullable = false, unique = true, length = 100)
    var sku: String,

    @Column(nullable = false)
    var name: String,

    @Lob
    var description: String,

    @Embedded
    private var price: Money,

    @Column(length = 500, name = "image_url")
    var imageUrl: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "product_status")
    private var productStatus: ProductStatus = ProductStatus.AVAILABLE
): VersionedBaseEntity() {

    @Id @GeneratedValue(strategy = IDENTITY)
    var id: Long? = null
        protected set

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val mutableOptions: MutableList<ProductOption> = mutableListOf()

    val options: List<ProductOption>
        get() = mutableOptions.toList()

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
        val optionSku = platform.ecommerce.utils.SkuGenerator.generateOptionSku(this.sku, optionName)
        val option = ProductOption.create(
            product = this,
            sku = optionSku,
            optionName = optionName,
            stockQuantity = stockQuantity
        )
        mutableOptions.add(option)
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
        return mutableOptions.any { it.isOrderable() }
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