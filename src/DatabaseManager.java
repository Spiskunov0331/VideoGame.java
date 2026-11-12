import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Steven Piskunov
 * CEN-3024C - Software Development I
 * November 8, 2026
 * DatabaseManager.java
 *
 * This Data Access Object class manages SQLite database connectivity via JDBC
 * for the RetroVault DMS. It provides CRUD query execution, connection lifecycle management,
 * and parameterized SQL statements to safeguard against SQL injection and database corruption.
 */
public class DatabaseManager {

    private Connection connection;
    private String databasePath;

    /**
     * Constructs an uninitialized DatabaseManager instance with null connection and path references.
     */
    public DatabaseManager() {
        this.connection = null;
        this.databasePath = null;
    }

    /**
     * Establishes a JDBC connection to the SQLite database file supplied by the user,
     * creates the 'games' table if it does not yet exist, and returns true upon success.
     *
     * @param dbPath the file path to the SQLite database
     * @return true if connection and table initialization succeed
     * @throws SQLException if the path is blank or connection fails
     */
    public boolean connect(String dbPath) throws SQLException {
        if (dbPath == null || dbPath.trim().isEmpty()) {
            throw new SQLException("Database path cannot be blank.");
        }

        String sanitizedPath = dbPath.trim();
        closeConnection();

        String url = "jdbc:sqlite:" + sanitizedPath;
        this.connection = DriverManager.getConnection(url);
        this.databasePath = sanitizedPath;

        return initializeTable();
    }

    /**
     * Executes a DDL statement ensuring the 'games' relation exists with all
     * six required domain attributes and a primary key constraint.
     *
     * @return true if table creation succeeds
     * @throws SQLException if a database access error occurs
     */
    public boolean initializeTable() throws SQLException {
        if (connection == null || connection.isClosed()) {
            return false;
        }

        String sql = "CREATE TABLE IF NOT EXISTS games ("
                + "gameId INTEGER PRIMARY KEY, "
                + "title TEXT NOT NULL, "
                + "platform TEXT NOT NULL, "
                + "basePrice REAL NOT NULL, "
                + "quantityInStock INTEGER NOT NULL, "
                + "isCollectorEdition INTEGER NOT NULL"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            return true;
        }
    }

    /**
     * Queries all rows from the 'games' table, reconstructs VideoGame domain objects,
     * and returns an in-memory list sorted ascending by gameId.
     *
     * @return a List of VideoGame objects retrieved from the database
     * @throws SQLException if a database access error occurs
     */
    public List<VideoGame> getAllGames() throws SQLException {
        List<VideoGame> list = new ArrayList<>();
        if (connection == null || connection.isClosed()) {
            return list;
        }

        String sql = "SELECT gameId, title, platform, basePrice, quantityInStock, isCollectorEdition "
                + "FROM games ORDER BY gameId ASC;";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("gameId");
                String title = rs.getString("title");
                String platform = rs.getString("platform");
                double price = rs.getDouble("basePrice");
                int stock = rs.getInt("quantityInStock");
                boolean isCollector = rs.getInt("isCollectorEdition") == 1;

                list.add(new VideoGame(id, title, platform, price, stock, isCollector));
            }
        }
        return list;
    }

    /**
     * Inserts a new video game record into the database table using a parameterized statement.
     *
     * @param game the VideoGame object to insert
     * @return true if the insert modified at least one row
     * @throws SQLException if a database access error occurs
     */
    public boolean addGame(VideoGame game) throws SQLException {
        if (connection == null || connection.isClosed() || game == null) {
            return false;
        }

        String sql = "INSERT INTO games(gameId, title, platform, basePrice, quantityInStock, isCollectorEdition) "
                + "VALUES(?, ?, ?, ?, ?, ?);";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, game.getGameId());
            pstmt.setString(2, game.getTitle());
            pstmt.setString(3, game.getPlatform());
            pstmt.setDouble(4, game.getBasePrice());
            pstmt.setInt(5, game.getQuantityInStock());
            pstmt.setInt(6, game.isCollectorEdition() ? 1 : 0);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Updates the title, platform, price, stock, and collector edition status
     * for an existing game identified by its primary key gameId.
     *
     * @param game the VideoGame object containing updated attributes
     * @return true if the update modified at least one row
     * @throws SQLException if a database access error occurs
     */
    public boolean updateGame(VideoGame game) throws SQLException {
        if (connection == null || connection.isClosed() || game == null) {
            return false;
        }

        String sql = "UPDATE games SET title = ?, platform = ?, basePrice = ?, "
                + "quantityInStock = ?, isCollectorEdition = ? WHERE gameId = ?;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, game.getTitle());
            pstmt.setString(2, game.getPlatform());
            pstmt.setDouble(3, game.getBasePrice());
            pstmt.setInt(4, game.getQuantityInStock());
            pstmt.setInt(5, game.isCollectorEdition() ? 1 : 0);
            pstmt.setInt(6, game.getGameId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Purges a single record from the database table matching the specified gameId.
     *
     * @param gameId the primary key of the game to delete
     * @return true if the delete modified at least one row
     * @throws SQLException if a database access error occurs
     */
    public boolean deleteGame(int gameId) throws SQLException {
        if (connection == null || connection.isClosed()) {
            return false;
        }

        String sql = "DELETE FROM games WHERE gameId = ?;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, gameId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Queries the database for a specific record matching gameId; returns the reconstructed
     * VideoGame or null if no matching row exists.
     *
     * @param gameId the primary key to search for
     * @return the matching VideoGame object, or null if not found
     * @throws SQLException if a database access error occurs
     */
    public VideoGame findGameById(int gameId) throws SQLException {
        if (connection == null || connection.isClosed()) {
            return null;
        }

        String sql = "SELECT gameId, title, platform, basePrice, quantityInStock, isCollectorEdition "
                + "FROM games WHERE gameId = ?;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, gameId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new VideoGame(
                            rs.getInt("gameId"),
                            rs.getString("title"),
                            rs.getString("platform"),
                            rs.getDouble("basePrice"),
                            rs.getInt("quantityInStock"),
                            rs.getInt("isCollectorEdition") == 1
                    );
                }
            }
        }
        return null;
    }

    /**
     * Checks whether an active JDBC connection currently exists.
     *
     * @return true if connected and open, false otherwise
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Safely releases and closes active database resources.
     *
     * @return true if closed successfully or already closed
     */
    public boolean closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                return true;
            } catch (SQLException ignored) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the active database connection string path.
     *
     * @return the database path string
     */
    public String getDatabasePath() {
        return databasePath;
    }
}