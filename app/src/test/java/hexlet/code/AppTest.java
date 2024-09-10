package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.Utils;
import io.javalin.Javalin;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {
    public static Javalin app;
    public static String localUrl;
    public static MockWebServer mockWebServer;

    @BeforeAll
    public static void setUp() throws Exception {
        App.useH2DatabaseOnStart = true;
        App.enableDevLoggingOnStart = false;

        app = App.getApp();
        app.start(7070);

        localUrl = "http://localhost:" + app.port();

        mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse().setBody(
                """
                <title>Тестовый сайт</title>
                <meta name ="description" content="Описание тестового сайта"/>
                <h1> <a></a> h1 тестового сайта</h1>
                """));
        mockWebServer.start();
    }

    @AfterAll
    public static void stopApp() throws Exception {
        app.stop();
        mockWebServer.shutdown();
    }

    @BeforeEach
    void createTable() throws SQLException, IOException {
        Utils.createDatabaseTables();
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

    @Test
    @DisplayName("Test create and check URL and flash")
    void testCreateAndCreateURL() throws Exception {
        String urlString = mockWebServer.url("/").toString();

        if (urlString.endsWith("/")) {
            urlString = urlString.substring(0, urlString.length() - 1);
        }

        HttpResponse<String> responseCreate = Unirest
                .post(localUrl + NamedRoutes.urlsPath())
                .field("url", urlString)
                .asString();

        assertThat(responseCreate.getStatus()).isEqualTo(200);

        String responseCreateBody = responseCreate.getBody();
        assertThat(responseCreateBody).contains(urlString);
        assertThat(responseCreateBody).contains("Страница успешно добавлена");

        Optional<Url> urlAsOptional = UrlRepository.findByName(urlString);

        assertThat(urlAsOptional.isPresent()).isTrue();

        HttpResponse<String> responseCheck = Unirest
                .post(localUrl + NamedRoutes.checkUrlPath(urlAsOptional.get().getId()))
                .asString();

        assertThat(responseCheck.getStatus()).isEqualTo(200);

        String responseCheckBody = responseCheck.getBody();
        assertThat(responseCheckBody).contains(urlString);
        assertThat(responseCheckBody).contains("Страница успешно проверена");
        assertThat(responseCheckBody).contains("Тестовый сайт");
        assertThat(responseCheckBody).contains("Описание тестового сайта");
        assertThat(responseCheckBody).contains("h1 тестового сайта");
    }

    @Test
    @DisplayName("Test create and incorrect check URL and flash")
    void testCreateAndIncorrectCreateURL() throws Exception {
        String urlString = mockWebServer.url("/").toString();

        if (urlString.endsWith("/")) {
            urlString = urlString.substring(0, urlString.length() - 1);
        }

        urlString = urlString + "0";

        HttpResponse<String> responseCreate = Unirest
                .post(localUrl + NamedRoutes.urlsPath())
                .field("url", urlString)
                .asString();

        assertThat(responseCreate.getStatus()).isEqualTo(200);

        String responseCreateBody = responseCreate.getBody();
        assertThat(responseCreateBody).contains(urlString);
        assertThat(responseCreateBody).contains("Страница успешно добавлена");

        Optional<Url> urlAsOptional = UrlRepository.findByName(urlString);

        assertThat(urlAsOptional.isPresent()).isTrue();

        HttpResponse<String> responseCheck = Unirest
                .post(localUrl + NamedRoutes.checkUrlPath(urlAsOptional.get().getId()))
                .asString();

        assertThat(responseCheck.getStatus()).isEqualTo(200);

        String responseCheckBody = responseCheck.getBody();
        assertThat(responseCheckBody).contains(urlString);
        assertThat(responseCheckBody).contains(
                String.format("Некорректный адрес: %s", urlString));
    }
}
