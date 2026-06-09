package HospitalManagementSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Billing {
    private Connection connection;
    private Scanner scanner;

    public Billing(Connection connection, Scanner scanner){
        this.connection = connection;
        this.scanner = scanner;
    }

    public void generateBill(){
        System.out.print("Enter Patient Id: ");
        int patientId = scanner.nextInt();
        if(!new Patient(connection, scanner).getPatientById(patientId)){
            System.out.println("Patient not found.");
            return;
        }
        scanner.nextLine();
        List<BillItem> items = new ArrayList<>();
        while(true){
            System.out.print("Enter service/item name (or 'done' to finish): ");
            String item = scanner.nextLine();
            if(item.equalsIgnoreCase("done")){
                break;
            }
            System.out.print("Enter cost for " + item + ": ");
            double cost = scanner.nextDouble();
            scanner.nextLine();
            items.add(new BillItem(item, cost));
        }
        if(items.isEmpty()){
            System.out.println("No bill items provided.");
            return;
        }
        if(generateBill(patientId, items)){
            System.out.println("Bill generated successfully.");
        } else {
            System.out.println("Failed to generate bill.");
        }
    }

    public boolean generateBill(int patientId, List<BillItem> items){
        String billQuery = "INSERT INTO bills(patient_id, bill_date, total_amount) VALUES(?, ?, ?)";
        try(PreparedStatement billStatement = connection.prepareStatement(billQuery, PreparedStatement.RETURN_GENERATED_KEYS)){
            double total = items.stream().mapToDouble(BillItem::getItemCost).sum();
            billStatement.setInt(1, patientId);
            billStatement.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            billStatement.setDouble(3, total);
            int affectedRows = billStatement.executeUpdate();
            if(affectedRows == 0){
                return false;
            }
            try(ResultSet generatedKeys = billStatement.getGeneratedKeys()){
                if(generatedKeys.next()){
                    int billId = generatedKeys.getInt(1);
                    return saveBillItems(billId, items);
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    private boolean saveBillItems(int billId, List<BillItem> items){
        String itemQuery = "INSERT INTO bill_items(bill_id, item_name, item_cost) VALUES(?, ?, ?)";
        try(PreparedStatement itemStatement = connection.prepareStatement(itemQuery)){
            for(BillItem item : items){
                itemStatement.setInt(1, billId);
                itemStatement.setString(2, item.getItemName());
                itemStatement.setDouble(3, item.getItemCost());
                itemStatement.addBatch();
            }
            int[] rows = itemStatement.executeBatch();
            return rows.length == items.size();
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public void viewBills(){
        System.out.println(getBillListString());
    }

    public String getBillListString(){
        StringBuilder output = new StringBuilder();
        String billQuery = "SELECT b.id, b.patient_id, b.bill_date, b.total_amount, p.name FROM bills b JOIN patients p ON b.patient_id = p.id ORDER BY b.bill_date DESC";
        try(PreparedStatement billStatement = connection.prepareStatement(billQuery);
            ResultSet resultSet = billStatement.executeQuery()){
            while(resultSet.next()){
                int billId = resultSet.getInt("id");
                output.append("Bill ID: ").append(billId).append("\n");
                output.append("Patient ID: ").append(resultSet.getInt("patient_id")).append("\n");
                output.append("Patient Name: ").append(resultSet.getString("name")).append("\n");
                output.append("Date: ").append(resultSet.getDate("bill_date")).append("\n");
                output.append("Total Amount: Rs. ").append(resultSet.getDouble("total_amount")).append("\n");
                output.append("Items:\n");
                output.append(getBillItemsForBill(billId));
                output.append("------------------------------------------------------------\n");
            }
        }catch(SQLException e){
            e.printStackTrace();
            return "Failed to load bills.";
        }
        return output.length() == 0 ? "No bills available." : output.toString();
    }

    private String getBillItemsForBill(int billId){
        StringBuilder output = new StringBuilder();
        String itemQuery = "SELECT item_name, item_cost FROM bill_items WHERE bill_id = ?";
        try(PreparedStatement itemStatement = connection.prepareStatement(itemQuery)){
            itemStatement.setInt(1, billId);
            try(ResultSet resultSet = itemStatement.executeQuery()){
                while(resultSet.next()){
                    output.append("  - ").append(resultSet.getString("item_name"))
                        .append(": Rs. ").append(resultSet.getDouble("item_cost")).append("\n");
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
            return "  Error loading bill items.\n";
        }
        return output.toString();
    }
}
