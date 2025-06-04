package API;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsResponse {
    @JsonProperty("status")
    private String status;

    @JsonProperty("totalResults")
    private int totalResults;

    @JsonProperty("articles")
    private List<Article> articles;

    public String getStatus() { return status; }
    public int getTotalResults() { return totalResults; }
    public List<Article> getArticles() { return articles; }

    public void setStatus(String status) { this.status = status; }
    public void setTotalResults(int totalResults) { this.totalResults = totalResults; }
    public void setArticles(List<Article> articles) { this.articles = articles; }

}



// 60fa7dd76c0e4b279e66bcd35fc359fc
