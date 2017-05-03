package br.edu.insper.coffeemeup;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;


public class MainActivity extends AppCompatActivity {

    // From https://examples.javacodegeeks.com/android/core/socket-core/android-socket-example/
    private   ServerSocket serverSocket;
    protected Handler      updateConversationHandler;
    protected Thread       serverThread = null;

    protected static final int    PORT = 5000;
//    protected static final String HOST = "192.168.43.189";  // Pedro's phone
    protected static final String HOST = "192.168.0.13";   // Pedro's house

    private Socket  socket;
    private boolean connected = false;

    private Button button;  // msg sender
    private TextView text;  // msgs recieved from the server

    @Override
    protected void onStop()
    {
        super.onStop();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        openSocket();

//        // msg receiver
//        updateConversationHandler = new Handler();
//        this.serverThread = new Thread(new ServerThread());
//        this.serverThread.start();
//        text = (TextView) findViewById(R.id.txt_msg);

        // msg sender
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ClientThread().execute();
            }
        });
    }

    class ClientThread extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params)
        {
            Log.d("Beg Client Thread", "Start now");
            try {
                socket = new Socket(HOST, PORT);
                Log.d("SOCKET CONNECTION", "socket created");
                OutputStream os = socket.getOutputStream();
                String msg = "hora:07:30";
                os.write(msg.getBytes(Charset.forName("UTF-8")));
                Log.d("SOCKET CONNECTION", "Socket connection successful!");
            } catch (IOException e) {
                Log.d("SOCKET CONNECTION", "Socket connection erorr");
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    class ServerThread implements Runnable {

        @Override
        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    CommunicationThread communicationThread = new CommunicationThread(socket);
                    new Thread(communicationThread).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;
        private BufferedReader input;

        public CommunicationThread(Socket clientSocket)
        {
            this.clientSocket = clientSocket;
            try {
                this.input = new BufferedReader(
                        new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String msg = input.readLine();
                    updateConversationHandler.post(new UpdateUIThread(msg));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class UpdateUIThread implements Runnable {

        private String msg;

        public UpdateUIThread(String msg)
        {
            this.msg = msg;
        }

        @Override
        public void run() {
            text.setText(msg);
        }
    }
}
