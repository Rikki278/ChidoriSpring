package as.tobi.chidorispring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient kitsuWebClient() {
        return WebClient.builder()
                .baseUrl("https://kitsu.io/api/edge")
                .defaultHeader("Accept", "application/vnd.api+json")
                .defaultHeader("Content-Type", "application/vnd.api+json")
                .defaultHeader("User-Agent", "AnimeApp/1.0")
                .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
                    System.out.println("Request: " + clientRequest.url());
                    return Mono.just(clientRequest);
                }))
                .build();
    }
}