package Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerStart {
    private static final int DEFAULT_HTTP_PORT = 8080;
    private static final int DEFAULT_PROTOBUF_PORT = 9090;


    public static void main(String[] args) {
        int httpPort = DEFAULT_HTTP_PORT;
        int protobufPort = DEFAULT_PROTOBUF_PORT;
        
        for (int i = 0; i < args.length; i++) {
            if ((args[i].equals("--http-port") || args[i].equals("--port")) && i + 1 < args.length) {
                try {
                    httpPort = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid HTTP port number. Using default port " + DEFAULT_HTTP_PORT);
                }
            } else if (args[i].equals("--protobuf-port") && i + 1 < args.length) {
                try {
                    protobufPort = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid protobuf port number. Using default port " + DEFAULT_PROTOBUF_PORT);
                }
            }
        }

        try {
            WebHttpServer.start(httpPort);
            System.out.println("HTTP web server started on port " + httpPort);
            System.out.println("Open http://localhost:" + httpPort + "/login");
        } catch (IOException e) {
            System.err.println("Error starting HTTP web server: " + e.getMessage());
            return;
        }

        startProtobufSocketServer(protobufPort);
    }

    private static void startProtobufSocketServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Protobuf socket server started on port " + port);
            System.out.println("Waiting for protobuf clients to connect...");

            int clientID = 0;
            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientID++;
                final int id = clientID;
                System.out.println("Client " + id + " connected from " + 
                    clientSocket.getInetAddress().getHostAddress());

                Thread clientThread = new Thread(() -> {
                    try {
                        processConnection(clientSocket, id);
                    } catch (IOException e) {
                        System.err.println("Error handling client " + id + ": " + e.getMessage());
                    } finally {
                        try {
                            if (clientSocket != null && !clientSocket.isClosed()) {
                                clientSocket.close();
                            }
                            System.out.println("Client " + id + " disconnected.");
                        } catch (IOException e) {
                            System.err.println("Error closing client socket for client " + id + ": " + e.getMessage());
                        }
                    }
                });
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Error starting protobuf socket server: " + e.getMessage());
        }
    }


    private static void processConnection(Socket clientSocket, int clientID) throws IOException {
        try (InputStream in = clientSocket.getInputStream();
             OutputStream out = clientSocket.getOutputStream()) {

            int type = in.read();
            if (type == 1) {
                AppToServerHandler.handleAppToServer(in, out, clientID);
            } else if (type == 2) {
                WebToServerHandler.handleWebToServer(in, out, clientID);
            } else {
                System.err.println("Unknown connection type from client " + clientID);
            }
        } catch (IOException e) {
            System.err.println("Error processing connection from client " + clientID + ": " + e.getMessage());
        } finally {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        }
    }


}
