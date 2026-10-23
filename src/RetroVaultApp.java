import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Steven Piskunov
 * CEN-3024C - Software Development I
 * October 13, 2026
 * RetroVaultApp.java
 *
 * Main controller and console interface for the RetroVault Video Game DMS.
 * This class drives an interactive, crash-proof menu loop allowing users to perform
 * batch ingestion, record creation, searching, updating, deletion, catalog display,
 * and custom financial valuation calculations over video game inventory.
 */
public class RetroVaultApp {

    private InventoryManager inventory;
    private Scanner scanner;

    public RetroVaultApp() {
        this.inventory = new InventoryManager();
        this.scanner = new Scanner(System.in);
    }

    /**
     * method: main
     * parameters: String[] args
     * return: void
     * purpose: Standard entry point required by the JVM; instantiates the application object
     *          and hands off execution to non-static instance methods.
     */
    public static void main(String[] args) {
        RetroVaultApp app = new RetroVaultApp();
        app.run();
    }

    /**
     * method: run
     * parameters: none
     * return: boolean
     * purpose: Executes the persistent menu loop until the user explicitly selects option 7 to exit.
     */
    public boolean run() {
        System.out.println("WELCOME TO RETROVAULT - VIDEO GAME INVENTORY DMS              ");

        boolean running = true;
        while (running) {
            int choice = displayMenu();
            switch (choice) {
                case 1:
                    handleBatchLoad();
                    break;
                case 2:
                    handleDisplayAll();
                    break;
                case 3:
                    handleManualAdd();
                    break;
                case 4:
                    handleRemove();
                    break;
                case 5:
                    handleUpdate();
                    break;
                case 6:
                    handleValuationAction();
                    break;
                case 7:
                    System.out.println("\nThank you for using RetroVault DMS. System shutdown complete. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Unrecognized selection. Please enter a valid number (1-7).\n");
                    break;
            }
        }
        return true;
    }

    /**
     * method: displayMenu
     * parameters: none
     * return: int
     * purpose: Displays the formatted operational menu and captures a validated integer choice.
     */
    public int displayMenu() {
        System.out.println("\n         MAIN MENU");
        System.out.println("1. Load Games From Text File");
        System.out.println("2. Display All Games");
        System.out.println("3. Add Game Manually");
        System.out.println("4. Remove Game by ID");
        System.out.println("5. Update Game Attributes");
        System.out.println("6. Run Custom Valuation & Discount Action");
        System.out.println("7. Exit");
        System.out.print("Enter your choice (1-7): ");
        return parsePositiveInt(scanner.nextLine().trim());
    }

    /**
     * method: handleBatchLoad
     * parameters: none
     * return: int
     * purpose: Prompts for a file path, invokes parsing logic, prints confirmation,
     *          and automatically displays the updated catalog. Satisfies Rubric Test 2 & 3.
     */
    public int handleBatchLoad() {
        System.out.print("\nEnter text file name/path containing game data: ");
        String path = scanner.nextLine().trim();
        try {
            int imported = inventory.loadGamesFromFile(path);
            System.out.println("Successfully imported " + imported + " video game records from '" + path + "'.");
            handleDisplayAll();
            return imported;
        } catch (IOException e) {
            System.out.println("File Error: " + e.getMessage());
            System.out.println("Returning to main menu without crashing.");
            return -1;
        }
    }

    /**
     * method: handleDisplayAll
     * parameters: none
     * return: int
     * purpose: Renders the entire video game catalog in formatted tabular rows. Satisfies Rubric Test 3.
     */
    public int handleDisplayAll() {
        System.out.println("\n         CURRENT CATALOG");
        List<VideoGame> games = inventory.getAllGames();
        if (games.isEmpty()) {
            System.out.println("No video game records currently exist in the system.");
        } else {
            for (VideoGame g : games) {
                System.out.println(g.toString());
            }
        }
        return games.size();
    }

    /**
     * method: handleManualAdd
     * parameters: none
     * return: boolean
     * purpose: Captures and validates all six attributes, adds the new entity to the catalog,
     *          and displays the refreshed catalog. Satisfies Rubric Test 4.
     */
    public boolean handleManualAdd() {
        System.out.println("\n Add New Video Game Record");

        int id = -1;
        while (true) {
            System.out.print("Enter Unique Game ID (Positive Integer): ");
            id = parsePositiveInt(scanner.nextLine().trim());
            if (id <= 0) {
                System.out.println("Invalid ID. ID must be a positive whole integer greater than zero.");
                continue;
            }
            if (inventory.findGameById(id) != null) {
                System.out.println("Error: Game ID '" + id + "' already exists in catalog. Please use a unique ID.");
                continue;
            }
            break;
        }

        String title = "";
        while (title.isEmpty()) {
            System.out.print("Enter Game Title: ");
            title = scanner.nextLine().trim();
            if (title.isEmpty()) {
                System.out.println("Error: Game title cannot be blank.");
            }
        }

        String platform = "";
        while (platform.isEmpty()) {
            System.out.print("Enter Platform (e.g., PS5, Switch, PC, Xbox): ");
            platform = scanner.nextLine().trim();
            if (platform.isEmpty()) {
                System.out.println("Error: Platform cannot be blank.");
            }
        }

        double price = -1.0;
        while (price < 0.0) {
            System.out.print("Enter Base Retail Price (USD): ");
            price = parsePositiveDouble(scanner.nextLine().trim());
            if (price < 0.0) {
                System.out.println("Invalid Price. Please enter a positive decimal number (e.g., 59.99).");
            }
        }

        int stock = -1;
        while (stock < 0) {
            System.out.print("Enter Quantity In Stock: ");
            stock = parsePositiveInt(scanner.nextLine().trim());
            if (stock < 0) {
                System.out.println("Invalid Quantity. Quantity must be 0 or greater.");
            }
        }

        boolean isCollector = false;
        while (true) {
            System.out.print("Is this a Collector's Edition? (true/false): ");
            String boolInput = scanner.nextLine().trim().toLowerCase();
            if (boolInput.equals("true") || boolInput.equals("t") || boolInput.equals("yes") || boolInput.equals("y")) {
                isCollector = true;
                break;
            } else if (boolInput.equals("false") || boolInput.equals("f") || boolInput.equals("no") || boolInput.equals("n")) {
                isCollector = false;
                break;
            }
            System.out.println("Invalid input. Please enter 'true' or 'false'.");
        }

        VideoGame newGame = new VideoGame(id, title, platform, price, stock, isCollector);
        boolean success = inventory.addGame(newGame);
        if (success) {
            System.out.println("Success: Video game added successfully.");
        } else {
            System.out.println("Error: Unable to add video game to inventory.");
        }

        handleDisplayAll();
        return success;
    }

    /**
     * method: handleRemove
     * parameters: none
     * return: boolean
     * purpose: Prompts for a target ID, prompts for user confirmation before deletion
     *          to address stakeholder feedback, executes removal, and displays the catalog.
     */
    public boolean handleRemove() {
        System.out.println("\n-- Remove Video Game Record --");
        System.out.print("Enter the Game ID of the record to remove: ");
        int id = parsePositiveInt(scanner.nextLine().trim());
        if (id <= 0) {
            System.out.println("Invalid ID format. Removal cancelled.");
            return false;
        }

        VideoGame target = inventory.findGameById(id);
        if (target == null) {
            System.out.println("Error: Game with ID " + id + " was not found in catalog.");
            handleDisplayAll();
            return false;
        }

        // Professor Feedback Implementation: Confirmation prompt before deleting
        System.out.println("Found Record: " + target.getTitle() + " (" + target.getPlatform() + ")");
        System.out.print("Are you sure you want to permanently delete this game? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("Deletion cancelled by user. Record preserved.");
            handleDisplayAll();
            return false;
        }

        boolean removed = inventory.removeGame(id);
        if (removed) {
            System.out.println("Success: Game ID " + id + " has been permanently removed.");
        } else {
            System.out.println("Error: Could not remove game.");
        }

        handleDisplayAll();
        return removed;
    }

    /**
     * method: handleUpdate
     * parameters: none
     * return: boolean
     * purpose: Allows the user to select an existing record by ID and modify any chosen attribute
     * with complete input validation.
     */
    public boolean handleUpdate() {
        System.out.println("\nUpdate Video Game Record");
        System.out.print("Enter the Game ID of the record you wish to update: ");
        int id = parsePositiveInt(scanner.nextLine().trim());
        VideoGame target = inventory.findGameById(id);
        if (target == null) {
            System.out.println("Error: Record with ID " + id + " does not exist.");
            return false;
        }

        System.out.println("\nSelected Record: " + target);
        System.out.println("Select Attribute to Update:");
        System.out.println("1. Title");
        System.out.println("2. Platform");
        System.out.println("3. Base Price");
        System.out.println("4. Quantity in Stock");
        System.out.println("5. Collector's Edition Status");
        System.out.print("Enter field choice (1-5): ");
        int fieldChoice = parsePositiveInt(scanner.nextLine().trim());

        boolean updated = false;
        switch (fieldChoice) {
            case 1:
                System.out.print("Enter New Title: ");
                String newTitle = scanner.nextLine().trim();
                if (!newTitle.isEmpty()) {
                    updated = target.setTitle(newTitle);
                } else {
                    System.out.println("Title cannot be blank. Update cancelled.");
                }
                break;
            case 2:
                System.out.print("Enter New Platform: ");
                String newPlatform = scanner.nextLine().trim();
                if (!newPlatform.isEmpty()) {
                    updated = target.setPlatform(newPlatform);
                } else {
                    System.out.println("Platform cannot be blank. Update cancelled.");
                }
                break;
            case 3:
                System.out.print("Enter New Base Price: ");
                double newPrice = parsePositiveDouble(scanner.nextLine().trim());
                if (newPrice >= 0.0) {
                    updated = target.setBasePrice(newPrice);
                } else {
                    System.out.println("Price must be a positive number. Update cancelled.");
                }
                break;
            case 4:
                System.out.print("Enter New Quantity in Stock: ");
                int newStock = parsePositiveInt(scanner.nextLine().trim());
                if (newStock >= 0) {
                    updated = target.setQuantityInStock(newStock);
                } else {
                    System.out.println("Stock must be 0 or greater. Update cancelled.");
                }
                break;
            case 5:
                System.out.print("Set Collector's Edition? (true/false): ");
                String boolStr = scanner.nextLine().trim().toLowerCase();
                if (boolStr.equals("true") || boolStr.equals("yes")) {
                    updated = target.setCollectorEdition(true);
                } else if (boolStr.equals("false") || boolStr.equals("no")) {
                    updated = target.setCollectorEdition(false);
                } else {
                    System.out.println("Invalid input. Update cancelled.");
                }
                break;
            default:
                System.out.println("Invalid field choice. Update cancelled.");
                break;
        }

        if (updated) {
            System.out.println("Success: Record updated successfully.");
        } else {
            System.out.println("Notice: No changes were applied.");
        }

        handleDisplayAll();
        return updated;
    }

    /**
     * method: handleValuationAction
     * parameters: none
     * return: double
     * purpose: Executes the custom mathematical calculation across the inventory catalog,
     * printing line-by-line valuations and computing catalog totals.
     */
    public double handleValuationAction() {
        System.out.println("\n         INVENTORY VALUATION & DISCOUNT REPORT");
        List<VideoGame> games = inventory.getAllGames();
        if (games.isEmpty()) {
            System.out.println("Inventory is empty. Total valuation: $0.00");
            return 0.0;
        }

        double catalogTotal = 0.0;
        for (VideoGame g : games) {
            double lineValue = g.calculateTotalValuation();
            catalogTotal += lineValue;
            String note = "";
            if (g.isCollectorEdition() && g.getQuantityInStock() > 50) {
                note = "(+15% Collector Surcharge, -10% Wholesale Discount Applied)";
            } else if (g.isCollectorEdition()) {
                note = "(+15% Collector Surcharge Applied)";
            } else if (g.getQuantityInStock() > 50) {
                note = "(-10% Wholesale Discount Applied)";
            }
            System.out.printf("Game ID %-5d | %-24s | Valued at: $%10.2f %s\n",
                    g.getGameId(), g.getTitle(), lineValue, note);
        }

        System.out.printf("TOTAL CUMULATIVE INVENTORY VALUATION: $%.2f\n", catalogTotal);
        return catalogTotal;
    }

    /**
     * method: parsePositiveInt
     * parameters: String input
     * return: int
     * purpose: Helper method parsing integer strings; catches NumberFormatException
     * and returns -1 to guarantee the program never crashes on invalid numeric inputs.
     */
    public int parsePositiveInt(String input) {
        if (input == null || input.trim().isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * method: parsePositiveDouble
     * parameters: String input
     * return: double
     * purpose: Helper method parsing floating point strings; catches NumberFormatException
     * and returns -1.0 to guarantee crash-proof decimal input handling.
     */
    public double parsePositiveDouble(String input) {
        if (input == null || input.trim().isEmpty()) {
            return -1.0;
        }
        try {
            return Double.parseDouble(input.trim());
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }
}