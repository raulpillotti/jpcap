package jpcap.api;

import jpcap.JpcapCaptor;
import jpcap.packet.Packet;

import java.io.IOException;
import java.net.NetworkInterface;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class Capture implements AutoCloseable {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private final JpcapCaptor captor;
    private final BlockingQueue<Packet> queue = new LinkedBlockingDeque<>(10);
    private final CopyOnWriteArrayList<PacketHandler> handlers = new CopyOnWriteArrayList<>();

    private final Runnable captureWorker = () -> {
        while (!Thread.interrupted()) {
            Packet packet = queue.poll();
            for (PacketHandler handler : handlers) {
                handler.handle(packet);
            }
        }
    };

    public Capture(JpcapCaptor captor) {
        this.captor = captor;
    }

    public void start() {
        EXECUTOR.execute(captureWorker);
        captor.loopPacket(-1, queue::offer);
    }

    @Override
    public void close() throws Exception {
        captor.close();
    }

    public void addHandler(PacketHandler handler) {
        handlers.add(handler);
    }

    public void removeHandler(PacketHandler handler) {
        handlers.remove(handler);
    }

    public static Capture fromFile(String filename) {
        JpcapCaptor captor;
        try {
            captor = JpcapCaptor.openFile(filename);
        } catch (IOException e) {
            throw new CaptureException(e);
        }
        return new Capture(captor);
    }

    public static Capture fromInterface(NetworkInterface iface) {
        JpcapCaptor captor;
        try {
            captor = JpcapCaptor.openDevice(null, Integer.MAX_VALUE, true, 1000);
        } catch (IOException e) {
            throw new CaptureException(e);
        }
        return new Capture(captor);
    }

    public static void main(String... args) throws Exception {
        // System.out.println(System.getProperty("java.library.path"));
        
        jpcap.NetworkInterface[] devices = JpcapCaptor.getDeviceList();

        System.out.println("Interfaces de rede:");
        for (int i = 0; i < devices.length; i++) {
            System.out.println(devices[i].name + " " + devices[i].description);
        }

        System.out.println("Usando interface " + devices[0].description);
        
        try {
            JpcapCaptor captor = JpcapCaptor.openDevice(devices[0], 65535, false, 5000);
            while (true) {
                JpcapCaptor.capturePacket(captor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
