package segmentedfilesystem;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class Main {
    
    public static void main(String[] args) {
        int port = 6014;
        InetAddress address;
        DatagramSocket socket = null;
        DatagramPacket packet;
        byte[] buf = new byte[1028]; // 1KB + 4 bytes for the header

        if (args.length != 1) {
            System.out.println("Usage: java Main <hostname>");
            return;
        }

        // open the socket
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            System.err.println("Failed to open socket\n" + e);
        }

        try {
            // send an empty packet to "start the conversation"
            address = InetAddress.getByName(args[0]);
            packet = new DatagramPacket(buf, buf.length, address, port);
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Failed to send header packet to server\n" + e);
            return;
        }

        // map containing the file objects
        HashMap<Byte, ReceivedFile> files = new HashMap<>();

        ArrayList<ReceivedFile> doneFiles = new ArrayList<>();

        // receive packets
        while(true) {
            packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                System.err.println("Error when receiving packets\n" + e);
                return;
            }
            byte fileId = buf[1];
            // if this file is new, add it to the map
            if(files.get(fileId)==null) {
                files.put(fileId, new ReceivedFile(packet));
            } else {
                files.get(fileId).addPacket(packet);
            }

            //if the file is done, move it to the done pile
            if(files.get(fileId).isDone()) {
                System.out.println("One file is done");
                doneFiles.add(files.remove(fileId));
                // if all the files are done, break out of the loop
                if(files.isEmpty()) {
                    System.out.println("Done");
                    break;
                }
            }

        }

        // Create all of the files
        for(ReceivedFile file: doneFiles) {
            try {
                file.createFile();
            } catch (IOException e) {
                System.err.println("Error when creating file\n" + e);
            }

        }


    }

}
class ReceivedFile {
    // -1 is the header packet, other numbers are the data for the file
    public HashMap<Integer, byte[]> packets; // made public for testing purposes
    private int numPackets;
    private int foundPackets;
    private boolean done;

    // create the object using a packet
    public ReceivedFile(DatagramPacket packet) {
        numPackets = Integer.MAX_VALUE; // set this to max value until we know the length for sure
        foundPackets = 0;
        done = false;
        packets = new HashMap<>();

        this.addPacket(packet);
    }

    public void addPacket(DatagramPacket packet) {
        foundPackets++;
        switch (packet.getData()[0] % 4) { // use the status byte mod 4 to figure out what kind of packet we have
            case 3: {
                //convert the packet number from bytes to an int so we can get the total number of packets
                numPackets = ((packet.getData()[2] & 0xff) << 8) | (packet.getData()[3] & 0xff) + 2;
                System.out.println("numPackets: " + numPackets);
            }
            case 1: {
                // if it's the last packet, we can get the packet length
                int packetNumber = ((packet.getData()[2] & 0xff) << 8) | (packet.getData()[3] & 0xff);
                packets.put(packetNumber, Arrays.copyOf(packet.getData(), packet.getLength()));
                break;
            }
            default: { // if it is 0 or 2 it is a header packet
                packets.put(-1, Arrays.copyOf(packet.getData(), packet.getLength()));
            }
        }
        if(numPackets==foundPackets) {
            done = true;
        }
//        System.out.println(packet.getData()[2]);

    }

    public boolean isDone() {
        return done;
    }

    // if all packets have been received, build the file
    public void createFile() throws IOException{
        byte[] header = packets.get(-1);
        String fileName = new String(header, 2, header.length-2);
        System.out.println(fileName);

        File newFile = new File(fileName);
        OutputStream writer = new FileOutputStream(newFile);

        if(newFile.createNewFile()) {
            System.out.println("File already exists.");
        }

        // loop through the data packets
        for(int j = 0; j < numPackets - 1; j++) {
            byte[] packet = packets.get(j);
            //write the data from the packet into the file
            writer.write(packet, 4, packet.length - 4);
        }

        writer.close();
    }
}
