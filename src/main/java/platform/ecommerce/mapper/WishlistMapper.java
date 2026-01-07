package platform.ecommerce.mapper;

import org.mapstruct.Mapper;
import platform.ecommerce.domain.wishlist.Wishlist;
import platform.ecommerce.dto.response.wishlist.WishlistResponse;

import java.util.List;

/**
 * MapStruct mapper for Wishlist entity.
 */
@Mapper(componentModel = "spring")
public interface WishlistMapper {

    WishlistResponse toResponse(Wishlist wishlist);

    List<WishlistResponse> toResponseList(List<Wishlist> wishlists);
}
