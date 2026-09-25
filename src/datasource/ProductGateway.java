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

    /**
     * Apparel constructor
     * @param type
     * @param sku
     * @param name
     * @param basePrice
     * @param dimensions
     * @param apparelSize
     * @throws DatabaseException
     */
    public ProductGateway(ProductType type,
                          String sku,
                          String name,
                          double basePrice,
                          Dimensions dimensions,
                          ApparelSize apparelSize)
            throws DatabaseException {

        this.type = type;
        this.sku = sku;
        this.name = name;
        this.basePrice = basePrice;
        this.dimensions = dimensions;
        this.apparelSize = apparelSize;

        insertNewRow();
    }

    /**
     * Electronics constructor
     * @param type
     * @param sku
     * @param name
     * @param basePrice
     * @param dimensions
     * @param voltage
     * @param supportedServices
     * @throws DatabaseException
     */
    public ProductGateway(ProductType type,
                          String sku,
                          String name,
                          double basePrice,
                          Dimensions dimensions,
                          Voltage voltage,
                          ArrayList<VideoStreaming> supportedServices)
            throws DatabaseException {

        this.type = type;
        this.sku = sku;
        this.name = name;
        this.basePrice = basePrice;
        this.dimensions = dimensions;
        this.voltage = voltage;
        this.supportedStreamingServices = supportedServices;

        insertNewRow();

        insertSupportedServices();
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

     public static List<Product> findAllRows(Map<ProductType, Function<ProductGateway, ? extends Product>> builders) throws DatabaseException {
        List<Product> products = new ArrayList<>();

        // find and build them all here

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

    public Set<AudioCodec> getCodecs() {
        return codecs;
    }

     public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getSku() {
        return sku;
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

    public ApparelSize getApparelSize() {
        return apparelSize;
    }

    public Voltage getVoltage() {
        return voltage;
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
     * gotta complete VideoStreaming.builder(). The version currently returns null,
     * so retrieving actual supported services will not work correctly until that method is implemented
     * @throws DatabaseException
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

    public Boolean isHasLyrics() {
        return hasLyrics;
    }

    public Boolean isHasSubtitles() {
        return hasSubtitles;
    }
}
