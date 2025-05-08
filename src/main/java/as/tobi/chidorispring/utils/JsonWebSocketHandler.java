package as.tobi.chidorispring.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

@Component
public class JsonWebSocketHandler implements WebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }


    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();

            // Обработка JSON
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode json = mapper.readTree(payload);
                // Ваша логика обработки

                // Отправка ответа
                String response = mapper.writeValueAsString(
                        Map.of("status", "received", "yourMessage", json)
                );
                session.sendMessage(new TextMessage(response));
            } catch (JsonProcessingException e) {
                session.sendMessage(new TextMessage("{\"error\":\"Invalid JSON\"}"));
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        sessions.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}