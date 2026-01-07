package platform.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import platform.ecommerce.domain.review.Review;
import platform.ecommerce.dto.response.review.ReviewResponse;

import java.util.List;

/**
 * MapStruct mapper for Review entity.
 */
@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "verified", expression = "java(review.isVerified())")
    ReviewResponse toResponse(Review review);

    List<ReviewResponse> toResponseList(List<Review> reviews);
}
