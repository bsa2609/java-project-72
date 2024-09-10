package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.controller.AppController;
import hexlet.code.controller.UrlCheckController;
import hexlet.code.controller.UrlController;
import hexlet.code.repository.BaseRepository;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.Utils;
import io.javalin.Javalin;

import io.javalin.rendering.template.JavalinJte;

public class App {
    public static final String H2_DATABASE_URL = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;";
    public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";
    public static boolean useH2DatabaseOnStart = false;
    public static boolean usingH2DatabaseOnWork = false;

    public static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);

        return templateEngine;
    }

    public static Javalin getApp() throws Exception {
        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(Utils.getDatabaseUrl());

        BaseRepository.dataSource = new HikariDataSource(hikariConfig);

        Utils.createDatabaseTables();

        var app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.fileRenderer(new JavalinJte(createTemplateEngine()));
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
