package hexlet.code.util;

public final class NamedRoutes {
    private static final String ROOT_PATH = "/";
    private static final String URLS_PATH = "/urls";

    public static String rootPath() {
        return ROOT_PATH;
    }

    public static String urlsPath() {
        return URLS_PATH;
    }

    public static String urlPath(Long id) {
        return urlPath(String.valueOf(id));
    }

    public static String urlPath(String id) {
        return URLS_PATH + "/" + id;
    }

    public static String checkUrlPath(Long id) {
        return checkUrlPath(String.valueOf(id));
    }

    public static String checkUrlPath(String id) {
        return URLS_PATH + "/" + id + "/check";
    }
}
