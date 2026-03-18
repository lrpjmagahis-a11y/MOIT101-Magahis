package motorph;

import java.io.*;
import java.util.*;

public class PayrollSystem {
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
        System.out.println("        MOTORPH SELF-SERVICE PORTAL v5.0       ");
        System.out.println("        Authorized Personnel Access Only       ");
        System.out.println("==============================================\n");
    }

    public static void loadEmployeeData() {
        try (BufferedReader br = new BufferedReader(new FileReader("EmployeeDetails.csv"))) {
            String line;
            br.readLine(); // Skip CSV Header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // Clean whitespace from ID
                employeeMap.put(data[0].trim(), data);
            }
        } catch (IOException e) {
            System.out.println("[!] Critical Error: EmployeeDetails.csv not found.");
        }
    }

    public static boolean authenticate(String id, String pin) {
        if (employeeMap.containsKey(id)) {
            String[] emp = employeeMap.get(id);
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
        System.out.print("\nEnter Month (e.g., 03 for March): ");
        String month = sc.nextLine();
        // Period selection is kept for UI, but the logic now scans the file
        System.out.println("Select Cutoff Period:");
        System.out.println("[1] 1st - 15th");
        System.out.println("[2] 16th - End of Month");
        String period = sc.nextLine();

        calculateAndDisplay(emp, month, period);
    }

    public static void calculateAndDisplay(String[] emp, String month, String period) {
        double hourlyRate = Double.parseDouble(emp[18].trim());
        double totalHours = 0;

        // NEW: Real-time calculation from AttendanceRecords.csv
        try (BufferedReader br = new BufferedReader(new FileReader("AttendanceRecords.csv"))) {
            String line;
            br.readLine(); // Skip Header
            while ((line = br.readLine()) != null) {
                String[] att = line.split(",");
                String empId = att[0].trim();
                String date = att[1].trim(); // Assuming format MM/DD/YYYY
                
                if (empId.equals(loggedInID) && date.startsWith(month)) {
                    totalHours += calculateHoursWorked(att[2].trim(), att[3].trim());
                }
            }
        } catch (IOException e) {
            System.out.println("[!] Error reading Attendance file. Using demo hours.");
            totalHours = 80.0;
        }

        double gross = hourlyRate * totalHours;
        double sss = gross * 0.045;
        double philHealth = gross * 0.025;
        double pagIbig = 100.00; // Simplified for demo
        double tax = (gross > 15000) ? (gross * 0.10) : 0;
        double netPay = gross - (sss + philHealth + pagIbig + tax);

        System.out.println("\n===========================================");
        System.out.println("        PAYSLIP: MONTH " + month + " (Period " + period + ")");
        System.out.println("===========================================");
        System.out.println("TOTAL HOURS:        " + String.format("%.2f", totalHours));
        System.out.println("HOURLY RATE:        P " + String.format("%.2f", hourlyRate));
        System.out.println("GROSS SALARY:       P " + String.format("%.2f", gross));
        System.out.println("-------------------------------------------");
        System.out.println("DEDUCTIONS:");
        System.out.println(" - SSS:             P " + String.format("%.2f", sss));
        System.out.println(" - PhilHealth:      P " + String.format("%.2f", philHealth));
        System.out.println(" - Pag-IBIG:        P " + String.format("%.2f", pagIbig));
        System.out.println(" - Tax:             P " + String.format("%.2f", tax));
        System.out.println("-------------------------------------------");
        System.out.println("NET PAY:            P " + String.format("%.2f", netPay));
        System.out.println("===========================================");
    }

    // Helper to calculate hours between two 24-hour strings (e.g., "08:00", "17:00")
    private static double calculateHoursWorked(String timeIn, String timeOut) {
        try {
            String[] inParts = timeIn.split(":");
            String[] outParts = timeOut.split(":");
            
            double inTime = Double.parseDouble(inParts[0]) + Double.parseDouble(inParts[1])/60;
            double outTime = Double.parseDouble(outParts[0]) + Double.parseDouble(outParts[1])/60;
            
            double total = outTime - inTime;
            return (total > 5) ? total - 1 : total; // Subtract 1hr lunch break
        } catch (Exception e) {
            return 0;
        }
    }
}
