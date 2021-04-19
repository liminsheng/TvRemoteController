package com.minsheng.controller;

import org.junit.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Scanner;

/**
 * ClassName:      UdpClient
 * Author:         李民生
 * Date:           2021/4/19 14:58
 * Description:
 */
public class UdpClient {

    public static void main(String[] args) {

    }

    @Test
    public void udpClient() {
        try {
            InetSocketAddress address = new InetSocketAddress("127.0.0.1", 88);
            byte[] dataOut = null;
            //发送数据需要address
            DatagramPacket datagramPacketOut = new DatagramPacket(new byte[0], 0, address);
            DatagramSocket client = new DatagramSocket();
            System.out.println("client端：");
            new ReceiveMsgThread(client).start();
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                dataOut = scanner.nextLine().getBytes();
                //获取键盘输入数据
                datagramPacketOut.setData(dataOut);
                //发送数据给server
                client.send(datagramPacketOut);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ReceiveMsgThread extends Thread {

    private DatagramSocket client;

    public ReceiveMsgThread(DatagramSocket client) {
        this.client = client;
    }

    @Override
    public void run() {
        int len = 1024;
        byte[] dataIn = new byte[len];
        //接收数据
        DatagramPacket datagramPacketIn = new DatagramPacket(dataIn, len);
        for (long i = 0; i < 100; i++) {
            try {
                //接收server发来的数据
                client.receive(datagramPacketIn);
                System.out.println("msg form server: " + new String(dataIn, 0, datagramPacketIn.getLength()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
