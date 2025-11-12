package platform.ecommerce.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import platform.ecommerce.domain.ProductOption
import platform.ecommerce.dto.response.ProductOptionResponse

@Mapper
interface ProductOptionMapper {
    @Mapping(source = "stockQuantity", target = "stockQuantity")
    fun toResponse(productOption: ProductOption): ProductOptionResponse
}
