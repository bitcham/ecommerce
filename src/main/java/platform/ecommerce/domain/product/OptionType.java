package platform.ecommerce.domain.product;

/**
 * Product option type enumeration.
 */
public enum OptionType {
    COLOR("Color variation"),
    SIZE("Size variation"),
    MATERIAL("Material variation"),
    STYLE("Style variation"),
    OTHER("Other variation");

    private final String description;

    OptionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
