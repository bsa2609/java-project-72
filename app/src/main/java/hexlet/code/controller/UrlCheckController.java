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

public class UrlCheckController {
    public static void check(Context ctx) throws Exception {
        Long id;

        try {
            id = ctx.pathParamAsClass("id", Long.class).getOrDefault(0L);
        } catch (Exception e) {
            throw new NotFoundResponse("Url id = " + ctx.pathParam("id") + " not Long type, url not found");
        }

        Url url = UrlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Url id = " + id + " not found"));

        String urlString = url.getName();
        int statusCode = 0;
        String body = "";

        try {
            HttpResponse<String> response = Unirest
                    .get(urlString)
                    .asString();

            statusCode = response.getStatus();

            body = response.getBody();

        } catch (Exception e) {
            ctx.sessionAttribute("flash",
                    String.format("Некорректный адрес: %s", urlString));
            ctx.sessionAttribute("flashType", FlashType.DANGER);

            ctx.redirect(NamedRoutes.urlPath(url.getId()));

            return;
        }

        String title = Utils.matchRegExp(body,
                "(<title[\\s\\S]*?>)(?<title>[\\s\\S]*?)(<\\/title>)",
                "title");

        String description = Utils.matchRegExp(body,
                "(<meta *name=\"description\")([\\s\\S]*?)(=\")(?<description>[\\s\\S]*?)(\"[\\s\\S]*?)?(\\/>)",
                "description");

        String h1 = Utils.matchRegExp(body,
                "(<h1[\\s\\S]*?)(\">)( *?(<a[\\s\\S]*?)(\\/a>))?(?<h1>[\\s\\S]*?)(<\\/h1>)",
                "h1");

        UrlCheck urlCheck = new UrlCheck(statusCode, title, h1, description);
        url.addUrlCheck(urlCheck);
        UrlCheckRepository.save(urlCheck);

        ctx.sessionAttribute("flash",
                "Страница успешно проверена");
        ctx.sessionAttribute("flashType", FlashType.SUCCESS);

        ctx.redirect(NamedRoutes.urlPath(url.getId()));
    }
}
