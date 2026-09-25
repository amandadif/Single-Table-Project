package domain;

/**
 * Create dimensions like this: Dimensions dimensions = new Dimensions(10, 20, 30);
 * program will have one Dimensions object, but database will store its three values in separate columns
 */
public class Dimensions {

  private double width;
  private double depth;
  private double height;

  public Dimensions(double width, double depth, double height) {
    this.width = width;
    this.depth = depth;
    this.height = height;
  }

  public double getWidth() {
    return width;
  }

  public double getDepth() {
    return depth;
  }

  public double getHeight() {
    return height;
  }
}
