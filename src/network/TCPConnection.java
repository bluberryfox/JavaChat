package network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.BatchUpdateException;

public class TCPConnection {
    private final Socket socket;
    private final Thread thread;
    private final BufferedReader bufferedReader;
    private final BufferedWriter bufferedWriter;
    private final TCPObserver tcpObserver;


    public TCPConnection(TCPObserver tcpObserver, String IPAdress, int port) throws IOException{
        this(new Socket(IPAdress, port), tcpObserver);
    }
    public TCPConnection(Socket socket, TCPObserver tcpObserver) throws IOException {
        this.socket = socket;
        this.tcpObserver = tcpObserver;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
        thread = new Thread(new Runnable() {
            @Override
            public void run()  {
                try {
                    tcpObserver.onConnectionReady(TCPConnection.this);
                    while(!thread.isInterrupted()) {
                        tcpObserver.onReceiveMessage(TCPConnection.this, bufferedReader.readLine());
                    }
                } catch (IOException exception) {
                    tcpObserver.onException(TCPConnection.this, exception);

                } finally{
                    tcpObserver.onDisconnect(TCPConnection.this);
                }

            }
        });
        thread.start();

    }
    //чтобы обращаться к следующим методам из разных потоков - synchronized
    public synchronized void sendMessage(String message)  {
        try {
            bufferedWriter.write(message + "\n");
            //сбросить все из буфера и отправить
            bufferedWriter.flush();
        } catch (IOException e) {
            disconnect();
        }
    }
    public synchronized void disconnect()  {
        thread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            tcpObserver.onException(TCPConnection.this, e);
        }
    }
    //для логов чтобы видеть кто подключился, кто отключился
    @Override
    public String toString() {
        return "TCPConnection " + socket.getInetAddress() + " " + socket.getPort();
    }

}