package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.DatabaseType;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import io.javalin.testtools.JavalinTest;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {
    private static Javalin app;

    @BeforeEach
    public void setUp() throws Exception {
        app = App.getApp(DatabaseType.H2);
    }

    @Test
    @DisplayName("Test main page")
    void testMainPage() throws Exception {
        JavalinTest.test(app, ((server, client) -> {
            Response response = client.get(NamedRoutes.rootPath());
            assertThat(response.code()).isEqualTo(200);

            String responseBody = response.body() == null ? "" : response.body().string();
            assertThat(responseBody).contains("Анализатор страниц");
            assertThat(responseBody).contains("Бесплатно проверяйте сайты на SEO пригодность");
        }));
    }

    @Test
    @DisplayName("Test create URL and flash")
    void testCreateURL() throws Exception {
        JavalinTest.test(app, ((server, client) -> {
            String requestBody = "url=http://www.mail.ru";
            Response response = client.post(NamedRoutes.urlsPath(), requestBody);
            assertThat(response.code()).isEqualTo(200);

            String responseBody = response.body() == null ? "" : response.body().string();
            assertThat(responseBody).contains("http://www.mail.ru");
            assertThat(responseBody).contains("Страница успешно добавлена");
        }));

        assertThat(UrlRepository.findByName("http://www.mail.ru").isPresent()).isTrue();
    }

    @Test
    @DisplayName("Test open created URL")
    void testOpenCreatedURL() throws Exception {
        Url url = new Url("http://www.mail.ru");
        UrlRepository.save(url);

        JavalinTest.test(app, ((server, client) -> {
            Response response = client.get(NamedRoutes.urlPath(url.getId()));
            assertThat(response.code()).isEqualTo(200);

            String responseBody = response.body() == null ? "" : response.body().string();
            assertThat(responseBody).contains("Сайт: http://www.mail.ru");
        }));
    }

    @Test
    @DisplayName("Test list of created URLs")
    void testListOfCreatedURLs() throws Exception {
        Url url1 = new Url("http://www.mail.ru");
        UrlRepository.save(url1);

        Url url2 = new Url("http://www.yandex.ru");
        UrlRepository.save(url2);

        Url url3 = new Url("http://www.nic.ru");
        UrlRepository.save(url3);

        JavalinTest.test(app, ((server, client) -> {
            Response response = client.get(NamedRoutes.urlsPath());
            assertThat(response.code()).isEqualTo(200);

            String responseBody = response.body() == null ? "" : response.body().string();
            assertThat(responseBody).contains("http://www.mail.ru");
            assertThat(responseBody).contains("http://www.yandex.ru");
            assertThat(responseBody).contains("http://www.nic.ru");
        }));
    }

    @Test
    @DisplayName("Test incorrect URL and flash")
    void testIncorrectURL() throws Exception {
        JavalinTest.test(app, ((server, client) -> {
            String requestBody = "url=http://www.mail.ru:456321254799985544";
            Response response = client.post(NamedRoutes.urlsPath(), requestBody);
            assertThat(response.code()).isEqualTo(400);

            String responseBody = response.body() == null ? "" : response.body().string();
            assertThat(responseBody).contains("Некорректный URL: http://www.mail.ru:456321254799985544");
        }));
    }

    @Test
    @DisplayName("Test URL already exists and flash")
    void testURLAlreadyExists() throws Exception {
        Url url = new Url("http://www.mail.ru");
        UrlRepository.save(url);

        JavalinTest.test(app, ((server, client) -> {
            String requestBody = "url=http://www.mail.ru";
            Response response = client.post(NamedRoutes.urlsPath(), requestBody);
            assertThat(response.code()).isEqualTo(200);

            String responseBody = response.body() == null ? "" : response.body().string();
            assertThat(responseBody).contains("http://www.mail.ru");
            assertThat(responseBody).contains(String.format("Страница уже существует. ID: %s", url.getId()));
        }));
    }

    @Test
    @DisplayName("Test unknown URL ID")
    void testUnknownURLID() throws Exception {
        JavalinTest.test(app, ((server, client) -> {
            Response response = client.get(NamedRoutes.urlPath(222L));
            assertThat(response.code()).isEqualTo(404);

            String responseBody = response.body() == null ? "" : response.body().string();
            assertThat(responseBody).contains("Url id = 222 not found");
        }));
    }

    @Test
    @DisplayName("Test not long type URL ID")
    void testNotLongTypeURLID() throws Exception {
        JavalinTest.test(app, ((server, client) -> {
            Response response = client.get(NamedRoutes.urlPath("asaslkj"));
            assertThat(response.code()).isEqualTo(404);

            String responseBody = response.body() == null ? "" : response.body().string();
            assertThat(responseBody).contains("Url id = asaslkj not Long type, url not found");
        }));
    }
}
