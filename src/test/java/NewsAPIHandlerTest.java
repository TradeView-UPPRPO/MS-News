import API.Article;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NewsAPIHandlerTest {
    private MockWebServer mockWebServer;
    private NewsAPIHandler newsAPIHandler;
    private final String testApiKey = "test-api-key";

    @BeforeEach
    void setUp() throws IOException {
        // Запускаем мок-сервер
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Создаем экземпляр обработчика с URL мок-сервера
        String baseUrl = mockWebServer.url("/").toString();
        newsAPIHandler = new NewsAPIHandler(baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Останавливаем сервер после теста
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    void getResponse_successfulResponse_returnsArticles() throws Exception {
        // Подготавливаем мок-ответ
        String jsonResponse = """
        {
            "status": "ok",
            "articles": [
                {"title": "Test News", "description": "Test description"}
            ]
        }
        """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Заменяем API-ключ тестовым значением
        NewsAPIHandler spyHandler = Mockito.spy(newsAPIHandler);
        doReturn(testApiKey).when(spyHandler).getApiKey();

        // Выполняем тестируемый метод
        CompletableFuture<List<Article>> future = spyHandler.getResponse(
                "bitcoin", "en", "publishedAt");

        List<Article> articles = future.get();

        // Проверяем результаты
        assertEquals(1, articles.size());
        assertEquals("Test News", articles.get(0).getTitle());

        // Проверяем что запрос был к мок-серверу
        RecordedRequest request = mockWebServer.takeRequest();
        assert request.getPath() != null;
        assertTrue(request.getPath().contains("q=bitcoin"));
        assertTrue(request.getPath().contains("apiKey=" + testApiKey));
    }

    @Test
    void getResponse_httpError_throwsException() {
        // Arrange
        // Настраиваем мок-сервер на возврат 500 ошибки
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // Создаем spy-объект для мокирования getApiKey()
        NewsAPIHandler spyHandler = spy(newsAPIHandler);
        doReturn(testApiKey).when(spyHandler).getApiKey();

        // Act
        CompletableFuture<List<Article>> future = spyHandler.getResponse(
                "bitcoin", "en", "publishedAt");

        // Assert
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);

        // Проверяем что исключение содержит нужные детали
        assertInstanceOf(RuntimeException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Failed to fetch news"));
        assertInstanceOf(IOException.class, exception.getCause().getCause());

        // Дополнительно проверяем что запрос был сделан с правильными параметрами
        try {
            RecordedRequest request = mockWebServer.takeRequest();
            assert request.getPath() != null;
            assertTrue(request.getPath().contains("q=bitcoin"));
            assertTrue(request.getPath().contains("language=en"));
            assertTrue(request.getPath().contains("sortBy=publishedAt"));
        } catch (InterruptedException e) {
            fail("Failed to verify request", e);
        }
    }

    @Test
    void getResponse_apiErrorStatus_throwsException() {
        // Arrange
        // Подготавливаем ответ со статусом error
        String errorJson = """
        {
            "status": "error",
            "code": "apiKeyInvalid",
            "message": "Your API key is invalid"
        }
        """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(errorJson)
                .addHeader("Content-Type", "application/json"));

        // Настраиваем spy
        NewsAPIHandler spyHandler = spy(newsAPIHandler);
        doReturn(testApiKey).when(spyHandler).getApiKey();

        // Act
        CompletableFuture<List<Article>> future = spyHandler.getResponse(
                "bitcoin", "en", "publishedAt");

        // Assert
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);

        // Проверяем тип и сообщение исключения
        assertInstanceOf(RuntimeException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Failed to fetch news"));

        // Проверяем что запрос был корректным
        try {
            RecordedRequest request = mockWebServer.takeRequest();
            assertEquals("GET", request.getMethod());
            assert request.getPath() != null;
            assertTrue(request.getPath().contains("apiKey=" + testApiKey));
        } catch (InterruptedException e) {
            fail("Failed to verify request", e);
        }
    }
}