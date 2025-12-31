package platform.ecommerce.domain.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import platform.ecommerce.exception.InvalidStateException;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Category aggregate.
 */
class CategoryTest {

    private Category rootCategory;
    private static final Long CATEGORY_ID = 1L;

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
        ReflectionTestUtils.setField(rootCategory, "id", CATEGORY_ID);
    }

    @Nested
    @DisplayName("Category Creation")
    class CategoryCreation {

        @Test
        @DisplayName("should create root category with no parent")
        void createRootCategory() {
            // when
            Category category = Category.builder()
                    .name("Fashion")
                    .slug("fashion")
                    .description("Fashion items")
                    .parentId(null)
                    .depth(0)
                    .displayOrder(1)
                    .build();

            // then
            assertThat(category.getName()).isEqualTo("Fashion");
            assertThat(category.getSlug()).isEqualTo("fashion");
            assertThat(category.getParentId()).isNull();
            assertThat(category.getDepth()).isZero();
            assertThat(category.isRoot()).isTrue();
            assertThat(category.isActive()).isTrue();
        }

        @Test
        @DisplayName("should create child category with parent")
        void createChildCategory() {
            // when
            Category child = Category.builder()
                    .name("Smartphones")
                    .slug("smartphones")
                    .description("Mobile phones")
                    .parentId(CATEGORY_ID)
                    .depth(1)
                    .displayOrder(1)
                    .build();

            // then
            assertThat(child.getParentId()).isEqualTo(CATEGORY_ID);
            assertThat(child.getDepth()).isEqualTo(1);
            assertThat(child.isRoot()).isFalse();
        }

        @Test
        @DisplayName("should initialize as active")
        void initializeAsActive() {
            // when
            Category category = Category.builder()
                    .name("Books")
                    .slug("books")
                    .depth(0)
                    .build();

            // then
            assertThat(category.isActive()).isTrue();
            assertThat(category.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("should throw exception for empty name")
        void throwOnEmptyName() {
            // when & then
            assertThatThrownBy(() -> Category.builder()
                    .name("")
                    .slug("test")
                    .depth(0)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("should throw exception for invalid slug")
        void throwOnInvalidSlug() {
            // when & then
            assertThatThrownBy(() -> Category.builder()
                    .name("Test")
                    .slug("Invalid Slug!")
                    .depth(0)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Slug");
        }

        @Test
        @DisplayName("should throw exception when depth exceeds maximum")
        void throwOnExceedingMaxDepth() {
            // when & then
            assertThatThrownBy(() -> Category.builder()
                    .name("Deep Category")
                    .slug("deep")
                    .depth(4)
                    .build())
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("Update Info")
    class UpdateInfo {

        @Test
        @DisplayName("should update name and description")
        void updateNameAndDescription() {
            // when
            rootCategory.updateInfo("Consumer Electronics", "consumer-electronics", "Updated description");

            // then
            assertThat(rootCategory.getName()).isEqualTo("Consumer Electronics");
            assertThat(rootCategory.getSlug()).isEqualTo("consumer-electronics");
            assertThat(rootCategory.getDescription()).isEqualTo("Updated description");
        }

        @Test
        @DisplayName("should throw exception for empty name on update")
        void throwOnEmptyNameUpdate() {
            // when & then
            assertThatThrownBy(() -> rootCategory.updateInfo("", "slug", "desc"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw exception for invalid slug on update")
        void throwOnInvalidSlugUpdate() {
            // when & then
            assertThatThrownBy(() -> rootCategory.updateInfo("Name", "Invalid!", "desc"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should update display order")
        void updateDisplayOrder() {
            // when
            rootCategory.updateDisplayOrder(5);

            // then
            assertThat(rootCategory.getDisplayOrder()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Move Category")
    class MoveCategory {

        @Test
        @DisplayName("should move to new parent")
        void moveToNewParent() {
            // given
            Category child = Category.builder()
                    .name("Laptops")
                    .slug("laptops")
                    .parentId(CATEGORY_ID)
                    .depth(1)
                    .build();
            ReflectionTestUtils.setField(child, "id", 2L);

            // when
            child.moveTo(3L, 2);

            // then
            assertThat(child.getParentId()).isEqualTo(3L);
            assertThat(child.getDepth()).isEqualTo(2);
        }

        @Test
        @DisplayName("should move to root level")
        void moveToRoot() {
            // given
            Category child = Category.builder()
                    .name("Laptops")
                    .slug("laptops")
                    .parentId(CATEGORY_ID)
                    .depth(1)
                    .build();

            // when
            child.moveTo(null, 0);

            // then
            assertThat(child.getParentId()).isNull();
            assertThat(child.getDepth()).isZero();
            assertThat(child.isRoot()).isTrue();
        }

        @Test
        @DisplayName("should throw exception when moving to self")
        void throwOnSelfReference() {
            // when & then
            assertThatThrownBy(() -> rootCategory.moveTo(CATEGORY_ID, 1))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining("own parent");
        }

        @Test
        @DisplayName("should throw exception when depth exceeds maximum")
        void throwOnExceedingMaxDepth() {
            // when & then
            assertThatThrownBy(() -> rootCategory.moveTo(2L, 5))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("Activation")
    class Activation {

        @Test
        @DisplayName("should activate category")
        void activateCategory() {
            // given
            rootCategory.deactivate();
            assertThat(rootCategory.isActive()).isFalse();

            // when
            rootCategory.activate();

            // then
            assertThat(rootCategory.isActive()).isTrue();
        }

        @Test
        @DisplayName("should deactivate category")
        void deactivateCategory() {
            // when
            rootCategory.deactivate();

            // then
            assertThat(rootCategory.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Soft Delete")
    class SoftDelete {

        @Test
        @DisplayName("should mark as deleted")
        void markAsDeleted() {
            // when
            rootCategory.delete();

            // then
            assertThat(rootCategory.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("should set deletedAt timestamp")
        void setDeletedAtTimestamp() {
            // when
            rootCategory.delete();

            // then
            assertThat(rootCategory.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Slug Validation")
    class SlugValidation {

        @Test
        @DisplayName("should accept valid slug with lowercase letters")
        void acceptLowercaseSlug() {
            // when
            Category category = Category.builder()
                    .name("Test")
                    .slug("valid-slug")
                    .depth(0)
                    .build();

            // then
            assertThat(category.getSlug()).isEqualTo("valid-slug");
        }

        @Test
        @DisplayName("should accept valid slug with numbers")
        void acceptSlugWithNumbers() {
            // when
            Category category = Category.builder()
                    .name("Test")
                    .slug("category-123")
                    .depth(0)
                    .build();

            // then
            assertThat(category.getSlug()).isEqualTo("category-123");
        }

        @Test
        @DisplayName("should reject slug with uppercase letters")
        void rejectUppercaseSlug() {
            // when & then
            assertThatThrownBy(() -> Category.builder()
                    .name("Test")
                    .slug("Invalid")
                    .depth(0)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should reject slug with spaces")
        void rejectSlugWithSpaces() {
            // when & then
            assertThatThrownBy(() -> Category.builder()
                    .name("Test")
                    .slug("invalid slug")
                    .depth(0)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
