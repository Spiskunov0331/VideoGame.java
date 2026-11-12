import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Steven Piskunov
 * CEN-3024C - Software Development I
 * November 8, 2026
 * DatabaseSeeder.java
 *
 * One-time setup utility that creates 'retrovault.db' and populates it with
 * 20 distinct video game records for Phase 4 grading and testing.
 */
public class DatabaseSeeder {

    /**
     * Main entry point for the database seeder utility.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        String dbPath = "retrovault.db";
        String url = "jdbc:sqlite:" + dbPath;

        String createTableSQL = "CREATE TABLE IF NOT EXISTS games ("
                + "gameId INTEGER PRIMARY KEY, "
                + "title TEXT NOT NULL, "
                + "platform TEXT NOT NULL, "
                + "basePrice REAL NOT NULL, "
                + "quantityInStock INTEGER NOT NULL, "
                + "isCollectorEdition INTEGER NOT NULL"
                + ");";

        Object[][] seedData = {
                {101, "The Legend of Zelda", "Switch", 59.99, 40, 1},
                {102, "Super Mario Odyssey", "Switch", 49.99, 65, 0},
                {103, "Elden Ring", "PS5", 59.99, 12, 0},
                {104, "God of War Ragnarok", "PS5", 69.99, 30, 1},
                {105, "Halo Infinite", "Xbox", 59.99, 75, 0},
                {106, "Cyberpunk 2077", "PC", 49.99, 55, 0},
                {107, "Final Fantasy VII Rebirth", "PS5", 69.99, 18, 1},
                {108, "Metroid Dread", "Switch", 59.99, 25, 0},
                {109, "Starfield", "Xbox", 69.99, 80, 0},
                {110, "Baldurs Gate 3", "PC", 59.99, 45, 1},
                {111, "Spider-Man 2", "PS5", 69.99, 60, 0},
                {112, "Super Smash Bros Ultimate", "Switch", 59.99, 90, 0},
                {113, "Forza Horizon 5", "Xbox", 59.99, 35, 0},
                {114, "Resident Evil 4 Remake", "PC", 59.99, 14, 1},
                {115, "The Witcher 3 Wild Hunt", "PC", 39.99, 110, 0},
                {116, "Gran Turismo 7", "PS5", 69.99, 22, 0},
                {117, "Pokemon Scarlet", "Switch", 59.99, 85, 0},
                {118, "Ghost of Tsushima", "PS5", 49.99, 40, 1},
                {119, "Gears 5", "Xbox", 39.99, 60, 0},
                {120, "Red Dead Redemption 2", "PC", 59.99, 95, 1}
        };

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            stmt.execute("DELETE FROM games;");

            String insertSQL = "INSERT INTO games VALUES(?, ?, ?, ?, ?, ?);";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                for (Object[] row : seedData) {
                    pstmt.setInt(1, (int) row[0]);
                    pstmt.setString(2, (String) row[1]);
                    pstmt.setString(3, (String) row[2]);
                    pstmt.setDouble(4, (double) row[3]);
                    pstmt.setInt(5, (int) row[4]);
                    pstmt.setInt(6, (int) row[5]);
                    pstmt.executeUpdate();
                }
            }
            System.out.println("Success! Created 'retrovault.db' populated with 20 video game records.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}