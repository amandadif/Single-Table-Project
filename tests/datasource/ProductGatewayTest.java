
package datasource;

import domain.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ProductGatewayTest {
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
    @Disabled("Fix and run this only when you change the structure of the table")
    public void canCreateTable() throws DatabaseException {
        ProductGateway.createTable();

        try (Statement stmt = conn.createStatement()) {
            // Create the table
            ResultSet tables = stmt.executeQuery("SELECT name FROM sqlite_master " + "WHERE " + "type='table' AND name NOT LIKE 'sqlite_%'");

            boolean productTableExists = false;
            boolean electronicsTableExists = false;
            boolean bothTablesExists = false;
            while (tables.next() && !bothTablesExists) {
                String tableName = tables.getString("name");
                if (tableName.equals("products")) {
                    productTableExists = true;
                }
                if (tableName.equals("ELECTRONICS_SUPPORTED_SERVICE")) {
                    electronicsTableExists = true;
                }

                if (productTableExists && electronicsTableExists) {
                    bothTablesExists = true;
                }
            }

            assert (bothTablesExists);
            stmt.execute("COMMIT;");
        } catch (SQLException e) {
            fail("SQL Exception" + e.getMessage());
        }

    }

    @Test
    void findAndBuildUsesInjectedBuilder()
            throws Exception {

        AudioTrack track =
                new AudioTrack(
                        "BUILD-001",
                        "Builder Test",
                        10,
                        1,
                        true,
                        AudioCodec.values()[0]
                );

        String result =
                ProductGateway.findAndBuild(
                        track.getId(),
                        gateway -> gateway.getSku()
                );

        assertEquals(
                "BUILD-001",
                result
        );
    }


    //find all

    @Test
    void productFindAllReturnsAllProducts()
            throws Exception {

        AudioTrack audio = new AudioTrack("AUDIO", "Audio", 5, 1, true, AudioCodec.values()[0]);
        VideoStreaming video = new VideoStreaming("VIDEO", "Video", 10, 1000, true, Set.of(AudioCodec.values()[0]));

        Apparel apparel = new Apparel("APPAREL", "Shirt", 20, new Dimensions(1, 2, 3), ApparelSize.values()[0]);
        Electronics electronics = new Electronics("ELECTRONICS", "TV", 500, new Dimensions(4, 5, 6), Voltage.V_110, null);

        List<Product> products = Product.findAll();

        assertEquals(4, products.size()); //gets amount

        //checks ids
        assertTrue(products.stream().anyMatch(p -> p.getId() == audio.getId()));
        assertTrue(products.stream().anyMatch(p -> p.getId() == video.getId()));
        assertTrue(products.stream().anyMatch(p -> p.getId() == apparel.getId()));
        assertTrue(products.stream().anyMatch(p -> p.getId() == electronics.getId()));

        //Checks theres one of each type
        assertTrue(products.stream().anyMatch(p -> p instanceof AudioTrack));
        assertTrue(products.stream().anyMatch(p -> p instanceof VideoStreaming));
        assertTrue(products.stream().anyMatch(p -> p instanceof Apparel));
        assertTrue(products.stream().anyMatch(p -> p instanceof Electronics));

    }

    /*
    findBySkuPrefix()
     */
    @Test
    void findBySkuPrefixFindsMatchingProducts()
            throws Exception {

        new AudioTrack("ABC1234","Song 1",5,1,true,AudioCodec.values()[0]);
        new AudioTrack("ABC5432","Song 2",6,1,false,AudioCodec.values()[0]);
        new AudioTrack("XYZ001","Song 3",7,1,true,AudioCodec.values()[0]);
        //conn.commit();
        List<Product> results = Product.findBySkuPrefix("ABC");
        assertEquals(2, results.size());
    }

    @Test
    void findBySkuPrefixWithNoMatchesReturnsEmptyList()
            throws Exception {

        new AudioTrack("ABC12345","Song",5,1,true,AudioCodec.values()[0]);

        List<Product> results = Product.findBySkuPrefix("ZZZ");

        assertTrue(results.isEmpty());
    }


    /*
     * findTracksWithLyrics()
     */

    @Test
    void findTracksWithLyricsOnlyReturnsTracksWithLyrics()
            throws Exception {

        AudioTrack withLyrics = new AudioTrack("LYRICS","Has Lyrics",5,1,true,AudioCodec.values()[0]);
        AudioTrack withoutLyrics = new AudioTrack("NOLYRICS","No Lyrics",5,1,false,AudioCodec.values()[0]);

        List<AudioTrack> results = AudioTrack.findTracksWithLyrics();

        assertEquals(1, results.size());
        assertEquals(withLyrics.getId(),results.get(0).getId());
        assertNotEquals(withoutLyrics.getId(),results.get(0).getId());
    }


    @Test
    void findTracksWithLyricsReturnsEmptyWhenNoneMatch()
            throws Exception {

        new AudioTrack("NOLYRICS1","Song 1",5,1,false,AudioCodec.values()[0]);
        new AudioTrack("NOLYRICS2","Song 2",5,1,false,AudioCodec.values()[0]);

        List<AudioTrack> results = AudioTrack.findTracksWithLyrics();

        assertTrue(results.isEmpty());
    }



     /*
     * findApparelWithSize()
     */

    @Test
    void findApparelWithSizeOnlyReturnsMatchingSize()
            throws Exception {

        ApparelSize wantedSize = ApparelSize.values()[0];
        ApparelSize otherSize =ApparelSize.values()[1];

        Apparel matching = new Apparel("MATCH","Matching Shirt",20,new Dimensions(1, 2, 3),wantedSize);

        new Apparel("NOMATCH","Other Shirt",20,new Dimensions(4, 5, 6),otherSize);

        List<Apparel> results = Apparel.findApparelWithSize(wantedSize);

        assertEquals(1, results.size());
        assertEquals(matching.getId(),results.get(0).getId());
    }


    @Test
    void findApparelWithSizeReturnsEmptyWhenNoMatch()
            throws Exception {

        new Apparel("SIZE1","Small Shirt",20,new Dimensions(1, 2, 3),ApparelSize.values()[0]);

        List<Apparel> results =ProductGateway.findApparelWithSize(ApparelSize.values().length + 10,Apparel::builder);

        assertTrue(results.isEmpty());
    }


    /*
     * findAllThatSupport()
    */

    @Test
    void findAllThatSupportFindsElectronics()
            throws Exception, InvalidArgumentException {

        VideoStreaming service = new VideoStreaming("SUPPORTED1","Service",10,1000,true,Set.of(AudioCodec.values()[0]));

        ArrayList<VideoStreaming> services = new ArrayList<>();

        services.add(service);

        Electronics supportingElectronics =new Electronics("SUPPORTING-ELEC","Supporting TV",
                500,new Dimensions(1, 2, 3),Voltage.V_110, services);

        new Electronics("NON-SUPPORTING-ELEC","Other TV",500,new Dimensions(4, 5, 6),Voltage.V_220,
                null);

        List<Electronics> results = Electronics.findAllThatSupport((int) service.getId());

        assertEquals(1, results.size());
        assertEquals(supportingElectronics.getId(),results.get(0).getId());
    }

    @Test
    void electronicsSupportedServicesRoundTrip() throws Exception {

        VideoStreaming service = new VideoStreaming(
                "ROUNDTRIP-SERVICE",
                "Test Streaming Service",
                10,
                1000,
                true,
                Set.of(AudioCodec.MP3)
        );

        ArrayList<VideoStreaming> services = new ArrayList<>();
        services.add(service);

        Electronics original = new Electronics(
                "ROUNDTRIP-TV",
                "Test TV",
                500,
                new Dimensions(10, 20, 30),
                Voltage.V_110,
                services
        );

        Electronics found =
                Electronics.findElectronics(original.getId());

        assertEquals(1, found.getSupportedServices().size());

        assertEquals(
                service.getId(),
                found.getSupportedServices().get(0).getId()
        );
    }


    @Test
    void findAllThatSupportCanFindMultipleElectronics()
            throws Exception, InvalidArgumentException {

        VideoStreaming service =new VideoStreaming("MULTI-SERVICE","Service",10,1000,true,Set.of(AudioCodec.values()[0]));

        ArrayList<VideoStreaming> services =new ArrayList<>();

        services.add(service);

        Electronics electronics1 =new Electronics("ELECA","TV A",500,new Dimensions(1, 2, 3),Voltage.V_110,
                        services);

        Electronics electronics2 =new Electronics("ELECB","TV B",600,new Dimensions(4, 5, 6),
                Voltage.V_220, services);

        List<Electronics> results = Electronics.findAllThatSupport((int) service.getId());

        assertEquals(2, results.size());

        Set<Long> ids = new HashSet<>();

        for (Electronics electronics : results) {
            ids.add(electronics.getId());
        }

        assertTrue(ids.contains(electronics1.getId()));
        assertTrue(ids.contains(electronics2.getId()));
    }


    @Test
    void findAllThatSupportReturnsEmptyWhenNoElectronicsSupportService() throws Exception, InvalidArgumentException {
        VideoStreaming service = new VideoStreaming("UNSUPPORTEDSERVICE", "Unused Service", 10, 1000, true, Set.of(AudioCodec.values()[0]));
        new Electronics("NOSERVICE", "TV", 500, new Dimensions(1, 2, 3), Voltage.V_110, null);

        List<Electronics> results = Electronics.findAllThatSupport((int) service.getId());

        assertTrue(results.isEmpty());
    }

    @Test
    void findAllThatSupportRejectsNonVideoStreamingId() throws Exception {
        AudioTrack track = new AudioTrack("INVALIDSERVICEID", "Not A Service", 5, 1, true, AudioCodec.values()[0]);

        assertThrows(
                InvalidArgumentException.class, () -> Electronics.findAllThatSupport((int) track.getId()));
    }

    @Test
    void findAllThatSupportRejectsNonexistentId() throws Exception {

        assertThrows(InvalidArgumentException.class, () -> Electronics.findAllThatSupport(999999999));
    }

}
