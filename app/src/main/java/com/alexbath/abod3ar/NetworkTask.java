package com.alexbath.abod3ar;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkTask implements Runnable {

    private int port;
    private String ipAddress;
    private int pollInterval;
    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader br = null;
    private String request = null;
    private String response = null;
    private static final int SERVER_RESPONSE = 1;
    private Handler handler = null;

    public NetworkTask(String ipAddress, int port){
        this.port = port;
        this.ipAddress = ipAddress;
        this.pollInterval = 50;
    }

    @Override
    public void run() {

        try {
            socket = new Socket(ipAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while (!Thread.currentThread().isInterrupted()) {

                Thread.sleep(pollInterval);

                System.out.println(request);
                out.println(request);
                try {
                    response = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        } catch (InterruptedException threadE) {
            try {
                br.close();
                out.close();
                socket.close();
            } catch (IOException socketE) {
                socketE.printStackTrace();
            }
            Log.i("NETWORK_TASK_THREAD", "NETWORK TASK closed gracefully!");
        }

    }

    public void setRequest(String request) {
        this.request = request;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}
