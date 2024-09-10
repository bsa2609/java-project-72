package hexlet.code.controller;

import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.FlashType;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.Utils;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import java.sql.SQLException;
import java.util.List;

import static io.javalin.rendering.template.TemplateUtil.model;

public final class UrlController {
    public static void index(Context ctx) throws SQLException {
        List<Url> urls = UrlRepository.getEntities();
        UrlsPage page = new UrlsPage(urls);

        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));

        ctx.render("urls/index.jte", model("page", page));
    }

    public static void show(Context ctx) throws Exception {
        Long id = extractIdFromPathParam(ctx);
        Url url = UrlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse(
                        String.format("Url with id = %s not found", id)));

        UrlPage page = new UrlPage(url);

        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));

        ctx.render("urls/show.jte", model("page", page));
    }

    public static void create(Context ctx) throws SQLException {
        String urlString = ctx.formParamAsClass("url", String.class)
                .getOrDefault("")
                .trim()
                .toLowerCase();

        String name;

        try {
            name = Utils.parseUrlString(urlString);

        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flashType", FlashType.DANGER);

            ctx.redirect(NamedRoutes.rootPath());

            return;
        }

        if (UrlRepository.findByName(name).isEmpty()) {
            Url url = new Url(name);
            UrlRepository.save(url);

            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flashType", FlashType.SUCCESS);

        } else {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flashType", FlashType.INFO);
        }

        ctx.redirect(NamedRoutes.urlsPath());
    }

    public static Long extractIdFromPathParam(Context ctx) throws NotFoundResponse {
        Long id;

        try {
            id = ctx.pathParamAsClass("id", Long.class).getOrDefault(0L);

        } catch (Exception e) {
            throw new NotFoundResponse(
                    String.format("The \"%s\" id is not numeric, URL not found", ctx.pathParam("id")));
        }

        return id;
    }

}
