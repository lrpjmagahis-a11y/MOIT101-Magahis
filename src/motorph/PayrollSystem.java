package motorph;

import java.io.*;
import java.util.*;

public class PayrollSystem {
    // Capacity for 1,500+ employees using a fast-lookup Map
    static Map<String, String[]> employeeMap = new HashMap<>();
    static Scanner sc = new Scanner(System.in);
    static String loggedInID = "";

    public static void main(String[] args) {
        loadEmployeeData();
        displaySplash();

        System.out.println("--- 🔐 EMPLOYEE LOGIN ---");
        System.out.print("👤 Employee ID: ");
        String id = sc.nextLine();
        System.out.print("🔑 5-Digit PIN: ");
        String pin = sc.nextLine();

        if (authenticate(id, pin)) {
            loggedInID = id;
            employeeDashboard();
        } else {
            System.out.println("\n[!] Access Denied: Incorrect ID or PIN.");
        }
    }

    private static void displaySplash() {
        System.out.println("==============================================");
        System.out.println("       MOTORPH SELF-SERVICE PORTAL v5.0       ");
        System.out.println("       Authorized Personnel Access Only       ");
        System.out.println("==============================================\n");
    }

    // High-speed Data Loading
    public static void loadEmployeeData() {
        try (BufferedReader br = new BufferedReader(new FileReader("EmployeeDetails.csv"))) {
            String line;
            br.readLine(); // Skip CSV Header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // We use EmployeeID (Index 0) as the Key
                employeeMap.put(data[0], data);
            }
        } catch (IOException e) {
            System.out.println("[!] Critical Error: EmployeeDetails.csv not found.");
        }
    }

    public static boolean authenticate(String id, String pin) {
        if (employeeMap.containsKey(id)) {
            String[] emp = employeeMap.get(id);
            // Assuming PIN is in the last column (Index 19)
            return emp[19].trim().equals(pin);
        }
        return false;
    }

    public static void employeeDashboard() {
        String[] emp = employeeMap.get(loggedInID);
        while (true) {
            System.out.println("\nWelcome, " + emp[2] + " " + emp[1] + " [" + emp[11] + "]");
            System.out.println("----------------------------------------------");
            System.out.println("[1] View My Profile");
            System.out.println("[2] Request Payslip (Calculate Payroll)");
            System.out.println("[3] Logout");
            System.out.print("Selection: ");
            String choice = sc.nextLine();

            if (choice.equals("1")) viewProfile(emp);
            else if (choice.equals("2")) requestPayslip(emp);
            else break;
        }
    }

    public static void viewProfile(String[] emp) {
        System.out.println("\n--- 👤 MY PROFILE ---");
        System.out.println("ID: " + emp[0]);
        System.out.println("Name: " + emp[2] + " " + emp[1]);
        System.out.println("Birthday: " + emp[3]);
        System.out.println("Address: " + emp[4]);
        System.out.println("SSS: " + emp[6] + " | PhilHealth: " + emp[7]);
        System.out.println("TIN: " + emp[8] + " | Pag-IBIG: " + emp[9]);
        System.out.println("----------------------");
    }

    public static void requestPayslip(String[] emp) {
        System.out.print("\nEnter Month (e.g., March): ");
        String month = sc.nextLine();
        System.out.println("Select Cutoff Period:");
        System.out.println("[1] 1st - 15th");
        System.out.println("[2] 16th - End of Month");
        String period = sc.nextLine();

        calculateAndDisplay(emp, month, period);
    }

    public static void calculateAndDisplay(String[] emp, String month, String period) {
        double hourlyRate = Double.parseDouble(emp[18]);
        // Logic: Scan AttendanceRecords.csv for this ID and Period
        // For this demo, we simulate the hour parsing logic
        double hoursWorked = (period.equals("1")) ? 80.0 : 88.0; 
        
        double gross = hourlyRate * hoursWorked;
        double sss = gross * 0.045;
        double philHealth = gross * 0.025;
        double pagIbig = 200.00;
        double tax = (gross > 15000) ? (gross * 0.12) : 0;
        double netPay = gross - (sss + philHealth + pagIbig + tax);

        System.out.println("\n===========================================");
        System.out.println("        PAYSLIP: " + month.toUpperCase() + " (Period " + period + ")");
        System.out.println("===========================================");
        System.out.println("GROSS SALARY:     P " + String.format("%.2f", gross));
        System.out.println("DEDUCTIONS:");
        System.out.println(" - SSS (4.5%):    P " + String.format("%.2f", sss));
        System.out.println(" - PhilHealth:    P " + String.format("%.2f", philHealth));
        System.out.println(" - Pag-IBIG:      P " + String.format("%.2f", pagIbig));
        System.out.println(" - Tax:           P " + String.format("%.2f", tax));
        System.out.println("-------------------------------------------");
        System.out.println("NET PAY:          P " + String.format("%.2f", netPay));
        System.out.println("===========================================");
    }
}
