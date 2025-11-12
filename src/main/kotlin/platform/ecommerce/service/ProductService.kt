package platform.ecommerce.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import platform.ecommerce.domain.Product
import platform.ecommerce.domain.ProductOption
import platform.ecommerce.dto.request.ProductCreateRequest
import platform.ecommerce.dto.request.ProductOptionRequest
import platform.ecommerce.exception.ProductNotFoundException
import platform.ecommerce.repository.ProductRepository
import platform.ecommerce.utils.Logger.Companion.logger

@Service
class ProductService(
    private val productRepository: ProductRepository
) {

    @Transactional
    fun createProduct(request: ProductCreateRequest): Product {
        val product = Product.create(
            sku = request.sku,
            name = request.name,
            description = request.description,
            price = request.price,
            imageUrl = request.imageUrl
        )

        val savedProduct = productRepository.save(product)

        logger.info { "Product created: productId=${savedProduct.id}, sku=${savedProduct.sku}" }

        return savedProduct
    }

    @Transactional
    fun addProductOption(productId: Long, request: ProductOptionRequest): ProductOption {
        val product = productRepository.findById(productId)
            ?: throw ProductNotFoundException("Product not found: productId=$productId")

        product.addOption(
            optionName = request.optionName,
            stockQuantity = request.stockQuantity
        )

        logger.info { "Option added: productId=$productId, optionName=${request.optionName}" }

        return product.options.last()
    }
}
