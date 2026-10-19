/**
 * Steven Piskunov
 * CEN-3024C - Software Development 1
 * October 13, 2026
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

    public VideoGame(int gameId, String title, String platform, double basePrice,
                     int quantityInStock, boolean isCollectorEdition) {
        this.gameId = gameId;
        this.title = title;
        this.platform = platform;
        setBasePrice(basePrice);
        setQuantityInStock(quantityInStock);
        this.isCollectorEdition = isCollectorEdition;
    }

    public int getGameId() {
        return gameId;
    }

    public String getTitle() {
        return title;
    }

    public String getPlatform() {
        return platform;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public boolean isCollectorEdition() {
        return isCollectorEdition;
    }

    public boolean setTitle(String title) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
            return true;
        }
        return false;
    }

    public boolean setPlatform(String platform) {
        if (platform != null && !platform.trim().isEmpty()) {
            this.platform = platform.trim();
            return true;
        }
        return false;
    }

    public boolean setBasePrice(double basePrice) {
        if (basePrice >= 0.0) {
            this.basePrice = basePrice;
            return true;
        }
        return false;
    }

    public boolean setQuantityInStock(int quantityInStock) {
        if (quantityInStock >= 0) {
            this.quantityInStock = quantityInStock;
            return true;
        }
        return false;
    }

    public boolean setCollectorEdition(boolean isCollectorEdition) {
        this.isCollectorEdition = isCollectorEdition;
        return true;
    }

    /**
     * method: calculateTotalValuation
     * parameters: none
     * return: double
     * purpose: Computes total inventory valuation for this game line, adding a 15%
     *          surcharge for collector editions and applying a 10% wholesale markdown
     *          if the quantity in stock exceeds 50 units.
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
     * method: toString
     * parameters: none
     * return: String
     * purpose: Returns a clean, aligned tabular row representation of all six game attributes.
     */
    @Override
    public String toString() {
        String editionStr = isCollectorEdition ? "Collector" : "Standard";
        return String.format("ID: %-5d | Title: %-26s | Platform: %-8s | Price: $%7.2f | Stock: %-4d | Edition: %-9s",
                gameId, title, platform, basePrice, quantityInStock, editionStr);
    }
}