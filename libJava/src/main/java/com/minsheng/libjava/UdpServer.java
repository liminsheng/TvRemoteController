package com.minsheng.libjava;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Author:       LiMinsheng
 * Description:
 */
public class UdpServer {

    public static void main(String[] args) {
        udpServer();
    }

    public static void udpServer() {
        try {
            DatagramSocket server = new DatagramSocket(88);
            int len = 1024;
            byte[] dataIn = new byte[len];
            byte[] dataOut;
            DatagramPacket datagramPacketIn = new DatagramPacket(dataIn, len);
            DatagramPacket datagramPacketOut = null;
            System.out.println("server 127.0.0.1准备接收数据...");
            for (int i = 0; i < 100; i++) {
                //接收client数据
                server.receive(datagramPacketIn);
                System.out.println("msg form client: " + new String(dataIn, 0, datagramPacketIn.getLength()));
                dataOut = ("I am server, " + new String(dataIn, 0, datagramPacketIn.getLength())).getBytes();
                datagramPacketOut = new DatagramPacket(dataOut, dataOut.length, datagramPacketIn.getSocketAddress());
                //返回数据给client
                server.send(datagramPacketOut);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
