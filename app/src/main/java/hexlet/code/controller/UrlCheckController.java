package hexlet.code.controller;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.FlashType;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.Utils;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;

import java.util.Map;

public final class UrlCheckController {
    public static void check(Context ctx) throws Exception {
        Long id = UrlController.extractIdFromPathParam(ctx);
        Url url = UrlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse(
                        String.format("Url with id = %s not found", id)));

        String urlString = url.getName();
        int statusCode;
        String body;

        try {
            HttpResponse<String> response = Unirest
                    .get(urlString)
                    .asString();

            statusCode = response.getStatus();
            body = response.getBody();

        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный адрес");
            ctx.sessionAttribute("flashType", FlashType.DANGER);

            ctx.redirect(NamedRoutes.urlPath(url.getId()));

            return;
        }

        Map<String, String> tags = Utils.parseHTML(body);
        UrlCheck urlCheck = new UrlCheck(statusCode, tags.get("title"), tags.get("h1"), tags.get("description"));

        url.addUrlCheck(urlCheck);
        UrlCheckRepository.save(urlCheck);

        ctx.sessionAttribute("flash", "Страница успешно проверена");
        ctx.sessionAttribute("flashType", FlashType.SUCCESS);

        ctx.redirect(NamedRoutes.urlPath(url.getId()));
    }
}
