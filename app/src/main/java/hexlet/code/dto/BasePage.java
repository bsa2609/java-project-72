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

    /**
     * Get flash message.
     * @return - message text
     */
    public String getFlash() {
        return flash;
    }

    /**
     * Set flash message.
     * @param flash - message text
     */
    public void setFlash(String flash) {
        this.flash = flash;
    }

    /**
     * Get page title.
     * @return - title text
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set page title.
     * @param title - title text
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get flash message type.
     * @return - enum.FlashType
     */
    public FlashType getFlashType() {
        return flashType;
    }

    /**
     * Set flash message type.
     * @param flashType - enum.FlashType
     */
    public void setFlashType(FlashType flashType) {
        this.flashType = Objects.requireNonNullElse(flashType, FlashType.INFO);
    }
}
