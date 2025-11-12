package platform.ecommerce.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import platform.ecommerce.domain.Product
import platform.ecommerce.dto.response.ProductResponse

@Mapper(uses = [ProductOptionMapper::class])
interface ProductMapper {

    @Mapping(target = "price", expression = "java(product.getPrice().getAmount().doubleValue())")
    @Mapping(target = "status", source = "productStatus")
    fun toResponse(product: Product): ProductResponse
}