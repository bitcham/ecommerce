package platform.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import platform.ecommerce.domain.order.Order;
import platform.ecommerce.domain.order.OrderItem;
import platform.ecommerce.domain.order.ShippingAddress;
import platform.ecommerce.dto.response.order.OrderItemResponse;
import platform.ecommerce.dto.response.order.OrderResponse;
import platform.ecommerce.dto.response.order.ShippingAddressResponse;

import java.util.List;

/**
 * MapStruct mapper for Order entity.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "shippingAddress", source = "shippingAddress")
    @Mapping(target = "items", source = "items")
    OrderResponse toResponse(Order order);

    List<OrderResponse> toResponseList(List<Order> orders);

    @Mapping(target = "fullAddress", expression = "java(address != null ? address.getFullAddress() : null)")
    ShippingAddressResponse toShippingAddressResponse(ShippingAddress address);

    @Mapping(target = "subtotal", expression = "java(item.getSubtotal())")
    OrderItemResponse toItemResponse(OrderItem item);

    List<OrderItemResponse> toItemResponseList(List<OrderItem> items);
}
