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
    public void testCreateHeaderPacket() {
        byte[] buf = new byte[10];
        buf[0] = 0; // set status byte to 0
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        ReceivedFile file = new ReceivedFile(packet);

        // this packet should have been added to the hashmap at -1
        assertNotNull(file.packets.get(-1));
    }

    @Test
    public void testCreateRegularPacket() {
        byte[] buf = new byte[10];
        buf[0] = 1; // set status byte to 1

        buf[2] = 0;
        buf[3] = 0;// set the packet number to 0
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        ReceivedFile file = new ReceivedFile(packet);

        // this packet should have been added to the hashmap at 0
        assertNotNull(file.packets.get(0));
    }

    @Test
    public void testCreateFinalPacket() {
        byte[] buf = new byte[10];
        buf[0] = 3; // set status byte to 3

        buf[2] = 0;
        buf[3] = 4; // set the packet number to 4
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        ReceivedFile file = new ReceivedFile(packet);

        // this packet should have been added to the hashmap at 4
        assertNotNull(file.packets.get(4));
        assertEquals(file.numPackets, 6); // should assume the number of packets is 6
    }

    @Test
    public void testDone() {
        byte[] buf = new byte[10];
        buf[0] = 0; // set status byte to 0
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        ReceivedFile file = new ReceivedFile(packet);

        // add final packet
        buf[0] = 3; // set status byte to 3

        buf[2] = 0;
        buf[3] = 0; // set the packet number to 0
        packet = new DatagramPacket(buf, buf.length);
        file.addPacket(packet);

        assertTrue(file.isDone());

    }

    @Test
    public void testGetFileName() {
        byte[] buf = {0, 0, 'h', 'e', 'l', 'l', 'o', '.', 't', 'x', 't'};

        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        ReceivedFile file = new ReceivedFile(packet);

        assertEquals(file.getFileName(), "hello.txt");
    }

}
