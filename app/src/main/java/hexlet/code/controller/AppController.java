package hexlet.code.controller;

import hexlet.code.dto.main.MainPage;
import io.javalin.http.Context;

import static io.javalin.rendering.template.TemplateUtil.model;

public final class AppController {
    public static void index(Context ctx) {
        var page = new MainPage();

        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));

        ctx.render("index.jte", model("page", page));
    }
}
