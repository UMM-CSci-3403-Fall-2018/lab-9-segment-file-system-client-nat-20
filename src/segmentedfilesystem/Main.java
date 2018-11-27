package segmentedfilesystem;

import java.net.*;

public class Main {
    
    public static void main(String[] args) {
        int port = 4556;
        InetAddress address;
        DatagramSocket socket = null;
        DatagramPacket packet;
        byte[] sendBuf = new byte[256];

        if (args.length != 1) {
            System.out.println("Usage: java Main <hostname>");
            return;
        }

        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(args[0]);
            packet = new DatagramPacket(sendBuf, sendBuf.length, address, port);
            socket.send(packet);
        } catch (Exception e) {
            System.err.println("Failed to connect with server\n" + e);
        }


    }

}
