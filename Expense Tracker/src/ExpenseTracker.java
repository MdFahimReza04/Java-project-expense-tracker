import java.sql.*;
import java.util.*;

public class ExpenseTracker {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/expense_tracker";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "me,my,mine";
    private static final Scanner sc = new Scanner(System.in);
    private static Connection conn;
    private static String currentUser = null;
    private static int userId = -1;

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            while (true) {
                System.out.println("1. Register\n2. Login\n3. Forgot Password\n4. Exit");
                int choice = getValidIntInput("choice");
                sc.nextLine();
                switch (choice) {
                    case 1 -> register();
                    case 2 -> {
                        if (login()) {
                            dashboard();
                        }
                    }
                    case 3 -> forgotPassword();
                    case 4 -> {
                        System.out.println("Exiting application...");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please enter a number between 1 and 4.");
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error initializing the application: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
                sc.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    private static void dashboard() {
        while (true) {
            System.out.println("\n--- Dashboard ---");
            System.out.println("1. Expense\n2. Summary\n3. Budget\n4. Search Expense\n" +
                               "5. Filter Expense\n6. Change Password\n7. Exit");
            int choice = getValidIntInput("choice");
            sc.nextLine();
            switch (choice) {
                case 1 -> expenseMenu();
                case 2 -> summaryMenu();
                case 3 -> budgetMenu();
                case 4 -> searchExpenseMenu();
                case 5 -> filterExpenseMenu();
                case 6 -> changePassword();
                case 7 -> {
                    currentUser = null;
                    userId = -1;
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter a number between 1 and 7.");
            }
        }
    }

    private static int getValidIntInput(String field) {
        while (true) {
            try {
                System.out.print("Enter " + field + ": ");
                return sc.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                sc.nextLine();
            }
        }
    }

    private static double getValidDoubleInput(String field) {
        while (true) {
            try {
                System.out.print("Enter " + field + ": ");
                double value = sc.nextDouble();
                if (value >= 0) return value;
                System.out.println("Please enter a non-negative number.");
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                sc.nextLine();
            }
        }
    }

    private static void register() {
        try {
            System.out.print("Enter username: ");
            String username = sc.nextLine();
            if (username.trim().isEmpty()) {
                System.out.println("Username cannot be empty.");
                return;
            }
            System.out.print("Enter password: ");
            String password = sc.nextLine();
            if (password.trim().isEmpty()) {
                System.out.println("Password cannot be empty.");
                return;
            }
            System.out.print("Enter email address: ");
            String email = sc.nextLine();
            if (email.trim().isEmpty()) {
                System.out.println("Email cannot be empty.");
                return;
            }

            PreparedStatement ps = conn.prepareStatement("INSERT INTO users(username, password, email) VALUES(?, ?, ?)");
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            ps.executeUpdate();
            System.out.println("Registration successful.");
        } catch (SQLException e) {
            System.err.println("An error occurred during registration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean login() {
        try {
            System.out.print("Enter username: ");
            String username = sc.nextLine();
            System.out.print("Enter password: ");
            String password = sc.nextLine();

            PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE username=? AND password=?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                currentUser = username;
                userId = rs.getInt("id");
                System.out.println("Login successful.");
                return true;
            } else {
                System.out.println("Invalid credentials.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("An error occurred during login: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static void forgotPassword() {
        try {
            System.out.print("Enter email address: ");
            String email = sc.nextLine();

            PreparedStatement ps = conn.prepareStatement("SELECT username, password FROM users WHERE email=?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                System.out.println("Username: " + username + "\nPassword: " + password);
            } else {
                System.out.println("No account found with that email address.");
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while retrieving password: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void expenseMenu() {
        while (true) {
            System.out.println("\n--- Expense Menu ---");
            System.out.println("1. Add Expense\n2. View Expenses\n3. Edit Expense\n4. Delete Expense\n5. Back");
            int choice = getValidIntInput("choice");
            sc.nextLine();
            switch (choice) {
                case 1 -> addExpense();
                case 2 -> viewExpenses();
                case 3 -> editExpense();
                case 4 -> deleteExpense();
                case 5 -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter a number between 1 and 5.");
            }
        }
    }

    private static void addExpense() {
        try {
            System.out.print("Enter category: ");
            String category = sc.nextLine();
            if (category.trim().isEmpty()) {
                System.out.println("Category cannot be empty.");
                return;
            }
            double amount = getValidDoubleInput("amount");
            sc.nextLine();
            java.sql.Date date = java.sql.Date.valueOf(java.time.LocalDate.now());

            PreparedStatement ps = conn.prepareStatement("INSERT INTO expenses(user_id, category, amount, date) VALUES(?, ?, ?, ?)");
            ps.setInt(1, userId);
            ps.setString(2, category);
            ps.setDouble(3, amount);
            ps.setDate(4, date);
            ps.executeUpdate();
            System.out.println("Expense added successfully.");
        } catch (SQLException e) {
            System.err.println("An error occurred while adding expense: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void viewExpenses() {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM expenses WHERE user_id = ?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Expenses ---");
            if (!rs.isBeforeFirst()) {
                System.out.println("No expenses found.");
                return;
            }
            while (rs.next()) {
                System.out.printf("ID: %d | Category: %s | Amount: %.2f | Date: %s\n",
                        rs.getInt("id"), rs.getString("category"), rs.getDouble("amount"), rs.getDate("date"));
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while viewing expenses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void editExpense() {
        try {
            int id = getValidIntInput("expense ID to edit");
            sc.nextLine();
            System.out.print("Enter new category: ");
            String category = sc.nextLine();
            if (category.trim().isEmpty()) {
                System.out.println("Category cannot be empty.");
                return;
            }
            double amount = getValidDoubleInput("new amount");
            sc.nextLine();

            PreparedStatement ps = conn.prepareStatement("UPDATE expenses SET category=?, amount=? WHERE id=? AND user_id=?");
            ps.setString(1, category);
            ps.setDouble(2, amount);
            ps.setInt(3, id);
            ps.setInt(4, userId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                System.out.println("Expense updated successfully.");
            } else {
                System.out.println("Expense not found or you don't have permission to edit it.");
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while editing expense: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void deleteExpense() {
        try {
            int id = getValidIntInput("expense ID to delete");
            sc.nextLine();
            System.out.print("Are you sure you want to delete this expense? (yes/no): ");
            String confirmation = sc.nextLine();
            if ("yes".equalsIgnoreCase(confirmation)) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM expenses WHERE id=? AND user_id=?");
                ps.setInt(1, id);
                ps.setInt(2, userId);
                int deleted = ps.executeUpdate();
                if (deleted > 0) {
                    System.out.println("Expense deleted successfully.");
                } else {
                    System.out.println("Expense not found or you don't have permission to delete it.");
                }
            } else {
                System.out.println("Expense deletion cancelled.");
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while deleting expense: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void summaryMenu() {
        while (true) {
            System.out.println("\n--- Summary Menu ---");
            System.out.println("1. Monthly Summary\n2. Weekly Summary\n3. Daily Summary\n4. Back");
            int choice = getValidIntInput("choice");
            sc.nextLine();
            switch (choice) {
                case 1 -> monthlySummary();
                case 2 -> weeklySummary();
                case 3 -> dailySummary();
                case 4 -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter a number between 1 and 4.");
            }
        }
    }

    private static void monthlySummary() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT category, SUM(amount) FROM expenses WHERE user_id=? AND MONTH(date)=MONTH(CURDATE()) AND YEAR(date)=YEAR(CURDATE()) GROUP BY category");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Monthly Summary ---");
            if (!rs.isBeforeFirst()) {
                System.out.println("No expenses found for this month.");
                return;
            }
            while (rs.next()) {
                System.out.printf("Category: %s | Total: %.2f\n", rs.getString(1), rs.getDouble(2));
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while getting monthly summary: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void weeklySummary() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT category, SUM(amount) FROM expenses WHERE user_id=? AND date BETWEEN " +
                            "DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) AND " +
                            "DATE_ADD(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 6 DAY) " +
                            "GROUP BY category");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Weekly Summary (Monday to Sunday) ---");
            if (!rs.isBeforeFirst()) {
                System.out.println("No expenses found for this week.");
                return;
            }
            while (rs.next()) {
                System.out.printf("Category: %s | Total: %.2f\n", rs.getString(1), rs.getDouble(2));
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while getting weekly summary: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void dailySummary() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT category, SUM(amount) FROM expenses WHERE user_id=? AND date = CURDATE() GROUP BY category");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Daily Summary ---");
            if (!rs.isBeforeFirst()) {
                System.out.println("No expenses found for today.");
                return;
            }
            while (rs.next()) {
                System.out.printf("Category: %s | Total: %.2f\n", rs.getString(1), rs.getDouble(2));
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while getting daily summary: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void budgetMenu() {
        while (true) {
            System.out.println("\n--- Budget Menu ---");
            System.out.println("1. Monthly Budget\n2. Weekly Budget\n3. Daily Budget\n4. Back");
            int choice = getValidIntInput("choice");
            sc.nextLine();
            switch (choice) {
                case 1 -> monthlyBudgetMenu();
                case 2 -> weeklyBudgetMenu();
                case 3 -> dailyBudgetMenu();
                case 4 -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter a number between 1 and 4.");
            }
        }
    }

    private static void monthlyBudgetMenu() {
        while (true) {
            System.out.println("\n--- Monthly Budget Menu ---");
            System.out.println("1. Set Monthly Budget\n2. Edit Monthly Budget\n3. View Monthly Budget\n" +
                               "4. Delete Monthly Budget\n5. Track Monthly Budget\n6. Back");
            int choice = getValidIntInput("choice");
            sc.nextLine();
            switch (choice) {
                case 1 -> setMonthlyBudget();
                case 2 -> editMonthlyBudget();
                case 3 -> viewMonthlyBudget();
                case 4 -> deleteMonthlyBudget();
                case 5 -> trackMonthlyBudget();
                case 6 -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter a number between 1 and 6.");
            }
        }
    }

    private static void setMonthlyBudget() {
        try {
            double budget = getValidDoubleInput("monthly budget amount");
            sc.nextLine();

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO budgets(user_id, type, amount) VALUES(?, 'monthly', ?) " +
                            "ON DUPLICATE KEY UPDATE amount=?");
            ps.setInt(1, userId);
            ps.setDouble(2, budget);
            ps.setDouble(3, budget);
            ps.executeUpdate();
            System.out.println("Monthly budget set successfully.");
        } catch (SQLException e) {
            System.err.println("An error occurred while setting monthly budget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void editMonthlyBudget() {
        try {
            PreparedStatement checkPs = conn.prepareStatement(
                    "SELECT amount FROM budgets WHERE user_id=? AND type='monthly'");
            checkPs.setInt(1, userId);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next()) {
                System.out.printf("Current monthly budget: %.2f\n", rs.getDouble("amount"));
            } else {
                System.out.println("No monthly budget set.");
                return;
            }

            double newBudget = getValidDoubleInput("new monthly budget amount");
            sc.nextLine();

            PreparedStatement updatePs = conn.prepareStatement(
                    "UPDATE budgets SET amount=? WHERE user_id=? AND type='monthly'");
            updatePs.setDouble(1, newBudget);
            updatePs.setInt(2, userId);
            int updated = updatePs.executeUpdate();
            if (updated > 0) {
                System.out.println("Monthly budget updated successfully.");
            } else {
                System.out.println("Failed to update monthly budget.");
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while editing monthly budget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void viewMonthlyBudget() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT amount FROM budgets WHERE user_id=? AND type='monthly'");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Monthly Budget ---");
            if (rs.next()) {
                System.out.printf("Your monthly budget is: %.2f\n", rs.getDouble("amount"));
            } else {
                System.out.println("No monthly budget set.");
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while viewing monthly budget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void deleteMonthlyBudget() {
        try {
            System.out.print("Are you sure you want to delete the monthly budget? (yes/no): ");
            String confirmation = sc.nextLine();
            if ("yes".equalsIgnoreCase(confirmation)) {
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM budgets WHERE user_id=? AND type='monthly'");
                ps.setInt(1, userId);
                int deleted = ps.executeUpdate();
                if (deleted > 0) {
                    System.out.println("Monthly budget deleted successfully.");
                } else {
                    System.out.println("No monthly budget found.");
                }
            } else {
                System.out.println("Monthly budget deletion cancelled.");
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while deleting monthly budget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void trackMonthlyBudget() {
        try {
            PreparedStatement budgetPs = conn.prepareStatement(
                    "SELECT amount FROM budgets WHERE user_id=? AND type='monthly'");
            budgetPs.setInt(1, userId);
            ResultSet budgetRs = budgetPs.executeQuery();
            double budget = 0;
            if (budgetRs.next()) {
                budget = budgetRs.getDouble("amount");
            } else {
                System.out.println("\n--- Budget Tracking --- \nNo monthly budget set.");
                return;
            }

            PreparedStatement expensePs = conn.prepareStatement(
                    "SELECT SUM(amount) FROM expenses WHERE user_id=? AND MONTH(date)=MONTH(CURDATE()) AND YEAR(date)=YEAR(CURDATE())");
            expensePs.setInt(1, userId);
            ResultSet expenseRs = expensePs.executeQuery();
            double totalExpenses = 0;
            if (expenseRs.next() && expenseRs.getDouble(1) != 0) {
                totalExpenses = expenseRs.getDouble(1);
            }

            double remainingBudget = budget - totalExpenses;
            System.out.println("\n--- Monthly Budget Tracking ---");
            System.out.printf("Monthly Budget: %.2f\n", budget);
            System.out.printf("Total Expenses: %.2f\n", totalExpenses);
            System.out.printf("Remaining Budget: %.2f\n", remainingBudget);
            if (remainingBudget < 0) {
                System.out.println("Warning: You have exceeded your monthly budget!");
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while tracking monthly budget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void weeklyBudgetMenu() {
        while (true) {
            System.out.println("\n--- Weekly Budget Menu ---");
            System.out.println("1. Set Weekly Budget\n2. View Weekly Budget\n3. Edit Weekly Budget\n" +
                               "4. Delete Weekly Budget\n5. Track Weekly Budget\n6. Back");
            int choice = getValidIntInput("choice");
            sc.nextLine();
            switch (choice) {
                case 1 -> setWeeklyBudget();
                case 2 -> viewWeeklyBudget();
                case 3 -> editWeeklyBudget();
                case 4 -> deleteWeeklyBudget();
                case 5 -> trackWeeklyBudget();
                case 6 -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter a number between 1 and 6.");
            }
        }
    }

    private static void setWeeklyBudget() {
        try {
            double budget = getValidDoubleInput("weekly budget amount");
            sc.nextLine();
            java.sql.Date weekStart = java.sql.Date.valueOf(
                    java.time.LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO budgets(user_id, type, amount, week_start) VALUES(?, 'weekly', ?, ?) " +
                            "ON DUPLICATE KEY UPDATE amount=?");
            ps.setInt(1, userId);
            ps.setDouble(2, budget);
            ps.setDate(3, weekStart);
            ps.setDouble(4, budget);
            ps.executeUpdate();
            System.out.println("Weekly budget set successfully.");
        } catch (SQLException e) {
            System.err.println("An error occurred while setting weekly budget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void viewWeeklyBudget() {
        try {
            java.sql.Date weekStart = java.sql.Date.valueOf(
                    java.time.LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT amount FROM budgets WHERE user_id=? AND type='weekly' AND week_start=?");
            ps.setInt(1, userId);
            ps.setDate(2, weekStart);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Weekly Budget ---");
            if (rs.next()) {
                System.out.printf("Your weekly budget is: %.2f\n", rs.getDouble("amount"));
            } else {
                System.out.println("No weekly budget set for this week.");
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while viewing weekly budget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void editWeeklyBudget() {
        try {
            java.sql.Date weekStart = java.sql.Date.valueOf(
                    java.time.LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));
            PreparedStatement checkPs = conn.prepareStatement(
                    "SELECT amount FROM budgets WHERE user_id=? AND type='weekly' AND week_start=?");
            checkPs.setInt(1, userId);
            checkPs.setDate(2, weekStart);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next()) {
                System.out.printf("Current weekly budget: %.2f\n", rs.getDouble("amount"));
            } else {
                System.out.println("No weekly budget set for this week.");
                return;
            }

            double newBudget = getValidDoubleInput("new weekly budget amount");
            sc.nextLine();

            PreparedStatement updatePs = conn.prepareStatement(
                    "UPDATE budgets SET amount=? WHERE user_id=? AND type='weekly' AND week_start=?");
            updatePs.setDouble(1, newBudget);
            updatePs.setInt(2, userId);
            updatePs.setDate(3, weekStart);
            int updated = updatePs.executeUpdate();
            if (updated > 0) {
                System.out.println("Weekly budget updated successfully.");
            } else {
                System.out.println("Failed to update weekly budget.");
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while editing weekly budget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void deleteWeeklyBudget() {
        try {
            java.sql.Date weekStart = java.sql.Date.valueOf(
                    java.time.LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));
            System.out.print("Are you sure you want to delete the weekly budget? (yes/no): ");
            String confirmation = sc.nextLine();
            if ("yes".equalsIgnoreCase(confirmation)) {
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM budgets WHERE user_id=? AND type='weekly' AND week_start=?");
                ps.setInt(1, userId);
                ps.setDate(2, weekStart);
                int deleted = ps.executeUpdate();
                if (deleted > 0) {
                    System.out.println("Weekly budget deleted successfully.");
                } else {
                    System.out.println("No weekly budget found for this week.");
                }
            } else {
                System.out.println("Weekly budget deletion cancelled.");
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while deleting weekly budget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void trackWeeklyBudget() {
        try {
            java.sql.Date weekStart = java.sql.Date.valueOf(
                    java.time.LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));
            PreparedStatement budgetPs = conn.prepareStatement(
                    "SELECT amount FROM budgets WHERE user_id=? AND type='weekly' AND week_start=?");
            budgetPs.setInt(1, userId);
            budgetPs.setDate(2, weekStart);
            ResultSet budgetRs = budgetPs.executeQuery();
            double budget = 0;
            if (budgetRs.next()) {
                budget = budgetRs.getDouble("amount");
            } else {
                System.out.println("\n--- Weekly Budget Tracking --- \nNo weekly budget set.");
                return;
            }

            PreparedStatement expensePs = conn.prepareStatement(
                    "SELECT SUM(amount) FROM expenses WHERE user_id=? AND date BETWEEN ? AND DATE_ADD(?, INTERVAL 6 DAY)");
            expensePs.setInt(1, userId);
            expensePs.setDate(2, weekStart);
            expensePs.setDate(3, weekStart);
            ResultSet expenseRs = expensePs.executeQuery();
            double totalExpenses = 0;
            if (expenseRs.next() && expenseRs.getDouble(1) != 0) {
                totalExpenses = expenseRs.getDouble(1);
            }

            double remainingBudget = budget - totalExpenses;
            System.out.println("\n--- Weekly Budget Tracking ---");
            System.out.printf("Weekly Budget: %.2f\n", budget);
            System.out.printf("Total Expenses: %.2f\n", totalExpenses);
            System.out.printf("Remaining Budget: %.2f\n", remainingBudget);
            if (remainingBudget < 0) {
                System.out.println("Warning: You have exceeded your weekly budget!");
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while tracking weekly budget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void dailyBudgetMenu() {
        while (true) {
            System.out.println("\n--- Daily Budget Menu ---");
            System.out.println("1. Set Daily Budget\n2. View Daily Budget\n3. Edit Daily Budget\n" +
                               "4. Delete Daily Budget\n5. Track Daily Budget\n6. Back");
            int choice = getValidIntInput("choice");
            sc.nextLine();
            switch (choice) {
                case 1 -> setDailyBudget();
                case 2 -> viewDailyBudget();
                case 3 -> editDailyBudget();
                case 4 -> deleteDailyBudget();
                case 5 -> trackDailyBudget();
                case 6 -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter a number between 1 and 6.");
            }
        }
    }

    private static void setDailyBudget() {
        try {
            double budget = getValidDoubleInput("daily budget amount");
            sc.nextLine();
            java.sql.Date date = java.sql.Date.valueOf(java.time.LocalDate.now());

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO budgets(user_id, type, amount, date) VALUES(?, 'daily', ?, ?) " +
                            "ON DUPLICATE KEY UPDATE amount=?");
            ps.setInt(1, userId);
            ps.setDouble(2, budget);
            ps.setDate(3, date);
            ps.setDouble(4, budget);
            ps.executeUpdate();
            System.out.println("Daily budget set successfully.");
        } catch (SQLException e) {
            System.err.println("An error occurred while setting daily budget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void viewDailyBudget() {
        try {
            java.sql.Date date = java.sql.Date.valueOf(java.time.LocalDate.now());
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT amount FROM budgets WHERE user_id=? AND type='daily' AND date=?");
            ps.setInt(1, userId);
            ps.setDate(2, date);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Daily Budget ---");
            if (rs.next()) {
                System.out.printf("Your daily budget is: %.2f\n", rs.getDouble("amount"));
            } else {
                System.out.println("No daily budget set for today.");
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while viewing daily budget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void editDailyBudget() {
        try {
            java.sql.Date date = java.sql.Date.valueOf(java.time.LocalDate.now());
            PreparedStatement checkPs = conn.prepareStatement(
                    "SELECT amount FROM budgets WHERE user_id=? AND type='daily' AND date=?");
            checkPs.setInt(1, userId);
            checkPs.setDate(2, date);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next()) {
                System.out.printf("Current daily budget: %.2f\n", rs.getDouble("amount"));
            } else {
                System.out.println("No daily budget set for today.");
                return;
            }

            double newBudget = getValidDoubleInput("new daily budget amount");
            sc.nextLine();

            PreparedStatement updatePs = conn.prepareStatement(
                    "UPDATE budgets SET amount=? WHERE user_id=? AND type='daily' AND date=?");
            updatePs.setDouble(1, newBudget);
            updatePs.setInt(2, userId);
            updatePs.setDate(3, date);
            int updated = updatePs.executeUpdate();
            if (updated > 0) {
                System.out.println("Daily budget updated successfully.");
            } else {
                System.out.println("Failed to update daily budget.");
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while editing daily budget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void deleteDailyBudget() {
        try {
            java.sql.Date date = java.sql.Date.valueOf(java.time.LocalDate.now());
            System.out.print("Are you sure you want to delete the daily budget? (yes/no): ");
            String confirmation = sc.nextLine();
            if ("yes".equalsIgnoreCase(confirmation)) {
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM budgets WHERE user_id=? AND type='daily' AND date=?");
                ps.setInt(1, userId);
                ps.setDate(2, date);
                int deleted = ps.executeUpdate();
                if (deleted > 0) {
                    System.out.println("Daily budget deleted successfully.");
                } else {
                    System.out.println("No daily budget found for today.");
                }
            } else {
                System.out.println("Daily budget deletion cancelled.");
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while deleting daily budget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void trackDailyBudget() {
        try {
            java.sql.Date date = java.sql.Date.valueOf(java.time.LocalDate.now());
            PreparedStatement budgetPs = conn.prepareStatement(
                    "SELECT amount FROM budgets WHERE user_id=? AND type='daily' AND date=?");
            budgetPs.setInt(1, userId);
            budgetPs.setDate(2, date);
            ResultSet budgetRs = budgetPs.executeQuery();
            double budget = 0;
            if (budgetRs.next()) {
                budget = budgetRs.getDouble("amount");
            } else {
                System.out.println("\n--- Daily Budget Tracking --- \nNo daily budget set.");
                return;
            }

            PreparedStatement expensePs = conn.prepareStatement(
                    "SELECT SUM(amount) FROM expenses WHERE user_id=? AND date=?");
            expensePs.setInt(1, userId);
            expensePs.setDate(2, date);
            ResultSet expenseRs = expensePs.executeQuery();
            double totalExpenses = 0;
            if (expenseRs.next() && expenseRs.getDouble(1) != 0) {
                totalExpenses = expenseRs.getDouble(1);
            }

            double remainingBudget = budget - totalExpenses;
            System.out.println("\n--- Daily Budget Tracking ---");
            System.out.printf("Daily Budget: %.2f\n", budget);
            System.out.printf("Total Expenses: %.2f\n", totalExpenses);
            System.out.printf("Remaining Budget: %.2f\n", remainingBudget);
            if (remainingBudget < 0) {
                System.out.println("Warning: You have exceeded your daily budget!");
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while tracking daily budget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void searchExpenseMenu() {
        while (true) {
            System.out.println("\n--- Search Expense Menu ---");
            System.out.println("1. Search by Date\n2. Search by Amount\n3. Search by Category\n4. Back");
            int choice = getValidIntInput("choice");
            sc.nextLine();
            switch (choice) {
                case 1 -> searchByDate();
                case 2 -> searchByAmount();
                case 3 -> searchByCategory();
                case 4 -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter a number between 1 and 4.");
            }
        }
    }

    private static void searchByDate() {
        try {
            System.out.print("Enter start date (YYYY-MM-DD): ");
            String startDate = sc.nextLine();
            System.out.print("Enter end date (YYYY-MM-DD): ");
            String endDate = sc.nextLine();

            try {
                java.sql.Date.valueOf(startDate);
                java.sql.Date.valueOf(endDate);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
                return;
            }

            PreparedStatement ps = conn.prepareStatement("SELECT * FROM expenses WHERE user_id=? AND date BETWEEN ? AND ?");
            ps.setInt(1, userId);
            ps.setString(2, startDate);
            ps.setString(3, endDate);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Search Results ---");
            if (!rs.isBeforeFirst()) {
                System.out.println("No expenses found for the given date range.");
                return;
            }
            while (rs.next()) {
                System.out.printf("ID: %d | Category: %s | Amount: %.2f | Date: %s\n",
                        rs.getInt("id"), rs.getString("category"), rs.getDouble("amount"), rs.getDate("date"));
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while searching expenses by date: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void searchByAmount() {
        try {
            double minAmount = getValidDoubleInput("minimum amount");
            double maxAmount = getValidDoubleInput("maximum amount");
            sc.nextLine();

            if (minAmount > maxAmount) {
                System.out.println("Minimum amount cannot be greater than maximum amount.");
                return;
            }

            PreparedStatement ps = conn.prepareStatement("SELECT * FROM expenses WHERE user_id=? AND amount BETWEEN ? AND ?");
            ps.setInt(1, userId);
            ps.setDouble(2, minAmount);
            ps.setDouble(3, maxAmount);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Search Results ---");
            if (!rs.isBeforeFirst()) {
                System.out.println("No expenses found for the given amount range.");
                return;
            }
            while (rs.next()) {
                System.out.printf("ID: %d | Category: %s | Amount: %.2f | Date: %s\n",
                        rs.getInt("id"), rs.getString("category"), rs.getDouble("amount"), rs.getDate("date"));
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while searching expenses by amount: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void searchByCategory() {
        try {
            System.out.print("Enter category to search for: ");
            String category = sc.nextLine();
            if (category.trim().isEmpty()) {
                System.out.println("Category cannot be empty.");
                return;
            }

            PreparedStatement ps = conn.prepareStatement("SELECT * FROM expenses WHERE user_id=? AND category LIKE ?");
            ps.setInt(1, userId);
            ps.setString(2, "%" + category + "%");
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Search Results ---");
            if (!rs.isBeforeFirst()) {
                System.out.println("No expenses found for the given category.");
                return;
            }
            while (rs.next()) {
                System.out.printf("ID: %d | Category: %s | Amount: %.2f | Date: %s\n",
                        rs.getInt("id"), rs.getString("category"), rs.getDouble("amount"), rs.getDate("date"));
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while searching expenses by category: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void filterExpenseMenu() {
        while (true) {
            System.out.println("\n--- Filter Expense Menu ---");
            System.out.println("1. Filter by Date\n2. Filter by Amount\n3. Filter by Category\n4. Back");
            int choice = getValidIntInput("choice");
            sc.nextLine();
            switch (choice) {
                case 1 -> filterByDate();
                case 2 -> filterByAmount();
                case 3 -> filterByCategory();
                case 4 -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter a number between 1 and 4.");
            }
        }
    }

    private static void filterByDate() {
        try {
            System.out.print("Enter start date (YYYY-MM-DD): ");
            String startDate = sc.nextLine();
            System.out.print("Enter end date (YYYY-MM-DD): ");
            String endDate = sc.nextLine();

            try {
                java.sql.Date.valueOf(startDate);
                java.sql.Date.valueOf(endDate);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
                return;
            }

            PreparedStatement ps = conn.prepareStatement("SELECT * FROM expenses WHERE user_id=? AND date BETWEEN ? AND ?");
            ps.setInt(1, userId);
            ps.setString(2, startDate);
            ps.setString(3, endDate);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Filtered Expenses ---");
            if (!rs.isBeforeFirst()) {
                System.out.println("No expenses found for the given date range.");
                return;
            }
            while (rs.next()) {
                System.out.printf("ID: %d | Category: %s | Amount: %.2f | Date: %s\n",
                        rs.getInt("id"), rs.getString("category"), rs.getDouble("amount"), rs.getDate("date"));
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while filtering expenses by date: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void filterByAmount() {
        try {
            double minAmount = getValidDoubleInput("minimum amount");
            double maxAmount = getValidDoubleInput("maximum amount");
            sc.nextLine();

            if (minAmount > maxAmount) {
                System.out.println("Minimum amount cannot be greater than maximum amount.");
                return;
            }

            PreparedStatement ps = conn.prepareStatement("SELECT * FROM expenses WHERE user_id=? AND amount BETWEEN ? AND ?");
            ps.setInt(1, userId);
            ps.setDouble(2, minAmount);
            ps.setDouble(3, maxAmount);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Filtered Expenses ---");
            if (!rs.isBeforeFirst()) {
                System.out.println("No expenses found for the given amount range.");
                return;
            }
            while (rs.next()) {
                System.out.printf("ID: %d | Category: %s | Amount: %.2f | Date: %s\n",
                        rs.getInt("id"), rs.getString("category"), rs.getDouble("amount"), rs.getDate("date"));
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while filtering expenses by amount: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void filterByCategory() {
        try {
            System.out.print("Enter category to filter: ");
            String category = sc.nextLine();
            if (category.trim().isEmpty()) {
                System.out.println("Category cannot be empty.");
                return;
            }

            PreparedStatement ps = conn.prepareStatement("SELECT * FROM expenses WHERE user_id=? AND category LIKE ?");
            ps.setInt(1, userId);
            ps.setString(2, "%" + category + "%");
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Filtered Expenses ---");
            if (!rs.isBeforeFirst()) {
                System.out.println("No expenses found for the given category.");
                return;
            }
            while (rs.next()) {
                System.out.printf("ID: %d | Category: %s | Amount: %.2f | Date: %s\n",
                        rs.getInt("id"), rs.getString("category"), rs.getDouble("amount"), rs.getDate("date"));
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while filtering expenses by category: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void changePassword() {
        try {
            System.out.print("Enter new password: ");
            String newPassword = sc.nextLine();
            if (newPassword.trim().isEmpty()) {
                System.out.println("Password cannot be empty.");
                return;
            }

            PreparedStatement ps = conn.prepareStatement("UPDATE users SET password=? WHERE id=?");
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
            System.out.println("Password changed successfully.");
        } catch (SQLException e) {
            System.err.println("An error occurred while changing password: " + e.getMessage());
            e.printStackTrace();
        }
    }
}