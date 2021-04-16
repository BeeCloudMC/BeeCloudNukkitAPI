package net.fap.beecloud.api;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;
import net.fap.beecloud.api.network.HandlePacket;
import net.fap.beecloud.client.TestClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Synapse {

    private int port1;
    private int port2;
    private DatagramSocket ds;

    public Synapse(int server_port) {
        try {
            this.port1 = server_port;
            this.port2 = this.port1 + 1;
            (new Thread(new Runnable() {
                public void run() {
                    try {
                        Synapse.this.receive();
                    } catch (IOException var2) {
                        var2.printStackTrace();
                    }

                }
            })).start();
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    private void receive() throws IOException {
        DatagramSocket ds = new DatagramSocket(this.port2);
        while(true) {
            byte[] bytes = new byte[1024];
            DatagramPacket dp = new DatagramPacket(bytes, bytes.length);
            ds.receive(dp);
            String pk1 = new String(dp.getData(), 0, dp.getLength());
            HandlePacket.handlePacket(pk1);
        }
    }

    public void send(DataPacket dataPacket){
        try{
            DatagramSocket ds = new DatagramSocket();
            new BufferedReader(new InputStreamReader(System.in));
            byte[] bytes = dataPacket.toString().getBytes();
            InetAddress address = InetAddress.getByName("127.0.0.1");
            DatagramPacket dp = new DatagramPacket(bytes, bytes.length, address, this.port1);
            ds.send(dp);
            ds.close();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void send(String packet){
        try{
            DatagramSocket ds = new DatagramSocket();
            new BufferedReader(new InputStreamReader(System.in));
            byte[] bytes = packet.getBytes();
            InetAddress address = InetAddress.getByName("127.0.0.1");
            DatagramPacket dp = new DatagramPacket(bytes, bytes.length, address, this.port1);
            ds.send(dp);
            ds.close();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}