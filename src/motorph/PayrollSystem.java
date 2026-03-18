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
        System.out.println("        MOTORPH SELF-SERVICE PORTAL v5.0       ");
        System.out.println("        Authorized Personnel Access Only       ");
        System.out.println("==============================================\n");
    }

    // High-speed Data Loading from EmployeeDetails.csv
    public static void loadEmployeeData() {
        try (BufferedReader br = new BufferedReader(new FileReader("EmployeeDetails.csv"))) {
            String line;
            br.readLine(); // Skip CSV Header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // Use EmployeeID (Index 0) as the Key, trim to remove spaces
                employeeMap.put(data[0].trim(), data);
            }
        } catch (IOException e) {
            System.out.println("[!] Critical Error: EmployeeDetails.csv not found.");
        }
    }

    public static boolean authenticate(String id, String pin) {
        if (employeeMap.containsKey(id)) {
            String[] emp = employeeMap.get(id);
            // PIN is in column index 19
            return emp[19].trim().equals(pin);
        }
        return false;
    }

    public static void employeeDashboard() {
        String[] emp = employeeMap.get(loggedInID);
        while (true) {
            System.out.println("\nWelcome, " + emp[2] + " " + emp[1]);
            System.out.println("----------------------------------------------");
            System.out.println("[1] View My Profile");
            System.out.println("[2] Request Payslip (Calculate Attendance)");
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
        System.out.println("SSS: " + emp[6] + " | PhilHealth: " + emp[7]);
        System.out.println("TIN: " + emp[8] + " | Pag-IBIG: " + emp[9]);
        System.out.println("----------------------");
    }

    public static void requestPayslip(String[] emp) {
        System.out.print("\nEnter Month (e.g., 03 for March): ");
        String month = sc.nextLine();
        
        double hourlyRate = Double.parseDouble(emp[18].trim());
        double totalHours = 0;
        double totalLateMinutes = 0;

        System.out.println("[...] Scanning AttendanceRecords.csv...");

        try (BufferedReader br = new BufferedReader(new FileReader("AttendanceRecords.csv"))) {
            String line;
            br.readLine(); // Skip Header
            while ((line = br.readLine()) != null) {
                String[] att = line.split(",");
                String empId = att[0].trim();
                String date = att[1].trim(); 
                
                // Filter by LoggedIn User and Month
                if (empId.equals(loggedInID) && date.startsWith(month)) {
                    double[] dailyResult = calculateDailyStats(att[2].trim(), att[3].trim());
                    totalHours += dailyResult[0];
                    totalLateMinutes += dailyResult[1];
                }
            }
        } catch (IOException e) {
            System.out.println("[!] Error: Could not read AttendanceRecords.csv");
            return;
        }

        displayPayslip(totalHours, totalLateMinutes, hourlyRate);
    }

    // Helper to calculate hours worked and tardiness
    private static double[] calculateDailyStats(String timeIn, String timeOut) {
        try {
            String[] inParts = timeIn.split(":");
            String[] outParts = timeOut.split(":");
            
            double inH = Double.parseDouble(inParts[0]);
            double inM = Double.parseDouble(inParts[1]);
            double outH = Double.parseDouble(outParts[0]);
            double outM = Double.parseDouble(outParts[1]);

            // Late calculation (Standard start is 08:00)
            double late = 0;
            if (inH > 8 || (inH == 8 && inM > 0)) {
                late = ((inH - 8) * 60) + inM;
            }

            // Hour calculation
            double start = inH + (inM / 60);
            double end = outH + (outM / 60);
            double total = end - start;
            double netHours = (total > 5) ? total - 1 : total; // Subtract lunch

            return new double[]{netHours, late};
        } catch (Exception e) {
            return new double[]{0, 0};
        }
    }

    public static void displayPayslip(double hours, double lates, double rate) {
        double lateDeduction = (rate / 60) * lates;
        double gross = (rate * hours) - lateDeduction;
        double sss = gross * 0.045;
        double tax = (gross > 15000) ? (gross * 0.10) : 0;
        double netPay = gross - sss - tax - 100.00; // 100 is Pag-IBIG

        System.out.println("\n===========================================");
        System.out.println("            OFFICIAL PAYSLIP               ");
        System.out.println("===========================================");
        System.out.println("Total Hours Worked: " + String.format("%.2f", hours));
        System.out.println("Total Late (Mins):  " + (int)lates);
        System.out.println("Late Deduction:    -P " + String.format("%.2f", lateDeduction));
        System.out.println("-------------------------------------------");
        System.out.println("GROSS SALARY:       P " + String.format("%.2f", gross));
        System.out.println("NET PAY:            P " + String.format("%.2f", netPay));
        System.out.println("===========================================");
    }
}
