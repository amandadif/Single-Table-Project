package domain;

import datasource.ProductGateway;
import datasource.ProductType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * You can't change this file at all.
 */
public abstract class Product {
    private long id;
    private boolean idHasBeenSet = false;
    private String name;
    private String sku;
    private Cost basePrice;
    private ProductGateway gateway;

    /**
     * You can call this one time to set the id.  After that, it will throw an exception
     *
     * @param generatedId
     */
    void assignId(long generatedId) {
        if (idHasBeenSet) {
            throw new IllegalStateException("Identity Conflict: This product's ID has already been permanently set to " + this.id);
        }
        if (generatedId <= 0) {
            throw new IllegalArgumentException("Invalid ID assignment: " + generatedId);
        }
        this.id = generatedId;
        this.idHasBeenSet = true;
    }

    // This is a map of builders that will let us create domain objects
    private static final Map<ProductType, Function<ProductGateway, ? extends Product>> BUILDERS = Map.of(
            ProductType.AudioTrack, AudioTrack::builder
            // TODO: append others here as they are built
    );

    /**
     * Build the entire catalog of products
     * @return
     */
    public static List<Product> findAll() {
        List<Product> domainCatalog = new ArrayList<>();

        // 1. Call the data source layer to get the raw data rows
        List<ProductGateway> rows = ProductGateway.findAllRows();

        // 2. Map the data source rows to domain objects using our constructor map
        for (ProductGateway row : rows) {
            var builder = BUILDERS.get(row.getType());
            if (builder == null) {
                throw new IllegalStateException("Unknown product type discriminator: " + row.getType());
            }
            domainCatalog.add(builder.apply(row));
        }

        return domainCatalog;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setBasePrice(Cost basePrice) {
        this.basePrice = basePrice;
    }

    public void setGateway(ProductGateway gateway) {
        this.gateway = gateway;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public Cost getBasePrice() {
        return basePrice;
    }

    public ProductGateway getGateway() {
        return gateway;
    }
}
