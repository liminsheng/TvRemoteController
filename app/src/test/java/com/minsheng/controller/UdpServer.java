package com.minsheng.controller;

import org.junit.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Scanner;

/**
 * Author:       LiMinsheng
 * Description:
 */
public class UdpServer {

    public static void main(String[] args) {

    }

    @Test
    public void udpServer() {
        try {
            DatagramSocket server = new DatagramSocket(88);
            int len = 1024;
            byte[] dataIn = new byte[len];
            DatagramPacket datagramPacketIn = new DatagramPacket(dataIn, len);
            Scanner sc = new Scanner(System.in);
            System.out.println("server 127.0.0.1准备接收数据...");
            for (int i = 0; i < 100; i++) {
                //接收client数据
                server.receive(datagramPacketIn);
                if (i == 0) {
                    //收到客户端连接后开启发送线程
                    new SendMsgThread(server, datagramPacketIn.getSocketAddress()).start();
                }
                System.out.println("msg form client: " + new String(dataIn, 0, datagramPacketIn.getLength()));
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class SendMsgThread extends Thread {

    private DatagramSocket server;
    private SocketAddress address;

    public SendMsgThread(DatagramSocket server, SocketAddress address) {
        this.server = server;
        this.address = address;
    }

    @Override
    public void run() {
        System.out.println("server 发送线程已开启...");
        try {
            byte[] dataOut;
            DatagramPacket datagramPacketOut = null;
            Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                dataOut = sc.nextLine().getBytes();
                datagramPacketOut = new DatagramPacket(dataOut, dataOut.length, address);
                //发送数据给client
                server.send(datagramPacketOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("server 异常了 " + e.getMessage());
        }
    }
}