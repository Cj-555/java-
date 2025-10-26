import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.sql.*;
import java.awt.event.ActionListener;
import java.util.Vector; 

/**
 * Main application class for the customer-facing side of the Turf Booking Platform.
 * Displays a list of turfs fetched from the MySQL database using JDBC.
 * Adheres to SRP (TurfService for DB, TurfHub for UI) and uses basic Swing components.
 */
public class TurfHub extends JFrame {

    // --- Color Palette ---
    private final Color DARK_TEXT = new Color(50, 50, 50);
    private final Color WHITE = Color.WHITE;
    private final Color GRAY_BG = new Color(245, 245, 245);
    private final Color ACCENT_BUTTON_COLOR = new Color(30, 0, 150); // Royal Blue accent
    private final Color SUCCESS_COLOR = new Color(0, 150, 0); // Green for success message

    private JPanel turfDisplayPanel; 
    private final CardLayout cardLayout = new CardLayout();
    private JPanel mainCardPanel; 

    private final static String HOME_VIEW = "HOME";
    private final static String BOOKING_VIEW = "BOOKING";
    
    // --- Application Initialization ---

    public TurfHub() { 
        this("Football"); 
    }

    public TurfHub(String category) {
        super("Turf Booking App - " + category);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 700); 
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());
        
        add(createHeader(), BorderLayout.NORTH);

        // Initialize CardLayout for view switching (Home/Booking)
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.add(createHubContent(), HOME_VIEW);
        
        // Add the Booking Form view
        mainCardPanel.add(createBookingFormPanel(), BOOKING_VIEW); 
        
        add(mainCardPanel, BorderLayout.CENTER); 
        
        setVisible(true);
        // Load the actual data from the database
        displayTurfsForCategory(category); 
    }
    
    // --- GUI Components ---

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ACCENT_BUTTON_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel title = new JLabel("TurfZone - Explore & Book");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(WHITE);
        header.add(title, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        logoutButton.setBackground(Color.RED.darker());
        logoutButton.setForeground(WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> handleLogout());
        
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(logoutButton);
        header.add(buttonWrapper, BorderLayout.EAST);
        
        return header;
    }

    private JScrollPane createHubContent() {
        // Outer container uses FlowLayout to center the inner display panel
        JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER));
        container.setBackground(GRAY_BG);
        
        // This panel now needs to be wrapped in a scroll pane
        turfDisplayPanel = new JPanel(); 
        turfDisplayPanel.setLayout(new BoxLayout(turfDisplayPanel, BoxLayout.Y_AXIS));
        turfDisplayPanel.setBackground(GRAY_BG);
        turfDisplayPanel.setPreferredSize(new Dimension(750, 600)); 
        turfDisplayPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        
        JScrollPane scrollPane = new JScrollPane(turfDisplayPanel);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(780, 650));
        
        container.add(scrollPane); 
        return new JScrollPane(container);
    }

    private void displayTurfsForCategory(String category) {
        turfDisplayPanel.removeAll(); 
        
        List<TurfModel> turfs = TurfService.getTurfsByCategory(category);

        if (turfs.isEmpty()) {
            turfDisplayPanel.add(new JLabel("No " + category + " turfs found."));
        } else {
            JLabel title = new JLabel(category + " Turfs");
            title.setFont(new Font("SansSerif", Font.BOLD, 24));
            title.setForeground(ACCENT_BUTTON_COLOR); 
            title.setAlignmentX(Component.LEFT_ALIGNMENT); 
            turfDisplayPanel.add(title);
            turfDisplayPanel.add(Box.createVerticalStrut(15)); 

            for (TurfModel turf : turfs) {
                turfDisplayPanel.add(createTurfCard(turf));
                turfDisplayPanel.add(Box.createVerticalStrut(15));
            }
        }
        turfDisplayPanel.revalidate();
        turfDisplayPanel.repaint();
    }
    
    /**
     * Creates a card for a single turf using pure Swing layout (no HTML).
     */
    private JPanel createTurfCard(TurfModel turf) { 
        JPanel card = new JPanel(new BorderLayout()); 
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        )); 
        card.setPreferredSize(new Dimension(750, 120)); 
        card.setMaximumSize(new Dimension(750, 120));

        // --- Left Side: Details Panel ---
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 5, 2, 5);
        
        // 1. Name (Bold)
        JLabel nameLabel = new JLabel(turf.getName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; detailsPanel.add(nameLabel, gbc);
        
        // 2. Address (Regular)
        JLabel addressLabel = new JLabel(turf.getAddress());
        addressLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridy = 1; detailsPanel.add(addressLabel, gbc);

        // 3. Hours (Regular)
        JLabel hoursLabel = new JLabel("Hours: " + turf.getOperatingHours());
        hoursLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridy = 2; detailsPanel.add(hoursLabel, gbc);

        card.add(detailsPanel, BorderLayout.WEST);

        // --- Right side: Price and Button ---
        JPanel actionPanel = new JPanel(); 
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBackground(WHITE);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 15)); 

        JLabel priceLabel = new JLabel(String.format("Price: ₹%.2f/hour", turf.getPricePerHour()), SwingConstants.RIGHT); 
        priceLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        priceLabel.setForeground(ACCENT_BUTTON_COLOR); 
        priceLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        JButton bookButton = new JButton("Book Now");
        bookButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        bookButton.setBackground(ACCENT_BUTTON_COLOR);
        bookButton.setForeground(WHITE);
        bookButton.setFocusPainted(false);
        bookButton.setMaximumSize(new Dimension(150, 35));
        
        // Action: Opens Booking Form
        bookButton.addActionListener(e -> handleBookNow(turf.getName())); 
        bookButton.setAlignmentX(Component.RIGHT_ALIGNMENT);

        actionPanel.add(priceLabel); 
        actionPanel.add(Box.createVerticalStrut(10)); 
        actionPanel.add(bookButton); 
        
        card.add(actionPanel, BorderLayout.EAST);
        return card;
    }

    // --- NEW VIEW: Booking Form Panel (Simple Interface) ---
    private JPanel createBookingFormPanel() {
        JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER));
        container.setBackground(GRAY_BG);
        
        // The inner card panel
        JPanel formCard = new JPanel(new BorderLayout());
        formCard.setBackground(WHITE);
        formCard.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        formCard.setPreferredSize(new Dimension(500, 450)); 
        
        // 1. Title Area
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Confirm Your Booking", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(ACCENT_BUTTON_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Please select your turf, date, and time.", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(DARK_TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(title);
        headerPanel.add(subtitle);
        headerPanel.add(Box.createVerticalStrut(20));
        
        formCard.add(headerPanel, BorderLayout.NORTH);

        // 2. Form Fields Area (using GridBagLayout for alignment)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.weightx = 1.0; 

        // Simulated data for the dropdowns
        Vector<String> turfs = new Vector<>();
        turfs.add("Star Turf Club (Default)");
        turfs.add("Champions Dome");
        turfs.add("Ground Zero Arena");
        
        Vector<String> slots = new Vector<>();
        slots.add("10:00 - 11:00 (10 AM)");
        slots.add("11:00 - 12:00 (11 AM)");
        slots.add("12:00 - 13:00 (12 PM)");
        
        // Input Components
        JComboBox<String> turfBox = new JComboBox<>(turfs);
        JTextField dateField = new JTextField("2025-10-27"); 
        JComboBox<String> timeBox = new JComboBox<>(slots);
        
        // Add components to the grid
        addRow(formPanel, gbc, 0, "Select Turf Name", turfBox);
        addRow(formPanel, gbc, 1, "Select Booking Date", dateField);
        addRow(formPanel, gbc, 2, "Select Time Slot", timeBox);

        formCard.add(formPanel, BorderLayout.CENTER);

        // 3. Action Buttons
        JButton confirmButton = new JButton("Confirm Booking");
        JButton cancelButton = new JButton("Cancel");
        
        // Style Buttons simply (Dark Blue for Confirm, Gray for Cancel)
        confirmButton.setBackground(ACCENT_BUTTON_COLOR);
        confirmButton.setForeground(WHITE);
        confirmButton.setFocusPainted(false);
        confirmButton.setPreferredSize(new Dimension(150, 35));

        cancelButton.setBackground(Color.GRAY.darker());
        cancelButton.setForeground(WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setPreferredSize(new Dimension(100, 35));

        // Action Listeners
        cancelButton.addActionListener(e -> cardLayout.show(mainCardPanel, HOME_VIEW));
        // Pass the JFrame reference to the confirmation dialog
        confirmButton.addActionListener(e -> showBookingConfirmationDialog(this, turfBox.getSelectedItem().toString(), dateField.getText())); 
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        buttonPanel.setOpaque(false);
        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);
        
        formCard.add(buttonPanel, BorderLayout.SOUTH);
        
        container.add(formCard);
        return container;
    }
    
    /** Helper to add a label-input pair to the GridBagLayout. */
    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent inputComponent) {
        // Label
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0; 
        gbc.anchor = GridBagConstraints.WEST;
        JLabel label = new JLabel(labelText + ":");
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(DARK_TEXT);
        panel.add(label, gbc);

        // Input Component
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0; 
        panel.add(inputComponent, gbc);
    }
    
    /**
     * Shows a custom JDialog for booking confirmation, matching the style requested.
     * This replaces the simple JOptionPane.
     */
    private void showBookingConfirmationDialog(JFrame parent, String turfName, String date) {
        // Mock data for the success message
        String confirmationId = "#" + (int)(Math.random() * 9000 + 1000);
        String timeSlot = "10:00 - 11:00";
        int userId = 13; // Simulated user ID
        
        JDialog dialog = new JDialog(parent, "Booking Confirmed!", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().setBackground(WHITE);

        // --- Content Panel ---
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        contentPanel.setBackground(WHITE);
        
        // 1. Success Title
        JLabel successTitle = new JLabel("BOOKING SUCCESS!", SwingConstants.CENTER);
        successTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        successTitle.setForeground(SUCCESS_COLOR);
        successTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(successTitle);
        contentPanel.add(Box.createVerticalStrut(20));

        // 2. Details Grid
        JPanel detailGrid = new JPanel(new GridLayout(4, 2, 5, 10));
        detailGrid.setOpaque(false);
        
        // Helper method to add key/value pair to grid
        Action addDetail = new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String[] parts = e.getActionCommand().split(":", 2);
                JLabel key = new JLabel(parts[0] + ":");
                key.setFont(new Font("SansSerif", Font.BOLD, 14));
                key.setForeground(DARK_TEXT);
                
                JLabel value = new JLabel(parts[1].trim());
                value.setFont(new Font("SansSerif", Font.PLAIN, 14));
                value.setForeground(ACCENT_BUTTON_COLOR);
                
                detailGrid.add(key);
                detailGrid.add(value);
            }
        };

        addDetail.actionPerformed(new java.awt.event.ActionEvent(this, 0, "Confirmation ID: " + confirmationId));
        addDetail.actionPerformed(new java.awt.event.ActionEvent(this, 0, "Turf: " + turfName));
        addDetail.actionPerformed(new java.awt.event.ActionEvent(this, 0, "Date & Time: " + date + " @ " + timeSlot));
        addDetail.actionPerformed(new java.awt.event.ActionEvent(this, 0, "Logged in User ID: " + userId));
        
        contentPanel.add(detailGrid);
        contentPanel.add(Box.createVerticalGlue()); 

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        buttonPanel.setBackground(WHITE);
        
        JButton okButton = new JButton("OK / View Bookings");
        okButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        // 🚩 FIX: Remove button color to make it look neutral/default
        okButton.setBackground(null); // Set background to null for default look
        okButton.setForeground(ACCENT_BUTTON_COLOR); // Use accent color for text
        okButton.setFocusPainted(false);
        okButton.setPreferredSize(new Dimension(200, 40)); 
        
        okButton.addActionListener(e -> {
            dialog.dispose(); // Close the dialog
            cardLayout.show(mainCardPanel, HOME_VIEW); // Go back to home view
        });

        buttonPanel.add(okButton);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    // --- Controller Logic ---

    private void handleBookingConfirmation() {
        // Show the custom dialog instead of the generic JOptionPane
        showBookingConfirmationDialog(this, "Star Turf Club", "2025-10-27"); 
    }

    private void handleBookNow(String turfName) {
        if (!SessionManager.isLoggedIn()) {
             JOptionPane.showMessageDialog(this, "Please log in to book a slot.", "Session Required", JOptionPane.INFORMATION_MESSAGE);
             return;
        }
        
        cardLayout.show(mainCardPanel, BOOKING_VIEW);
    }
    
    private void handleLogout() {
        SessionManager.logout(); 
        JOptionPane.showMessageDialog(null, "Logged out successfully.", "Logout", JOptionPane.INFORMATION_MESSAGE);
        this.dispose(); 
    }

    // --- OOP Data Model (Encapsulation) ---

    /** Class to model the data of a single turf. Adheres to Encapsulation. */
    static class TurfModel {
        private final int id;
        private final String name;
        private final String address;
        private final double pricePerHour; 
        private final String operatingHours;
        private final String category;

        public TurfModel(int id, String name, String address, double pricePerHour, String operatingHours, String category) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.pricePerHour = pricePerHour;
            this.operatingHours = operatingHours;
            this.category = category;
        }

        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getOperatingHours() { return operatingHours; }
        public double getPricePerHour() { return pricePerHour; } 
    }
    
    /** Simple class to manage the user's logged-in status. Adheres to Single Responsibility. */
    static class SessionManager {
        private static boolean isLoggedIn = true; 

        public static boolean isLoggedIn() { return isLoggedIn; }
        public static void login() { isLoggedIn = true; }
        public static void logout() { isLoggedIn = false; }
    }
    
    // --- New OOP Class: Data Access Object (DAO) ---

    /** * Adheres to SRP: Handles all communication between the application and the database. 
     */
    static class TurfService {
        
        // Prepared Statement for querying turfs by category
        private static final String SELECT_TURFS_SQL = "SELECT id, name, address, hourly_rate, operating_hours, category FROM turfs WHERE category = ?";
        
        public static List<TurfModel> getTurfsByCategory(String category) {
            List<TurfModel> turfs = new ArrayList<>();
            // Use try-with-resources to ensure connection and statement are closed
            try (Connection conn = DBConnection.getConnection(); 
                 PreparedStatement pst = conn.prepareStatement(SELECT_TURFS_SQL)) {
                
                pst.setString(1, category);
                
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        // Map the ResultSet row to a TurfModel object
                        turfs.add(new TurfModel(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("address"),
                            rs.getDouble("hourly_rate"),
                            rs.getString("operating_hours"),
                            rs.getString("category")
                        ));
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, 
                    "Database Error: Could not load turfs. Check XAMPP/MySQL connection.", 
                    "JDBC Connection Failed", 
                    JOptionPane.ERROR_MESSAGE);
                System.err.println("SQL Error while fetching turfs: " + e.getMessage());
            }
            return turfs;
        }
    }

    // --- JDBC Connection Class ---

    /** * Utility class to manage database connection details. 
     */
    static class DBConnection {
        private static final String DB_URL = "jdbc:mysql://localhost:3306/turf_booking_db";
        private static final String DB_USER = "root";
        private static final String DB_PASS = ""; // Default XAMPP password is usually empty

        public static Connection getConnection() throws SQLException {
            try {
                // Load the MySQL JDBC Driver (Step 1: Register the Driver class)
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                System.err.println("FATAL ERROR: MySQL JDBC Driver not found. Ensure the Connector/J JAR is in your project's classpath.");
                throw new SQLException("MySQL JDBC Driver not found.", e);
            }
            // Establish the connection (Step 2: Create connection)
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        }
    }

    // --- Main Method ---
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new TurfHub("Football"));
    }
}
