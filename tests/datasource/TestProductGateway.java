package datasource;

import datasource.DatabaseException;
import datasource.ProductGateway;
import datasource.ProductType;
import domain.AudioCodec;
import domain.AudioTrack;
import domain.DatasourceTypeMismatch;
import domain.VideoStreaming;
import org.junit.Assert;
import org.junit.Test;
import java.util.EnumSet;
import java.util.Set;

import java.util.ArrayList;

import static datasource.ProductType.AudioTrack;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TestProductGateway {

  public void main() throws DatabaseException {
    createAudioProductIDNotZero();
  }

  @Test
  public void createAudioProductIDNotZero() throws DatabaseException {
    Set<AudioCodec> emptyCodecs = EnumSet.noneOf(AudioCodec.class);
    ArrayList<VideoStreaming> arrayListVideo = new ArrayList<>();

    ProductGateway gw = new ProductGateway(AudioTrack, "1111", "Awesome Song", 13.99,
            10, true, emptyCodecs, arrayListVideo);

    assertNotEquals(0, gw.getId());
  }

  @Test
  public void createAudioProductIDMatch() throws DatabaseException {
    Set<AudioCodec> emptyCodecs = EnumSet.noneOf(AudioCodec.class);
    ArrayList<VideoStreaming> arrayListVideo = new ArrayList<>();

    ProductGateway gw = new ProductGateway(AudioTrack, "1111", "Awesome Song", 13.99,
            10, true, emptyCodecs, arrayListVideo);
  }
}