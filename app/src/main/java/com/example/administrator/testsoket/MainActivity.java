package com.example.administrator.testsoket;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    Button sendbutton;
    Button reievebutton;
    EditText snededit;
    EditText reieveedit;
    TextView sendtextshow;
    TextView recievetextshow;
    ExecutorService executorService;
    Socket socket;
    OutputStream outputStream;
    Handler handler;
    String s;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                recieve();
                try {
                    socket = new Socket("127.0.0.1", 8845);
                    socket.setKeepAlive(true);
                    outputStream = socket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sendbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        send("tmt" + snededit.getText());
                    }
                });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        events();
    }

    private void events() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 1);
        } else {
            network();

        }
    }

    private void network() {
        recieve();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket("127.0.0.1", 5555);
                    socket.setKeepAlive(true);
                    outputStream = socket.getOutputStream();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        };
        executorService.submit(runnable);
        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable runnable1 = new Runnable() {
                    @Override
                    public void run() {
                        while (true){
                            send("tmt" + snededit.getText());
                        }

                    }
                };
                executorService.submit(runnable1);
            }
        });
    }

    private void init() {
        executorService = Executors.newCachedThreadPool();
        sendbutton = findViewById(R.id.sendbutton);
        reievebutton = findViewById(R.id.reievebutton);
        snededit = findViewById(R.id.sendedit);
        reieveedit = findViewById(R.id.reieveedit);
        sendtextshow = findViewById(R.id.sendshowtext);
        recievetextshow = findViewById(R.id.reieveshowtext);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1:
                        recievetextshow.setText(recievetextshow.getText()+"\n"+msg.obj.toString());
                }
            }
        };
    }

    public void send(final String s1) {
        if (outputStream==null){
            Log.d("xiao","out is null");
        }
        s = s1;
        int i = 0;
        try {
            byte[] bytes = s.getBytes();
            Log.d("xiaoxiao", String.valueOf(bytes));
            Log.d("xiaoxiao", "down");
            String adds = i + s + "\r\n";
            byte[] bytes1 = adds.getBytes();
            outputStream.write(bytes1);Log.d("xiaoxiao", String.valueOf(bytes));
            outputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void recieve() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Log.d("xiaotao2","开始接受");
                        byte[] bytes;
                        String s;
                        ServerSocket serverSocket = new ServerSocket(5555);
                        serverSocket.setSoTimeout(5000);
                        Socket socket = serverSocket.accept();
                        Log.d("xiaotao1", "accept");
                        socket.setKeepAlive(true);
                        InputStream inputStream = socket.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        Log.d("xiaotao1", "input");
                        while ((s = bufferedReader.readLine()) != null) {
                            bytes = s.getBytes();
                            int num = bytes[0];
                            Message message = Message.obtain();
                            message.what = 1;
                            message.obj = s;
                            handler.sendMessage(message);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        executorService.submit(runnable);
    }
}
