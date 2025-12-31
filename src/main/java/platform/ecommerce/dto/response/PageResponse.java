package platform.ecommerce.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Paginated response wrapper.
 */
@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final PageInfo page;

    @Builder
    private PageResponse(List<T> content, PageInfo page) {
        this.content = content;
        this.page = page;
    }

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(PageInfo.of(page))
                .build();
    }

    public static <T, R> PageResponse<R> of(Page<T> page, Function<T, R> converter) {
        return PageResponse.<R>builder()
                .content(page.getContent().stream().map(converter).toList())
                .page(PageInfo.of(page))
                .build();
    }

    public static <T> PageResponse<T> of(List<T> content, int totalElements, int pageNumber, int pageSize) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        return PageResponse.<T>builder()
                .content(content)
                .page(PageInfo.builder()
                        .number(pageNumber)
                        .size(pageSize)
                        .totalElements(totalElements)
                        .totalPages(totalPages)
                        .first(pageNumber == 0)
                        .last(pageNumber >= totalPages - 1)
                        .hasNext(pageNumber < totalPages - 1)
                        .hasPrevious(pageNumber > 0)
                        .build())
                .build();
    }

    @Getter
    @Builder
    public static class PageInfo {
        private final int number;
        private final int size;
        private final long totalElements;
        private final int totalPages;
        private final boolean first;
        private final boolean last;
        private final boolean hasNext;
        private final boolean hasPrevious;

        public static <T> PageInfo of(Page<T> page) {
            return PageInfo.builder()
                    .number(page.getNumber())
                    .size(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .first(page.isFirst())
                    .last(page.isLast())
                    .hasNext(page.hasNext())
                    .hasPrevious(page.hasPrevious())
                    .build();
        }
    }
}
