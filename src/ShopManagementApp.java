import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ShopManagementApp extends JFrame {
    private Connection connection;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public ShopManagementApp() {
        super("*****  Ganesh Auto-Mobile Bambwade ******");

        // Initialize GUI components
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton viewProductsButton = new JButton("View Products");
        JButton viewSalesButton = new JButton("View Sales");
        JButton addProductButton = new JButton("Add Product");
        JButton recordSaleButton = new JButton("Record Sale");
        JButton searchButton = new JButton("Search");
        JButton emptyStockbtn = new JButton("Empty Stock");
        JButton showemptyStockbtn = new JButton("Show Empty Stock");
        JButton showTodaySalesButton = new JButton("Today's Sales Amount");
        JButton manageMechanicsButton = new JButton("Manage Mechanics");
        JButton Refreshbtn = new JButton("Refresh");
        JButton delars = new JButton("Delars_info");
        JButton adddealar = new JButton("add Dealar");

        searchField = new JTextField();

        // Add action listeners
        viewProductsButton.addActionListener(e -> viewProducts());
        viewSalesButton.addActionListener(e -> viewSales());
        addProductButton.addActionListener(e -> addProduct());
        recordSaleButton.addActionListener(e -> recordSale());
        searchButton.addActionListener(e -> searchProducts());
        emptyStockbtn.addActionListener(e -> emptyStock());
        showemptyStockbtn.addActionListener(e -> showEmptyStock());
        showTodaySalesButton.addActionListener(e -> showTodaySalesAmount());
        manageMechanicsButton.addActionListener(e -> manageMechanics());
        Refreshbtn.addActionListener(e -> refresh());
        delars.addActionListener(e->Delars());
        adddealar.addActionListener(e->addDealer());

        // Add components to the frame
        JPanel buttonPanel = new JPanel(new GridLayout(11, 1));
        buttonPanel.add(Refreshbtn);
        buttonPanel.add(viewProductsButton);
        buttonPanel.add(viewSalesButton);
        buttonPanel.add(addProductButton);
        buttonPanel.add(recordSaleButton);
        buttonPanel.add(emptyStockbtn);
        buttonPanel.add(showemptyStockbtn);
        buttonPanel.add(showTodaySalesButton);
        buttonPanel.add(manageMechanicsButton);
        buttonPanel.add(delars);
        buttonPanel.add(adddealar);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        Container contentPane = getContentPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.WEST);
        contentPane.add(searchPanel, BorderLayout.NORTH);

        // Connect to the database
        connectToDatabase();

        // Set up frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // this for exit button
        setSize(800, 600);
        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }

    private void connectToDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/myshop2";
            String user = "root";
            String password = "@#aditya2006";
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the database.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void viewProducts() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM products");
            tableModel.setColumnIdentifiers(new String[]{"ID", "Name", "Quantity", "Price"});
            tableModel.setRowCount(0); // Clear existing rows
            while (resultSet.next()) {
                tableModel.addRow(new Object[]{
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("quantity"),
                        String.format("%.2f", resultSet.getBigDecimal("price"))
                });
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to retrieve products.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewSales() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM sales");
            tableModel.setColumnIdentifiers(new String[]{"ID", "Product ID", "Quantity", "Total Price", "Sale Date"});
            tableModel.setRowCount(0); // Clear existing rows
            while (resultSet.next()) {
                tableModel.addRow(new Object[]{
                        resultSet.getInt("id"),
                        resultSet.getInt("product_id"),
                        resultSet.getInt("quantity"),
                        String.format("%.2f", resultSet.getBigDecimal("total_price")),
                        resultSet.getDate("sale_date")
                });
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to retrieve sales.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addProduct() {
        String name = JOptionPane.showInputDialog(this, "Enter product name:");
        if (name != null && !name.trim().isEmpty()) {
            try {
                int quantity = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter product quantity:"));
                double price = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter product price:"));
                PreparedStatement statement = connection.prepareStatement("INSERT INTO products (name, quantity, price) VALUES (?, ?, ?)");
                statement.setString(1, name);
                statement.setInt(2, quantity);
                statement.setDouble(3, price);
                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "Product added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add product.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                statement.close();
            } catch (SQLException | NumberFormatException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void recordSale() {
        String productIdStr = JOptionPane.showInputDialog(this, "Enter product ID:");
        if (productIdStr != null && !productIdStr.trim().isEmpty()) {
            try {
                int productId = Integer.parseInt(productIdStr);
                int quantitySold = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter quantity sold:"));
                double totalPrice = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter total price:"));

                connection.setAutoCommit(false); // Start transaction

                // Check product availability
                PreparedStatement checkProductStmt = connection.prepareStatement("SELECT quantity FROM products WHERE id = ?");
                checkProductStmt.setInt(1, productId);
                ResultSet resultSet = checkProductStmt.executeQuery();
                if (resultSet.next()) {
                    int currentQuantity = resultSet.getInt("quantity");
                    if (currentQuantity >= quantitySold) {
                        // Update product quantity
                        PreparedStatement updateProductStmt = connection.prepareStatement("UPDATE products SET quantity = quantity - ? WHERE id = ?");
                        updateProductStmt.setInt(1, quantitySold);
                        updateProductStmt.setInt(2, productId);
                        updateProductStmt.executeUpdate();

                        // Record the sale
                        PreparedStatement insertSaleStmt = connection.prepareStatement("INSERT INTO sales (product_id, quantity, total_price, sale_date) VALUES (?, ?, ?, )");
                        insertSaleStmt.setInt(1, productId);
                        insertSaleStmt.setInt(2, quantitySold);
                        insertSaleStmt.setDouble(3, totalPrice);
                        insertSaleStmt.executeUpdate();

                        connection.commit(); // Commit transaction
                        JOptionPane.showMessageDialog(this, "Sale recorded successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Insufficient stock.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Product not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }

                connection.setAutoCommit(true); // End transaction
                resultSet.close();
                checkProductStmt.close();
            } catch (SQLException | NumberFormatException e) {
                e.printStackTrace();
                try {
                    connection.rollback(); // Rollback transaction on error
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
                JOptionPane.showMessageDialog(this, "Invalid input or transaction failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void emptyStock() {
        String getProductName = JOptionPane.showInputDialog(this, "Enter name of empty stock:");
        if (getProductName != null && !getProductName.trim().isEmpty()) {
            String q = "INSERT INTO stock (st_name) VALUES (?)";
            try {
                PreparedStatement ps = connection.prepareStatement(q);
                ps.setString(1, getProductName);
                int afr = ps.executeUpdate();
                if (afr > 0) {
                    JOptionPane.showMessageDialog(this, "Successfully done...", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Error... Retry.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error... Retry.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEmptyStock() {
        try {
            PreparedStatement ps = connection.prepareStatement("select * from stock;");
            ResultSet rs = ps.executeQuery();
//            ResultSet rs = st.executeQuery("SELECT * FROM stock");
            tableModel.setColumnIdentifiers(new String[]{"ID", "Stock Name"});
            tableModel.setRowCount(0); // Clear existing rows
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("pid"),
                        rs.getString("st_name")
                });
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error...", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTodaySalesAmount() {
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT SUM(total_price) AS total_amount FROM sales WHERE sale_date = CURDATE()");
            tableModel.setColumnIdentifiers(new String[]{"Today's Total Sales Amount"});
            tableModel.setRowCount(0); // Clear existing rows
            if (rs.next()) {
//                tableModel.addRow(new Object[]{
//                        String.format("%.2f", rs.getBigDecimal("total_amount"))
//                });
                JOptionPane.showMessageDialog(this,"today all sale is : "+rs.getBigDecimal("total_amount"));
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error...", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void refresh() {
        String query = "SELECT name FROM products WHERE quantity = 0";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String productName = rs.getString("name");

                // Check if the product is already in the stock table
                String checkQuery = "SELECT COUNT(*) FROM stock WHERE st_name = ?";
                PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
                checkStmt.setString(1, productName);
                ResultSet checkRs = checkStmt.executeQuery();

                if (checkRs.next() && checkRs.getInt(1) == 0) {
                    // Product is not in stock table, so add it
                    String insertQuery = "INSERT INTO stock(st_name) VALUES(?)";
                    PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                    insertStmt.setString(1, productName);
                    int afr = insertStmt.executeUpdate();
                    if (afr > 0) {
                        JOptionPane.showMessageDialog(this, "Refreshed: Added new zero quantity product.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Refreshed: No changes made.");
                    }
                    insertStmt.close();
                }

                checkRs.close();
                checkStmt.close();
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Some error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void searchProducts() {
        String searchTerm = searchField.getText();
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM products WHERE name LIKE ?");
                statement.setString(1, "%" + searchTerm + "%");
                ResultSet resultSet = statement.executeQuery();
                tableModel.setColumnIdentifiers(new String[]{"ID", "Name", "Quantity", "Price"});
                tableModel.setRowCount(0); // Clear existing rows
                while (resultSet.next()) {
                    tableModel.addRow(new Object[]{
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getInt("quantity"),
                            String.format("%.2f", resultSet.getBigDecimal("price"))
                    });
                }
                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to search products.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void Delars(){
        String query = "select * from delars;";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet resultSet = ps.executeQuery();
            tableModel.setColumnIdentifiers(new String[]{"ID", "comp/dealar Name", "Last deal Tamount", "Pending amount", "deal Date"});
            tableModel.setRowCount(0); // Clear existing rows
            while (resultSet.next()) {
                tableModel.addRow(new Object[]{
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("total_price"),
                        resultSet.getInt("pending_amount"),
                        resultSet.getDate("stRecDate")
                });

            }
            resultSet.close();
            ps.close();
        }catch (SQLException e) {
            JOptionPane.showMessageDialog(this,"error occur try Again..!");
        }
    }
    private void addDealer() {
        String query = "insert into delars(name,total_price,pending_amount,stRecDate) values(?,?,?,CURDATE());";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            String name = JOptionPane.showInputDialog(this,"enter dealar/company name: ");
            if(name != null && !name.trim().isEmpty()) {
                int totAmount = Integer.parseInt(JOptionPane.showInputDialog(this, "enter total deal amount : "));
                int pendingAmount = Integer.parseInt(JOptionPane.showInputDialog(this, "enter pending amount : "));

                ps.setString(1, name);
                ps.setInt(2, totAmount);
                ps.setInt(3, pendingAmount);

                int afr = ps.executeUpdate();
                if (afr > 0) {
                    JOptionPane.showMessageDialog(this, "deal succesFully added..");
                } else {
                    JOptionPane.showMessageDialog(this, "deal unsuccesFul  to added..");
                }
            }

        }catch (SQLException e) {
            JOptionPane.showMessageDialog(this,"error occurs tyr Again..!");
        }
    }
// if some one click on ManageMach then then call this function then using this function i create the object of ManaageMachanics class then invoked tha
    // constructor
    private void manageMechanics() {
        // Open a new window for managing mechanics
        //SwingUtilities.invokeLater(() -> new ManageMechanicsFrame(connection));
        ManageMechanicsFrame mm = new ManageMechanicsFrame(connection);
    }

    public static void main(String[] args) {
//        SwingUtilities.invokeLater(ShopManagementApp::new);
        ShopManagementApp Ganesh_auto = new ShopManagementApp();
    }
}

class ManageMechanicsFrame extends JFrame {
    private Connection connection;
    private JTable table;
    private DefaultTableModel tableModel;

    public ManageMechanicsFrame(Connection connection) {
        super("Manage Mechanics");

        this.connection = connection;

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        JButton addMechanicButton = new JButton("Add Mechanic");
        JButton deleteMechanicButton = new JButton("Delete Mechanic");
        JButton showMechanicsButton = new JButton("Show Mechanics");

        addMechanicButton.addActionListener(e -> addMechanic());
        deleteMechanicButton.addActionListener(e -> deleteMechanic());
        showMechanicsButton.addActionListener(e -> viewMechanics());

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1));
        buttonPanel.add(addMechanicButton);
        buttonPanel.add(deleteMechanicButton);
        buttonPanel.add(showMechanicsButton);

        Container contentPane = getContentPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.WEST);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }

    private void addMechanic() {
        String name = JOptionPane.showInputDialog(this, "Enter mechanic name:");
        String specialization = JOptionPane.showInputDialog(this, "Enter mechanic specialization:");
        String contactNumber = JOptionPane.showInputDialog(this, "Enter mechanic contact number:");

        if (name != null && !name.trim().isEmpty() && specialization != null && !specialization.trim().isEmpty()) {
            try {
                String query = "INSERT INTO mechanics (name, specialization, contact_number) VALUES (?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, name);
                statement.setString(2, specialization);
                statement.setString(3, contactNumber);
                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "Mechanic added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add mechanic.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding mechanic to database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Name and specialization are required.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteMechanic() {
        String mechanicIdStr = JOptionPane.showInputDialog(this, "Enter mechanic ID to delete:");
        if (mechanicIdStr != null && !mechanicIdStr.trim().isEmpty()) {
            try {
                int mechanicId = Integer.parseInt(mechanicIdStr);
                String query = "DELETE FROM mechanics WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, mechanicId);
                int rowsDeleted = statement.executeUpdate();
                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Mechanic deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Mechanic not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                statement.close();
            } catch (SQLException | NumberFormatException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid mechanic ID.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Mechanic ID is required.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void viewMechanics() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM mechanics");
            tableModel.setColumnIdentifiers(new String[]{"ID", "Name", "Specialization", "Contact Number"});
            tableModel.setRowCount(0);
            while (resultSet.next()) {
                tableModel.addRow(new Object[]{
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("specialization"),
                        resultSet.getString("contact_number")
                });
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to retrieve mechanics.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void updateMechanic() {
        String idStr = JOptionPane.showInputDialog(this, "Enter mechanic ID to update:");
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                String name = JOptionPane.showInputDialog(this, "Enter new mechanic name:");
                String specialization = JOptionPane.showInputDialog(this, "Enter new specialization:");
                String contactNumber = JOptionPane.showInputDialog(this, "Enter new contact number:");

                if (name != null && !name.trim().isEmpty() && specialization != null && !specialization.trim().isEmpty()) {
                    String query = "UPDATE mechanics SET name = ?, specialization = ?, contact_number = ? WHERE id = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, name);
                    statement.setString(2, specialization);
                    statement.setString(3, contactNumber);
                    statement.setInt(4, id);
                    int rowsUpdated = statement.executeUpdate();
                    if (rowsUpdated > 0) {
                        JOptionPane.showMessageDialog(this, "Mechanic updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "No mechanic found with the provided ID.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    statement.close();
                } else {
                    JOptionPane.showMessageDialog(this, "Name and specialization are required.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Invalid input or failed to update mechanic.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


}