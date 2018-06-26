package org.swaggertools.demo;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.swaggertools.demo.web.PetsResource;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App extends ResourceConfig {

    public App() {
        super(
                PetsResource.class,
                JacksonFeature.class
        );
    }

    private static final URI BASE_URI = URI.create("http://localhost:8080");

    public static void main(String[] args) {
        try {
            HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, new App(), true);
            System.out.println("Server started at port 8080.");
            Thread.currentThread().join();
        } catch (Exception ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
