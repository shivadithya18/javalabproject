package HospitalManagementSystem;

public class BillItem {
    private String itemName;
    private double itemCost;

    public BillItem(String itemName, double itemCost) {
        this.itemName = itemName;
        this.itemCost = itemCost;
    }

    public String getItemName() {
        return itemName;
    }

    public double getItemCost() {
        return itemCost;
    }
}
