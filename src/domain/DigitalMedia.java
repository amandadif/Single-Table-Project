package domain;

import datasource.DatabaseException;
import datasource.ProductGateway;
import datasource.ProductType;

public class DigitalMedia extends Product{
    private long size;

    public DigitalMedia(String sku, String name, double basePrice, long size) throws DatabaseException {
        super();
        this.size = size;
    }

    public DigitalMedia findDigitalMedia(long id) throws DatabaseException {
        return ProductGateway.findAndBuild(id, DigitalMedia::builder);
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

    public static DigitalMedia builder(ProductGateway gateway) throws DatasourceTypeMismatch{
        DigitalMedia digitalMedia = new DigitalMedia();
        digitalMedia.getDataOutOfGateway(gateway);
        return digitalMedia;
    }
}


