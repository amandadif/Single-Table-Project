package datasource;

import domain.AudioCodec;
import domain.Cost;

public class ProductGateway {

    private long id;
    private int type;
    private String sku;
    private String name;
    private double basePrice;
    private long size;
    private boolean hasLyrics;
    private int codecs;
    private boolean hasSubtitles;
    private int videoResolution;

    public long getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public long getSize() {
        return size;
    }

    public boolean isHasLyrics() {
        return hasLyrics;
    }

    public int getCodecs() {
        return codecs;
    }

    public boolean isHasSubtitles() {
        return hasSubtitles;
    }

    public int getVideoResolution() {
        return videoResolution;
    }

    /**
     * Constructor to insert a new row
     * @param type
     * @param sku
     * @param name
     * @param basePrice
     * @param size
     * @param hasLyrics
     * @param codecs
     * @param hasSubtitles
     * @param videoResolution
     */
    public ProductGateway(int type, String sku, String name, double basePrice, long size, boolean hasLyrics, int codecs, boolean hasSubtitles, int videoResolution) {

        this.type = type;
        this.sku = sku;
        this.name = name;
        this.basePrice = basePrice;
        this.size = size;
        this.hasLyrics = hasLyrics;
        this.codecs = codecs;
        this.hasSubtitles = hasSubtitles;
        this.videoResolution = videoResolution;

        // insert this row into the table and get an id
        this.id = 42;
    }

    /**
     * Constructor to populate this object from a row in the db
     * @param id
     */
    public ProductGateway(long id) {
        // retrieve the row and fill it all of the instance variables from it
    }
}
