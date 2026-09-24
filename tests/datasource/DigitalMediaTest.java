package datasource;

import domain.AudioCodec;
import domain.AudioTrack;
import domain.DigitalMedia;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DigitalMediaTest {
  private static final Connection conn;

  static {
    try {
      conn = DatabaseRegistry.getConnection();
      assert !conn.isClosed();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeAll
  public static void setUpDB() throws DatabaseException {

//        ProductGateway.createTable();
  }

  @AfterEach
  public void rollback() throws SQLException {
    conn.rollback();
  }

  @Test
  public void createDigitalMediaObject() throws DatabaseException, SQLException {
    DigitalMedia digitalMedia = new DigitalMedia("sku", "name", 12.99, 1);
    //conn.commit();
    assertEquals("sku", digitalMedia.getSku());
    assertEquals("name", digitalMedia.getName());
    assertEquals(12.99, digitalMedia.getBasePrice());
    assertEquals(1, digitalMedia.getSize());
  }

  @Test
  public void findAndBuildDigitalMediaObject() throws DatabaseException, SQLException {
    DigitalMedia digitalMedia = new DigitalMedia("sku", "name", 12.99, 2);
    //conn.commit();

    DigitalMedia digitalMediaCopy = digitalMedia.findDigitalMedia(digitalMedia.getId());

    assertEquals(digitalMediaCopy.getSku(), digitalMedia.getSku());
    assertEquals(digitalMedia.getName(), digitalMediaCopy.getName());
    assertEquals(digitalMedia.getBasePrice(), digitalMediaCopy.getBasePrice());
    assertEquals(digitalMedia.getSize(), digitalMediaCopy.getSize());
  }
}
