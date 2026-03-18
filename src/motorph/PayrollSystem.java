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
        System.out.println("        MOTORPH PAYROLL SYSTEM v8.0          ");
        System.out.println("==============================================\n");

        System.out.print("👤 Employee ID: ");
        String id = sc.nextLine().trim();
        System.out.print("🔑 PIN: ");
        String pin = sc.nextLine().trim();

        if (authenticate(id, pin)) {
            loggedInID = id;
            System.out.println("\n✅ Login Successful!");
            showDashboard();
        } else {
            System.out.println("\n❌ Access Denied: Incorrect ID or PIN.");
        }
    }

    public static void loadEmployeeData() {
        // Loads 1,500 employees into memory for instant access
        try (BufferedReader br = new BufferedReader(new FileReader("EmployeeDetails.csv"))) {
            String line;
            br.readLine(); // Skip Header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length >= 20) {
                    employeeMap.put(data[0].trim(), data);
                }
            }
            System.out.println("📊 System Ready: " + employeeMap.size() + " Employees Loaded.");
        } catch (Exception e) {
            System.out.println("[!] ERROR: EmployeeDetails.csv not found.");
        }
    }

    public static boolean authenticate(String id, String pin) {
        if (!employeeMap.containsKey(id)) return false;
        return employeeMap.get(id)[19].trim().equals(pin);
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
                System.out.println("ID: " + emp[0] + " | Position: " + emp[11]);
                System.out.println("SSS: " + emp[6] + " | TIN: " + emp[8]);
            } else if (choice.equals("2")) {
                calculatePayslip(emp);
            } else if (choice.equals("3")) break;
        }
    }

    public static void calculatePayslip(String[] emp) {
        Scanner sc = new Scanner(System.in);
        System.out.print("\nEnter Month (1-12): ");
        int userMonth = Integer.parseInt(sc.nextLine().trim());
        
        System.out.print("Select Cutoff [1] 1-15  [2] 16-31: ");
        String cutoff = sc.nextLine().trim();
        
        double hourlyRate = Double.parseDouble(emp[18].trim());
        double totalHours = 0, totalLateMins = 0;
        int count = 0;

        // Path check: Looks for AttendanceRecords.csv in project root
        try (BufferedReader br = new BufferedReader(new FileReader("AttendanceRecords.csv"))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] att = line.split(",");
                
                // Match Employee ID
                if (att[0].trim().equals(emp[0])) {
                    String[] dateParts = att[1].trim().split("/");
                    int csvMonth = Integer.parseInt(dateParts[0]);
                    int csvDay = Integer.parseInt(dateParts[1]);

                    // Match Month and Cutoff
                    if (csvMonth == userMonth) {
                        boolean inCutoff = (cutoff.equals("1") && csvDay <= 15) || (cutoff.equals("2") && csvDay > 15);
                        
                        if (inCutoff) {
                            String[] in = att[2].trim().split(":");
                            String[] out = att[3].trim().split(":");
                            double hIn = Double.parseDouble(in[0]), mIn = Double.parseDouble(in[1]);
                            double hOut = Double.parseDouble(out[0]), mOut = Double.parseDouble(out[1]);

                            if (hIn > 8 || (hIn == 8 && mIn > 0)) totalLateMins += ((hIn - 8) * 60) + mIn;
                            double dayHrs = (hOut + mOut/60) - (hIn + mIn/60);
                            totalHours += (dayHrs > 5) ? dayHrs - 1 : dayHrs;
                            count++;
                        }
                    }
                }
            }

            if (count == 0) {
                System.out.println("⚠️ No attendance data found for this period.");
                return;
            }

            // Calculations
            double gross = (hourlyRate * totalHours);
            double sss = gross * 0.045;
            double tax = (gross > 12500) ? (gross - 12500) * 0.20 : 0;
            double net = gross - (sss + tax + 100);

            System.out.println("\n==============================================");
            System.out.println("           OFFICIAL PAYSLIP SUMMARY           ");
            System.out.println("==============================================");
            System.out.printf("Total Days:    %d days\n", count);
            System.out.printf("Total Hours:   %.2f hrs\n", totalHours);
            System.out.printf("Gross Salary:  P %,.2f\n", gross);
            System.out.printf("NET PAY:       P %,.2f\n", net);
            System.out.println("==============================================\n");

        } catch (Exception e) {
            System.out.println("❌ ERROR: Could not process AttendanceRecords.csv. Ensure file is in project root.");
        }
    }
}
