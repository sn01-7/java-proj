import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductPriceApp extends JFrame {
    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/price_comparison_db";
    private static final String DB_USER = "narayan"; // Replace with your username
    private static final String DB_PASSWORD = "Qwerty@12345"; // Replace with your password

    // GUI components
    private JTextField productNameField;
    private JTextField priceField;
    private JTextField urlField;
    private JButton saveButton;
    private JButton fetchButton;
    private JButton showAllButton; // Button to show all products
    private JButton clearAllButton; // Button to clear all entries
    private JTextArea resultArea;

    public ProductPriceApp() {
        setTitle("Product Price Management");
        setSize(800, 600); // Increased size for better visibility
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Create input fields and buttons
        productNameField = new JTextField(20);
        priceField = new JTextField(20);
        urlField = new JTextField(20);

        saveButton = new JButton("Save Product");
        fetchButton = new JButton("Fetch Lowest Price Product");
        showAllButton = new JButton("Show All Products");
        clearAllButton = new JButton("Clear All Entries"); // New button

        // Set button colors
        saveButton.setBackground(Color.RED);
        fetchButton.setBackground(Color.BLUE);
        showAllButton.setBackground(Color.PINK);
        clearAllButton.setBackground(Color.YELLOW); // Different color for clarity

        // Result area
        resultArea = new JTextArea(15, 50); // Increased height
        resultArea.setEditable(false);
        resultArea.setBackground(Color.WHITE); // Set brighter background color for result area
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Use monospaced font for better alignment
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Add components to the frame with GridBagLayout
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10); // Add padding between components

        // Product Name
        gbc.gridx = 0; gbc.gridy = 0; add(new JLabel("Product Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; add(productNameField, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 1; add(new JLabel("Price:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; add(priceField, gbc);

        // Product URL
        gbc.gridx = 0; gbc.gridy = 2; add(new JLabel("Product URL:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; add(urlField, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 3; add(saveButton, gbc);
        gbc.gridx = 1; gbc.gridy = 3; add(fetchButton, gbc);
        gbc.gridx = 2; gbc.gridy = 3; add(showAllButton, gbc);
        gbc.gridx = 3; gbc.gridy = 3; add(clearAllButton, gbc); // Add clear all button

        // Result Area
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4; add(scrollPane, gbc);

        // Add Action Listeners for buttons
        saveButton.addActionListener(new SaveProductAction());
        fetchButton.addActionListener(new FetchLowestPriceProductAction());
        showAllButton.addActionListener(new ShowAllProductsAction());
        clearAllButton.addActionListener(new ClearAllEntriesAction()); // Add action listener for clear all

        setVisible(true);
    }

    // Action to save product information
    private class SaveProductAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String productName = productNameField.getText();
            String priceText = priceField.getText();
            String url = urlField.getText();

            if (productName.isEmpty() || priceText.isEmpty() || url.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill all fields.");
                return;
            }

            try {
                double price = Double.parseDouble(priceText);
                saveProductToDatabase(productName, price, url);
                JOptionPane.showMessageDialog(null, "Product saved successfully.");
                clearFields(); // Clear fields after saving
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid price format.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error saving product: " + ex.getMessage());
            }
        }
    }

    // Action to fetch product information for the lowest price
    private class FetchLowestPriceProductAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String productName = productNameField.getText();

            if (productName.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter a product name to fetch.");
                return;
            }

            try {
                Product product = fetchLowestPriceProductFromDatabase(productName);
                if (product != null) {
                    resultArea.setText(String.format("Product: %s\nPrice: %.2f\nURL: %s",
                            product.getName(), product.getPrice(), product.getUrl()));
                } else {
                    resultArea.setText("Product not found.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error fetching product: " + ex.getMessage());
            }
        }
    }

    // Action to show all products by name
    private class ShowAllProductsAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String productName = productNameField.getText(); // Get the product name from the input field

            if (productName.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter a product name to show.");
                return;
            }

            try {
                List<Product> products = fetchProductsByNameFromDatabase(productName);
                if (!products.isEmpty()) {
                    StringBuilder allProducts = new StringBuilder("Products for '" + productName + "':\n");
                    allProducts.append(String.format("%-25s %-10s %-30s%n", "Product", "Price", "URL")); // Header
                    allProducts.append("-".repeat(70)).append("\n"); // Separator
                    for (Product product : products) {
                        allProducts.append(String.format("%-25s %-10.2f %-30s%n",
                                product.getName(), product.getPrice(), product.getUrl()));
                    }
                    resultArea.setText(allProducts.toString());
                } else {
                    resultArea.setText("No products found for: " + productName);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error fetching products: " + ex.getMessage());
            }
        }
    }

    // Action to clear all input fields
    private class ClearAllEntriesAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            clearFields(); // Clear input fields
            resultArea.setText(""); // Clear the result area
        }
    }

    // Method to save product data to the database
    private void saveProductToDatabase(String productName, double price, String url) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO products (product_name, price, url) VALUES (?, ?, ?)")) { // Removed site
            stmt.setString(1, productName);
            stmt.setDouble(2, price);
            stmt.setString(3, url);
            stmt.executeUpdate();
        }
    }

    // Method to fetch the product with the lowest price from the database
    private Product fetchLowestPriceProductFromDatabase(String productName) throws SQLException {
        Product lowestPriceProduct = null;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM products WHERE product_name = ? ORDER BY price ASC LIMIT 1")) {
            stmt.setString(1, productName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                lowestPriceProduct = new Product(
                        rs.getString("product_name"),
                        rs.getDouble("price"),
                        rs.getString("url") // Removed site
                );
            }
        }
        return lowestPriceProduct;
    }

    // Method to fetch all products by name from the database
    private List<Product> fetchProductsByNameFromDatabase(String productName) throws SQLException {
        List<Product> products = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM products WHERE product_name = ?")) {
                stmt.setString(1, productName);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    products.add(new Product(
                            rs.getString("product_name"),
                            rs.getDouble("price"),
                            rs.getString("url") // Removed site
                    ));
                }
            }
            return products;
        }
    
        // Clear input fields after saving
        private void clearFields() {
            productNameField.setText("");
            priceField.setText("");
            urlField.setText("");
        }
    
        // Inner class to represent a Product
        private static class Product {
            private String name;
            private double price;
            private String url;
    
            public Product(String name, double price, String url) { // Removed site parameter
                this.name = name;
                this.price = price;
                this.url = url;
            }
    
            public String getName() { return name; }
            public double getPrice() { return price; }
            public String getUrl() { return url; }
        }
    
        public static void main(String[] args) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, "MySQL Driver not found: " + e.getMessage());
                return;
            }
    
            SwingUtilities.invokeLater(ProductPriceApp::new);
        }
    }
   