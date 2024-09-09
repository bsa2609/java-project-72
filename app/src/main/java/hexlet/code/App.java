package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.controller.AppController;
import hexlet.code.controller.UrlController;
import hexlet.code.repository.BaseRepository;
import hexlet.code.util.DatabaseType;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import io.javalin.rendering.template.JavalinJte;

public class App {
    private static final String H2_DATABASE_URL = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;";
    private static DatabaseType typeOfDatabaseUsed;

    private static int getPort() {
        return Integer.valueOf(
                System.getenv().getOrDefault(
                        "PORT",
                        "7070"
                )
        );
    }

    public static String getDataBaseUrl(DatabaseType databaseType) throws Exception {
        String dataBaseUrl = "";

        switch (databaseType) {
            case H2:
                dataBaseUrl = H2_DATABASE_URL;
                typeOfDatabaseUsed = DatabaseType.H2;
                break;

            case PSQL:
                String dataBaseUrlFromEnvForPSQL = System.getenv("JDBC_DATABASE_URL");

                if (dataBaseUrlFromEnvForPSQL == null) {
                    throw new Exception("The value of the JDBC_DATABASE_URL environment variable is not set");
                } else {
                    dataBaseUrl = dataBaseUrlFromEnvForPSQL;
                }

                typeOfDatabaseUsed = DatabaseType.PSQL;
                break;

            case CHOICE_OF_THE_SYSTEM:
                String dataBaseUrlFromEnvForAny = System.getenv("JDBC_DATABASE_URL");

                if (dataBaseUrlFromEnvForAny == null) {
                    dataBaseUrl = H2_DATABASE_URL;
                    typeOfDatabaseUsed = DatabaseType.H2;

                } else {
                    dataBaseUrl = dataBaseUrlFromEnvForAny;
                    typeOfDatabaseUsed = DatabaseType.PSQL;
                }

                break;

            default:
                throw new Exception("The database URL for the \"" + databaseType + "\" database type is not defined");
        }

        return dataBaseUrl;
    }

    private static String readResourceFile(String fileName) throws IOException {
        var inputStream = App.class.getClassLoader().getResourceAsStream(fileName);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
        return templateEngine;
    }

    public static Javalin getApp() throws Exception {
        return getApp(DatabaseType.CHOICE_OF_THE_SYSTEM);
    }

    public static Javalin getApp(DatabaseType databaseType) throws Exception {
        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getDataBaseUrl(databaseType));

        var dataSource = new HikariDataSource(hikariConfig);
        var sql = readResourceFile("schema.sql");

        if (typeOfDatabaseUsed == DatabaseType.PSQL) {
            sql = sql.replaceAll("AUTO_INCREMENT", "GENERATED ALWAYS AS IDENTITY");
        }

        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute(sql);
        }

        BaseRepository.dataSource = dataSource;

        var app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.fileRenderer(new JavalinJte(createTemplateEngine()));
        });

        app.get(NamedRoutes.rootPath(), AppController::index);
        app.get(NamedRoutes.urlsPath(), UrlController::index);
        app.post(NamedRoutes.urlsPath(), UrlController::create);
        app.get(NamedRoutes.urlPath("{id}"), UrlController::show);

        return app;
    }

    public static void main(String[] args) throws Exception {
        var app = getApp();
        app.start(getPort());
    }
}
