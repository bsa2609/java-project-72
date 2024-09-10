package hexlet.code.dto.urls;

import hexlet.code.dto.BasePage;
import hexlet.code.model.Url;

import java.util.List;

public final class UrlsPage extends BasePage {
    private List<Url> urls;

    public UrlsPage(List<Url> urls) {
        setTitle("Анализируемые страницы");
        this.urls = urls;
    }

    public List<Url> getUrls() {
        return urls;
    }

    public void setUrls(List<Url> urls) {
        this.urls = urls;
    }
}
