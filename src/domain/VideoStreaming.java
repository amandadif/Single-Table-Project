package domain;

import datasource.DatabaseException;
import datasource.ProductGateway;
import datasource.ProductType;

import java.util.Set;

public class VideoStreaming extends DigitalMedia { // Or a component/subclass
    private AudioCodec supportedCodecs;
    private Boolean hasSubtitles; // Put the enum field right here!


    public VideoStreaming findVideoStreaming(long id) throws DatabaseException {
        return ProductGateway.findAndBuild(id, VideoStreaming::builder);
    }

    public Boolean getHasSubtitles()
    {
        return hasSubtitles;
    }

    public AudioCodec getSupportedCodecs()
    {
        return supportedCodecs;
    }

    /**
     * Create constructor
     *
     * @param sku
     * @param name
     * @param basePrice
     * @param size
     * @param hasSubtitles
     * @param codecs
     */
    public VideoStreaming(String sku, String name, double basePrice, long size, Boolean hasSubtitles, Set<AudioCodec> codecs) throws DatabaseException {
        super(sku, name, basePrice, size);
        ProductGateway gateway = new ProductGateway(
                ProductType.VideoStreaming,
                sku,
                name,
                basePrice,
                size,
                null,       // hasLyrics
                null,       // singleCodec
                codecs,
                hasSubtitles,
                null,       // dimensions
                null,       // apparelSize
                null,       // voltage
                null        // supportedStreamingServices
        );
        assignId(gateway.getId());
        getDataOutOfGateway(gateway);

    }

    protected void getDataOutOfGateway(ProductGateway gateway)
    {
        super.getDataOutOfGateway(gateway);
        if (gateway.getCodecs()!=null)
        {
            for (AudioCodec supportedCodecs : gateway.getCodecs())
            {
                this.supportedCodecs = supportedCodecs;
            }
        }
        this.hasSubtitles = gateway.isHasSubtitles();
    }

    /**
     * This is used by the builder and no one else.  It doesn't need anything
     */
    private VideoStreaming() throws DatabaseException {
        super(null, null, 0, 0);
    }

    /**
     * This is the function injected into the datasource layr that allows it to build a VideoStreaming service
     * @param productGateway
     * @return
     */
    public static VideoStreaming builder(ProductGateway productGateway) {
        VideoStreaming videoStreaming = null;
        try {
            videoStreaming = new VideoStreaming();
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }
        videoStreaming.getDataOutOfGateway(productGateway);
        return videoStreaming;
    }


}