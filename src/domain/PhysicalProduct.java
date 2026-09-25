package domain;

import datasource.ProductGateway;

/**
 * PhysicalProduct inherits the common product fields from Product.java, it adds
 * one field: private Dimensions dimensions.
 * getDataOutOfGateway() method retrieves the common product fields and the three
 * dimension values from the database gateway.
 * Both Apparel and Electronics will inherit this method
 */
public abstract class PhysicalProduct extends Product {

  private Dimensions dimensions;

  protected PhysicalProduct() {
  }

  protected PhysicalProduct(Dimensions dimensions) {
    this.dimensions = dimensions;
  }

  public Dimensions getDimensions() {
    return dimensions;
  }

  protected void getDataOutOfGateway(ProductGateway gateway) {

    super.getDataOutOfGateway(gateway);

    this.dimensions = new Dimensions(
            gateway.getDimensionWidth(),
            gateway.getDimensionDepth(),
            gateway.getDimensionHeight()
    );
  }

}
