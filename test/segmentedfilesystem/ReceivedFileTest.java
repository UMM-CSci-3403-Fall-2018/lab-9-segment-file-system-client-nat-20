package segmentedfilesystem;

import static org.junit.Assert.*;

import org.junit.Test;

import java.net.DatagramPacket;

/**
 * This is just a stub test file. You should rename it to
 * something meaningful in your context and populate it with
 * useful tests.
 */
public class ReceivedFileTest {

    @Test
    public void testCreateFile() {
        byte[] buf = new byte[10];
        buf[0] = 0; // set status byte to 0
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        ReceivedFile file = new ReceivedFile(packet);

        // this packet should have been added to the hashmap at -1
        assertNotNull(file.packets.get(-1));
    }

}
