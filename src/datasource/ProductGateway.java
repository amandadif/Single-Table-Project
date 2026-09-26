package datasource;

import domain.*;

import java.sql.*;
import java.util.*;
import java.util.function.Function;

public class ProductGateway {

    private long id;
    private ProductType type;
    private String sku;
    private String name;
    private double basePrice;
    private Long size = null;
    private Boolean hasLyrics = null;
    private AudioCodec singleCodec = null;
    private Set<AudioCodec> codecs = null;
    private Boolean hasSubtitles;
    private int videoResolution;
    private ArrayList<VideoStreaming> supportedStreamingServices;
    private Dimensions dimensions;
    private ApparelSize apparelSize;
    private Voltage voltage;

    /**
     * Create constructor - used to create any type of Product.
     * Fields that do not apply to a particular ProductType are passed as null.
     */
    public ProductGateway(ProductType type, String sku, String name, double basePrice,
            Long size, Boolean hasLyrics, AudioCodec singleCodec, Set<AudioCodec> codecs,
            Boolean hasSubtitles, Dimensions dimensions, ApparelSize apparelSize, Voltage voltage,
            ArrayList<VideoStreaming> supportedStreamingServices) throws DatabaseException {

        this.type = type;
        this.sku = sku;
        this.name = name;
        this.basePrice = basePrice;
        this.size = size;
        this.hasLyrics = hasLyrics;
        this.singleCodec = singleCodec;
        this.codecs = codecs;
        this.hasSubtitles = hasSubtitles;
        this.dimensions = dimensions;
        this.apparelSize = apparelSize;
        this.voltage = voltage;
        this.supportedStreamingServices = supportedStreamingServices;

        insertNewRow();

        if (type == ProductType.Electronics) {
            insertSupportedServices();
        }
    }


    /**
     * A finder constructor that will be used by findAndBuild only
     *
     * @param id
     */
    private ProductGateway(long id) throws DatabaseException {
        String sql = "Select * FROM products Where id = ?";

        Connection conn = getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                getDataOutOfResultSet(rs);
                if (this.type == ProductType.Electronics) {
                    loadSupportedServices();
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }



    private ProductGateway() {
        // Used to have empty gateways that we fill in incrementally
    }

    public static void createTable() throws DatabaseException {
        try {
            Connection conn = DatabaseRegistry.getConnection();
            Statement stmt = conn.createStatement();

            String productsTable = """
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type INTEGER NOT NULL,
                    sku TEXT NOT NULL,
                    name TEXT NOT NULL,
                    basePrice REAL NOT NULL,
                    size INTEGER,
                    hasLyrics BOOLEAN,
                    hasSubtitles BOOLEAN,
                    singleCodec INTEGER,
                    codecs INTEGER,
                    dimensionWidth REAL,
                    dimensionDepth REAL,
                    dimensionHeight REAL,
                    apparelSize INTEGER,
                    voltage INTEGER
                )
                """;

            stmt.execute(productsTable);

            String supportedServicesTable = """
                CREATE TABLE IF NOT EXISTS ELECTRONICS_SUPPORTED_SERVICE (
                    electronicsID INTEGER NOT NULL,
                    videoStreamingID INTEGER NOT NULL,
                    PRIMARY KEY (electronicsID, videoStreamingID),
                    FOREIGN KEY (electronicsID) REFERENCES products(id),
                    FOREIGN KEY (videoStreamingID) REFERENCES products(id)
                )
                """;

            stmt.execute(supportedServicesTable);

        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public static List<Product> findAllRows(
            Map<ProductType, Function<ProductGateway, ? extends Product>> builders)
            throws DatabaseException {

        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products";

        Connection conn = getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {

                ProductGateway gateway = new ProductGateway();

                gateway.getDataOutOfResultSet(rs);

                if (gateway.getType() == ProductType.Electronics) {
                    gateway.loadSupportedServices();
                }

                Function<ProductGateway, ? extends Product> builder =
                        builders.get(gateway.getType());

                Product product = builder.apply(gateway);

                products.add(product);
            }

        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }

        return products;
    }

    public static <T> T findAndBuild(long id, Function<ProductGateway, T> domainBuilder) throws DatabaseException {
        ProductGateway gateway = new ProductGateway(id);
        return domainBuilder.apply(gateway);
    }

    private static Connection getConnection() throws DatabaseException {
        Connection conn = null;
        try {
            conn = DatabaseRegistry.getConnection();
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
        return conn;
    }

    /**
     * This reads the "type" column first to determine which additional fields need to be reconstructed.
     * The method also preserves NULL values for the existing DigitalMedia fields.
     * @param rs
     * @throws SQLException
     */
    private void getDataOutOfResultSet(ResultSet rs)
            throws SQLException {

        this.id = rs.getLong("id");
        this.type = ProductType.values()[
                rs.getInt("type")
                ];

        this.sku = rs.getString("sku");
        this.name = rs.getString("name");
        this.basePrice = rs.getDouble("basePrice");

        // DigitalMedia
        long storedSize = rs.getLong("size");
        this.size = rs.wasNull() ? null : storedSize;
        Boolean storedLyrics = rs.getBoolean("hasLyrics");
        this.hasLyrics =
                rs.wasNull() ? null : storedLyrics;

        Boolean storedSubtitles = rs.getBoolean("hasSubtitles");
        this.hasSubtitles =
                rs.wasNull() ? null : storedSubtitles;

        int storedSingleCodec = rs.getInt("singleCodec");
        this.singleCodec = rs.wasNull()
                ? null
                : AudioCodec.values()[storedSingleCodec];

        int storedCodecs = rs.getInt("codecs");
        this.codecs = rs.wasNull()
                ? null
                : getSupportedCodecsSet(storedCodecs);

        // PhysicalProduct
        if (type == ProductType.Apparel ||
                type == ProductType.Electronics) {
            this.dimensions = new Dimensions(
                    rs.getDouble("dimensionWidth"),
                    rs.getDouble("dimensionDepth"),
                    rs.getDouble("dimensionHeight")
            );
        }
        // Apparel
        if (type == ProductType.Apparel) {
            this.apparelSize = ApparelSize.values()[
                    rs.getInt("apparelSize")
                    ];
        }
        // Electronics
        if (type == ProductType.Electronics) {
            this.voltage = Voltage.values()[
                    rs.getInt("voltage")
                    ];
        }
    }

    int calculateBitmask(Set<AudioCodec> codecs) {
        if (codecs == null) return 0;

        int mask = 0;
        for (AudioCodec codec : codecs) {
            // Shift 1 left by the enum's ordinal position to create the binary flag
            mask |= (1 << codec.ordinal());
        }
        return mask;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) throws DatabaseException {
        this.basePrice = basePrice;
        updateColumn("basePrice", basePrice);
    }

    public Set<AudioCodec> getCodecs() {
        return codecs;
    }

    public void setCodecs(Set<AudioCodec> codecs) throws DatabaseException {
        this.codecs = codecs;

        if (codecs == null) {
            updateColumn("codecs", null);
        } else {
            updateColumn("codecs", calculateBitmask(codecs));
        }
    }

     public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws DatabaseException {
        this.name = name;
        updateColumn("name", name);
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) throws DatabaseException {
        this.size = size;
        updateColumn("size", size);
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) throws DatabaseException {
        this.sku = sku;
        updateColumn("sku", sku);
    }


    public double getDimensionWidth() {
        return dimensions.getWidth();
    }

    public double getDimensionDepth() {
        return dimensions.getDepth();
    }

    public double getDimensionHeight() {
        return dimensions.getHeight();
    }

    public void setDimensions(Dimensions dimensions) throws DatabaseException {
        this.dimensions = dimensions;

        if (dimensions == null) {
            updateColumn("dimensionWidth", null);
            updateColumn("dimensionDepth", null);
            updateColumn("dimensionHeight", null);
        } else {
            updateColumn("dimensionWidth", dimensions.getWidth());
            updateColumn("dimensionDepth", dimensions.getDepth());
            updateColumn("dimensionHeight", dimensions.getHeight());
        }
    }

    public ApparelSize getApparelSize() {
        return apparelSize;
    }

    public void setApparelSize(ApparelSize apparelSize) throws DatabaseException {
        this.apparelSize = apparelSize;

        if (apparelSize == null) {
            updateColumn("apparelSize", null);
        } else {
            updateColumn("apparelSize", apparelSize.ordinal());
        }
    }

    public Voltage getVoltage() {
        return voltage;
    }

    public void setVoltage(Voltage voltage) throws DatabaseException {
        this.voltage = voltage;

        if (voltage == null) {
            updateColumn("voltage", null);
        } else {
            updateColumn("voltage", voltage.ordinal());
        }
    }

    private Set<AudioCodec> getSupportedCodecsSet(int mask) {
        Set<AudioCodec> codecs = new java.util.HashSet<>();

        AudioCodec[] values = AudioCodec.values();
        for (int i = 0; i < values.length; i++) {
            // Check if the i-th bit is set to 1
            if ((mask & (1 << i)) != 0) {
                codecs.add(values[i]);
            }
        }
        return codecs;
    }

    public ArrayList<VideoStreaming> getSupportedStreamingServices() {
        return supportedStreamingServices;
    }

    public ProductType getType() {
        return type;
    }

    public int getVideoResolution() {
        return videoResolution;
    }

    public AudioCodec getSingleCodec() {
        return singleCodec;
    }

    public void setSingleCodec(AudioCodec singleCodec) throws DatabaseException {
        this.singleCodec = singleCodec;

        if (singleCodec == null) {
            updateColumn("singleCodec", null);
        } else {
            updateColumn("singleCodec", singleCodec.ordinal());
        }
    }

    private void updateColumn(String column, Object value)
            throws DatabaseException {

        String sql = "UPDATE products SET " + column + " = ? WHERE id = ?";

        Connection conn = getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, value);
            pstmt.setLong(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }


    /**
     * When you create an Apparel object, the gateway stores:
     * - The common Product fields.
     * - The Apparel product type.
     * - The three dimension values.
     * - The ApparelSize integer.
     * The DigitalMedia and Electronics-specific fields remain NULL
     * When you create Electronics, the same method stores the Electronics fields instead.
     * @throws DatabaseException
     */
    private void insertNewRow() throws DatabaseException {

        String sql = """
        INSERT INTO products (
            type,
            sku,
            name,
            basePrice,
            size,
            hasLyrics,
            hasSubtitles,
            singleCodec,
            codecs,
            dimensionWidth,
            dimensionDepth,
            dimensionHeight,
            apparelSize,
            voltage
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;

        Connection conn = getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, type.ordinal());
            pstmt.setString(2, sku);
            pstmt.setString(3, name);
            pstmt.setDouble(4, basePrice);

            // DigitalMedia size
            if (size != null) {
                pstmt.setLong(5, size);
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            // AudioTrack hasLyrics
            if (hasLyrics != null) {
                pstmt.setBoolean(6, hasLyrics);
            } else {
                pstmt.setNull(6, Types.BOOLEAN);
            }
            // Video Streaming hasSubtitles
            if (hasSubtitles != null) {
                pstmt.setBoolean(7, hasSubtitles);
            } else {
                pstmt.setNull(7, Types.BOOLEAN);
            }
            // Single Audio Codec
            if (singleCodec != null) {
                pstmt.setInt(8, singleCodec.ordinal());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }
            // Audio codecs
            if (codecs != null) {
                pstmt.setInt(9, calculateBitmask(codecs));
            } else {
                pstmt.setNull(9, Types.INTEGER);
            }
            // PhysicalProduct dimensions
            if (dimensions != null) {
                pstmt.setDouble(10, dimensions.getWidth());
                pstmt.setDouble(11, dimensions.getDepth());
                pstmt.setDouble(12, dimensions.getHeight());
            } else {
                pstmt.setNull(10, Types.DOUBLE);
                pstmt.setNull(11, Types.DOUBLE);
                pstmt.setNull(12, Types.DOUBLE);
            }

            // Apparel size
            if (apparelSize != null) {
                pstmt.setInt(13, apparelSize.ordinal());
            } else {
                pstmt.setNull(13, Types.INTEGER);
            }
            // Electronics voltage
            if (voltage != null) {
                pstmt.setInt(14, voltage.ordinal());
            } else {
                pstmt.setNull(14, Types.INTEGER);
            }
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys =
                             pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        this.id = generatedKeys.getLong(1);
                    }
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    /**
     * This method inserts one relationship row for every supported VideoStreaming service.
     * It assumes each VideoStreaming object has already been saved and has a valid database ID.
     * @throws DatabaseException
     */
    private void insertSupportedServices() throws DatabaseException {

        if (supportedStreamingServices == null) {
            return;
        }

        String sql = """
        INSERT INTO ELECTRONICS_SUPPORTED_SERVICE
        (electronicsID, videoStreamingID)
        VALUES (?, ?);
        """;

        Connection conn = getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (VideoStreaming service : supportedStreamingServices) {
                pstmt.setLong(1, this.id);
                pstmt.setLong(2, service.getId());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    /**
     * Loads the VideoStreaming services supported by this Electronics product.
     * @throws DatabaseException if the database query fails
     */
    private void loadSupportedServices()
            throws DatabaseException {

        supportedStreamingServices = new ArrayList<>();

        String sql = """
        SELECT videoStreamingID
        FROM ELECTRONICS_SUPPORTED_SERVICE
        WHERE electronicsID = ?;
        """;

        Connection conn = getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, this.id);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    long serviceId =
                            rs.getLong("videoStreamingID");

                    VideoStreaming service =
                            ProductGateway.findAndBuild(
                                    serviceId,
                                    VideoStreaming::builder
                            );
                    supportedStreamingServices.add(service);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public static List<ProductGateway> findBySkuPrefix(String prefix)
            throws DatabaseException {

        List<ProductGateway> gateways = new ArrayList<>();

        String sql = "SELECT id FROM products WHERE sku LIKE ?";

        Connection conn = getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, prefix + "%");

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    gateways.add(new ProductGateway(rs.getLong("id")));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }

        return gateways;
    }


    public static List<AudioTrack> findTracksWithLyrics(
            Function<ProductGateway, AudioTrack> domainBuilder)
            throws DatabaseException {

        List<AudioTrack> tracks = new ArrayList<>();

        String sql = """
        SELECT id
        FROM products
        WHERE type = ?
        AND hasLyrics = ?
        """;

        Connection conn = getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ProductType.AudioTrack.ordinal());
            pstmt.setBoolean(2, true);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    ProductGateway gateway =
                            new ProductGateway(rs.getLong("id"));

                    tracks.add(domainBuilder.apply(gateway));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }

        return tracks;
    }


    public static List<Apparel> findApparelWithSize(
            int size,
            Function<ProductGateway, Apparel> domainBuilder)
            throws DatabaseException {

        List<Apparel> apparel = new ArrayList<>();

        String sql = """
        SELECT id
        FROM products
        WHERE type = ?
        AND apparelSize = ?
        """;

        Connection conn = getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ProductType.Apparel.ordinal());
            pstmt.setInt(2, size);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    ProductGateway gateway =
                            new ProductGateway(rs.getLong("id"));

                    apparel.add(domainBuilder.apply(gateway));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }

        return apparel;
    }


    public static List<Electronics> findAllThatSupport(
            int videoStreamingId,
            Function<ProductGateway, Electronics> domainBuilder)
            throws DatabaseException , InvalidArgumentException{

        List<Electronics> electronics = new ArrayList<>();

        String checkSql = """
        SELECT type
        FROM products
        WHERE id = ?
        """;

        Connection conn = getConnection();

        try (PreparedStatement checkStmt =
                     conn.prepareStatement(checkSql)) {

            checkStmt.setLong(1, videoStreamingId);

            try (ResultSet rs = checkStmt.executeQuery()) {

                if (!rs.next() ||
                        rs.getInt("type") != ProductType.VideoStreaming.ordinal()) {
                    throw new InvalidArgumentException(
                            "ID is not a VideoStreaming product");
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }

        String sql = """
        SELECT electronicsID
        FROM ELECTRONICS_SUPPORTED_SERVICE
        WHERE videoStreamingID = ?
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, videoStreamingId);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    ProductGateway gateway =
                            new ProductGateway(rs.getLong("electronicsID"));

                    electronics.add(domainBuilder.apply(gateway));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }

        return electronics;
    }

    public Boolean isHasLyrics() {
        return hasLyrics;
    }

    public void setHasLyrics(Boolean hasLyrics) throws DatabaseException {
        this.hasLyrics = hasLyrics;
        updateColumn("hasLyrics", hasLyrics);
    }

    public Boolean isHasSubtitles() {
        return hasSubtitles;
    }

    public void setHasSubtitles(Boolean hasSubtitles) throws DatabaseException {
        this.hasSubtitles = hasSubtitles;
        updateColumn("hasSubtitles", hasSubtitles);
    }
}
