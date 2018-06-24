package com.alexbath.abod3ar;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkThread implements Runnable {

    private ArrayList<ARPlanElement> drivesList;
    private Thread worker;
    private final AtomicBoolean running;
    private int interval;
    private Handler handler = null;
    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader br = null;
    private String response = null;
    private int port;
    private String ipAddress;
    private static final int SERVER_RESPONSE = 1;
    private String request;

    public NetworkThread(int interval, Handler handler, String ipAddress, int port){
        this.interval = interval;
        this.handler = handler;
        this.port = port;
        this.ipAddress = ipAddress;
        this.drivesList = drivesList;
        this.running = new AtomicBoolean(true);
    }

    public void start() {
        worker = new Thread(this);
        worker.start();
    }

    public void stop() {
        running.set(false);
    }

    @Override
    public void run() {

        try {
            socket = new Socket(ipAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while(running.get()){
                System.out.println("talking to server");
                try {
                    Thread.sleep(interval); //network is might be slow too!
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }

                //out.println("Request for Robot: "+robotIdx);
                out.println(request);
                response = br.readLine();
                if(!response.equals("doNothing")){
                    Message message = new Message();
                    message.what = SERVER_RESPONSE;
                    message.obj = response;
                    handler.sendMessage(message);
                }else if(response.equals("No Robot Connected to Server!")){
                    Message message = new Message();
                    message.what = SERVER_RESPONSE;
                    message.obj = response;
                    handler.sendMessage(message);
                }
            }

        } catch (IOException e) {
            System.out.println("stopeed talking to server");
            e.printStackTrace();
        }

    }

    public void join(){
        try {
            worker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setRequest(String request) {
        this.request = request;
    }

}
