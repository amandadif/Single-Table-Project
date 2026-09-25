package datasource;

/**
 * confirm whether DigitalMedia records are required in the final database,
 * since the original Single Table design assigns type values only to the four concrete classes
 */
public enum ProductType {
    VideoStreaming,
    AudioTrack,
    Apparel,
    Electronics,
    DigitalMedia,
    // TODO you need the whole list of concrete types here
}
