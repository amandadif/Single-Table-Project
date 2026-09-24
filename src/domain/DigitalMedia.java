package domain;

import datasource.DatabaseException;
import datasource.ProductGateway;
import datasource.ProductType;

import java.util.Set;

public class DigitalMedia extends Product{
    private long size;

    public DigitalMedia(String sku, String name, double basePrice, long size) throws DatabaseException {
        ProductGateway gateway = new ProductGateway(ProductType.DigitalMedia, sku, name, basePrice,
                size, null, null, null);
        assignId(gateway.getId());
        getDataOutOfGateway(gateway);
    }

    protected void getDataOutOfGateway(ProductGateway gateway)
    {
        super.getDataOutOfGateway(gateway);
        this.size = gateway.getSize();
    }

    public long getSize() {
        return size;
    }

    private DigitalMedia() {
    }

    /**
     * Create constructor
     *
     * @param sku
     * @param name
     * @param basePrice
     * @param size
     * @param hasLyrics
     * @param codecs
     */
    public DigitalMedia(String sku, String name, double basePrice, long size, boolean hasLyrics, Set<AudioCodec> codecs) throws DatabaseException {
        ProductGateway gateway = new ProductGateway(ProductType.DigitalMedia, sku, name, basePrice,
                size, hasLyrics, codecs, null);
        assignId(gateway.getId());
        getDataOutOfGateway(gateway);
    }

    public static DigitalMedia builder(ProductGateway gateway) throws DatasourceTypeMismatch{
        DigitalMedia digitalMedia = new DigitalMedia();
        digitalMedia.getDataOutOfGateway(gateway);
        return digitalMedia;
    }
}


