package domain;

import datasource.DatabaseException;
import datasource.ProductGateway;
import datasource.ProductType;
import java.util.List;

/**
 * The constructor creates a ProductGateway, which inserts the shirt into the database.
 * The gateway retrieves the generated ID, and assignId() assigns it to your Apparel object.
 *
 * Example of how to create Apparel:
 * Apparel shirt = new Apparel(
 *         "SHIRT001",
 *         "Blue Shirt",
 *         29.99,
 *         new Dimensions(10, 5, 2),
 *         ApparelSize.MEDIUM
 *      );
 */
public class Apparel extends PhysicalProduct {

  private ApparelSize size;

  // Create constructor
  public Apparel(String sku, String name, double basePrice, Dimensions dimensions,
                 ApparelSize size) throws DatabaseException {

    super(dimensions);

    ProductGateway gateway = new ProductGateway(
            ProductType.Apparel,
            sku,
            name,
            basePrice,
            null,       // size
            null,       // hasLyrics
            null,       // singleCodec
            null,       // codecs
            null,       // hasSubtitles
            dimensions,
            size,
            null,       // voltage
            null        // supportedStreamingServices
    );
    assignId(gateway.getId());
    getDataOutOfGateway(gateway);
  }

  // Finder constructor
  private Apparel() {
    super();
  }

  // Find an existing Apparel product
  public static Apparel findApparel(long id)
          throws DatabaseException {

    return ProductGateway.findAndBuild(
            id,
            Apparel::builder
    );
  }

  // Build an Apparel object from the database
  public static Apparel builder(ProductGateway gateway) {

    if (gateway.getType() != ProductType.Apparel) {
      throw new DatasourceTypeMismatch();
    }
    Apparel apparel = new Apparel();
    apparel.getDataOutOfGateway(gateway);

    return apparel;
  }

  public static List<Apparel> findApparelWithSize(ApparelSize size)
          throws DatabaseException {

    return ProductGateway.findApparelWithSize(
            size.ordinal(),
            Apparel::builder
    );
  }

  @Override
  protected void getDataOutOfGateway(ProductGateway gateway) {

    super.getDataOutOfGateway(gateway);
    this.size = gateway.getApparelSize();
  }

  public ApparelSize getSize() {
    return size;
  }

}
