package hexlet.code.dto;

import hexlet.code.util.FlashType;

import java.util.Objects;

public class BasePage {
    private String flash;
    private FlashType flashType;
    private String title;

    public BasePage() {
        flashType = FlashType.INFO;
    }

    public String getFlash() {
        return flash;
    }

    public void setFlash(String flash) {
        this.flash = flash;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public FlashType getFlashType() {
        return flashType;
    }

    public void setFlashType(FlashType flashType) {
        this.flashType = Objects.requireNonNullElse(flashType, FlashType.INFO);
    }
}
