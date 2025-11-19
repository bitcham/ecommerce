package platform.ecommerce.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import platform.ecommerce.domain.Product
import platform.ecommerce.dto.response.ProductDetailResponse
import platform.ecommerce.dto.response.ProductListResponse


@Mapper(uses = [ProductOptionMapper::class])
interface ProductMapper {


    @Mapping(target = "price", expression = "java(product.getPrice().getAmount().doubleValue())")
    @Mapping(target = "status", source = "productStatus")
    fun toListResponse(product: Product): ProductListResponse

    @Mapping(target = "price", expression = "java(product.getPrice().getAmount().doubleValue())")
    @Mapping(target = "status", source = "productStatus")
    fun toDetailResponse(product: Product): ProductDetailResponse

}