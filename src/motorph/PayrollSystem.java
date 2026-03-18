package motorph;

import java.io.*;
import java.util.*;

public class PayrollSystem {
    static Map<String, String[]> employeeMap = new HashMap<>();
    static String loggedInID = "";

    public static void main(String[] args) {
        // DEBUG: This tells us EXACTLY where to put the CSV
        System.out.println("DEBUG: Put your CSV files in: " + System.getProperty("user.dir"));
        
        loadEmployeeData();
        
        Scanner sc = new Scanner(System.in);
        System.out.println("\n--- 🔐 MOTORPH LOGIN ---");
        System.out.print("👤 Employee ID: ");
        String id = sc.nextLine();
        System.out.print("🔑 PIN: ");
        String pin = sc.nextLine();

        if (authenticate(id, pin)) {
            loggedInID = id;
            System.out.println("\n✅ Login Successful!");
            // Add your dashboard call here
        } else {
            System.out.println("\n❌ Access Denied: Incorrect ID or PIN.");
            System.out.println("Tip: Check if EmployeeDetails.csv is in the right folder.");
        }
    }

    public static void loadEmployeeData() {
        // Try multiple common paths just in case
        String[] paths = {"EmployeeDetails.csv", "src/EmployeeDetails.csv", "../EmployeeDetails.csv"};
        File file = null;
        
        for (String p : paths) {
            file = new File(p);
            if (file.exists()) break;
        }

        if (file == null || !file.exists()) {
            System.out.println("[!] ERROR: EmployeeDetails.csv not found anywhere.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                // Use a regex to split by comma but ignore commas inside quotes
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length > 0) {
                    employeeMap.put(data[0].trim(), data);
                }
            }
            System.out.println("✅ Loaded " + employeeMap.size() + " employees.");
        } catch (Exception e) {
            System.out.println("[!] Error reading file: " + e.getMessage());
        }
    }

    public static boolean authenticate(String id, String pin) {
        if (!employeeMap.containsKey(id)) return false;
        String[] emp = employeeMap.get(id);
        
        // Let's search all columns for the PIN to be safe
        for (String column : emp) {
            if (column.trim().equals(pin)) return true;
        }
        return false;
    }
}
