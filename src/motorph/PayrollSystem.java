package motorph;

import java.io.*;
import java.util.*;

public class PayrollSystem {
    static Map<String, String[]> employeeMap = new HashMap<>();
    static String loggedInID = "";

    public static void main(String[] args) {
        loadEmployeeData();
        
        Scanner sc = new Scanner(System.in);
        System.out.println("==============================================");
        System.out.println("        MOTORPH SELF-SERVICE PORTAL v5.0       ");
        System.out.println("==============================================\n");

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
        // Standard NetBeans looks in the project root (above src)
        try (BufferedReader br = new BufferedReader(new FileReader("EmployeeDetails.csv"))) {
            String line;
            br.readLine(); 
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                employeeMap.put(data[0].trim(), data);
            }
            System.out.println("✅ Loaded " + employeeMap.size() + " employees.");
        } catch (Exception e) {
            System.out.println("[!] ERROR: EmployeeDetails.csv not found in project root.");
        }
    }

    public static boolean authenticate(String id, String pin) {
        if (!employeeMap.containsKey(id)) return false;
        String[] emp = employeeMap.get(id);
        // This checks if the PIN you typed matches the PIN in the CSV (Column 20)
        return emp[19].trim().equals(pin);
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
                System.out.println("\n--- PROFILE ---");
                System.out.println("ID: " + emp[0]);
                System.out.println("Name: " + emp[2] + " " + emp[1]);
                System.out.println("Birthday: " + emp[3]);
                System.out.println("Position: " + emp[11]);
            } else if (choice.equals("2")) {
                calculatePayslip(emp);
            } else if (choice.equals("3")) {
                break;
            }
        }
    }

    public static void calculatePayslip(String[] emp) {
        Scanner sc = new Scanner(System.in);
        System.out.print("\nEnter Month (e.g., 03): ");
        String month = sc.nextLine();
        
        double hourlyRate = Double.parseDouble(emp[18].trim());
        double totalHours = 0;
        double totalLateMins = 0;

        try (BufferedReader br = new BufferedReader(new FileReader("AttendanceRecords.csv"))) {
            String line;
            br.readLine(); 
            while ((line = br.readLine()) != null) {
                String[] att = line.split(",");
                if (att[0].trim().equals(loggedInID) && att[1].trim().startsWith(month)) {
                    String[] in = att[2].trim().split(":");
                    String[] out = att[3].trim().split(":");
                    double hIn = Double.parseDouble(in[0]);
                    double mIn = Double.parseDouble(in[1]);
                    double hOut = Double.parseDouble(out[0]);
                    double mOut = Double.parseDouble(out[1]);

                    if (hIn > 8 || (hIn == 8 && mIn > 0)) {
                        totalLateMins += ((hIn - 8) * 60) + mIn;
                    }

                    double dayHrs = (hOut + mOut/60) - (hIn + mIn/60);
                    totalHours += (dayHrs > 5) ? dayHrs - 1 : dayHrs;
                }
            }
        } catch (Exception e) { System.out.println("[!] Attendance Records not found."); }

        double lateDeduction = (hourlyRate / 60) * totalLateMins;
        double gross = (hourlyRate * totalHours) - lateDeduction;

        System.out.println("\n--- MARCH PAYSLIP ---");
        System.out.println("Total Hours: " + String.format("%.2f", totalHours));
        System.out.println("Total Late:  " + (int)totalLateMins + " mins");
        System.out.println("GROSS PAY:   P " + String.format("%.2f", gross));
    }
}
