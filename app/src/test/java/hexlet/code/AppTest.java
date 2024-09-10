package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.Utils;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {
    public static Javalin app;
    public static String localUrl;

    @BeforeAll
    public static void setUp() throws Exception {
        Utils.useH2DatabaseOnStart = true;
        Utils.enableDevLogging = false;

        app = App.getApp();
        app.start(7070);

        localUrl = "http://localhost:" + app.port();
    }

    @AfterAll
    public static void stopApp() {
        app.stop();
    }

    @BeforeEach
    void createTable() throws SQLException, IOException {
        Utils.createDBTables();
    }

    @Test
    @DisplayName("Test main page")
    void testMainPage() throws Exception {
        HttpResponse<String> response = Unirest
                .get(localUrl + NamedRoutes.rootPath())
                .asString();

        assertThat(response.getStatus()).isEqualTo(200);

        String responseBody = response.getBody();
        assertThat(responseBody).contains("Анализатор страниц");
        assertThat(responseBody).contains("Бесплатно проверяйте сайты на SEO пригодность");

    }

    @Test
    @DisplayName("Test create URL and flash")
    void testCreateURL() throws Exception {
        HttpResponse<String> response = Unirest
                .post(localUrl + NamedRoutes.urlsPath())
                .field("url", "https://www.mail.ru")
                .asString();

        assertThat(response.getStatus()).isEqualTo(200);

        String responseBody = response.getBody();
        assertThat(responseBody).contains("https://www.mail.ru");
        assertThat(responseBody).contains("Страница успешно добавлена");

        assertThat(UrlRepository.findByName("https://www.mail.ru").isPresent()).isTrue();
    }

    @Test
    @DisplayName("Test open created URL")
    void testOpenCreatedURL() throws Exception {
        Url url = new Url("https://www.mail.ru");
        UrlRepository.save(url);

        HttpResponse<String> response = Unirest
                .get(localUrl + NamedRoutes.urlPath(url.getId()))
                .asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("Сайт: https://www.mail.ru");
    }

    @Test
    @DisplayName("Test list of created URLs")
    void testListOfCreatedURLs() throws Exception {
        Url url1 = new Url("https://www.mail.ru");
        UrlRepository.save(url1);

        Url url2 = new Url("https://www.yandex.ru");
        UrlRepository.save(url2);

        Url url3 = new Url("https://www.nic.ru");
        UrlRepository.save(url3);

        HttpResponse<String> response = Unirest
                .get(localUrl + NamedRoutes.urlsPath())
                .asString();

        assertThat(response.getStatus()).isEqualTo(200);

        String responseBody = response.getBody();
        assertThat(responseBody).contains("https://www.mail.ru");
        assertThat(responseBody).contains("https://www.yandex.ru");
        assertThat(responseBody).contains("https://www.nic.ru");
    }

    @Test
    @DisplayName("Test incorrect URL and flash")
    void testIncorrectURL() throws Exception {
        HttpResponse<String> response = Unirest
                .post(localUrl + NamedRoutes.urlsPath())
                .field("url", "https://www.mail.ru:456321254799985544")
                .asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("Некорректный URL: https://www.mail.ru:456321254799985544");
    }

    @Test
    @DisplayName("Test URL already exists and flash")
    void testURLAlreadyExists() throws Exception {
        Url url = new Url("https://www.mail.ru");
        UrlRepository.save(url);

        HttpResponse<String> response = Unirest
                .post(localUrl + NamedRoutes.urlsPath())
                .field("url", "https://www.mail.ru")
                .asString();

        assertThat(response.getStatus()).isEqualTo(200);

        String responseBody = response.getBody();
        assertThat(responseBody).contains("https://www.mail.ru");
        assertThat(responseBody).contains(String.format("Страница уже существует. ID: %s", url.getId()));
    }

    @Test
    @DisplayName("Test unknown URL ID")
    void testUnknownURLID() throws Exception {
        HttpResponse<String> response = Unirest
                .get(localUrl + NamedRoutes.urlPath(222L))
                .asString();

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getBody()).contains("Url id = 222 not found");
    }

    @Test
    @DisplayName("Test not long type URL ID")
    void testNotLongTypeURLID() throws Exception {
        HttpResponse<String> response = Unirest
                .get(localUrl + NamedRoutes.urlPath("asaslkj"))
                .asString();

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getBody()).contains("Url id = asaslkj not Long type, url not found");
    }
}
