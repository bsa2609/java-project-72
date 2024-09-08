package hexlet.code.dto;

public class BasePage {
    private String flash;
    private String flashType;
    private String title;

    public BasePage() {
        flashType = "info";
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

    public String getFlashType() {
        return flashType;
    }

    public void setFlashType(String flashType) {
        if (flashType == null || flashType.isBlank()) {
            this.flashType = "info";

        } else {
            this.flashType = flashType;
        }
    }
}
