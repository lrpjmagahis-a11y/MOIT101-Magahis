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
        System.out.println("        MOTORPH PAYROLL SYSTEM v9.0          ");
        System.out.println("==============================================\n");

        System.out.print("👤 Employee ID: ");
        String id = sc.nextLine().trim();
        System.out.print("🔑 PIN: ");
        String pin = sc.nextLine().trim();

        if (authenticate(id, pin)) {
            loggedInID = id;
            showDashboard();
        } else {
            System.out.println("\n❌ Login Failed: Check ID/PIN.");
        }
    }

    public static void loadEmployeeData() {
        try (BufferedReader br = new BufferedReader(new FileReader("EmployeeDetails.csv"))) {
            String line;
            br.readLine(); // Skip Header
            while ((line = br.readLine()) != null) {
                // This regex is vital: it ignores commas located inside quotation marks
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (data.length >= 19) {
                    employeeMap.put(data[0].trim(), data);
                }
            }
            System.out.println("✅ " + employeeMap.size() + " Employees Loaded Successfully.");
        } catch (Exception e) {
            System.out.println("[!] ERROR: Cannot find EmployeeDetails.csv in project root.");
        }
    }

    public static boolean authenticate(String id, String pin) {
        if (!employeeMap.containsKey(id)) return false;
        // PIN is usually the last column (Index 19)
        return employeeMap.get(id)[19].trim().equals(pin);
    }

    public static void showDashboard() {
        Scanner sc = new Scanner(System.in);
        String[] emp = employeeMap.get(loggedInID);
        
        while (true) {
            System.out.println("\n--- WELCOME, " + emp[2].toUpperCase() + " " + emp[1].toUpperCase() + " ---");
            System.out.println("[1] Profile  [2] Payslip  [3] Logout");
            System.out.print("Selection: ");
            String choice = sc.nextLine();

            if (choice.equals("1")) {
                System.out.println("\nID: " + emp[0] + "\nPosition: " + emp[11] + "\nBasic: P" + emp[13]);
            } else if (choice.equals("2")) {
                calculatePayslip(emp);
            } else if (choice.equals("3")) break;
        }
    }

    public static void calculatePayslip(String[] emp) {
        Scanner sc = new Scanner(System.in);
        System.out.print("\nEnter Month (1-12): ");
        int userMonth;
        try {
            userMonth = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception e) { System.out.println("Invalid Month."); return; }

        System.out.print("Select Cutoff [1] 1-15  [2] 16-31: ");
        String cutoff = sc.nextLine().trim();
        
        double hourlyRate = Double.parseDouble(emp[18].trim());
        double totalHours = 0, totalLateMins = 0;
        int count = 0;

        try (BufferedReader br = new BufferedReader(new FileReader("AttendanceRecords.csv"))) {
            String line;
            br.readLine(); 
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] att = line.split(",");
                
                if (att[0].trim().equals(emp[0])) {
                    String[] dateParts = att[1].trim().split("/");
                    int csvMonth = Integer.parseInt(dateParts[0]);
                    int csvDay = Integer.parseInt(dateParts[1]);

                    if (csvMonth == userMonth) {
                        boolean inCutoff = (cutoff.equals("1") && csvDay <= 15) || (cutoff.equals("2") && csvDay > 15);
                        
                        if (inCutoff) {
                            String[] inT = att[2].trim().split(":");
                            String[] outT = att[3].trim().split(":");
                            
                            double hIn = Double.parseDouble(inT[0]), mIn = Double.parseDouble(inT[1]);
                            double hOut = Double.parseDouble(outT[0]), mOut = Double.parseDouble(outT[1]);

                            // Standard 8:00 AM Late Check
                            if (hIn > 8 || (hIn == 8 && mIn > 0)) totalLateMins += ((hIn - 8) * 60) + mIn;
                            
                            // Work duration minus 1hr lunch
                            double dayHrs = (hOut + mOut/60) - (hIn + mIn/60);
                            totalHours += (dayHrs > 5) ? dayHrs - 1 : dayHrs;
                            count++;
                        }
                    }
                }
            }

            if (count == 0) {
                System.out.println("⚠️ No data for ID " + emp[0] + " in Month " + userMonth + " Cutoff " + cutoff);
                return;
            }

            double gross = (hourlyRate * totalHours);
            double sss = gross * 0.045;
            double tax = (gross > 12500) ? (gross - 12500) * 0.20 : 0;
            double net = gross - (sss + tax + 100);

            System.out.println("\n==============================================");
            System.out.println("           MOTORPH OFFICIAL PAYSLIP           ");
            System.out.println("==============================================");
            System.out.printf("Period:        Month %d (Cutoff %s)\n", userMonth, (cutoff.equals("1")?"1st":"2nd"));
            System.out.printf("Total Hours:   %.2f hrs\n", totalHours);
            System.out.printf("Gross Salary:  P %,.2f\n", gross);
            System.out.printf("NET PAY:       P %,.2f\n", net);
            System.out.println("==============================================\n");

        } catch (Exception e) {
            System.out.println("❌ ERROR: Attendance parsing failed. Check CSV format.");
        }
    }
}
