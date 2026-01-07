package platform.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import platform.ecommerce.domain.product.Product;
import platform.ecommerce.domain.product.ProductImage;
import platform.ecommerce.domain.product.ProductOption;
import platform.ecommerce.dto.response.product.ProductDetailResponse;
import platform.ecommerce.dto.response.product.ProductImageResponse;
import platform.ecommerce.dto.response.product.ProductOptionResponse;
import platform.ecommerce.dto.response.product.ProductResponse;

import java.util.List;

/**
 * MapStruct mapper for Product entity.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "mainImageUrl", source = "product", qualifiedByName = "mapMainImageUrl")
    ProductResponse toResponse(Product product);

    List<ProductResponse> toResponseList(List<Product> products);

    @Mapping(target = "options", source = "options")
    @Mapping(target = "images", source = "images")
    ProductDetailResponse toDetailResponse(Product product);

    @Mapping(target = "inStock", expression = "java(option.isInStock())")
    ProductOptionResponse toOptionResponse(ProductOption option);

    List<ProductOptionResponse> toOptionResponseList(List<ProductOption> options);

    ProductImageResponse toImageResponse(ProductImage image);

    List<ProductImageResponse> toImageResponseList(List<ProductImage> images);

    @Named("mapMainImageUrl")
    default String mapMainImageUrl(Product product) {
        ProductImage mainImage = product.getMainImage();
        return mainImage != null ? mainImage.getImageUrl() : null;
    }
}
