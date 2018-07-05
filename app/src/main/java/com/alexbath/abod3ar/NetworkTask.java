package com.alexbath.abod3ar;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NetworkTask implements Runnable {

    private final ScheduledExecutorService serverPingerScheduler;
    private int port;
    private String ipAddress;
    private Socket socket = null;
    private BufferedReader br = null;
    private String response = null;
    private static final int SERVER_RESPONSE = 1;
    private Handler handler = null;

    public NetworkTask(String ipAddress, int port, Handler generalHandler, ScheduledExecutorService serverPingerScheduler){
        this.port = port;
        this.ipAddress = ipAddress;
        this.handler = generalHandler;
        this.serverPingerScheduler = serverPingerScheduler;
    }

    @Override
    public void run() {

        try {
            socket = new Socket(ipAddress, port);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connection Successful!");

            final Runnable pinger = new Runnable() {

                @Override
                public void run() {
                    try {
                        response = br.readLine();
                        Message message = new Message();
                        message.what = SERVER_RESPONSE;
                        message.obj = response;
                        handler.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            serverPingerScheduler.scheduleAtFixedRate(pinger, 100, 60, TimeUnit.MILLISECONDS);

        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            while (!Thread.currentThread().isInterrupted()) {
//
//                Thread.sleep(pollInterval);
//
//                try {
//                    response = br.readLine();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                if(!response.equals("doNothing")){
//                    Message message = new Message();
//                    message.what = SERVER_RESPONSE;
//                    message.obj = response;
//                    handler.sendMessage(message);
//                }else if(response.equals("No Robot Connected to Server!")){
//                    Message message = new Message();
//                    message.what = SERVER_RESPONSE;
//                    message.obj = response;
//                    handler.sendMessage(message);
//                }
//            }
//        } catch (InterruptedException threadE) {
//            try {
//                br.close();
//                socket.close();
//            } catch (IOException socketE) {
//                socketE.printStackTrace();
//            }
//            Log.i("NETWORK_TASK_THREAD", "NETWORK TASK closed gracefully!");
//        }

        System.out.println("NetworkTask Runnable completed!");

    }
}
