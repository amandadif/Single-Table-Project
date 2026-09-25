
package datasource;

import domain.AudioCodec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ProductGatewayTest
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
    public static void setUpDB() throws  DatabaseException {

//        ProductGateway.createTable();
    }

    @AfterEach
    public void rollback() throws SQLException
    {
        //conn.rollback();
    }

    @Test
    public void SingleCodecBitmaskTest() throws DatabaseException {
        Set<AudioCodec> codecs = Set.of(AudioCodec.MP3);
        ProductGateway gateway = new ProductGateway(ProductType.AudioTrack, "sku", "name", 2.22, 0, false, null, codecs, null, null);
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

    @Test
    @Disabled("Fix and run this only when you change the structure of the table")
    public void canCreateTable() throws DatabaseException {
        ProductGateway.createTable();

        try(Statement stmt = conn.createStatement())
        {
            // Create the table
            ResultSet tables = stmt.executeQuery("SELECT name FROM sqlite_master " + "WHERE " + "type='table' AND name NOT LIKE 'sqlite_%'");

            boolean tableExists = false;
            while (tables.next() && !tableExists)
            {
                String tableName = tables.getString("name");
                if (tableName.equals("products"))
                {
                    tableExists = true;
                }
            }
            assert (tableExists);
            stmt.execute("COMMIT;");
        } catch(SQLException e){
            fail("SQL Exception" + e.getMessage());
        }

    }}
/*
    @Test
    public void canInsertAndRetrieveAudioTrack() throws DatabaseException, SQLException {
        ProductGateway gateway = new ProductGateway(ProductType.AudioTrack, "sku", "name", 2.22,
                0, false, Set.of(AudioCodec.MP3), null);
        // now retrieve it and make sure it has all of its stuff
        AudioTrack track = ProductGateway.findAndBuild(gateway.getId(), AudioTrack::builder);
        assertEquals("sku", track.getSku());
        assertEquals("name", track.getName());
        assertEquals(2.22, track.getBasePrice(),0.001);
        assertFalse(track.hasLyrics());
        assertEquals(AudioCodec.MP3, track.getCodec());

    }
    }
 */
