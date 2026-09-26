package datasource;

import domain.AudioCodec;
import domain.AudioTrack;
import domain.DigitalMedia;
import domain.VideoStreaming;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DigitalMediaTest {
  private static final Connection conn;


  static {
    try {
      conn = DatabaseRegistry.getConnection();
      assert !conn.isClosed();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeAll
  public static void setUpDB() throws DatabaseException {

//        ProductGateway.createTable();
  }

  @AfterEach
  public void rollback() throws SQLException {
    conn.rollback();
  }

  @Test
  public void createAudioTrackObject() throws DatabaseException, SQLException {
    AudioTrack audioTrack = new AudioTrack("12345", "Amanda", 499.99, 1, true, AudioCodec.WAV);
    //conn.commit();
    ProductGateway gateway = audioTrack.getGateway();
    assertEquals("12345", audioTrack.getSku());
    assertEquals("Amanda", audioTrack.getName());
    assertEquals(499.99, audioTrack.getBasePrice());
    assertEquals(1, audioTrack.getSize());
    assertEquals(true, audioTrack.hasLyrics());
    assertEquals(AudioCodec.WAV, audioTrack.getSingleCodec());

    //Test finding an audiotrack
    assertEquals(audioTrack.getSku(), gateway.findAndBuild(audioTrack.getId(),VideoStreaming::builder).getSku());
    assertEquals(audioTrack.getBasePrice(), gateway.findAndBuild(audioTrack.getId(),VideoStreaming::builder).getBasePrice());
  }

  @Test
  public void createVideoStreamingObject() throws DatabaseException, SQLException {
    Set<AudioCodec> audioCodecs = Set.of(AudioCodec.MP3, AudioCodec.AAC);
    VideoStreaming videoStreaming = new VideoStreaming("54321", "VideoStream", 399.99, 10, false, audioCodecs);
    //conn.commit();
    ProductGateway gateway = videoStreaming.getGateway();
    assertEquals("54321", videoStreaming.getSku());
    assertEquals("VideoStream", videoStreaming.getName());
    assertEquals(399.99, videoStreaming.getBasePrice());
    assertEquals(10, videoStreaming.getSize());
    assertEquals(false, videoStreaming.getHasSubtitles());
    assertEquals(3, gateway.calculateBitmask(videoStreaming.getSupportedCodecs()));

    //Test finding VideoStreaming object
    assertEquals(videoStreaming.getSku(), gateway.findAndBuild(videoStreaming.getId(),VideoStreaming::builder).getSku());
    assertEquals(videoStreaming.getBasePrice(), gateway.findAndBuild(videoStreaming.getId(),VideoStreaming::builder).getBasePrice());
  }

  @Test
  public void videoStreamingCodecsBitmaskTest() throws DatabaseException {
    ProductGateway gateway = new ProductGateway(
            ProductType.VideoStreaming,
            "V001",
            "Test Video",
            2.22,
            0L,
            null,
            null,
            Set.of(AudioCodec.MP3),
            false,
            null,
            null,
            null,
            null
    );

    Set<AudioCodec> codecs = Set.of(AudioCodec.MP3);
    assertEquals(1, gateway.calculateBitmask(codecs));

    codecs = Set.of(AudioCodec.AAC);
    assertEquals(2, gateway.calculateBitmask(codecs));

    codecs = Set.of(AudioCodec.FLAC);
    assertEquals(4, gateway.calculateBitmask(codecs));

    codecs = Set.of(AudioCodec.WAV);
    assertEquals(8, gateway.calculateBitmask(codecs));
  }
}
