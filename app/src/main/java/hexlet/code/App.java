package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;

public class App {
    private static int getPort() {
        return Integer.valueOf(
                System.getenv().getOrDefault(
                        "PORT",
                        "7070"
                )
        );
    }

    public static String getDataBaseUrl() {
        return System.getenv().getOrDefault(
                "JDBC_DATABASE_URL",
                "jdbc:h2:mem:java_project_72;DB_CLOSE_DELAY=-1;"
        );
    }

    public static Javalin getApp() throws Exception {
        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getDataBaseUrl());

        var dataSource = new HikariDataSource(hikariConfig);

        var app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.fileRenderer(new JavalinJte());
        });

        app.get("/", ctx -> {
            ctx.result("Hello World");
        });

        return app;
    }

    public static void main(String[] args) throws Exception {
        var app = getApp();
        app.start(getPort());
    }
}
