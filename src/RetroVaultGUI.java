import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Steven Piskunov
 * CEN-3024C - Software Development I
 * October 30, 2026
 * RetroVaultGUI.java
 *
 * Graphical User Interface application controller for the RetroVault Video Game DMS.
 * This class builds an interactive, crash-proof desktop dashboard enabling users to
 * batch import from text files, view catalog tables, add, update, remove, and execute
 * dynamic valuation and discount calculations without using console prompts.
 */
public class RetroVaultGUI extends JFrame {

    private InventoryManager inventory;
    private DefaultTableModel tableModel;
    private JTable gamesTable;

    private JTextField txtId;
    private JTextField txtTitle;
    private JTextField txtPlatform;
    private JTextField txtPrice;
    private JTextField txtStock;
    private JCheckBox chkCollector;
    private JTextField txtFilePath;

    public RetroVaultGUI() {
        this.inventory = new InventoryManager();
        initializeUI();
    }

    /**
     * method: main
     * parameters: String[] args
     * return: void
     * purpose: Main entry point for Phase 3; instantiates the graphical desktop application
     *          on the Swing Event Dispatch Thread and displays the GUI dashboard.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RetroVaultGUI app = new RetroVaultGUI();
            app.setVisible(true);
        });
    }

    /**
     * method: initializeUI
     * parameters: none
     * return: boolean
     * purpose: Configures the main application window, visual styling, layout panels,
     *          data tables, and interactive action buttons for all CRUD operations.
     */
    public boolean initializeUI() {
        setTitle("RetroVault DMS - Video Game Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 247, 250));

        mainPanel.add(buildTopFileBar(), BorderLayout.NORTH);
        mainPanel.add(buildCenterTablePanel(), BorderLayout.CENTER);
        mainPanel.add(buildLeftFormPanel(), BorderLayout.WEST);
        mainPanel.add(buildBottomActionBar(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
        return true;
    }

    /**
     * method: buildTopFileBar
     * parameters: none
     * return: JPanel
     * purpose: Constructs the file loading toolbar containing text input, file chooser browser,
     *          and batch ingestion execution triggers. Satisfies Rubric Test 2.
     */
    public JPanel buildTopFileBar() {
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filePanel.setBorder(BorderFactory.createTitledBorder("Batch Ingestion"));
        filePanel.setBackground(Color.WHITE);

        JLabel lblPath = new JLabel("File Path / Name:");
        lblPath.setFont(new Font("Segoe UI", Font.BOLD, 12));

        txtFilePath = new JTextField("games.txt", 25);
        txtFilePath.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton btnBrowse = new JButton("Browse...");
        styleButton(btnBrowse, new Color(74, 85, 104));
        btnBrowse.addActionListener(e -> handleBrowseFile());

        JButton btnLoad = new JButton("Load Games File");
        styleButton(btnLoad, new Color(43, 108, 176));
        btnLoad.addActionListener(e -> handleLoadFile());

        filePanel.add(lblPath);
        filePanel.add(txtFilePath);
        filePanel.add(btnBrowse);
        filePanel.add(btnLoad);

        return filePanel;
    }

    /**
     * method: buildCenterTablePanel
     * parameters: none
     * return: JPanel
     * purpose: Creates the central tabular display rendering all 6 attributes of each
     *          VideoGame record in sortable columns. Satisfies Rubric Test 3.
     */
    public JPanel buildCenterTablePanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Catalog Overview"));
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
     * method: buildLeftFormPanel
     * parameters: none
     * return: JPanel
     * purpose: Builds the structured data entry form for adding and updating game attributes
     *          with integrated input validation controls. Satisfies Rubric Test 4 & Test 6.
     */
    public JPanel buildLeftFormPanel() {
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setPreferredSize(new Dimension(300, 0));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Record Details"));
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
        JButton btnAdd = new JButton("Add Record (Create)");
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
     * method: buildBottomActionBar
     * parameters: none
     * return: JPanel
     * purpose: Constructs the operational footer providing Update, Delete (with confirmation),
     *          Custom Valuation action reporting, and clean Exit controls.
     */
    public JPanel buildBottomActionBar() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("System Operations"));
        bottomPanel.setBackground(Color.WHITE);

        JButton btnUpdate = new JButton("Update Selected Record");
        styleButton(btnUpdate, new Color(214, 158, 46));
        btnUpdate.addActionListener(e -> handleUpdateRecord());

        JButton btnDelete = new JButton("Delete Record");
        styleButton(btnDelete, new Color(229, 62, 62));
        btnDelete.addActionListener(e -> handleDeleteRecord());

        JButton btnCustom = new JButton("Run Valuation & Discounts");
        styleButton(btnCustom, new Color(107, 70, 193));
        btnCustom.addActionListener(e -> handleCustomAction());

        JButton btnExit = new JButton("Exit Application");
        styleButton(btnExit, new Color(26, 32, 44));
        btnExit.addActionListener(e -> handleExit());

        bottomPanel.add(btnUpdate);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnCustom);
        bottomPanel.add(btnExit);

        return bottomPanel;
    }

    /**
     * method: handleBrowseFile
     * parameters: none
     * return: boolean
     * purpose: Launches a native JFileChooser enabling the user to browse and select a text file.
     */
    public boolean handleBrowseFile() {
        JFileChooser chooser = new JFileChooser(new File("."));
        int status = chooser.showOpenDialog(this);
        if (status == JFileChooser.APPROVE_OPTION) {
            txtFilePath.setText(chooser.getSelectedFile().getPath());
            return true;
        }
        return false;
    }

    /**
     * method: handleLoadFile
     * parameters: none
     * return: int
     * purpose: Reads and validates the user-specified text file path, loads valid records,
     *          handles blank/invalid file names safely, and refreshes the catalog table.
     */
    public int handleLoadFile() {
        String path = txtFilePath.getText().trim();
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Error: File path cannot be blank. Please enter a valid file path or browse.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }

        try {
            int imported = inventory.loadGamesFromFile(path);
            refreshTableData();
            JOptionPane.showMessageDialog(this,
                    "Successfully imported " + imported + " video game records from:\n" + path,
                    "Batch Load Complete", JOptionPane.INFORMATION_MESSAGE);
            return imported;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "File Error: " + e.getMessage(),
                    "File Load Failed", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    /**
     * method: handleAddRecord
     * parameters: none
     * return: boolean
     * purpose: Validates all six form attributes, prevents duplicate IDs and crashes,
     *          instantiates the VideoGame object, and updates the table. Satisfies Rubric Test 4.
     */
    public boolean handleAddRecord() {
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

        if (inventory.findGameById(id) != null) {
            JOptionPane.showMessageDialog(this, "Error: Game ID '" + id + "' already exists in catalog. IDs must be unique.",
                    "Duplicate Key Error", JOptionPane.ERROR_MESSAGE);
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
        boolean added = inventory.addGame(game);
        if (added) {
            refreshTableData();
            clearFormFields();
            JOptionPane.showMessageDialog(this, "Video Game '" + title + "' added successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        return false;
    }

    /**
     * method: handleUpdateRecord
     * parameters: none
     * return: boolean
     * purpose: Updates editable attributes of an existing game selected from the table or form,
     *          safely validating data types and refreshing the display. Satisfies Rubric Test 6.
     */
    public boolean handleUpdateRecord() {
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

        VideoGame target = inventory.findGameById(id);
        if (target == null) {
            JOptionPane.showMessageDialog(this, "Error: Game ID " + id + " was not found in the catalog.",
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
        int newStock;
        try {
            newPrice = Double.parseDouble(priceStr);
            if (newPrice < 0.0) {
                JOptionPane.showMessageDialog(this, "Price cannot be negative.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid decimal format for price.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

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

        refreshTableData();
        JOptionPane.showMessageDialog(this, "Record " + id + " successfully updated!",
                "Update Complete", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    /**
     * method: handleDeleteRecord
     * parameters: none
     * return: boolean
     * purpose: Purges a record by ID after prompting for user confirmation, verifying the record
     *          no longer exists, and refreshing the catalog table. Satisfies Rubric Test 5.
     */
    public boolean handleDeleteRecord() {
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

        VideoGame target = inventory.findGameById(id);
        if (target == null) {
            JOptionPane.showMessageDialog(this, "Error: Game ID " + id + " does not exist in the system.",
                    "Not Found", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete '" + target.getTitle() + "' (ID: " + id + ")?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return false;
        }

        boolean removed = inventory.removeGame(id);
        if (removed) {
            refreshTableData();
            clearFormFields();
            JOptionPane.showMessageDialog(this, "Game record ID " + id + " successfully deleted.",
                    "Record Deleted", JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        return false;
    }

    /**
     * method: handleCustomAction
     * parameters: none
     * return: double
     * purpose: Executes inventory valuation and wholesale discount calculations across the catalog
     *          and displays a formatted summary dialog. Satisfies Rubric Test 7.
     */
    public double handleCustomAction() {
        List<VideoGame> games = inventory.getAllGames();
        if (games.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Catalog is currently empty. Total inventory valuation is $0.00.",
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
        report.append(String.format("TOTAL CUMULATIVE CATALOG VALUATION: $%.2f\n", total));

        JTextArea textArea = new JTextArea(report.toString());
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(650, 350));

        JOptionPane.showMessageDialog(this, scrollPane, "Inventory Valuation & Discount Report", JOptionPane.INFORMATION_MESSAGE);
        return total;
    }

    /**
     * method: handleExit
     * parameters: none
     * return: boolean
     * purpose: Prompts confirmation before closing the application cleanly. Satisfies GUI Layout & Test 1.
     */
    public boolean handleExit() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit RetroVault DMS?",
                "Exit Confirmation", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0);
            return true;
        }
        return false;
    }

    /**
     * method: refreshTableData
     * parameters: none
     * return: int
     * purpose: Clears and repopulates the JTable rows from the InventoryManager list.
     */
    public int refreshTableData() {
        tableModel.setRowCount(0);
        List<VideoGame> games = inventory.getAllGames();
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
    }

    /**
     * method: populateFormFromSelectedRow
     * parameters: none
     * return: boolean
     * purpose: Synchronizes the left-hand form text fields with the row currently selected in the table.
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
     * method: clearFormFields
     * parameters: none
     * return: boolean
     * purpose: Clears all text input fields in the CRUD form and resets table selection.
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
     * method: styleButton
     * parameters: JButton btn, Color bg
     * return: boolean
     * purpose: Applies consistent colors, fonts, and borders to buttons for UI/UX readability.
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