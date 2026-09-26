package domain;

import datasource.DatabaseException;
import datasource.ProductGateway;
import datasource.ProductType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 */
public abstract class Product {
    private long id;
    private boolean idHasBeenSet = false;
    private String name;
    private String sku;
    private double basePrice;
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
            ProductType.AudioTrack, AudioTrack::builder,
            ProductType.VideoStreaming, VideoStreaming::builder,
            ProductType.Apparel, Apparel::builder,
            ProductType.Electronics, Electronics::builder


            // TODO: append others here as they are built
    );

    /**
     * Build the entire catalog of products
     * @return
     */
    public static List<Product> findAll() throws DatabaseException {
        //List<Product> domainCatalog = new ArrayList<>();

        return ProductGateway.findAllRows(BUILDERS);
    }

    public static List<Product> findBySkuPrefix(String prefix)
            throws DatabaseException {

        List<ProductGateway> gateways =
                ProductGateway.findBySkuPrefix(prefix);

        List<Product> products = new ArrayList<>();

        for (ProductGateway gateway : gateways) {
            Function<ProductGateway, ? extends Product> builder =
                    BUILDERS.get(gateway.getType());

            products.add(builder.apply(gateway));
        }

        return products;
    }

    protected void getDataOutOfGateway(ProductGateway gateway)
    {
        this.id = gateway.getId();
        this.sku = gateway.getSku();
        this.name = gateway.getName();
        this.basePrice = gateway.getBasePrice();
        this.gateway = gateway;
    }



    public void setName(String name) {
        this.name = name;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setBasePrice(double basePrice) {
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

    public double getBasePrice() {
        return basePrice;
    }

    public ProductGateway getGateway() {
        return gateway;
    }
}
