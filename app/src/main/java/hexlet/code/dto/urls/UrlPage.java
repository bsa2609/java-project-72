package hexlet.code.dto.urls;

import hexlet.code.dto.BasePage;
import hexlet.code.model.Url;

public final class UrlPage extends BasePage {
    private Url url;

    public UrlPage(Url url) {
        setTitle("Анализируемая страница");
        this.url = url;
    }

    public Url getUrl() {
        return url;
    }

    public void setUrl(Url url) {
        this.url = url;
    }
}
