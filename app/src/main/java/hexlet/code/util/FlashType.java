package hexlet.code.util;

public enum FlashType {
    INFO("info"),
    SUCCESS("success"),
    WARNING("warning"),
    DANGER("danger");

    private String title;

    FlashType(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
