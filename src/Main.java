import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class Main {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Smart Expense Tracker");
        frame.setSize(700, 500);
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

        JComboBox<String> categoryBox =
                new JComboBox<>(categories);

        categoryBox.setBounds(150, 130, 200, 25);
        frame.add(categoryBox);

        JButton addButton = new JButton("Add Expense");
        addButton.setBounds(50, 200, 140, 35);
        frame.add(addButton);

        JButton viewButton = new JButton("View Expenses");
        viewButton.setBounds(220, 200, 140, 35);
        frame.add(viewButton);

        JButton deleteButton = new JButton("Delete Expense");
        deleteButton.setBounds(390, 200, 150, 35);
        frame.add(deleteButton);

        String[] columns = {
                "ID",
                "Title",
                "Amount",
                "Category"
        };

        DefaultTableModel model =
                new DefaultTableModel(columns, 0);

        JTable table = new JTable(model);

        JScrollPane scrollPane =
                new JScrollPane(table);

        scrollPane.setBounds(30, 270, 620, 150);

        frame.add(scrollPane);

        addButton.addActionListener(e -> {

            String title = titleField.getText();
            String amount = amountField.getText();
            String category =
                    (String) categoryBox.getSelectedItem();

            try {

                Connection con = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/expense_tracker",
                        "root",
                        "root"
                );

                String query =
                        "INSERT INTO expenses(title, amount, category) VALUES (?, ?, ?)";

                PreparedStatement pst =
                        con.prepareStatement(query);

                pst.setString(1, title);

                pst.setDouble(2,
                        Double.parseDouble(amount));

                pst.setString(3, category);

                pst.executeUpdate();

                JOptionPane.showMessageDialog(frame,
                        "Expense Added Successfully!");

                titleField.setText("");
                amountField.setText("");

                con.close();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(frame,
                        ex.getMessage());
            }

        });

        viewButton.addActionListener(e -> {

            model.setRowCount(0);

            try {

                Connection con = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/expense_tracker",
                        "root",
                        "root"
                );

                Statement st = con.createStatement();

                ResultSet rs =
                        st.executeQuery("SELECT * FROM expenses");

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

                JOptionPane.showMessageDialog(frame,
                        ex.getMessage());
            }

        });

        deleteButton.addActionListener(e -> {

            int selectedRow = table.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame,
                        "Please select an expense to delete.");
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

                JOptionPane.showMessageDialog(frame,
                        "Expense Deleted Successfully!");

                con.close();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame,
                        ex.getMessage());
            }
        });
        frame.setVisible(true);

    }
}