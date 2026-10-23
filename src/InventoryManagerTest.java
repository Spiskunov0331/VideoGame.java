import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Steven Piskunov
 * CEN-3024C - Software Development I
 * October 23, 2026
 * InventoryManagerTest.java
 *
 * Comprehensive unit test suite for RetroVault DMS. Verifies affirmative and negative
 * execution paths for file ingestion, CRUD operations, boundary validation, and
 * custom pricing and discount calculations using JUnit assertions.
 */
public class InventoryManagerTest {

    private InventoryManager inventory;
    private VideoGame standardGame;
    private VideoGame collectorGame;

    @BeforeEach
    public void setUp() {
        inventory = new InventoryManager();
        standardGame = new VideoGame(501, "Super Mario World", "SNES", 40.00, 60, false);
        collectorGame = new VideoGame(502, "Chrono Trigger", "SNES", 100.00, 10, true);
    }

    /**
     * method: testOpenFile_Success
     * parameters: none
     * return: void
     * purpose: Affirmative test verifying that a properly formatted text file can be
     *          opened, parsed, and populated into the inventory collection.
     */
    @Test
    public void testOpenFile_Success() throws IOException {
        File tempFile = File.createTempFile("test_games", ".txt");
        tempFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("901-Sonic The Hedgehog-Genesis-29.99-20-false\n");
            writer.write("902-Streets of Rage-Genesis-34.99-15-true\n");
        }

        int importedCount = inventory.loadGamesFromFile(tempFile.getAbsolutePath());
        assertEquals(2, importedCount, "Should successfully load 2 records from valid text file.");
        assertNotNull(inventory.findGameById(901), "Imported game ID 901 should exist in catalog.");
    }

    /**
     * method: testOpenFile_FileNotFound
     * parameters: none
     * return: void
     * purpose: Negative test asserting that attempting to open a non-existent or blank
     *          file path throws an IOException and preserves system stability.
     */
    @Test
    public void testOpenFile_FileNotFound() {
        assertThrows(IOException.class, () -> {
            inventory.loadGamesFromFile("non_existent_file_path_xyz.txt");
        }, "Should throw IOException when attempting to open a missing file.");

        assertThrows(IOException.class, () -> {
            inventory.loadGamesFromFile("");
        }, "Should throw IOException when attempting to open a blank file path.");
    }

    /**
     * method: testAddGame_Success
     * parameters: none
     * return: void
     * purpose: Affirmative test verifying that a valid VideoGame entity is appended
     *          to the list and increments the catalog count.
     */
    @Test
    public void testAddGame_Success() {
        boolean result = inventory.addGame(standardGame);
        assertTrue(result, "Adding a new unique game should return true.");
        assertEquals(1, inventory.getAllGames().size(), "Catalog size should increment to 1.");
        assertEquals("Super Mario World", inventory.findGameById(501).getTitle());
    }

    /**
     * method: testAddGame_DuplicateAndNullRejection
     * parameters: none
     * return: void
     * purpose: Negative test ensuring duplicate primary keys and null instances are
     *          rejected without corrupting collection state.
     */
    @Test
    public void testAddGame_DuplicateAndNullRejection() {
        inventory.addGame(standardGame);
        VideoGame duplicate = new VideoGame(501, "Duplicate Mario", "SNES", 40.00, 10, false);

        boolean duplicateResult = inventory.addGame(duplicate);
        assertFalse(duplicateResult, "Adding a game with an existing ID should return false.");
        assertEquals(1, inventory.getAllGames().size(), "Catalog size must remain 1 after duplicate rejection.");

        boolean nullResult = inventory.addGame(null);
        assertFalse(nullResult, "Adding null reference should return false.");
    }

    /**
     * method: testRemoveGame_Success
     * parameters: none
     * return: void
     * purpose: Affirmative test verifying that an existing record is purged by ID
     *          and subsequent queries return null.
     */
    @Test
    public void testRemoveGame_Success() {
        inventory.addGame(standardGame);
        inventory.addGame(collectorGame);

        boolean removed = inventory.removeGame(501);
        assertTrue(removed, "Removing an existing game ID should return true.");
        assertNull(inventory.findGameById(501), "Target game ID 501 must return null post-deletion.");
        assertEquals(1, inventory.getAllGames().size(), "Catalog size should decrement to 1.");
    }

    /**
     * method: testRemoveGame_NotFound
     * parameters: none
     * return: void
     * purpose: Negative test asserting that attempting to remove an unmapped ID
     *          returns false and does not alter system state.
     */
    @Test
    public void testRemoveGame_NotFound() {
        inventory.addGame(standardGame);

        boolean removed = inventory.removeGame(9999);
        assertFalse(removed, "Attempting to remove a non-existent ID should return false.");
        assertEquals(1, inventory.getAllGames().size(), "Inventory size must remain unchanged.");
    }

    /**
     * method: testUpdateGame_Success
     * parameters: none
     * return: void
     * purpose: Affirmative test verifying that individual attributes of a game
     *          (price, stock, title) can be successfully updated.
     */
    @Test
    public void testUpdateGame_Success() {
        inventory.addGame(standardGame);

        boolean priceUpdated = inventory.updateGamePrice(501, 45.50);
        assertTrue(priceUpdated, "Valid positive price update should return true.");
        assertEquals(45.50, inventory.findGameById(501).getBasePrice(), 0.001);

        VideoGame game = inventory.findGameById(501);
        assertTrue(game.setQuantityInStock(75), "Valid stock update should return true.");
        assertEquals(75, game.getQuantityInStock());

        assertTrue(game.setTitle("Super Mario World: Special Edition"), "Valid title update should return true.");
        assertEquals("Super Mario World: Special Edition", game.getTitle());
    }

    /**
     * method: testUpdateGame_InvalidInputRejection
     * parameters: none
     * return: void
     * purpose: Negative test ensuring mutator methods reject out-of-bounds data
     *          (negative price, negative stock) and preserve existing attributes.
     */
    @Test
    public void testUpdateGame_InvalidInputRejection() {
        inventory.addGame(standardGame);
        VideoGame game = inventory.findGameById(501);

        boolean invalidPrice = game.setBasePrice(-15.00);
        assertFalse(invalidPrice, "Setting negative price must return false.");
        assertEquals(40.00, game.getBasePrice(), "Base price must remain unchanged after rejected update.");

        boolean invalidStock = game.setQuantityInStock(-5);
        assertFalse(invalidStock, "Setting negative stock must return false.");
        assertEquals(60, game.getQuantityInStock(), "Stock count must remain unchanged after rejected update.");

        boolean invalidTitle = game.setTitle("");
        assertFalse(invalidTitle, "Setting blank title must return false.");
    }

    /**
     * method: testCustomAction_ValuationCalculation
     * parameters: none
     * return: void
     * purpose: Affirmative test asserting that base valuations, 15% collector surcharges,
     *          and 10% wholesale markdowns (quantity > 50) compute with mathematical accuracy.
     */
    @Test
    public void testCustomAction_ValuationCalculation() {
        // standardGame: 40.00 * 60 = 2400.00. Quantity > 50 applies 10% wholesale markdown: 2400 - 240 = 2160.00
        assertEquals(2160.00, standardGame.calculateTotalValuation(), 0.001,
                "Standard game valuation with wholesale discount should equal 2160.00.");

        // collectorGame: base 100.00 + 15% surcharge = 115.00. 115.00 * 10 units = 1150.00 (no bulk discount)
        assertEquals(1150.00, collectorGame.calculateTotalValuation(), 0.001,
                "Collector edition valuation with 15% surcharge should equal 1150.00.");

        inventory.addGame(standardGame);
        inventory.addGame(collectorGame);
        // Total cumulative catalog valuation = 2160.00 + 1150.00 = 3310.00
        assertEquals(3310.00, inventory.calculateInventoryTotal(), 0.001,
                "Total inventory calculation should equal sum of all line item valuations.");
    }

    /**
     * method: testCustomAction_EmptyCatalog
     * parameters: none
     * return: void
     * purpose: Negative/Edge-case test verifying that running the valuation custom action
     *          on an empty catalog returns 0.00 without throwing exceptions.
     */
    @Test
    public void testCustomAction_EmptyCatalog() {
        assertEquals(0.0, inventory.calculateInventoryTotal(), 0.001,
                "Valuation on an empty collection must return 0.00 cleanly.");
    }
}