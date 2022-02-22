package com.example;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Path("/hello")
public class ExampleResource {

    private final ResteasyClient client;
    List<String> responses = new ArrayList<>(2050);

    {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
        cm.setMaxTotal(2000); // Increase max total connection to 200
        cm.setDefaultMaxPerRoute(2000); // Increase default max connection per route to 20
        ApacheHttpClient43Engine engine = new ApacheHttpClient43Engine(httpClient);

        client = new ResteasyClientBuilderImpl().httpEngine(engine).build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        try {
            Response response = client
                    .target(UriBuilder.fromPath("https://www.mudareganhar.pt/api/Participation/Validate"))
                    .request(MediaType.APPLICATION_JSON)
                    .buildPost(Entity.json(new Request("A9KRM763LF4XY")))
                    .invoke();

            responses.add(
                    String.join(";",
                            String.valueOf(response.getStatus()),
                            response.readEntity(String.class)
                    )
            );

            response.close();
        } catch (Exception e) {
            Logger.getGlobal().severe(e.getLocalizedMessage());
        }
        return "Hello RESTEasy Reactive";
    }

    @GET
    @Path("write")
    @Produces(MediaType.TEXT_PLAIN)
    public String write() {
        try {
            Files.write(
                    Paths.get("responses.csv"),
                    responses,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            responses.clear();
        } catch (Exception e) {
            Logger.getGlobal().severe(e.getLocalizedMessage());
        }


        return "Hello RESTEasy Reactive";
    }
}