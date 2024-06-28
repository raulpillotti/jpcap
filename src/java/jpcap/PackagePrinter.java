package jpcap;

import jpcap.packet.HttpPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
public class PackagePrinter implements PacketReceiver {
    @Override
    public void receivePacket(Packet p) {
        if (!(p instanceof TCPPacket)) return;

        if (HttpPacket.isHttp(p.data)) {
            HttpPacket packet = new HttpPacket((TCPPacket) p);
            System.out.println(packet.toString());
            return;
        }
        
        // System.out.println(p.toString());
    }

}

