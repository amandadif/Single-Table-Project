package domain;

import datasource.ProductGateway;

import java.util.Set;

public class VideoStreaming extends Product { // Or a component/subclass
    private Set<AudioCodec> supportedCodecs;
    private VideoResolution maxResolution; // Put the enum field right here!


    /**
     * This is the function injected into the datasource layr that allows it to build a VideoStreaming service
     * @param productGateway
     * @return
     */
    public static VideoStreaming builder(ProductGateway productGateway) {
        //TODO fill this in
        return null;
    }


}