import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Smart Expense Tracker");
        frame.setSize(780, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        JLabel titleLabel = new JLabel("Expense Title:");
        titleLabel.setBounds(30, 30, 100, 25);
        frame.add(titleLabel);

        JTextField titleField = new JTextField();
        titleField.setBounds(150, 30, 200, 25);
        frame.add(titleField);

        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setBounds(30, 80, 100, 25);
        frame.add(amountLabel);

        JTextField amountField = new JTextField();
        amountField.setBounds(150, 80, 200, 25);
        frame.add(amountField);

        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setBounds(30, 130, 100, 25);
        frame.add(categoryLabel);

        String[] categories = {
                "Food",
                "Travel",
                "Shopping",
                "Bills",
                "Entertainment"
        };

        JComboBox<String> categoryBox = new JComboBox<>(categories);
        categoryBox.setBounds(150, 130, 200, 25);
        frame.add(categoryBox);

        JButton addButton = new JButton("Add Expense");
        addButton.setBounds(30, 200, 140, 35);
        frame.add(addButton);

        JButton viewButton = new JButton("View Expenses");
        viewButton.setBounds(190, 200, 140, 35);
        frame.add(viewButton);

        JButton deleteButton = new JButton("Delete Expense");
        deleteButton.setBounds(350, 200, 150, 35);
        frame.add(deleteButton);

        JButton categoryButton = new JButton("Category Summary");
        categoryButton.setBounds(520, 200, 170, 35);
        frame.add(categoryButton);

        JButton pieChartButton = new JButton("Pie Chart");
        pieChartButton.setBounds(30, 240, 140, 35);
        frame.add(pieChartButton);

        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(30, 290, 700, 140);
        frame.add(scrollPane);

        addButton.addActionListener(e -> {
            String title = titleField.getText();
            String amount = amountField.getText();
            String category = (String) categoryBox.getSelectedItem();

            try {
                Connection con = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/expense_tracker",
                        "root",
                        "root"
                );

                String query = "INSERT INTO expenses(title, amount, category) VALUES (?, ?, ?)";

                PreparedStatement pst = con.prepareStatement(query);
                pst.setString(1, title);
                pst.setDouble(2, Double.parseDouble(amount));
                pst.setString(3, category);

                pst.executeUpdate();

                JOptionPane.showMessageDialog(frame, "Expense Added Successfully!");

                titleField.setText("");
                amountField.setText("");

                con.close();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        });

        viewButton.addActionListener(e -> {
            model.setColumnCount(0);
            model.addColumn("ID");
            model.addColumn("Title");
            model.addColumn("Amount");
            model.addColumn("Category");
            model.setRowCount(0);

            try {
                Connection con = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/expense_tracker",
                        "root",
                        "root"
                );

                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM expenses");

                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getDouble("amount"),
                            rs.getString("category")
                    });
                }

                con.close();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select an expense to delete.");
                return;
            }

            if (model.getColumnCount() < 4) {
                JOptionPane.showMessageDialog(frame, "Please click View Expenses before deleting.");
                return;
            }

            int id = (int) model.getValueAt(selectedRow, 0);

            try {
                Connection con = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/expense_tracker",
                        "root",
                        "root"
                );

                String query = "DELETE FROM expenses WHERE id = ?";

                PreparedStatement pst = con.prepareStatement(query);
                pst.setInt(1, id);

                pst.executeUpdate();

                model.removeRow(selectedRow);

                JOptionPane.showMessageDialog(frame, "Expense Deleted Successfully!");

                con.close();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        });

        categoryButton.addActionListener(e -> {
            model.setColumnCount(0);
            model.addColumn("Category");
            model.addColumn("Total Spending");
            model.setRowCount(0);

            try {
                Connection con = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/expense_tracker",
                        "root",
                        "root"
                );

                String query = "SELECT category, SUM(amount) AS total FROM expenses GROUP BY category";

                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(query);

                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("category"),
                            rs.getDouble("total")
                    });
                }

                con.close();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        });

        pieChartButton.addActionListener(e -> {

            Map<String, Double> categoryData = new HashMap<>();

            try {
                Connection con = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/expense_tracker",
                        "root",
                        "root"
                );

                String query = "SELECT category, SUM(amount) AS total FROM expenses GROUP BY category";

                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(query);

                while (rs.next()) {
                    categoryData.put(
                            rs.getString("category"),
                            rs.getDouble("total")
                    );
                }

                con.close();

                JFrame chartFrame = new JFrame("Category-wise Spending Pie Chart");
                chartFrame.setSize(500, 500);
                chartFrame.add(new PieChartPanel(categoryData));
                chartFrame.setVisible(true);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        });

        frame.setVisible(true);
    }
}

class PieChartPanel extends JPanel {

    private Map<String, Double> data;

    public PieChartPanel(Map<String, Double> data) {
        this.data = data;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (data == null || data.isEmpty()) {
            g.drawString("No data available", 180, 220);
            return;
        }

        Graphics2D g2d = (Graphics2D) g;

        double total = 0;
        for (double value : data.values()) {
            total += value;
        }

        int startAngle = 0;
        int x = 80;
        int y = 60;
        int width = 250;
        int height = 250;

        Color[] colors = {
                Color.RED,
                Color.BLUE,
                Color.GREEN,
                Color.ORANGE,
                Color.MAGENTA,
                Color.CYAN,
                Color.PINK
        };

        int colorIndex = 0;
        int legendY = 340;

        for (String category : data.keySet()) {
            double value = data.get(category);
            int arcAngle = (int) Math.round((value / total) * 360);

            g2d.setColor(colors[colorIndex % colors.length]);
            g2d.fillArc(x, y, width, height, startAngle, arcAngle);

            g2d.fillRect(80, legendY, 15, 15);
            g2d.setColor(Color.BLACK);
            g2d.drawString(category + " - ₹" + value, 110, legendY + 12);

            startAngle += arcAngle;
            legendY += 25;
            colorIndex++;
        }
    }
}