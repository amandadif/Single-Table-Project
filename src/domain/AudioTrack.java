package domain;

import datasource.DatabaseException;
import datasource.ProductGateway;
import datasource.ProductType;

import java.util.Set;

public class AudioTrack extends DigitalMedia {

    private boolean hasLyrics;
    private AudioCodec codec;

    /**
     * finder constructor
     */
    public AudioTrack findAudioTrack(long id) throws DatabaseException {
        return ProductGateway.findAndBuild(id, AudioTrack::builder);
    }

    public boolean hasLyrics()
    {
        return hasLyrics;
    }

    public AudioCodec getCodec()
    {
        return codec;
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
    public AudioTrack(String sku, String name, double basePrice, long size, boolean hasLyrics, Set<AudioCodec> codecs) throws DatabaseException {
        ProductGateway gateway = new ProductGateway(ProductType.AudioTrack, sku, name, basePrice,
                size, hasLyrics, codecs, null);
        assignId(gateway.getId());
        getDataOutOfGateway(gateway);

    }

    protected void getDataOutOfGateway(ProductGateway gateway)
    {
        super.getDataOutOfGateway(gateway);
        if (gateway.getCodecs()!=null)
        {
            for (AudioCodec codec : gateway.getCodecs())
            {
                this.codec = codec;
            }
        }
        this.hasLyrics = gateway.isHasLyrics();
    }

    /**
     * This is used by the builder and no one else.  It doesn't need anything
     */
    private AudioTrack() {
    }

    /**
     * This is the function we are using for the dependency injection.
     * It will allow the gateway to fill in the details of audio track without knowing anything about the domain object
     * Look at how ProductGateway uses it: it only ever knows a generic T - not any specific type.
     */
    public static AudioTrack builder(ProductGateway gateway) throws DatasourceTypeMismatch{
        // TODO make sure that the gateway you are given represents an audio track.  If not, throw the exception
        AudioTrack audioTrack = new AudioTrack();
        audioTrack.getDataOutOfGateway(gateway);
        return audioTrack;
    }
}
