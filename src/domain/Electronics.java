package domain;

import datasource.DatabaseException;
import datasource.ProductGateway;
import datasource.ProductType;

import java.util.ArrayList;
import java.util.List;
import datasource.InvalidArgumentException;

/**
 * completes the six new domain files
 *
 * Electronics class has three main pieces of information:
 * The inherited dimensions, Its Voltage enum, Its list of supported VideoStreaming services.
 * We store the services in an ArrayList and use the relationship table to save their IDs.
 */
public class Electronics extends PhysicalProduct {
  private Voltage voltage;

  private ArrayList<VideoStreaming> supportedServices;

  // Create constructor
  public Electronics(String sku, String name,
                     double basePrice,
                     Dimensions dimensions,
                     Voltage voltage,
                     ArrayList<VideoStreaming> supportedServices)
          throws DatabaseException {

    super(dimensions);

    ProductGateway gateway = new ProductGateway(
            ProductType.Electronics,
            sku,
            name,
            basePrice,
            null,       // size
            null,       // hasLyrics
            null,       // singleCodec
            null,       // codecs
            null,       // hasSubtitles
            dimensions,
            null,       // apparelSize
            voltage,
            supportedServices
    );

    assignId(gateway.getId());

    getDataOutOfGateway(gateway);
  }

  // Finder constructor
  private Electronics() {
    super();
  }

  // Find an existing Electronics product
  public static Electronics findElectronics(long id)
          throws DatabaseException {

    return ProductGateway.findAndBuild(
            id,
            Electronics::builder
    );
  }

  // Build an Electronics object from the gateway
  public static Electronics builder(ProductGateway gateway) {

    if (gateway.getType() != ProductType.Electronics) {
      throw new DatasourceTypeMismatch();
    }

    Electronics electronics = new Electronics();

    electronics.getDataOutOfGateway(gateway);

    return electronics;
  }

  @Override
  protected void getDataOutOfGateway(ProductGateway gateway) {

    super.getDataOutOfGateway(gateway);

    this.voltage = gateway.getVoltage();

    this.supportedServices =
            gateway.getSupportedStreamingServices();
  }

  public Voltage getVoltage() {
    return voltage;
  }

  public ArrayList<VideoStreaming> getSupportedServices() {
    return supportedServices;
  }

  public static List<Electronics> findAllThatSupport(
          int videoStreamingId)
          throws DatabaseException, InvalidArgumentException {

    return ProductGateway.findAllThatSupport(
            videoStreamingId,
            Electronics::builder
    );
  }

}
