package domain;

public enum VideoResolution {
    STANDARD_DEFINITION("SD"),
    HIGH_DEFINITION("HD"),
    FOUR_K("4K"),
    EIGHT_K("8K");

    private final String label;


    VideoResolution(String label) {
        this.label = label;

    }

    public String getLabel() {
        return label;
    }

}