package domain;

import datasource.DatabaseException;
import datasource.ProductGateway;
import datasource.ProductType;

public class AudioTrack extends DigitalMedia {

    private Boolean hasLyrics;
    private AudioCodec singleCodec;

    /**
     * finder constructor
     */
    public AudioTrack findAudioTrack(long id) throws DatabaseException {
        return ProductGateway.findAndBuild(id, AudioTrack::builder);
    }

    public Boolean hasLyrics()
    {
        return hasLyrics;
    }

    public AudioCodec getSingleCodec()
    {
        return singleCodec;
    }

    /**
     * Create constructor
     *
     * @param sku
     * @param name
     * @param basePrice
     * @param size
     * @param hasLyrics
     * @param singleCodec
     */
    public AudioTrack(String sku, String name, double basePrice, long size, Boolean hasLyrics, AudioCodec singleCodec) throws DatabaseException {
        super(sku, name, basePrice, size);
        ProductGateway gateway = new ProductGateway(ProductType.AudioTrack, sku, name, basePrice,
                size, hasLyrics, singleCodec, null, null, null);
        assignId(gateway.getId());
        getDataOutOfGateway(gateway);

    }

    protected void getDataOutOfGateway(ProductGateway gateway)
    {
        super.getDataOutOfGateway(gateway);
        this.singleCodec = gateway.getSingleCodec();
        /*
        if (gateway.getCodecs()!=null)
        {
            for (AudioCodec codec : gateway.getCodecs())
            {
                this.codec = codec;
            }
        }
         */
        this.hasLyrics = gateway.isHasLyrics();
    }

    /**
     * This is used by the builder and no one else.  It doesn't need anything
     */
    private AudioTrack() throws DatabaseException {
        super(null, null, 0, 0);
    }

    /**
     * This is the function we are using for the dependency injection.
     * It will allow the gateway to fill in the details of audio track without knowing anything about the domain object
     * Look at how ProductGateway uses it: it only ever knows a generic T - not any specific type.
     */
    public static AudioTrack builder(ProductGateway gateway) throws DatasourceTypeMismatch{
        // TODO make sure that the gateway you are given represents an audio track.  If not, throw the exception
      AudioTrack audioTrack = null;
      try {
        audioTrack = new AudioTrack();
      } catch (DatabaseException e) {
        throw new RuntimeException(e);
      }
      audioTrack.getDataOutOfGateway(gateway);
        return audioTrack;
    }
}
