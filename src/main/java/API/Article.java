package api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Article {
    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("url")
    private String articleUrl;

    @JsonProperty("urlToImage")
    private String imageUrl;

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getArticleUrl() { return articleUrl; }
    public String getImageUrl() { return imageUrl; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setArticleUrl(String articleUrl) { this.articleUrl = articleUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
