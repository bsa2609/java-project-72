package hexlet.code.repository;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UrlRepository extends BaseRepository {
    public static Optional<Url> findByField(String fieldName, Object fieldValue) throws SQLException {
        String sql =
                """
                SELECT
                    id,
                    name,
                    created_at
                FROM urls
                WHERE
                """ + fieldName + " = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String fieldValueClassTypeName = fieldValue.getClass().getTypeName();
            switch (fieldValueClassTypeName) {
                case "java.lang.Long":
                    stmt.setLong(1, (Long) fieldValue);
                    break;

                case "java.lang.String":
                    stmt.setString(1, (String) fieldValue);
                    break;

                default:
                    throw new SQLException(
                            String.format("Unsupported find field value type: %s", fieldValueClassTypeName));
            }

            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                String name = resultSet.getString("name");
                Long id = resultSet.getLong("id");
                LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();

                Url url = new Url(name);
                url.setId(id);
                url.setCreatedAt(createdAt);
                url.setUrlChecks(UrlCheckRepository.getEntities(url));

                return Optional.of(url);
            }

            return Optional.empty();
        }
    }

    public static Optional<Url> findByName(String name) throws SQLException {
        return findByField("name", name);
    }

    public static Optional<Url> find(Long id) throws SQLException {
        return findByField("id", id);
    }

    public static void save(Url url) throws SQLException {
        String sql =
                """
                INSERT INTO urls(
                    name,
                    created_at
                )
                VALUES(?, ?);
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, url.getName());
            stmt.setTimestamp(2, Timestamp.valueOf(url.getCreatedAt()));
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                url.setId(generatedKeys.getLong("id"));
            } else {
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        }
    }

    public static List<Url> getEntities() throws SQLException {
        String sql =
                """
                SELECT
                    id,
                    name,
                    created_at
                FROM urls
                ORDER BY
                    id
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet resultSet = stmt.executeQuery();
            List<Url> result = new ArrayList<>();

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();

                Url url = new Url(name);
                url.setId(id);
                url.setCreatedAt(createdAt);

                List<UrlCheck> urlChecks = UrlCheckRepository.getEntities(url);
                url.setUrlChecks(urlChecks);

                if (!urlChecks.isEmpty()) {
                    UrlCheck lastCheck = urlChecks.getLast();
                    url.setLastCheckCode(lastCheck.getStatusCode());
                    url.setLastCheckAt(lastCheck.getCreatedAt());
                }

                result.add(url);
            }

            return result;
        }
    }
}
