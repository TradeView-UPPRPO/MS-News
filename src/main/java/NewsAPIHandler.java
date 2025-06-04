import API.Article;
import API.NewsResponse;
import io.javalin.Javalin;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NewsAPIHandler {
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String getApiKey() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            return prop.getProperty("api_key");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load API key", e);
        }
    }

    public CompletableFuture<List<Article>> getResponse(String keyword, String language, String sortBy) {
        String destUrl = "https://newsapi.org/v2/everything?q=" + keyword +
                "&language=" + language +
                "&sortBy=" + sortBy +
                "&apiKey=" + getApiKey();

        System.out.println("Fetching news from: " + destUrl);

        Request request = new Request.Builder()
                .url(destUrl)
                .get()
                .build();

        return CompletableFuture.supplyAsync(() -> {
            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    System.err.println("News API Response Error: " + responseBody);
                    throw new IOException("Unexpected code " + response);
                }
                NewsResponse apiResponse = objectMapper.readValue(responseBody, NewsResponse.class);
                if ("error".equals(apiResponse.getStatus())) {
                    // News API может возвращать статус "error" с сообщением
                    throw new IOException("News API error: " + responseBody);
                }
                return apiResponse.getArticles();
            } catch (IOException e) {
                throw new RuntimeException("Failed to fetch news", e);
            }
        });
    }

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> {
                cors.add(corsConfig -> {
                    corsConfig.allowCredentials = true;
                    corsConfig.reflectClientOrigin = true;
                });
            });
        }).start(8080);

        NewsAPIHandler handler = new NewsAPIHandler();

        app.get("/api/news", ctx -> {
            ctx.future(() -> {
                long start = System.currentTimeMillis();
                String keyword = ctx.queryParam("keyword");
                String language = ctx.queryParam("language");

                if (language == null || language.isEmpty()) {
                    language = "ru"; // Устанавливаем значение по умолчанию
                }

                String sortBy = ctx.queryParam("sortBy");
                if (sortBy == null || sortBy.isEmpty()) {
                    sortBy = "publishedAt";
                }
                return handler.getResponse(keyword, language, sortBy)
                        .orTimeout(10, TimeUnit.SECONDS)
                        .thenApply(articles -> {
                            if (articles == null) {
                                throw new NullPointerException("Articles is null");
                            }
                            return ctx.json(articles);
                        })
                        .exceptionally(ex -> {
                            long duration = System.currentTimeMillis() - start;
                            System.err.println("Error processing request: " + ex.getMessage());
                            return ctx.status(500).json(Map.of(
                                    "error", "Internal server error",
                                    "details", ex.getMessage() != null ? ex.getMessage() : "Unknown error"
                            ));
                        });
            });
        });

        System.out.println("Server started on http://localhost:8080");
    }
}