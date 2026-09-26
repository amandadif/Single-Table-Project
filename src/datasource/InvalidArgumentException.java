package datasource;


public class InvalidArgumentException extends Throwable {
  public InvalidArgumentException(String s) {
    System.err.println("Invalid argument: " + s);
  }
}
