package motorph;

import java.io.*;
import java.util.*;

public class PayrollSystem {
    static Map<String, String[]> employeeMap = new HashMap<>();
    static String loggedInID = "";

    public static void main(String[] args) {
        // This helps us find the files in your NetBeans folder
        loadEmployeeData();
        
        Scanner sc = new Scanner(System.in);
        System.out.println("==============================================");
        System.out.println("        MOTORPH SELF-SERVICE PORTAL v5.0       ");
        System.out.println("==============================================\n");

        System.out.println("--- 🔐 LOGIN ---");
        System.out.print("👤 Employee ID: ");
        String id = sc.nextLine();
        System.out.print("🔑 PIN: ");
        String pin = sc.nextLine();

        if (authenticate(id, pin)) {
            loggedInID = id;
            System.out.println("\n✅ Login Successful!");
            showDashboard();
        } else {
            System.out.println("\n❌ Access Denied: Incorrect ID or PIN.");
        }
    }

    public static void loadEmployeeData() {
        // It checks the root and the JavaProject9 folder automatically
        String[] paths = {"EmployeeDetails.csv", "JavaProject9/EmployeeDetails.csv"};
        File file = null;
        for (String p : paths) {
            file = new File(p);
            if (file.exists()) break;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); 
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                employeeMap.put(data[0].trim(), data);
            }
        } catch (Exception e) {
            System.out.println("[!] Could not load Employee CSV.");
        }
    }

    public static boolean authenticate(String id, String pin) {
        if (!employeeMap.containsKey(id)) return false;
        String[] emp = employeeMap.get(id);
        for (String col : emp) { if (col.trim().equals(pin)) return true; }
        return false;
    }

    public static void showDashboard() {
        Scanner sc = new Scanner(System.in);
        String[] emp = employeeMap.get(loggedInID);
        
        while (true) {
            System.out.println("\nWELCOME, " + emp[2].toUpperCase() + " " + emp[1].toUpperCase());
            System.out.println("[1] View Profile  [2] Calculate Payslip  [3] Logout");
            System.out.print("Selection: ");
            String choice = sc.nextLine();

            if (choice.equals("1")) {
                System.out.println("\nID: " + emp[0] + "\nPosition: " + emp[11] + "\nStatus: " + emp[10]);
            } else if (choice.equals("2")) {
                calculatePayslip(emp);
            } else if (choice.equals("3")) {
                break;
            }
        }
    }

    public static void calculatePayslip(String[] emp) {
        Scanner sc = new Scanner(System.in);
        System.out.print("\nEnter Month (01-12): ");
        String month = sc.nextLine();
        
        double hourlyRate = Double.parseDouble(emp[18].trim());
        double totalHours = 0;
        double totalLateMins = 0;

        try (BufferedReader br = new BufferedReader(new FileReader("AttendanceRecords.csv"))) {
            String line;
            br.readLine(); 
            while ((line = br.readLine()) != null) {
                String[] att = line.split(",");
                // Filter by ID and Month
                if (att[0].trim().equals(loggedInID) && att[1].trim().startsWith(month)) {
                    String[] in = att[2].trim().split(":");
                    String[] out = att[3].trim().split(":");
                    double hIn = Double.parseDouble(in[0]);
                    double mIn = Double.parseDouble(in[1]);
                    double hOut = Double.parseDouble(out[0]);
                    double mOut = Double.parseDouble(out[1]);

                    // Late logic (past 08:00)
                    if (hIn > 8 || (hIn == 8 && mIn > 0)) {
                        totalLateMins += ((hIn - 8) * 60) + mIn;
                    }

                    double dayHrs = (hOut + mOut/60) - (hIn + mIn/60);
                    totalHours += (dayHrs > 5) ? dayHrs - 1 : dayHrs; // Lunch
                }
            }
        } catch (Exception e) { System.out.println("[!] Attendance error."); }

        double lateDeduction = (hourlyRate / 60) * totalLateMins;
        double gross = (hourlyRate * totalHours) - lateDeduction;

        System.out.println("\n--- 💵 RESULTS FOR MONTH " + month + " ---");
        System.out.println("Hours Worked: " + String.format("%.2f", totalHours));
        System.out.println("Late Minutes: " + (int)totalLateMins);
        System.out.println("GROSS PAY:    P " + String.format("%.2f", gross));
        System.out.println("--------------------------------");
    }
}
