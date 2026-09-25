package domain;

import datasource.DatabaseException;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class PhysicalProductTest {

  /**
   * Apparel can be created and inserted into SQLite
   * @throws DatabaseException
   */
  @Test
  public void createApparelTest()
          throws DatabaseException {

    Dimensions dimensions =
            new Dimensions(10, 20, 30);

    Apparel apparel = new Apparel(
            "A001",
            "Blue Shirt",
            29.99,
            dimensions,
            ApparelSize.MEDIUM
    );

    assertTrue(apparel.getId() > 0);

    assertEquals("A001", apparel.getSku());

    assertEquals("Blue Shirt", apparel.getName());

    assertEquals(29.99, apparel.getBasePrice());

    assertEquals(10,
            apparel.getDimensions().getWidth());

    assertEquals(20,
            apparel.getDimensions().getDepth());

    assertEquals(30,
            apparel.getDimensions().getHeight());

    assertEquals(ApparelSize.MEDIUM,
            apparel.getSize());
  }

  /**
   * Apparel can be retrieved and rebuilt correctly
   * @throws DatabaseException
   */
  @Test
  public void findApparelTest()
          throws DatabaseException {

    Apparel apparel = new Apparel(
            "A002",
            "Black Hoodie",
            49.99,
            new Dimensions(15, 10, 5),
            ApparelSize.LARGE
    );

    Apparel copy =
            Apparel.findApparel(apparel.getId());

    assertEquals(apparel.getId(), copy.getId());

    assertEquals(apparel.getSku(), copy.getSku());

    assertEquals(apparel.getName(), copy.getName());

    assertEquals(apparel.getSize(), copy.getSize());

    assertEquals(
            apparel.getDimensions().getWidth(),
            copy.getDimensions().getWidth()
    );
  }

  /**
   * Electronics can be created and inserted correctly
   * @throws DatabaseException
   */
  @Test
  public void createElectronicsTest()
          throws DatabaseException {

    Electronics electronics = new Electronics(
            "E001",
            "Smart TV",
            499.99,
            new Dimensions(50, 30, 5),
            Voltage.V_110,
            new ArrayList<>()
    );

    assertTrue(electronics.getId() > 0);

    assertEquals("Smart TV",
            electronics.getName());

    assertEquals(Voltage.V_110,
            electronics.getVoltage());

    assertEquals(50,
            electronics.getDimensions().getWidth());

    assertTrue(
            electronics.getSupportedServices().isEmpty()
    );
  }

  /**
   * Electronics can be retrieved and rebuilt correctly
   * @throws DatabaseException
   */
  @Test
  public void findElectronicsTest()
          throws DatabaseException {

    Electronics electronics = new Electronics(
            "E002",
            "Laptop",
            899.99,
            new Dimensions(15, 10, 1),
            Voltage.UNIVERSAL,
            new ArrayList<>()
    );

    Electronics copy =
            Electronics.findElectronics(
                    electronics.getId()
            );

    assertEquals(electronics.getId(),
            copy.getId());

    assertEquals(electronics.getSku(),
            copy.getSku());

    assertEquals(electronics.getVoltage(),
            copy.getVoltage());

    assertEquals(
            electronics.getDimensions().getWidth(),
            copy.getDimensions().getWidth()
    );
  }
}
