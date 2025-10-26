import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginSignup extends JFrame {
    private JTextField usernameField, emailField;
    private JPasswordField passwordField;
    private JButton loginBtn, signupBtn, switchBtn;
    private JComboBox<String> roleBox;
    private JLabel emailLabel, roleLabel;
    private boolean isLoginMode = true;

    public LoginSignup() {
        setTitle("Turf Booking - Login / Signup");
        setSize(420, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Define Final Color Palette ---
        Color TITLE_COLOR = Color.BLACK; // ⬅️ NEW: Black for the title
        Color BACKGROUND_COLOR = new Color(240, 240, 240); // Light Gray for main panel
        Color TEXT_COLOR = new Color(57, 62, 70); // Dark text for labels/inputs
        Color FIELD_BACKGROUND = Color.WHITE; // White input background
        Color BUTTON_BACKGROUND_COLOR = new Color(50, 50, 50); // Dark Gray/Near Black for buttons
        Color ACCENT_COLOR = new Color(0, 173, 181); // Teal (can be used for field borders)

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        // 🎨 Applied light background color
        panel.setBackground(BACKGROUND_COLOR);

        JLabel title = new JLabel("Turf Booking System", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        // 🚀 Correction Applied: Title text color changed to BLACK
        title.setForeground(TITLE_COLOR); 

        JLabel usernameLabel = new JLabel("Username:");
        emailLabel = new JLabel("Email:");
        JLabel passwordLabel = new JLabel("Password:");
        roleLabel = new JLabel("Role:");
        
        // Applied dark text color to labels
        usernameLabel.setForeground(TEXT_COLOR);
        emailLabel.setForeground(TEXT_COLOR);
        passwordLabel.setForeground(TEXT_COLOR);
        roleLabel.setForeground(TEXT_COLOR);

        usernameField = new JTextField();
        emailField = new JTextField();
        passwordField = new JPasswordField();
        roleBox = new JComboBox<>(new String[] {"User", "Turf Owner"});
        
        // Styled text fields and combo box
        JTextField[] fields = {usernameField, emailField, passwordField};
        for (JTextField field : fields) {
            field.setBackground(FIELD_BACKGROUND);
            field.setForeground(TEXT_COLOR);
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            field.setBorder(BorderFactory.createLineBorder(TEXT_COLOR, 1)); // Dark border
        }
        roleBox.setBackground(FIELD_BACKGROUND);
        roleBox.setForeground(TEXT_COLOR);
        roleBox.setBorder(BorderFactory.createLineBorder(TEXT_COLOR, 1));


        loginBtn = new JButton("Login");
        signupBtn = new JButton("Sign Up");
        switchBtn = new JButton("Switch to Signup");

        JButton[] buttons = {loginBtn, signupBtn, switchBtn};
        for (JButton b : buttons) {
            // Updated button style to dark gray background and white text
            b.setBackground(BUTTON_BACKGROUND_COLOR);
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createLineBorder(BUTTON_BACKGROUND_COLOR.darker(), 2)); 
            b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        }

        // --- Layout unchanged ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; panel.add(title, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; panel.add(usernameLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(emailLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(passwordLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; panel.add(roleLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 4; panel.add(roleBox, gbc);

        gbc.gridx = 0; gbc.gridy = 5; panel.add(loginBtn, gbc);
        gbc.gridx = 1; gbc.gridy = 5; panel.add(signupBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; panel.add(switchBtn, gbc);

        add(panel);

        emailLabel.setVisible(false);
        emailField.setVisible(false);
        signupBtn.setVisible(false);
        roleBox.setVisible(false);
        roleLabel.setVisible(false);
        
        switchBtn.addActionListener(e -> toggleMode());
        loginBtn.addActionListener(e -> loginUser());
        signupBtn.addActionListener(e -> signupUser());

        setVisible(true);
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        emailLabel.setVisible(!isLoginMode);
        emailField.setVisible(!isLoginMode);
        roleBox.setVisible(!isLoginMode);
        roleLabel.setVisible(!isLoginMode);
        loginBtn.setVisible(isLoginMode);
        signupBtn.setVisible(!isLoginMode);
        switchBtn.setText(isLoginMode ? "Switch to Signup" : "Switch to Login");
    }

    private void loginUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // Get user role from database
                String role = rs.getString("role");
                String email = rs.getString("email");
                
                // Store user session
                SessionManager.login(username, role);
                
                JOptionPane.showMessageDialog(this, 
                    "Login Successful! Welcome " + username + " (" + role + ")");
                
                // Redirect based on role
                SessionManager.redirectBasedOnRole(this);
                
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "A database error occurred during login.");
            ex.printStackTrace();
        }
    }

    private void signupUser() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String role = (String) roleBox.getSelectedItem();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields (Username, Email, Password) are required!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO users(username, email, password, role) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, username);
            pst.setString(2, email);
            pst.setString(3, password);
            pst.setString(4, role);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Signup Successful! You can now login.");
            
            // Switch to login mode and pre-fill username
            toggleMode();
            usernameField.setText(username); // Pre-fill the username
            passwordField.setText(""); // Clear password field
            
        } catch (SQLIntegrityConstraintViolationException e) {
            JOptionPane.showMessageDialog(this, "Username already exists!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "A database error occurred during signup.");
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginSignup());
    }
}

// SessionManager class for handling user sessions
class SessionManager {
    private static String currentUsername;
    private static String currentRole;
    private static boolean isLoggedIn = false;
    
    public static void login(String username, String role) {
        currentUsername = username;
        currentRole = role;
        isLoggedIn = true;
        System.out.println("✅ User logged in: " + username + " as " + role);
    }
    
    public static void logout() {
        currentUsername = null;
        currentRole = null;
        isLoggedIn = false;
        System.out.println("✅ User logged out");
    }
    
    public static boolean isLoggedIn() { return isLoggedIn; }
    public static String getCurrentUser() { return currentUsername; }
    public static String getCurrentRole() { return currentRole; }
    
    public static void redirectBasedOnRole(JFrame currentFrame) {
        if (!isLoggedIn) {
            System.out.println("❌ No user logged in for redirection");
            return;
        }
        
        System.out.println("🔄 Redirecting user: " + currentUsername + " with role: " + currentRole);
        currentFrame.dispose(); // Close login window
        
        // Small delay to ensure window closes properly
        Timer timer = new Timer(300, e -> {
            try {
                if ("Turf Owner".equals(currentRole)) {
                    System.out.println("🚀 Opening TurfOwnerDashboard...");
                    new TurfOwnerDashboard().setVisible(true);
                } else {
                    System.out.println("🚀 Opening TurfHub...");
                    new TurfHub().setVisible(true);
                }
            } catch (Exception ex) {
                System.err.println("❌ Error opening dashboard: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
}

// Ensure your non-public DBConnection class is present below this public class in the same file.
class DBConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/turf_booking_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = ""; // Changed to empty string for XAMPP

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("FATAL ERROR: MySQL JDBC Driver not found. Check your classpath!");
            throw new SQLException("MySQL JDBC Driver not found. Make sure your JDBC JAR is in the classpath.", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
}
