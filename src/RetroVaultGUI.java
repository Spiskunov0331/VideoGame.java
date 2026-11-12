import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * Steven Piskunov
 * CEN-3024C - Software Development I
 * November 8, 2026
 * RetroVaultGUI.java
 *
 * Graphical User Interface controller for Phase 4 of the RetroVault DMS.
 * Connects directly to a user-supplied SQLite relational database file via JDBC,
 * providing full CRUD operations, live synchronized tabular rendering, defensive
 * input validation, and real-time custom valuation calculations.
 */
public class RetroVaultGUI extends JFrame {

    private DatabaseManager dbManager;
    private DefaultTableModel tableModel;
    private JTable gamesTable;

    private JTextField txtDbPath;
    private JLabel lblDbStatus;

    private JTextField txtId;
    private JTextField txtTitle;
    private JTextField txtPlatform;
    private JTextField txtPrice;
    private JTextField txtStock;
    private JCheckBox chkCollector;

    /**
     * Constructs the RetroVaultGUI application window, instantiating the DatabaseManager
     * and initializing all UI components.
     */
    public RetroVaultGUI() {
        this.dbManager = new DatabaseManager();
        initializeUI();
    }

    /**
     * Main entry point for Phase 4; instantiates the desktop dashboard
     * on the Swing Event Dispatch Thread.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RetroVaultGUI app = new RetroVaultGUI();
            app.setVisible(true);
        });
    }

    /**
     * Assembles the primary dashboard layout, database connection toolbar,
     * catalog table, form controls, and action buttons.
     *
     * @return true upon successful UI assembly
     */
    public boolean initializeUI() {
        setTitle("RetroVault DMS - Video Game Inventory Management (Phase 4: SQLite Database)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 720);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 247, 250));

        mainPanel.add(buildTopDatabaseBar(), BorderLayout.NORTH);
        mainPanel.add(buildCenterTablePanel(), BorderLayout.CENTER);
        mainPanel.add(buildLeftFormPanel(), BorderLayout.WEST);
        mainPanel.add(buildBottomActionBar(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
        return true;
    }

    /**
     * Constructs the database connection toolbar allowing users to enter or browse
     * for any SQLite file path without hardcoding. Satisfies Rubric Test 2.
     *
     * @return a JPanel containing connection controls
     */
    public JPanel buildTopDatabaseBar() {
        JPanel dbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        dbPanel.setBorder(BorderFactory.createTitledBorder("Database Connection"));
        dbPanel.setBackground(Color.WHITE);

        JLabel lblPath = new JLabel("SQLite DB File Path:");
        lblPath.setFont(new Font("Segoe UI", Font.BOLD, 12));

        txtDbPath = new JTextField("retrovault.db", 26);
        txtDbPath.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton btnBrowse = new JButton("Browse...");
        styleButton(btnBrowse, new Color(74, 85, 104));
        btnBrowse.addActionListener(e -> handleBrowseDatabase());

        JButton btnConnect = new JButton("Connect & Load Database");
        styleButton(btnConnect, new Color(43, 108, 176));
        btnConnect.addActionListener(e -> handleConnectDatabase());

        lblDbStatus = new JLabel("Status: Disconnected");
        lblDbStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDbStatus.setForeground(new Color(197, 48, 48));

        dbPanel.add(lblPath);
        dbPanel.add(txtDbPath);
        dbPanel.add(btnBrowse);
        dbPanel.add(btnConnect);
        dbPanel.add(lblDbStatus);

        return dbPanel;
    }

    /**
     * Builds the central tabular view reflecting records directly queried
     * from the database.
     *
     * @return a JPanel containing the JTable inside a scroll pane
     */
    public JPanel buildCenterTablePanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Database Catalog Overview"));
        centerPanel.setBackground(Color.WHITE);

        String[] columnNames = {"Game ID", "Title", "Platform", "Base Price ($)", "Stock Qty", "Collector Edition"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        gamesTable = new JTable(tableModel);
        gamesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gamesTable.setRowHeight(26);
        gamesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gamesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        gamesTable.getTableHeader().setBackground(new Color(226, 232, 240));

        gamesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelectedRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(gamesTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        return centerPanel;
    }

    /**
     * Builds the record entry and modification form with input validation.
     *
     * @return a JPanel containing the form components
     */
    public JPanel buildLeftFormPanel() {
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setPreferredSize(new Dimension(310, 0));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Record Details (CRUD Form)"));
        leftPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        leftPanel.add(new JLabel("Game ID:"), gbc);
        gbc.gridx = 1;
        txtId = new JTextField();
        leftPanel.add(txtId, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        leftPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        txtTitle = new JTextField();
        leftPanel.add(txtTitle, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        leftPanel.add(new JLabel("Platform:"), gbc);
        gbc.gridx = 1;
        txtPlatform = new JTextField();
        leftPanel.add(txtPlatform, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        leftPanel.add(new JLabel("Base Price:"), gbc);
        gbc.gridx = 1;
        txtPrice = new JTextField();
        leftPanel.add(txtPrice, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        leftPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        txtStock = new JTextField();
        leftPanel.add(txtStock, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        leftPanel.add(new JLabel("Collector's:"), gbc);
        gbc.gridx = 1;
        chkCollector = new JCheckBox("Special Edition");
        chkCollector.setBackground(Color.WHITE);
        leftPanel.add(chkCollector, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JButton btnAdd = new JButton("Add Record to DB (Create)");
        styleButton(btnAdd, new Color(47, 133, 90));
        btnAdd.addActionListener(e -> handleAddRecord());
        leftPanel.add(btnAdd, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JButton btnClear = new JButton("Clear Form");
        styleButton(btnClear, new Color(113, 128, 150));
        btnClear.addActionListener(e -> clearFormFields());
        leftPanel.add(btnClear, gbc);

        return leftPanel;
    }

    /**
     * Constructs the action toolbar containing Update, Delete, Valuation,
     * and Exit controls.
     *
     * @return a JPanel containing action buttons
     */
    public JPanel buildBottomActionBar() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Database Operations (CRUD & Actions)"));
        bottomPanel.setBackground(Color.WHITE);

        JButton btnRefresh = new JButton("Refresh DB View");
        styleButton(btnRefresh, new Color(49, 130, 206));
        btnRefresh.addActionListener(e -> refreshTableData());

        JButton btnUpdate = new JButton("Update Record in DB");
        styleButton(btnUpdate, new Color(214, 158, 46));
        btnUpdate.addActionListener(e -> handleUpdateRecord());

        JButton btnDelete = new JButton("Delete Record from DB");
        styleButton(btnDelete, new Color(229, 62, 62));
        btnDelete.addActionListener(e -> handleDeleteRecord());

        JButton btnCustom = new JButton("Run Valuation & Discounts");
        styleButton(btnCustom, new Color(107, 70, 193));
        btnCustom.addActionListener(e -> handleCustomAction());

        JButton btnExit = new JButton("Exit Application");
        styleButton(btnExit, new Color(26, 32, 44));
        btnExit.addActionListener(e -> handleExit());

        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnUpdate);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnCustom);
        bottomPanel.add(btnExit);

        return bottomPanel;
    }

    /**
     * Opens a native JFileChooser to select any local SQLite database file (.db).
     *
     * @return true if a file was successfully chosen, false otherwise
     */
    public boolean handleBrowseDatabase() {
        JFileChooser chooser = new JFileChooser(new File("."));
        int status = chooser.showOpenDialog(this);
        if (status == JFileChooser.APPROVE_OPTION) {
            txtDbPath.setText(chooser.getSelectedFile().getPath());
            return true;
        }
        return false;
    }

    /**
     * Validates user database input, establishes the connection via JDBC,
     * updates the UI status, and populates the table.
     *
     * @return true if connection succeeds, false otherwise
     */
    public boolean handleConnectDatabase() {
        String path = txtDbPath.getText().trim();
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Error: Database file path cannot be blank. Please enter a valid SQLite path.",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            boolean connected = dbManager.connect(path);
            if (connected) {
                lblDbStatus.setText("Status: Connected (" + new File(path).getName() + ")");
                lblDbStatus.setForeground(new Color(47, 133, 90));
                refreshTableData();
                JOptionPane.showMessageDialog(this,
                        "Successfully connected to SQLite database:\n" + path,
                        "Database Connected", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        } catch (SQLException ex) {
            lblDbStatus.setText("Status: Connection Failed");
            lblDbStatus.setForeground(new Color(197, 48, 48));
            JOptionPane.showMessageDialog(this,
                    "Database Error: " + ex.getMessage(),
                    "Connection Failed", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    /**
     * Validates all input fields and executes an INSERT into the database.
     *
     * @return true if the record is successfully added, false otherwise
     */
    public boolean handleAddRecord() {
        if (!ensureConnected()) {
            return false;
        }

        String idStr = txtId.getText().trim();
        String title = txtTitle.getText().trim();
        String platform = txtPlatform.getText().trim();
        String priceStr = txtPrice.getText().trim();
        String stockStr = txtStock.getText().trim();
        boolean isCollector = chkCollector.isSelected();

        if (idStr.isEmpty() || title.isEmpty() || platform.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Error: All fields are required. Please populate all attributes.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
            if (id <= 0) {
                JOptionPane.showMessageDialog(this, "Game ID must be a positive integer greater than zero.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Game ID must be numeric.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            if (dbManager.findGameById(id) != null) {
                JOptionPane.showMessageDialog(this,
                        "Error: Game ID '" + id + "' already exists in the database. Primary keys must be unique.",
                        "Duplicate Key Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lookup Error: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price < 0.0) {
                JOptionPane.showMessageDialog(this, "Base price cannot be negative.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Base price must be a valid decimal number.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        int stock;
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                JOptionPane.showMessageDialog(this, "Quantity in stock cannot be negative.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity in stock must be a non-negative integer.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        VideoGame game = new VideoGame(id, title, platform, price, stock, isCollector);
        try {
            boolean inserted = dbManager.addGame(game);
            if (inserted) {
                refreshTableData();
                clearFormFields();
                JOptionPane.showMessageDialog(this,
                        "Record for '" + title + "' (ID: " + id + ") successfully written to database!",
                        "Record Created", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Insert Error: " + ex.getMessage(),
                    "Insert Failed", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    /**
     * Updates an existing database record using parameterized SQL.
     *
     * @return true if update succeeds, false otherwise
     */
    public boolean handleUpdateRecord() {
        if (!ensureConnected()) {
            return false;
        }

        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter or select a Game ID to update.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Game ID must be a valid integer.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            VideoGame target = dbManager.findGameById(id);
            if (target == null) {
                JOptionPane.showMessageDialog(this, "Error: Game ID " + id + " was not found in the database.",
                        "Record Not Found", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            String newTitle = txtTitle.getText().trim();
            String newPlatform = txtPlatform.getText().trim();
            String priceStr = txtPrice.getText().trim();
            String stockStr = txtStock.getText().trim();

            if (newTitle.isEmpty() || newPlatform.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Attributes cannot be blank.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            double newPrice;
            try {
                newPrice = Double.parseDouble(priceStr);
                if (newPrice < 0.0) {
                    JOptionPane.showMessageDialog(this, "Base price cannot be negative.",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid decimal format for price.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            int newStock;
            try {
                newStock = Integer.parseInt(stockStr);
                if (newStock < 0) {
                    JOptionPane.showMessageDialog(this, "Stock quantity cannot be negative.",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid integer format for stock.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            target.setTitle(newTitle);
            target.setPlatform(newPlatform);
            target.setBasePrice(newPrice);
            target.setQuantityInStock(newStock);
            target.setCollectorEdition(chkCollector.isSelected());

            boolean updated = dbManager.updateGame(target);
            if (updated) {
                refreshTableData();
                JOptionPane.showMessageDialog(this, "Record " + id + " updated in database successfully!",
                        "Update Complete", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Update Error: " + ex.getMessage(),
                    "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    /**
     * Prompts for confirmation and purges the selected record from the SQLite database.
     *
     * @return true if deletion succeeds, false otherwise
     */
    public boolean handleDeleteRecord() {
        if (!ensureConnected()) {
            return false;
        }

        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter or select a Game ID to delete.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Game ID must be a numeric integer.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            VideoGame target = dbManager.findGameById(id);
            if (target == null) {
                JOptionPane.showMessageDialog(this, "Error: Game ID " + id + " does not exist in the database.",
                        "Not Found", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to permanently delete '" + target.getTitle() + "' (ID: " + id + ") from the database?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (choice != JOptionPane.YES_OPTION) {
                return false;
            }

            boolean removed = dbManager.deleteGame(id);
            if (removed) {
                refreshTableData();
                clearFormFields();
                JOptionPane.showMessageDialog(this, "Record ID " + id + " purged from database successfully.",
                        "Record Deleted", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Deletion Error: " + ex.getMessage(),
                    "Delete Failed", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    /**
     * Queries all live records from SQLite, computes line-item valuations with
     * collector surcharges and wholesale markdowns, and displays the report dialog.
     *
     * @return the total cumulative database inventory valuation
     */
    public double handleCustomAction() {
        if (!ensureConnected()) {
            return 0.0;
        }

        try {
            List<VideoGame> games = dbManager.getAllGames();
            if (games.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Database table is empty. Total inventory valuation is $0.00.",
                        "Inventory Valuation", JOptionPane.INFORMATION_MESSAGE);
                return 0.0;
            }

            StringBuilder report = new StringBuilder();
            report.append(String.format("%-8s | %-25s | %-12s | %s\n", "ID", "Title", "Line Value", "Notes"));
            report.append("--------------------------------------------------------------------------------------\n");

            double total = 0.0;
            for (VideoGame g : games) {
                double lineVal = g.calculateTotalValuation();
                total += lineVal;
                String note = "";
                if (g.isCollectorEdition() && g.getQuantityInStock() > 50) {
                    note = "+15% Collector Surcharge, -10% Wholesale Discount";
                } else if (g.isCollectorEdition()) {
                    note = "+15% Collector Surcharge";
                } else if (g.getQuantityInStock() > 50) {
                    note = "-10% Wholesale Discount";
                }

                report.append(String.format("%-8d | %-25s | $%10.2f | %s\n",
                        g.getGameId(),
                        g.getTitle().length() > 25 ? g.getTitle().substring(0, 22) + "..." : g.getTitle(),
                        lineVal,
                        note));
            }

            report.append("======================================================================================\n");
            report.append(String.format("TOTAL DATABASE INVENTORY VALUATION: $%.2f\n", total));

            JTextArea textArea = new JTextArea(report.toString());
            textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(680, 360));

            JOptionPane.showMessageDialog(this, scrollPane, "Database Valuation & Discount Report", JOptionPane.INFORMATION_MESSAGE);
            return total;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Calculation Error: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return 0.0;
        }
    }

    /**
     * Queries the SQLite table and updates the JTable view in real time.
     *
     * @return the number of rows populated into the table
     */
    public int refreshTableData() {
        tableModel.setRowCount(0);
        if (!dbManager.isConnected()) {
            return 0;
        }

        try {
            List<VideoGame> games = dbManager.getAllGames();
            for (VideoGame g : games) {
                tableModel.addRow(new Object[]{
                        g.getGameId(),
                        g.getTitle(),
                        g.getPlatform(),
                        String.format("%.2f", g.getBasePrice()),
                        g.getQuantityInStock(),
                        g.isCollectorEdition() ? "Yes (Collector)" : "No (Standard)"
                });
            }
            return games.size();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Refresh Error: " + ex.getMessage(),
                    "Database Read Error", JOptionPane.ERROR_MESSAGE);
            return 0;
        }
    }

    /**
     * Synchronizes input text fields with the selected table row.
     *
     * @return true if a row is selected and populated, false otherwise
     */
    public boolean populateFormFromSelectedRow() {
        int selectedRow = gamesTable.getSelectedRow();
        if (selectedRow >= 0) {
            txtId.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtTitle.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtPlatform.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtPrice.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtStock.setText(tableModel.getValueAt(selectedRow, 4).toString());
            String collectorStr = tableModel.getValueAt(selectedRow, 5).toString();
            chkCollector.setSelected(collectorStr.startsWith("Yes"));
            return true;
        }
        return false;
    }

    /**
     * Clears text inputs and resets active table selection.
     *
     * @return true always
     */
    public boolean clearFormFields() {
        txtId.setText("");
        txtTitle.setText("");
        txtPlatform.setText("");
        txtPrice.setText("");
        txtStock.setText("");
        chkCollector.setSelected(false);
        gamesTable.clearSelection();
        return true;
    }

    /**
     * Safely closes the database connection and exits the application.
     *
     * @return true if exit is confirmed, false otherwise
     */
    public boolean handleExit() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to close RetroVault DMS?",
                "Exit Confirmation", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dbManager.closeConnection();
            System.exit(0);
            return true;
        }
        return false;
    }

    /**
     * Checks if a valid database connection is open, prompting the user if not.
     *
     * @return true if connected, false if disconnected
     */
    public boolean ensureConnected() {
        if (!dbManager.isConnected()) {
            JOptionPane.showMessageDialog(this,
                    "Database is not connected. Please connect to a database file at the top first.",
                    "Connection Required", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Formats buttons for visual hierarchy and readability.
     *
     * @param btn the JButton to style
     * @param bg  the background Color to apply
     * @return true always
     */
    public boolean styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        return true;
    }
}