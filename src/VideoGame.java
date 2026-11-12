/**
 * Steven Piskunov
 * CEN-3024C - Software Development 1
 * October 30, 2026
 * VideoGame.java
 *
 * This class serves as the core domain model entity for the RetroVault DMS,
 * encapsulating six distinct attributes for video games and defining
 * mutator validation rules and mathematical valuation logic.
 */
public class VideoGame {

    private int gameId;
    private String title;
    private String platform;
    private double basePrice;
    private int quantityInStock;
    private boolean isCollectorEdition;

    /**
     * Constructs a new VideoGame instance with specified identifiers, title, platform, pricing, stock, and edition.
     *
     * @param gameId             the unique primary identifier for the game
     * @param title              the title of the video game
     * @param platform           the gaming platform or console
     * @param basePrice          the baseline retail price in USD
     * @param quantityInStock    the current inventory count
     * @param isCollectorEdition true if the game is a collector/special edition, false otherwise
     */
    public VideoGame(int gameId, String title, String platform, double basePrice,
                     int quantityInStock, boolean isCollectorEdition) {
        this.gameId = gameId;
        this.title = title;
        this.platform = platform;
        setBasePrice(basePrice);
        setQuantityInStock(quantityInStock);
        this.isCollectorEdition = isCollectorEdition;
    }

    /**
     * Returns the unique game ID.
     *
     * @return the game ID integer
     */
    public int getGameId() {
        return gameId;
    }

    /**
     * Returns the video game title.
     *
     * @return the title string
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the gaming platform.
     *
     * @return the platform string
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Returns the baseline retail price.
     *
     * @return the base price value
     */
    public double getBasePrice() {
        return basePrice;
    }

    /**
     * Returns the stock quantity.
     *
     * @return the stock count integer
     */
    public int getQuantityInStock() {
        return quantityInStock;
    }

    /**
     * Checks if the game is a collector edition.
     *
     * @return true if collector edition, false otherwise
     */
    public boolean isCollectorEdition() {
        return isCollectorEdition;
    }

    /**
     * Updates the video game title if valid.
     *
     * @param title the new title string
     * @return true if updated successfully, false if null or blank
     */
    public boolean setTitle(String title) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
            return true;
        }
        return false;
    }

    /**
     * Updates the gaming platform if valid.
     *
     * @param platform the new platform string
     * @return true if updated successfully, false if null or blank
     */
    public boolean setPlatform(String platform) {
        if (platform != null && !platform.trim().isEmpty()) {
            this.platform = platform.trim();
            return true;
        }
        return false;
    }

    /**
     * Updates the base retail price if non-negative.
     *
     * @param basePrice the new base price value
     * @return true if updated successfully, false if negative
     */
    public boolean setBasePrice(double basePrice) {
        if (basePrice >= 0.0) {
            this.basePrice = basePrice;
            return true;
        }
        return false;
    }

    /**
     * Updates the quantity in stock if non-negative.
     *
     * @param quantityInStock the new stock count
     * @return true if updated successfully, false if negative
     */
    public boolean setQuantityInStock(int quantityInStock) {
        if (quantityInStock >= 0) {
            this.quantityInStock = quantityInStock;
            return true;
        }
        return false;
    }

    /**
     * Sets the collector edition status.
     *
     * @param isCollectorEdition boolean value for collector edition
     * @return true always
     */
    public boolean setCollectorEdition(boolean isCollectorEdition) {
        this.isCollectorEdition = isCollectorEdition;
        return true;
    }

    /**
     * Computes total inventory valuation for this game line, adding a 15%
     * surcharge for collector editions and applying a 10% wholesale markdown
     * if the quantity in stock exceeds 50 units.
     *
     * @return the calculated total monetary valuation for this game record
     */
    public double calculateTotalValuation() {
        double unitPrice = this.basePrice;
        if (this.isCollectorEdition) {
            unitPrice += (unitPrice * 0.15);
        }
        double total = unitPrice * this.quantityInStock;
        if (this.quantityInStock > 50) {
            total -= (total * 0.10);
        }
        return total;
    }

    /**
     * Returns a clean, aligned tabular row representation of all six game attributes.
     *
     * @return formatted string containing game attributes
     */
    @Override
    public String toString() {
        String editionStr = isCollectorEdition ? "Collector" : "Standard";
        return String.format("ID: %-5d | Title: %-26s | Platform: %-8s | Price: $%7.2f | Stock: %-4d | Edition: %-9s",
                gameId, title, platform, basePrice, quantityInStock, editionStr);
    }
}