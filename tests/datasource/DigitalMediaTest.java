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
    //conn.rollback();
  }

  @Test
  public void createDigitalMediaObject() throws DatabaseException, SQLException {
    DigitalMedia digitalMedia = new DigitalMedia("sku", "name", 12.99, 0);
    conn.commit();
  }
}
