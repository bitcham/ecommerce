package platform.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import platform.ecommerce.domain.category.Category;
import platform.ecommerce.dto.request.category.CategoryCreateRequest;
import platform.ecommerce.dto.request.category.CategoryUpdateRequest;
import platform.ecommerce.dto.response.category.CategoryResponse;
import platform.ecommerce.dto.response.category.CategoryTreeResponse;
import platform.ecommerce.exception.EntityNotFoundException;
import platform.ecommerce.exception.InvalidStateException;
import platform.ecommerce.repository.category.CategoryRepository;
import platform.ecommerce.service.category.CategoryServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for CategoryService.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category rootCategory;
    private Category childCategory;
    private static final Long ROOT_ID = 1L;
    private static final Long CHILD_ID = 2L;

    @BeforeEach
    void setUp() {
        rootCategory = Category.builder()
                .name("Electronics")
                .slug("electronics")
                .description("Electronic devices")
                .parentId(null)
                .depth(0)
                .displayOrder(1)
                .build();
        ReflectionTestUtils.setField(rootCategory, "id", ROOT_ID);

        childCategory = Category.builder()
                .name("Smartphones")
                .slug("smartphones")
                .description("Mobile phones")
                .parentId(ROOT_ID)
                .depth(1)
                .displayOrder(1)
                .build();
        ReflectionTestUtils.setField(childCategory, "id", CHILD_ID);
    }

    @Nested
    @DisplayName("createCategory")
    class CreateCategory {

        @Test
        @DisplayName("should create root category")
        void createRootCategory() {
            // given
            CategoryCreateRequest request = CategoryCreateRequest.builder()
                    .name("Fashion")
                    .slug("fashion")
                    .description("Fashion items")
                    .parentId(null)
                    .displayOrder(2)
                    .build();

            given(categoryRepository.findBySlug("fashion")).willReturn(Optional.empty());
            given(categoryRepository.save(any(Category.class))).willAnswer(invocation -> {
                Category category = invocation.getArgument(0);
                ReflectionTestUtils.setField(category, "id", 3L);
                return category;
            });
            given(categoryRepository.countChildren(anyLong())).willReturn(0);

            // when
            CategoryResponse response = categoryService.createCategory(request);

            // then
            assertThat(response.name()).isEqualTo("Fashion");
            assertThat(response.slug()).isEqualTo("fashion");
            assertThat(response.depth()).isZero();
            assertThat(response.parentId()).isNull();
        }

        @Test
        @DisplayName("should create child category with correct depth")
        void createChildCategory() {
            // given
            CategoryCreateRequest request = CategoryCreateRequest.builder()
                    .name("Laptops")
                    .slug("laptops")
                    .parentId(ROOT_ID)
                    .displayOrder(1)
                    .build();

            given(categoryRepository.findBySlug("laptops")).willReturn(Optional.empty());
            given(categoryRepository.findById(ROOT_ID)).willReturn(Optional.of(rootCategory));
            given(categoryRepository.save(any(Category.class))).willAnswer(invocation -> {
                Category category = invocation.getArgument(0);
                ReflectionTestUtils.setField(category, "id", 3L);
                return category;
            });
            given(categoryRepository.countChildren(anyLong())).willReturn(0);

            // when
            CategoryResponse response = categoryService.createCategory(request);

            // then
            assertThat(response.depth()).isEqualTo(1);
            assertThat(response.parentId()).isEqualTo(ROOT_ID);
        }

        @Test
        @DisplayName("should throw exception when slug already exists")
        void throwOnDuplicateSlug() {
            // given
            CategoryCreateRequest request = CategoryCreateRequest.builder()
                    .name("Electronics Duplicate")
                    .slug("electronics")
                    .displayOrder(1)
                    .build();

            given(categoryRepository.findBySlug("electronics")).willReturn(Optional.of(rootCategory));

            // when & then
            assertThatThrownBy(() -> categoryService.createCategory(request))
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("should throw exception when parent not found")
        void throwOnParentNotFound() {
            // given
            CategoryCreateRequest request = CategoryCreateRequest.builder()
                    .name("Child")
                    .slug("child")
                    .parentId(999L)
                    .displayOrder(1)
                    .build();

            given(categoryRepository.findBySlug("child")).willReturn(Optional.empty());
            given(categoryRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> categoryService.createCategory(request))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getCategory")
    class GetCategory {

        @Test
        @DisplayName("should return category by id")
        void returnCategoryById() {
            // given
            given(categoryRepository.findById(ROOT_ID)).willReturn(Optional.of(rootCategory));
            given(categoryRepository.countChildren(ROOT_ID)).willReturn(1);

            // when
            CategoryResponse response = categoryService.getCategory(ROOT_ID);

            // then
            assertThat(response.id()).isEqualTo(ROOT_ID);
            assertThat(response.name()).isEqualTo("Electronics");
            assertThat(response.childCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw exception when not found")
        void throwOnNotFound() {
            // given
            given(categoryRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> categoryService.getCategory(999L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getCategoryTree")
    class GetCategoryTree {

        @Test
        @DisplayName("should return hierarchical tree structure")
        void returnTreeStructure() {
            // given
            given(categoryRepository.findAllActive()).willReturn(List.of(rootCategory, childCategory));

            // when
            List<CategoryTreeResponse> tree = categoryService.getCategoryTree();

            // then
            assertThat(tree).hasSize(1);
            assertThat(tree.get(0).name()).isEqualTo("Electronics");
            assertThat(tree.get(0).children()).hasSize(1);
            assertThat(tree.get(0).children().get(0).name()).isEqualTo("Smartphones");
        }
    }

    @Nested
    @DisplayName("getRootCategories")
    class GetRootCategories {

        @Test
        @DisplayName("should return only root level categories")
        void returnRootCategories() {
            // given
            given(categoryRepository.findRootCategories()).willReturn(List.of(rootCategory));
            given(categoryRepository.countChildren(ROOT_ID)).willReturn(1);

            // when
            List<CategoryResponse> categories = categoryService.getRootCategories();

            // then
            assertThat(categories).hasSize(1);
            assertThat(categories.get(0).parentId()).isNull();
        }
    }

    @Nested
    @DisplayName("getChildren")
    class GetChildren {

        @Test
        @DisplayName("should return direct children only")
        void returnDirectChildren() {
            // given
            given(categoryRepository.findByParentId(ROOT_ID)).willReturn(List.of(childCategory));
            given(categoryRepository.countChildren(CHILD_ID)).willReturn(0);

            // when
            List<CategoryResponse> children = categoryService.getChildren(ROOT_ID);

            // then
            assertThat(children).hasSize(1);
            assertThat(children.get(0).parentId()).isEqualTo(ROOT_ID);
        }

        @Test
        @DisplayName("should return empty list for leaf category")
        void returnEmptyForLeaf() {
            // given
            given(categoryRepository.findByParentId(CHILD_ID)).willReturn(List.of());

            // when
            List<CategoryResponse> children = categoryService.getChildren(CHILD_ID);

            // then
            assertThat(children).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateCategory")
    class UpdateCategory {

        @Test
        @DisplayName("should update category info")
        void updateCategoryInfo() {
            // given
            CategoryUpdateRequest request = CategoryUpdateRequest.builder()
                    .name("Consumer Electronics")
                    .slug("consumer-electronics")
                    .description("Updated description")
                    .displayOrder(2)
                    .build();

            given(categoryRepository.findById(ROOT_ID)).willReturn(Optional.of(rootCategory));
            given(categoryRepository.findBySlug("consumer-electronics")).willReturn(Optional.empty());
            given(categoryRepository.countChildren(ROOT_ID)).willReturn(1);

            // when
            CategoryResponse response = categoryService.updateCategory(ROOT_ID, request);

            // then
            assertThat(response.name()).isEqualTo("Consumer Electronics");
            assertThat(response.slug()).isEqualTo("consumer-electronics");
        }

        @Test
        @DisplayName("should allow same slug on update")
        void allowSameSlugOnUpdate() {
            // given
            CategoryUpdateRequest request = CategoryUpdateRequest.builder()
                    .name("Updated Name")
                    .slug("electronics")
                    .description("Updated")
                    .displayOrder(1)
                    .build();

            given(categoryRepository.findById(ROOT_ID)).willReturn(Optional.of(rootCategory));
            given(categoryRepository.findBySlug("electronics")).willReturn(Optional.of(rootCategory));
            given(categoryRepository.countChildren(ROOT_ID)).willReturn(0);

            // when
            CategoryResponse response = categoryService.updateCategory(ROOT_ID, request);

            // then
            assertThat(response.slug()).isEqualTo("electronics");
        }
    }

    @Nested
    @DisplayName("moveCategory")
    class MoveCategory {

        @Test
        @DisplayName("should move category to new parent")
        void moveCategoryToNewParent() {
            // given
            Category anotherRoot = Category.builder()
                    .name("Fashion")
                    .slug("fashion")
                    .depth(0)
                    .build();
            ReflectionTestUtils.setField(anotherRoot, "id", 3L);

            given(categoryRepository.findById(CHILD_ID)).willReturn(Optional.of(childCategory));
            given(categoryRepository.findById(3L)).willReturn(Optional.of(anotherRoot));
            given(categoryRepository.countChildren(CHILD_ID)).willReturn(0);

            // when
            CategoryResponse response = categoryService.moveCategory(CHILD_ID, 3L);

            // then
            assertThat(response.parentId()).isEqualTo(3L);
            assertThat(response.depth()).isEqualTo(1);
        }

        @Test
        @DisplayName("should move category to root level")
        void moveCategoryToRoot() {
            // given
            given(categoryRepository.findById(CHILD_ID)).willReturn(Optional.of(childCategory));
            given(categoryRepository.countChildren(CHILD_ID)).willReturn(0);

            // when
            CategoryResponse response = categoryService.moveCategory(CHILD_ID, null);

            // then
            assertThat(response.parentId()).isNull();
            assertThat(response.depth()).isZero();
        }
    }

    @Nested
    @DisplayName("deleteCategory")
    class DeleteCategory {

        @Test
        @DisplayName("should delete category without children")
        void deleteCategoryWithoutChildren() {
            // given
            given(categoryRepository.findById(CHILD_ID)).willReturn(Optional.of(childCategory));
            given(categoryRepository.hasChildren(CHILD_ID)).willReturn(false);

            // when
            categoryService.deleteCategory(CHILD_ID);

            // then
            assertThat(childCategory.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("should throw exception when category has children")
        void throwOnHasChildren() {
            // given
            given(categoryRepository.findById(ROOT_ID)).willReturn(Optional.of(rootCategory));
            given(categoryRepository.hasChildren(ROOT_ID)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> categoryService.deleteCategory(ROOT_ID))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("activate/deactivate")
    class ActivateDeactivate {

        @Test
        @DisplayName("should activate category")
        void activateCategory() {
            // given
            childCategory.deactivate();
            given(categoryRepository.findById(CHILD_ID)).willReturn(Optional.of(childCategory));
            given(categoryRepository.countChildren(CHILD_ID)).willReturn(0);

            // when
            CategoryResponse response = categoryService.activateCategory(CHILD_ID);

            // then
            assertThat(response.active()).isTrue();
        }

        @Test
        @DisplayName("should deactivate category")
        void deactivateCategory() {
            // given
            given(categoryRepository.findById(CHILD_ID)).willReturn(Optional.of(childCategory));
            given(categoryRepository.countChildren(CHILD_ID)).willReturn(0);

            // when
            CategoryResponse response = categoryService.deactivateCategory(CHILD_ID);

            // then
            assertThat(response.active()).isFalse();
        }
    }
}
