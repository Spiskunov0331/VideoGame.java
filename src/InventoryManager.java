import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Steven Piskunov
 * CEN-3024C - Software Development I
 * October 13, 2026
 * InventoryManager.java
 *
 * This class provides business logic and in-memory storage for the RetroVault DMS,
 * managing the collection of VideoGame entities, verifying unique IDs,
 * parsing delimited batch files, and computing overall catalog valuation.
 */
public class InventoryManager {

    private List<VideoGame> gameList;

    public InventoryManager() {
        this.gameList = new ArrayList<>();
    }

    /**
     * method: addGame
     * parameters: VideoGame game
     * return: boolean
     * purpose: Adds a new VideoGame entity to the list after asserting non-null and duplicate checks.
     */
    public boolean addGame(VideoGame game) {
        if (game == null || findGameById(game.getGameId()) != null) {
            return false;
        }
        return gameList.add(game);
    }

    /**
     * method: removeGame
     * parameters: int gameId
     * return: boolean
     * purpose: Locates a game by its unique primary ID and purges it from the active collection.
     */
    public boolean removeGame(int gameId) {
        VideoGame target = findGameById(gameId);
        if (target != null) {
            return gameList.remove(target);
        }
        return false;
    }

    /**
     * method: updateGamePrice
     * parameters: int gameId, double newPrice
     * return: boolean
     * purpose: Modifies the base price of an existing game record given a non-negative price value.
     */
    public boolean updateGamePrice(int gameId, double newPrice) {
        VideoGame target = findGameById(gameId);
        if (target != null) {
            return target.setBasePrice(newPrice);
        }
        return false;
    }

    /**
     * method: findGameById
     * parameters: int gameId
     * return: VideoGame
     * purpose: Iterates the collection to locate and return the matching entity or null.
     */
    public VideoGame findGameById(int gameId) {
        for (VideoGame g : gameList) {
            if (g.getGameId() == gameId) {
                return g;
            }
        }
        return null;
    }

    /**
     * method: getAllGames
     * parameters: none
     * return: List<VideoGame>
     * purpose: Returns an unmodifiable view of the game collection to protect data encapsulation.
     */
    public List<VideoGame> getAllGames() {
        return Collections.unmodifiableList(gameList);
    }

    /**
     * method: loadGamesFromFile
     * parameters: String filePath
     * return: int
     * purpose: Parses a hyphen-delimited text file line-by-line, validates all tokens,
     *          instantiates valid game records, and returns the total successfully imported count.
     */
    public int loadGamesFromFile(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IOException("File path cannot be blank.");
        }

        File file = new File(filePath.trim());
        if (!file.exists() || !file.isFile()) {
            throw new IOException("File not found at specified path: " + filePath.trim());
        }

        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Format: ID-Title-Platform-BasePrice-Quantity-IsCollectorEdition
                String[] tokens = line.split("-");
                if (tokens.length == 6) {
                    try {
                        int id = Integer.parseInt(tokens[0].trim());
                        String title = tokens[1].trim();
                        String platform = tokens[2].trim();
                        double price = Double.parseDouble(tokens[3].trim());
                        int stock = Integer.parseInt(tokens[4].trim());
                        boolean isCollector = Boolean.parseBoolean(tokens[5].trim());

                        if (id > 0 && !title.isEmpty() && !platform.isEmpty() && price >= 0.0 && stock >= 0) {
                            VideoGame game = new VideoGame(id, title, platform, price, stock, isCollector);
                            if (addGame(game)) {
                                count++;
                            }
                        }
                    } catch (NumberFormatException ignored) {
                        // Skip malformed individual rows to guarantee file reader continuity
                    }
                }
            }
        }
        return count;
    }

    /**
     * method: calculateInventoryTotal
     * parameters: none
     * return: double
     * purpose: Sums the total dynamic valuations of all games currently registered in the catalog.
     */
    public double calculateInventoryTotal() {
        double total = 0.0;
        for (VideoGame g : gameList) {
            total += g.calculateTotalValuation();
        }
        return total;
    }
}