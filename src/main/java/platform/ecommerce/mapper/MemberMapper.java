package platform.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import platform.ecommerce.domain.member.Member;
import platform.ecommerce.domain.member.MemberAddress;
import platform.ecommerce.dto.response.AddressResponse;
import platform.ecommerce.dto.response.MemberDetailResponse;
import platform.ecommerce.dto.response.MemberResponse;

import java.util.List;

/**
 * MapStruct mapper for Member entity.
 */
@Mapper(componentModel = "spring")
public interface MemberMapper {

    @Mapping(target = "emailVerified", source = "emailVerified")
    MemberResponse toResponse(Member member);

    List<MemberResponse> toResponseList(List<Member> members);

    @Mapping(target = "addresses", source = "addresses")
    @Mapping(target = "defaultAddress", source = "member", qualifiedByName = "mapDefaultAddress")
    @Mapping(target = "emailVerified", source = "emailVerified")
    MemberDetailResponse toDetailResponse(Member member);

    @Mapping(target = "fullAddress", expression = "java(address.getFullAddress())")
    @Mapping(target = "isDefault", source = "default")
    AddressResponse toAddressResponse(MemberAddress address);

    List<AddressResponse> toAddressResponseList(List<MemberAddress> addresses);

    @Named("mapDefaultAddress")
    default AddressResponse mapDefaultAddress(Member member) {
        MemberAddress defaultAddress = member.getDefaultAddress();
        return defaultAddress != null ? toAddressResponse(defaultAddress) : null;
    }
}
