package com.example;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.paukov.combinatorics3.Generator;

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
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/hello")
public class ExampleResource {
    private ResteasyClient client;
    private ConcurrentSkipListSet<String> responses = new ConcurrentSkipListSet<>();
    private CopyOnWriteArrayList<String> permutations;

    {
        try {
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
            CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
            cm.setMaxTotal(2000); // Increase max total connection to 200
            cm.setDefaultMaxPerRoute(2000); // Increase default max connection per route to 20
            ApacheHttpClient43Engine engine = new ApacheHttpClient43Engine(httpClient);

            client = new ResteasyClientBuilderImpl().httpEngine(engine).build();

            permutations = Generator.permutation(
                            (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'G',
                            (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L',
                            (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R',
                            (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z',
                            (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8',
                            (byte) '9', (byte) '0'
                    )
                    .withRepetitions(0)
                    .stream()
                    .parallel()
                    .map(List::toArray)
                    .map(String::valueOf)
                    .collect(Collectors.toCollection(CopyOnWriteArrayList::new));

            Logger.getGlobal().info("ALL FINE");

        } catch (Exception e) {
            Logger.getGlobal().severe(e.getLocalizedMessage());
        }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        try {
            //Tentar partir o stream em pedaços de 2000 membros que fica dentro de um ciclo.
            //Depois escrevo o ficheiro e apago as 2000 permutações que já fiz.
            permutations.parallelStream()
                    .forEach(permutation ->
                    {
                        Response response = client
                                .target(UriBuilder.fromPath("https://www.mudareganhar.pt/api/Participation/Validate"))
                                .request(MediaType.APPLICATION_JSON)
                                .buildPost(Entity.json(new Request(permutation)))
                                .invoke();

                        responses.add(
                                String.join(
                                        ";",
                                        String.valueOf(response.getStatus()),
                                        response.readEntity(String.class),
                                        permutation
                                )
                        );

                        response.close();
                    });

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
            //Será melhor criar vários ficheiros e depois concatenalos?
            //assim posso ter a concurrencia na escrita.
            //posso dividir em batchs mais pequenos de trabalho e sempre que estes acabem escrevo em ficheiro
            //assim posso ter um fluxo 100% concurrrente?
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