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
        System.out.println("        MOTORPH SELF-SERVICE PORTAL v6.0      ");
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
        try (BufferedReader br = new BufferedReader(new FileReader("EmployeeDetails.csv"))) {
            String line;
            br.readLine(); // Skip Header
            while ((line = br.readLine()) != null) {
                // Regex handles commas inside addresses (quotes)
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length >= 20) {
                    employeeMap.put(data[0].trim(), data);
                }
            }
            System.out.println("📊 System: " + employeeMap.size() + " employees loaded.");
        } catch (Exception e) {
            System.out.println("[!] ERROR: EmployeeDetails.csv not found.");
        }
    }

    public static boolean authenticate(String id, String pin) {
        if (!employeeMap.containsKey(id)) return false;
        // Index 19 is the PIN column in your CSV
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
                System.out.println("ID: " + emp[0]);
                System.out.println("Position: " + emp[11]);
                System.out.println("Basic Salary: P " + emp[13]);
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
        String monthInput = sc.nextLine().trim();
        
        System.out.print("Select Cutoff [1] 1-15  [2] 16-31: ");
        String cutoff = sc.nextLine().trim();
        
        double hourlyRate = Double.parseDouble(emp[18].trim());
        double totalHours = 0;
        double totalLateMins = 0;

        try (BufferedReader br = new BufferedReader(new FileReader("AttendanceRecords.csv"))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] att = line.split(",");
                if (att.length < 4) continue;

                // Match ID
                if (att[0].trim().equals(emp[0])) {
                    String[] dateParts = att[1].trim().split("/");
                    int csvMonth = Integer.parseInt(dateParts[0]);
                    int csvDay = Integer.parseInt(dateParts[1]);
                    int userMonth = Integer.parseInt(monthInput);

                    // Month and Cutoff Logic
                    if (csvMonth == userMonth) {
                        boolean inPeriod = (cutoff.equals("1") && csvDay <= 15) || (cutoff.equals("2") && csvDay > 15);
                        
                        if (inPeriod) {
                            String[] in = att[2].trim().split(":");
                            String[] out = att[3].trim().split(":");
                            
                            double hIn = Double.parseDouble(in[0]), mIn = Double.parseDouble(in[1]);
                            double hOut = Double.parseDouble(out[0]), mOut = Double.parseDouble(out[1]);

                            // Late calculation (Standard 8:00 AM)
                            if (hIn > 8 || (hIn == 8 && mIn > 0)) {
                                totalLateMins += ((hIn - 8) * 60) + mIn;
                            }

                            double dayHrs = (hOut + mOut/60) - (hIn + mIn/60);
                            // Subtract 1hr lunch if they worked more than 5 hours
                            totalHours += (dayHrs > 5) ? dayHrs - 1 : dayHrs;
                        }
                    }
                }
            }

            if (totalHours == 0) {
                System.out.println("⚠️ No attendance found for this period.");
                return;
            }

            // Math Engine
            double latePenalty = (hourlyRate / 60) * totalLateMins;
            double gross = (hourlyRate * totalHours) - latePenalty;
            
            // Statutory Deductions (Simplified Engine)
            double sss = gross * 0.045;
            double phil = gross * 0.02;
            double pagibig = 100.00;
            double tax = (gross > 12500) ? (gross - 12500) * 0.20 : 0;
            double net = gross - (sss + phil + pagibig + tax);

            System.out.println("\n==============================================");
            System.out.println("           MOTORPH OFFICIAL PAYSLIP           ");
            System.out.println("==============================================");
            System.out.printf("Total Hours:    %.2f hrs\n", totalHours);
            System.out.printf("Late Minutes:   %d mins\n", (int)totalLateMins);
            System.out.printf("Gross Salary:   P %,.2f\n", gross);
            System.out.println("----------------------------------------------");
            System.out.printf("Total Deduct:  -P %,.2f\n", (sss+phil+pagibig+tax));
            System.out.printf("NET PAY:        P %,.2f\n", net);
            System.out.println("==============================================\n");

        } catch (Exception e) {
            System.out.println("[!] ERROR: Could not process AttendanceRecords.csv.");
        }
    }
}
