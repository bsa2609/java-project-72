package hexlet.code.repository;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UrlCheckRepository extends BaseRepository {
    public static void save(UrlCheck urlCheck) throws SQLException {
        String sql =
                """
                INSERT INTO url_checks(
                    url_id,
                    statusCode,
                    title,
                    h1,
                    description,
                    createdAt
                )
                VALUES(?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
            stmt.setLong(1, urlCheck.getUrl().getId());
            stmt.setInt(2, urlCheck.getStatusCode());
            stmt.setString(3, urlCheck.getTitle());
            stmt.setString(4, urlCheck.getH1());
            stmt.setString(5, urlCheck.getDescription());
            stmt.setTimestamp(6, Timestamp.valueOf(urlCheck.getCreatedAt()));
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                urlCheck.setId(generatedKeys.getLong("id"));
            } else {
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        }
    }

    public static Optional<UrlCheck> find(Long id) throws SQLException {
        String sql =
                """
                SELECT
                    url_id,
                    statusCode,
                    title,
                    h1,
                    description,
                    createdAt
                FROM url_checks
                WHERE
                    id = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                Long urlId = resultSet.getLong("url_id");
                int statusCode = resultSet.getInt("statusCode");
                String title = resultSet.getString("title");
                String h1 = resultSet.getString("h1");
                String description = resultSet.getString("description");
                LocalDateTime createdAt = resultSet.getTimestamp("createdAt").toLocalDateTime();

                UrlCheck urlCheck = new UrlCheck(statusCode, title, h1, description);
                urlCheck.setId(id);
                urlCheck.setCreatedAt(createdAt);

                Url url = UrlRepository.find(urlId)
                        .orElseThrow(() -> new SQLException(
                                String.format("Url with id = %s not found", urlId)));

                urlCheck.setUrl(url);

                return Optional.of(urlCheck);
            }
        }

        return Optional.empty();
    }

    public static List<UrlCheck> getEntities(Url url) throws SQLException {
        String sql =
                """
                SELECT
                    id,
                    statusCode,
                    title,
                    h1,
                    description,
                    createdAt
                FROM url_checks
                WHERE
                    url_id = ?
                ORDER BY
                    id ASC
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, url.getId());
            ResultSet resultSet = stmt.executeQuery();
            List<UrlCheck> result = new ArrayList<>();

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                int statusCode = resultSet.getInt("statusCode");
                String title = resultSet.getString("title");
                String h1 = resultSet.getString("h1");
                String description = resultSet.getString("description");
                LocalDateTime createdAt = resultSet.getTimestamp("createdAt").toLocalDateTime();

                UrlCheck urlCheck = new UrlCheck(statusCode, title, h1, description);
                urlCheck.setId(id);
                urlCheck.setCreatedAt(createdAt);
                urlCheck.setUrl(url);

                result.add(urlCheck);
            }

            return result;
        }
    }
}
