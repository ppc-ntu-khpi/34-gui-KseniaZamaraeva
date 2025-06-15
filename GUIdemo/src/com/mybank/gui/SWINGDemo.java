package com.mybank.gui;

import com.mybank.domain.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

public class SWINGDemo {

    private final JEditorPane log;
    private final JButton show;
    private final JButton report;
    private final JComboBox<String> clients;

    public SWINGDemo() {
        log = new JEditorPane("text/html", "");
        log.setPreferredSize(new Dimension(400, 250));
        log.setEditable(false);
        show = new JButton("Show");
        report = new JButton("Report");
        clients = new JComboBox<>();

        // Заповнення комбо-боксу іменами клієнтів
        for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
            Customer c = Bank.getCustomer(i);
            clients.addItem(c.getLastName() + ", " + c.getFirstName());
        }
    }

    private void launchFrame() {
        JFrame frame = new JFrame("MyBank clients");
        frame.setLayout(new BorderLayout());

        JPanel cpane = new JPanel(new GridLayout(1, 3));
        cpane.add(clients);
        cpane.add(show);
        cpane.add(report);

        frame.add(cpane, BorderLayout.NORTH);
        frame.add(log, BorderLayout.CENTER);

        show.addActionListener(e -> {
            int index = clients.getSelectedIndex();
            if (index < 0) {
                JOptionPane.showMessageDialog(null, "Please select a client.");
                return;
            }

            Customer customer = Bank.getCustomer(index);
            StringBuilder report = new StringBuilder();
            report.append("<html><body>");
            report.append("<h2>").append(customer.getLastName()).append(", ").append(customer.getFirstName()).append("</h2>");
            report.append("<hr>");

            for (int i = 0; i < customer.getNumberOfAccounts(); i++) {
                Account acc = customer.getAccount(i);
                report.append("<b>Account Type:</b> ")
                      .append((acc instanceof CheckingAccount) ? "Checking" : "Savings")
                      .append("<br><b>Balance:</b> <span style='color:red;'>$")
                      .append(String.format("%.2f", acc.getBalance()))
                      .append("</span><br><br>");
            }

            report.append("</body></html>");
            log.setText(report.toString());
        });

        report.addActionListener(e -> {
            StringBuilder reportText = new StringBuilder();
            reportText.append("<html><body>");
            reportText.append("<h2>Customer Report</h2>");
            reportText.append("<hr>");

            for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
                Customer customer = Bank.getCustomer(i);
                reportText.append("<b>")
                          .append(customer.getLastName())
                          .append(", ")
                          .append(customer.getFirstName())
                          .append("</b><br>");

                for (int j = 0; j < customer.getNumberOfAccounts(); j++) {
                    Account acc = customer.getAccount(j);
                    reportText.append("&nbsp;&nbsp;&nbsp;&nbsp;Account Type: ")
                              .append((acc instanceof CheckingAccount) ? "Checking" : "Savings")
                              .append(", Balance: <span style='color:green;'>$")
                              .append(String.format("%.2f", acc.getBalance()))
                              .append("</span><br>");
                }

                reportText.append("<br>");
            }

            reportText.append("</body></html>");
            log.setText(reportText.toString());
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        loadCustomers("C:\\Users\\aleks\\OneDrive\\Desktop\\GUIdemo\\src\\data\\test.dat");
        SWINGDemo demo = new SWINGDemo();
        demo.launchFrame();
    }

    private static void loadCustomers(String filepath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;

            // Пропустити порожні рядки на початку
            do {
                line = reader.readLine();
            } while (line != null && line.trim().isEmpty());

            int numCustomers = Integer.parseInt(line.trim());

            for (int i = 0; i < numCustomers; i++) {
                do {
                    line = reader.readLine();
                } while (line != null && line.trim().isEmpty());

                if (line == null) break;

                String[] customerData = line.trim().split("\\s+");
                if (customerData.length < 3) {
                    System.out.println("Неправильний формат клієнта: " + line);
                    continue;
                }

                String firstName = customerData[0];
                String lastName = customerData[1];
                int numAccounts = Integer.parseInt(customerData[2]);

                Bank.addCustomer(firstName, lastName);
                Customer customer = Bank.getCustomer(Bank.getNumberOfCustomers() - 1);

                for (int j = 0; j < numAccounts; j++) {
                    do {
                        line = reader.readLine();
                    } while (line != null && line.trim().isEmpty());

                    if (line == null) break;

                    String[] accData = line.trim().split("\\s+");
                    if (accData.length < 3) {
                        System.out.println("Неправильний формат рахунку: " + line);
                        continue;
                    }

                    String type = accData[0];

                    try {
                        if (type.equals("S")) {
                            double balance = Double.parseDouble(accData[1]);
                            double interest = Double.parseDouble(accData[2]);
                            customer.addAccount(new SavingsAccount(balance, interest));
                        } else if (type.equals("C")) {
                            double balance = Double.parseDouble(accData[1]);
                            double overdraft = Double.parseDouble(accData[2]);
                            customer.addAccount(new CheckingAccount(balance, overdraft));
                        } else {
                            System.out.println("Невідомий тип рахунку: " + type);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Помилка при розборі числа: " + line);
                    }
                }
            }

            System.out.println("Customers loaded: " + Bank.getNumberOfCustomers());

        } catch (IOException | NumberFormatException ex) {
            System.out.println("Помилка завантаження клієнтів: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
