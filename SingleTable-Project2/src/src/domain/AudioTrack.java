package domain;

import datasource.ProductGateway;
import datasource.ProductType;

public class AudioTrack extends DigitalMedia{

    private boolean hasLyrics;
    private AudioCodec codec;
    /**
     *  finder constructor
     */

    public AudioTrack(long id){
     ProductGateway gateway = new ProductGateway(id);
     // should check to mmake sure type is AudioTrack
     this.sku = gateway.getSku();
     this.name = gateway.getName();
     this.basePrice = new Cost(gateway.getBasePrice());
     this.size = gateway.getSize();
             this.hasLyrics = gateway.isHasLyrics();
       this.codec = AudioCodec.fromBitMap(gateway.getCodecs());
    }

    /**
     * Create constructor
     * @param sku
     * @param name
     * @param basePrice
     * @param size
     * @param hasLyrics
     * @param codec
     */
    public AudioTrack(String sku, String name, Cost basePrice, long size, boolean hasLyrics, AudioCodec codec){
        ProductGateway gateway = new ProductGateway(ProductType.valueOf("AudioTrack").ordinal(), sku,  name,  basePrice.dollars(),
                size,  hasLyrics, codec.getBitMap(),  false, 0);
        this.id = gateway.getId();
    }
}
