package Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import Server.App.AppToServerHandler;

@SpringBootApplication
public class ServerStart {

    public static void main(String[] args) {
        SpringApplication.run(ServerStart.class, args);
    }

    @Bean
    CommandLineRunner appServer(@Value("${app.port:8080}") int appPort) {
        return args -> new Thread(() -> startAppServer(appPort)).start();
    }

    @Bean
    ProtobufHttpMessageConverter protobufHttpMessageConverter() {
        return new ProtobufHttpMessageConverter();
    }

    @Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("(Content-Type", "Authorization");
            }
        };
    }

    private static void startAppServer(int appPort) {
        try (ServerSocket appServerSocket = new ServerSocket(appPort)) {
            System.out.println("App server started on port " + appPort);
            System.out.println("Waiting for app clients to connect...");

            int appID = 0;
            while (true) {
                Socket appSocket = appServerSocket.accept();
                appID++;
                final int id = appID;
                System.out.println("App " + id + " connected from " + 
                    appSocket.getInetAddress().getHostAddress());

                new Thread(() -> processAppConnection(appSocket, id)).start();
            }    
        } catch (IOException e) {
            System.err.println("Error starting app server: " + e.getMessage());
        }     
    }

    private static void processAppConnection(Socket clientSocket, int clientID) {
        try (InputStream in = clientSocket.getInputStream(); 
            OutputStream out = clientSocket.getOutputStream()) {
           
            AppToServerHandler.handleAppToServer(in, out, clientID);

        } catch (IOException e) {
            System.err.println("Error processing connection from client " + clientID + ": " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
                System.out.println("App client " + clientID + " disconnected.");
            } catch (IOException e) {
                System.err.println("Error closing socket for client " + clientID + ": " + e.getMessage());
            }
        }
    }

    private static void startWebServer(int webPort) {
        try (ServerSocket webServerSocket = new ServerSocket(webPort)) {
            System.out.println("Web server started on port " + webPort);
            System.out.println("Waiting for web clients to connect...");

            int webID = 0;
            while (true) {
                Socket webSocket = webServerSocket.accept();
                webID++;
                final int id = webID;
                System.out.println("Web " + id + " connected from " + 
                    webSocket.getInetAddress().getHostAddress());

                new Thread(() -> processWebConnection(webSocket, id)).start();
            }    
        } catch (IOException e) {
            System.err.println("Error starting web server: " + e.getMessage());
        }     
    }


    private static void processWebConnection(Socket webSocket, int webID) {
        try (InputStream in = webSocket.getInputStream();
            OutputStream out = webSocket.getOutputStream()) {

            //WebRequestHandler.handleRequest(in, out, webID);

        } catch (IOException e) {
            System.err.println("Error processing web connection from client " + webID + ": " + e.getMessage());
        } finally {
            try {
                if (webSocket != null && !webSocket.isClosed()) {
                    webSocket.close();
                }
                System.out.println("Web client " + webID + " disconnected.");
            } catch (IOException e) {
                System.err.println("Error closing web socket for client " + webID + ": " + e.getMessage());
            }
        }

    }


    


}