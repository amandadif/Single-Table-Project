/*
package domain;

import datasource.DatabaseException;
import domain.AudioCodec;
import datasource.DatabaseRegistry;
import domain.AudioTrack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AudioTrackTest
{
  private static final Connection conn;


  static
  {
    try
    {
      conn = DatabaseRegistry.getConnection();
      assert !conn.isClosed();
    } catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
  }

  @BeforeAll
  public static void setUpDB() throws DatabaseException {

//        ProductGateway.createTable();
  }

  @AfterEach
  public void rollback() throws SQLException
  {
    conn.rollback();
  }

  @Test
  public void SingleCodecBitmaskTest() throws DatabaseException {
    Set<AudioCodec> codecs = Set.of(AudioCodec.MP3);
    ProductGateway gateway = new ProductGateway(ProductType.AudioTrack, "sku", "name", 2.22, 0, false, codecs, null);
    int expectedBitmask = 1;
    int actualBitmask = gateway.calculateBitmask(codecs);
    assertEquals(expectedBitmask, actualBitmask);
    codecs = Set.of(AudioCodec.AAC);
    expectedBitmask = 2;
    actualBitmask = gateway.calculateBitmask(codecs);
    assertEquals(expectedBitmask, actualBitmask);
    codecs = Set.of(AudioCodec.FLAC);
    expectedBitmask = 4;
    actualBitmask = gateway.calculateBitmask(codecs);
    assertEquals(expectedBitmask, actualBitmask);
    codecs = Set.of(AudioCodec.WAV);
    expectedBitmask = 8;
    actualBitmask = gateway.calculateBitmask(codecs);
    assertEquals(expectedBitmask, actualBitmask);
  }



}

 */
