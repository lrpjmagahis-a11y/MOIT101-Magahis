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
        System.out.println("        MOTORPH PAYROLL SYSTEM v10.0         ");
        System.out.println("==============================================\n");

        System.out.print("👤 Employee ID: ");
        String id = sc.nextLine().trim();
        System.out.print("🔑 PIN: ");
        String pin = sc.nextLine().trim();

        if (authenticate(id, pin)) {
            loggedInID = id;
            showDashboard();
        } else {
            System.out.println("\n❌ Access Denied: Incorrect ID or PIN.");
        }
    }

    public static void loadEmployeeData() {
        String fileName = "EmployeeDetails.csv";
        File file = new File(fileName);
        
        // NetBeans fix: check parent directory if not in root
        if (!file.exists()) file = new File("..", fileName);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Skip Header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // Regex handles commas inside quoted addresses
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length >= 19) {
                    employeeMap.put(data[0].trim(), data);
                }
            }
            System.out.println("✅ " + employeeMap.size() + " Employees Synced from GitHub Data.");
        } catch (Exception e) {
            System.out.println("[!] ERROR: Cannot find " + fileName + ". Ensure it's in the project root!");
        }
    }

    public static boolean authenticate(String id, String pin) {
        if (!employeeMap.containsKey(id)) return false;
        String[] emp = employeeMap.get(id);
        // Column 20 (Index 19) is the PIN in your GitHub CSV
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
                System.out.println("Position: " + emp[11]);
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
        
        // Column 19 (Index 18) is the Hourly Rate
        double hourlyRate = Double.parseDouble(emp[18].trim());
        double totalHours = 0, totalLateMins = 0;
        int count = 0;

        File attFile = new File("AttendanceRecords.csv");
        if (!attFile.exists()) attFile = new File("..", "AttendanceRecords.csv");

        try (BufferedReader br = new BufferedReader(new FileReader(attFile))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] att = line.split(",");
                if (att[0].trim().equals(emp[0])) {
                    String[] dateParts = att[1].trim().split("/");
                    int csvMonth = Integer.parseInt(dateParts[0]);
                    int csvDay = Integer.parseInt(dateParts[1]);

                    if (csvMonth == userMonth) {
                        boolean inPeriod = (cutoff.equals("1") && csvDay <= 15) || (cutoff.equals("2") && csvDay > 15);
                        if (inPeriod) {
                            String[] inT = att[2].trim().split(":");
                            String[] outT = att[3].trim().split(":");
                            
                            double hIn = Double.parseDouble(inT[0]), mIn = Double.parseDouble(inT[1]);
                            double hOut = Double.parseDouble(outT[0]), mOut = Double.parseDouble(outT[1]);

                            if (hIn > 8 || (hIn == 8 && mIn > 0)) totalLateMins += ((hIn - 8) * 60) + mIn;
                            double dayHrs = (hOut + mOut/60) - (hIn + mIn/60);
                            totalHours += (dayHrs > 5) ? dayHrs - 1 : dayHrs;
                            count++;
                        }
                    }
                }
            }

            if (count == 0) {
                System.out.println("⚠️ No attendance found for this period.");
                return;
            }

            double gross = (hourlyRate * totalHours);
            double sss = gross * 0.045;
            double net = gross - (sss + 100); // 100 for Pag-IBIG

            System.out.println("\n==============================================");
            System.out.println("           MOTORPH OFFICIAL PAYSLIP           ");
            System.out.println("==============================================");
            System.out.printf("Total Days:    %d\n", count);
            System.out.printf("Gross Salary:  P %,.2f\n", gross);
            System.out.printf("NET PAY:       P %,.2f\n", net);
            System.out.println("==============================================\n");

        } catch (Exception e) {
            System.out.println("❌ ERROR: Attendance parsing failed. Check format.");
        }
    }
}
