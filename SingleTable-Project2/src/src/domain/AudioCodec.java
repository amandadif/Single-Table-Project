package domain;

public enum AudioCodec {
    MP3,
    AAC,
    FLAC,
    WAV;

    public static AudioCodec fromBitMap(int codecs) {
        int i=0;
        while(codecs>0){
            if (codecs%2 == 1){
                return AudioCodec.values()[i];
            }
            i++;
            codecs = codecs >> 1;
        }
        return null;
    }

    public int getBitMap(){
        return (int) Math.pow(this.ordinal(),2);
    }
}
