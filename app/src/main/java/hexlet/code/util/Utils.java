package hexlet.code.util;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.App;
import hexlet.code.repository.BaseRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
    public static String matchRegExp(String input, String regEx, String groupName) {
        Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(groupName);

        } else {
            return "";
        }
    }

    public static int getPort() {
        return Integer.valueOf(System.getenv().getOrDefault("PORT", "7070"));
    }

    public static String getDatabaseUrl() throws Exception {
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

    private static String readResourceFile(String fileName) throws IOException {
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

    public static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);

        return templateEngine;
    }
}
