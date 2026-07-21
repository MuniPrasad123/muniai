package com.muniai.ai.infrastructure;
import static com.github.tomakehurst.wiremock.client.WireMock.*; import static org.junit.jupiter.api.Assertions.*;
import com.github.tomakehurst.wiremock.WireMockServer; import com.muniai.ai.domain.*; import com.muniai.shared.exception.*; import java.net.URI; import java.time.Duration;
import org.junit.jupiter.api.*; import org.springframework.web.reactive.function.client.WebClient;
class OllamaLanguageModelProviderTest {
 WireMockServer server;
 @BeforeEach void start(){server=new WireMockServer(0);server.start();configureFor("localhost",server.port());}
 @AfterEach void stop(){if(server.isRunning())server.stop();}
 OllamaLanguageModelProvider provider(String model,Duration timeout){var props=new OllamaConfigurationProperties(URI.create(server.baseUrl()),model,timeout);return new OllamaLanguageModelProvider(WebClient.builder().baseUrl(server.baseUrl()).build(),props);}
 @Test void mapsOllamaResponse(){stubFor(post("/api/chat").willReturn(okJson("{\"model\":\"llama3.2:3b\",\"message\":{\"role\":\"assistant\",\"content\":\"hello\"}}")));ChatCompletion c=provider("llama3.2:3b",Duration.ofSeconds(10)).complete(new ChatCompletionRequest("hi"));assertEquals("hello",c.answer());assertEquals("ollama",c.provider());verify(postRequestedFor(urlEqualTo("/api/chat")).withRequestBody(containing("\"stream\":false")));}
 @Test void mapsUnavailable(){stubFor(post("/api/chat").willReturn(serverError()));assertThrows(AiProviderUnavailableException.class,()->provider("model",Duration.ofSeconds(10)).complete(new ChatCompletionRequest("hi")));}
 @Test void mapsTimeout(){stubFor(post("/api/chat").willReturn(okJson("{}").withFixedDelay(500)));assertThrows(AiProviderTimeoutException.class,()->provider("model",Duration.ofMillis(50)).complete(new ChatCompletionRequest("hi")));}
 @Test void rejectsMalformedResponse(){stubFor(post("/api/chat").willReturn(okJson("{\"message\":{}}")));assertThrows(MalformedProviderResponseException.class,()->provider("model",Duration.ofSeconds(10)).complete(new ChatCompletionRequest("hi")));}
 @Test void rejectsMissingModel(){assertThrows(MissingModelException.class,()->provider(" ",Duration.ofSeconds(1)).complete(new ChatCompletionRequest("hi")));}
}
