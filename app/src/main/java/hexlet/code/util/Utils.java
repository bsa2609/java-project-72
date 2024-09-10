package hexlet.code.util;

import hexlet.code.App;
import hexlet.code.repository.BaseRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class Utils {
    public static int getPort() {
        return Integer.parseInt(System.getenv().getOrDefault("PORT", "7070"));
    }

    public static String getDatabaseUrl() {
        String databaseUrl;

        if (App.useH2DatabaseOnStart) {
            databaseUrl = App.H2_DATABASE_URL;
            App.usingH2DatabaseOnWork = true;

        } else {
            String dataBaseUrlFromEnv = System.getenv("JDBC_DATABASE_URL");

            if (dataBaseUrlFromEnv == null) {
                databaseUrl = App.H2_DATABASE_URL;
                App.usingH2DatabaseOnWork = true;

            } else {
                databaseUrl = dataBaseUrlFromEnv;
                App.usingH2DatabaseOnWork = false;
            }
        }

        return databaseUrl;
    }

    public static String readResourceFile(String fileName) throws IOException {
        var inputStream = App.class.getClassLoader().getResourceAsStream(fileName);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    public static void createDatabaseTables() throws IOException, SQLException {
        var sql = readResourceFile("schema.sql");

        if (!App.usingH2DatabaseOnWork) {
            sql = sql.replaceAll("AUTO_INCREMENT", "GENERATED ALWAYS AS IDENTITY");
        }

        try (var connection = BaseRepository.dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    public static String parseUrlString(String urlString) throws Exception {
        URI uri = new URI(urlString);
        URL urlFromUri = uri.toURL();

        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(urlFromUri.getProtocol());
        nameBuilder.append("://");
        nameBuilder.append(urlFromUri.getHost());

        if (urlFromUri.getPort() > 0) {
            nameBuilder.append(":");
            nameBuilder.append(urlFromUri.getPort());
        }

        String name = nameBuilder.toString();

        return name;
    }

    public static Map<String, String> parseHTML(String body) {
        Map<String, String> tags = new HashMap<>();

        Document doc = Jsoup.parse(body);

        String title = doc.title().trim();
        String h1 = "";
        String description = "";

        Element h1Element = doc.selectFirst("h1");
        if (h1Element != null) {
            h1 = h1Element.text().trim();
        }

        Element descriptionElement = doc.selectFirst("meta[name=description]");
        if (descriptionElement != null) {
            description = descriptionElement.attr("content").trim();
        }

        tags.put("title", title);
        tags.put("h1", h1);
        tags.put("description", description);

        return tags;
    }
}
