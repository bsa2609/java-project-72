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
    private static final String H2_DATABASE_URL = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;";
    public static boolean useH2DatabaseOnStart = false;
    public static boolean usingH2DatabaseOnWork = false;
    public static boolean enableDevLogging = true;

    public static String matchRegExp(String input, String regEx, String groupName) {
        Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
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

    public static String getDataBaseUrl() throws Exception {
        String dataBaseUrl = "";

        if (useH2DatabaseOnStart) {
            dataBaseUrl = H2_DATABASE_URL;
            usingH2DatabaseOnWork = true;

        } else {
            String dataBaseUrlFromEnvForAny = System.getenv("JDBC_DATABASE_URL");

            if (dataBaseUrlFromEnvForAny == null) {
                dataBaseUrl = H2_DATABASE_URL;
                usingH2DatabaseOnWork = true;

            } else {
                dataBaseUrl = dataBaseUrlFromEnvForAny;
                usingH2DatabaseOnWork = false;
            }
        }

        return dataBaseUrl;
    }

    private static String readResourceFile(String fileName) throws IOException {
        var inputStream = App.class.getClassLoader().getResourceAsStream(fileName);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    public static void createDBTables() throws IOException, SQLException {
        var sql = readResourceFile("schema.sql");

        if (!usingH2DatabaseOnWork) {
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
