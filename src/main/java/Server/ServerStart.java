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

// Spring Boot Automatically Starts the web server
@SpringBootApplication
public class ServerStart {

    public static void main(String[] args) {
        SpringApplication.run(ServerStart.class, args);
    }

    // Start the app server in a separate thread
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
                .allowedHeaders("Content-Type", "Authorization");
            }
        };
    }

    // Start the app server in a separate thread
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

    // Proccess the connection from the app client
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
}
