package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.controller.AppController;
import hexlet.code.controller.UrlCheckController;
import hexlet.code.controller.UrlController;
import hexlet.code.repository.BaseRepository;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.Utils;
import io.javalin.Javalin;

import io.javalin.rendering.template.JavalinJte;

public class App {
    public static Javalin getApp() throws Exception {
        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(Utils.getDataBaseUrl());

        BaseRepository.dataSource = new HikariDataSource(hikariConfig);

        Utils.createDBTables();

        var app = Javalin.create(config -> {
            if (Utils.enableDevLogging) {
                config.bundledPlugins.enableDevLogging();
            }
            config.fileRenderer(new JavalinJte(Utils.createTemplateEngine()));
        });

        app.get(NamedRoutes.rootPath(), AppController::index);
        app.get(NamedRoutes.urlsPath(), UrlController::index);
        app.post(NamedRoutes.urlsPath(), UrlController::create);
        app.get(NamedRoutes.urlPath("{id}"), UrlController::show);
        app.post(NamedRoutes.checkUrlPath("{id}"), UrlCheckController::check);

        return app;
    }

    public static void main(String[] args) throws Exception {
        var app = getApp();
        app.start(Utils.getPort());
    }
}
