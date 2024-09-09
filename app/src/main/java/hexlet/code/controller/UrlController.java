package hexlet.code.controller;

import hexlet.code.dto.main.MainPage;
import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.FlashType;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

//import java.net.MalformedURLException;
import java.net.URI;
//import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static io.javalin.rendering.template.TemplateUtil.model;

public class UrlController {
    public static void index(Context ctx) throws SQLException {
        List<Url> urls = UrlRepository.getEntities();
        UrlsPage page = new UrlsPage(urls);

        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));

        ctx.render("urls/index.jte", model("page", page));
    }

    public static void show(Context ctx) throws Exception {
        Long id;

        try {
            id = ctx.pathParamAsClass("id", Long.class).getOrDefault(0L);
        } catch (Exception e) {
            throw new NotFoundResponse("Url id = " + ctx.pathParam("id") + " not Long type, url not found");
        }

        Url url = UrlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Url id = " + id + " not found"));

        UrlPage page = new UrlPage(url);

        ctx.render("urls/show.jte", model("page", page));
    }

    public static void create(Context ctx) throws SQLException {
        var urlString = ctx.formParamAsClass("url", String.class)
                .getOrDefault("")
                .trim()
                .toLowerCase();

        try {
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
            Optional<Url> urlAsOptional = UrlRepository.findByName(name);

            if (urlAsOptional.isEmpty()) {
                Url url = new Url(name);
                UrlRepository.save(url);

                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.sessionAttribute("flashType", FlashType.SUCCESS);

            } else {
                ctx.sessionAttribute("flash",
                        String.format("Страница уже существует. ID: %s", urlAsOptional.get().getId()));
                ctx.sessionAttribute("flashType", FlashType.WARNING);
            }

            ctx.status(200);
            UrlController.index(ctx);

        } catch (SQLException e) {
            ctx.sessionAttribute("flash",
                    String.format("Ошибка базы данных: %s", e.getMessage()));
            ctx.sessionAttribute("flashType", FlashType.DANGER);
            ctx.status(400);

            AppController.index(ctx);

        } catch (Exception e) {
            ctx.sessionAttribute("flash",
                    String.format("Некорректный URL: %s", urlString));
            ctx.sessionAttribute("flashType", FlashType.DANGER);
            ctx.status(400);

            AppController.index(ctx);
        }
     }
}
