package server;

import network.TCPConnection;
import network.TCPObserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ChatServer implements TCPObserver {

    //СПИСОК ВСЕХ СОЕДИНЕНИЙ НА СЕРВЕРЕ
    public final ArrayList<TCPConnection> tcpConnections = new ArrayList<TCPConnection>();

    private ChatServer() {
        System.out.println("Server Running...");
        //слушает порт и принимает данные
        try {
            ServerSocket serverSocket = new ServerSocket(8023);
            while (true) {
                try {
                    //accept - ждет нового соединения, и когда оно приходит, возвращает объект сокета, который связан с этим соединением
                    //себя включаем как листенера
                    new TCPConnection(serverSocket.accept(), this);
                } catch (IOException e) {
                    System.out.println("TCPConnection " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new ChatServer();
    }

    //нельзя одновременно из разных потоков в них попасть
    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        tcpConnections.add(tcpConnection);
        sendMessageToAll("Client connected " + tcpConnection);
    }

    @Override
    public synchronized void onReceiveMessage(TCPConnection tcpConnection, String message) {
        sendMessageToAll(message);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        tcpConnections.remove(tcpConnection);
        sendMessageToAll("Client disconnected " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception " + e.getMessage());
    }

    private void sendMessageToAll(String message) {
        System.out.println(message);
        for (TCPConnection connection : tcpConnections) {
            connection.sendMessage(message);
        }
    }
}


