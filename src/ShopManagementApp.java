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
        super("_Ganesh Auto-Mobile Bambwade____");

        Timer timer = new Timer(60000, e -> showUpcomingReminders());  // Check every minute for reminders
        timer.start();

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
        JButton updateQn = new JButton("updateQuantity");
        JButton upadateDeal = new JButton("updateDeal");
        JButton addReminderButton = new JButton("Add Reminder");

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
        updateQn.addActionListener(e->updateProductQn());
        upadateDeal.addActionListener(e->updateDeal());
        addReminderButton.addActionListener(e -> addReminder());

        // Add components to the frame
        JPanel buttonPanel = new JPanel(new GridLayout(14, 1));
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
        buttonPanel.add(updateQn);
        buttonPanel.add(upadateDeal);
        buttonPanel.add(addReminderButton);


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
                        PreparedStatement insertSaleStmt = connection.prepareStatement("INSERT INTO sales (product_id, quantity, total_price, sale_date) VALUES (?, ?, ?, CURDATE())");
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
            JOptionPane.showMessageDialog(this, "Error...");
        }
    }
//    private void adminAll(){
//        String password = new String();
//        password = JOptionPane.showInputDialog(this,"enter admin password : ");
//        if(password.equals("Ganesh2020")){
//            JOptionPane.showMessageDialog(this,"okay go it..");
//            String ans = new String();
//            ans = JOptionPane.showInputDialog(this,"clear all sales agree or not..!");
//            if(ans.equals("agree")) {
//                PreparedStatement ps = connection.prepareStatement("delete * from ")
//            }
//
//        }else {
//            JOptionPane.showMessageDialog(this,"Invalid....!");
//        }
//    }

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
    private void updateProductQn() {
        String query = "update products set quantity = ? where id = ?;";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            String pidstr = JOptionPane.showInputDialog(this,"enter product id : ");
           if(pidstr != null && !pidstr.trim().isEmpty()) {
               int pid = Integer.parseInt(pidstr);
               int quantity = Integer.parseInt(JOptionPane.showInputDialog(this, "enter product quantity : "));

               ps.setInt(2, pid);
               ps.setInt(1, quantity);
               int afr = ps.executeUpdate();
               if (afr > 0) {
                   JOptionPane.showMessageDialog(this, "succesfully updated..");
               } else {
                   JOptionPane.showMessageDialog(this, "Product not here...!");

               }
           }
        }catch (SQLException e) {
            JOptionPane.showMessageDialog(this,"error at Update product quantity..!");
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
    private void updateDeal(){
        String sdId = JOptionPane.showInputDialog(this, "Enter update deal ID: ");
        //int dId = Integer.parseInt(sdId);
       if(sdId != null && !sdId.trim().isEmpty()) {
           int dId = Integer.parseInt(sdId);
           if (idExist(dId)) {
               String uTotalAm = JOptionPane.showInputDialog(this, "Enter updated total deal amount: ");
               String uPendingAm = JOptionPane.showInputDialog(this, "Enter updated pending amount: ");
               int tAmount = Integer.parseInt(uTotalAm);
               int PendAm = Integer.parseInt(uPendingAm);

               // Query using CURDATE() for the current date
               String query = "UPDATE delars SET total_price = ?, pending_amount = ?, stRecDate = CURDATE() WHERE id = ?;";

               try {
                   PreparedStatement ps = connection.prepareStatement(query);
                   ps.setInt(1, tAmount);         // Set total_price
                   ps.setInt(2, PendAm);          // Set pending_amount
                   ps.setInt(3, dId);             // Set id in WHERE clause

                   int rowsUpdated = ps.executeUpdate();
                   if (rowsUpdated > 0) {
                       JOptionPane.showMessageDialog(this, "Deal updated successfully.");
                   } else {
                       JOptionPane.showMessageDialog(this, "Deal update failed.");
                   }
               } catch (SQLException e) {
                   JOptionPane.showMessageDialog(this, "An error occurred while updating the deal.");
                   e.printStackTrace();
               }
           } else {
               JOptionPane.showMessageDialog(this, "ID doesn't exist!");
           }
       }
    }

    public boolean idExist(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement("select name from delars where id = ?");
            ps.setInt(1,id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return true;
            }else {
                return false;
            }
        }catch (SQLException e) {
            //System.out.println("errror here..!!");
        }
        return false;
    }
    private void addReminder() {
        String reminderName = JOptionPane.showInputDialog(this, "Enter Reminder Name:");
        String reminderDateStr = JOptionPane.showInputDialog(this, "Enter Reminder Date (YYYY-MM-DD):");
        String reminderTimeStr = JOptionPane.showInputDialog(this, "Enter Reminder Time (HH:MM):");
        String description = JOptionPane.showInputDialog(this, "Enter Description:");

        try {
            Date reminderDate = Date.valueOf(reminderDateStr);
            Time reminderTime = Time.valueOf(reminderTimeStr + ":00");

            String query = "INSERT INTO reminders (reminder_name, reminder_date, reminder_time, description) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, reminderName);
            ps.setDate(2, reminderDate);
            ps.setTime(3, reminderTime);
            ps.setString(4, description);

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Reminder added successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add reminder.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error in adding reminder.");
        }
    }
    private void showUpcomingReminders() {
        String query = "SELECT * FROM reminders WHERE reminder_date = CURDATE() AND reminder_time > CURTIME() AND is_shown = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String reminderName = rs.getString("reminder_name");
                String description = rs.getString("description");

                // Show reminder notification
                JOptionPane.showMessageDialog(this, "Upcoming Reminder: " + reminderName + "\nDescription: " + description, "Reminder", JOptionPane.INFORMATION_MESSAGE);

                // Mark the reminder as shown
                int reminderId = rs.getInt("id");
                PreparedStatement updatePs = connection.prepareStatement("UPDATE reminders SET is_shown = 1 WHERE id = ?");
                updatePs.setInt(1, reminderId);
                updatePs.executeUpdate();
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error in fetching reminders.");
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

class Reminder {
    private int id;
    private String reminderName;
    private Date reminderDate;
    private Time reminderTime;
    private String description;
    private boolean isShown;

    public Reminder(int id, String reminderName, Date reminderDate, Time reminderTime, String description, boolean isShown) {
        this.id = id;
        this.reminderName = reminderName;
        this.reminderDate = reminderDate;
        this.reminderTime = reminderTime;
        this.description = description;
        this.isShown = isShown;
    }

    // Getters and setters
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
        JButton updateMech = new JButton("update Mech");

        addMechanicButton.addActionListener(e -> addMechanic());
        deleteMechanicButton.addActionListener(e -> deleteMechanic());
        showMechanicsButton.addActionListener(e -> viewMechanics());
        updateMech.addActionListener(e -> updateMechanic());

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1));
        buttonPanel.add(addMechanicButton);
        buttonPanel.add(deleteMechanicButton);
        buttonPanel.add(showMechanicsButton);
        buttonPanel.add(updateMech);

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
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, name);
                ps.setString(2, specialization);
                ps.setString(3, contactNumber);
                int rowsInserted = ps.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "Mechanic added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add mechanic.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                ps.close();
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
