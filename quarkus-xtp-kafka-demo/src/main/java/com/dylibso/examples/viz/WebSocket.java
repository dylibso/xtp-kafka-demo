package com.dylibso.examples.viz;

import com.dylibso.examples.kafka.Header;
import com.dylibso.examples.kafka.Record;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/viz/{subscriber}")
@ApplicationScoped
public class WebSocket {

    @Inject
    ObjectMapper mapper;

    Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("subscriber") String id) {
        sessions.put(id, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("subscriber") String id) {
        sessions.remove(id);
    }

    @OnError
    public void onError(Session session, @PathParam("subscriber") String id, Throwable throwable) {
        sessions.remove(id);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("subscriber") String id) {
    }


    @Incoming("internal-result-broadcast")
    CompletionStage<Void> mavg(Record r) {
            var topic = r.topic();
            String[] values = new String(r.value()).split(",");
            StringBuilder sb = new StringBuilder("{");
            for (Header h : r.headers()) {
                sb.append('"')
                        .append(h.key())
                        .append('"')
                        .append(": ")
                        .append('"')
                        .append(new String(h.value()))
                        .append('"')
                        .append(",");

            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("}");
            String jsonString = String.format("""
                    {"type":"%s","date":"%s","price":%s,"headers":%s}""", topic, values[0], values[1], sb);

            broadcast(jsonString);

        return CompletableFuture.completedFuture(null);
    }

    @Incoming("internal-price-broadcast")
    CompletionStage<Void> pricingData(Record r) {
            String[] values = new String(r.value()).split(",");
            String jsonString = String.format("""
                    {"type":"pricing-data","date":"%s","price":%s}""", values[0], values[4]);
            broadcast(jsonString);
        return CompletableFuture.completedFuture(null);
    }

    private void broadcast(String jsonString) {
        sessions.values().forEach(s -> {
            s.getAsyncRemote().sendObject(jsonString, result -> {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }

}
