package one;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

public class Tester {
    private static final Scanner scanner = new Scanner(System.in);
    private static OptimizedOperationScheduler scheduler = new OptimizedOperationScheduler();

    public static void main(String[] args) {
        int choice;
        do {
            printMenu();
            try {
                choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> addHealthProfessional();
                    case 2 -> editHealthProfessional();
                    case 3 -> deleteHealthProfessional();
                    case 4 -> viewDiary();
                    case 5 -> addAppointment();
                    case 6 -> searchAndScheduleAppointment();
                    case 7 -> FileManager.save(scheduler);
                    case 8 -> scheduler = FileManager.load();
                    case 9 -> undo();
                    case 10 -> viewAllProfessionals();
                    case 0 -> System.out.println("Exiting...");
                    default -> System.out.println("Invalid choice. Please enter a number between 0-10.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                choice = -1;
            }
        } while (choice != 0);
    }

    private static void printMenu() {
        System.out.println("Operation Scheduler");
        System.out.println("1. Add Health Professional");
        System.out.println("2. Edit Health Professional");
        System.out.println("3. Delete Health Professional");
        System.out.println("4. View Diary");
        System.out.println("5. Add Appointment");
        System.out.println("6. Search & Schedule Appointment");
        System.out.println("7. Save to File");
        System.out.println("8. Load from File");
        System.out.println("9. Undo Last Change");
        System.out.println("10. View All Health Professionals");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private static void addHealthProfessional() {
        try {
            System.out.print("Enter name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter profession: ");
            String profession = scanner.nextLine().trim();
            System.out.print("Enter location: ");
            String location = scanner.nextLine().trim();
            boolean added = scheduler.addHealthProfessional(new HealthProfessional(name, profession, location));
            System.out.println(added ? "Added." : "Already exists or invalid.");
        } catch (Exception e) {
            System.out.println("Error adding professional: " + e.getMessage());
        }
    }

    private static void editHealthProfessional() {
        try {
            System.out.print("Enter current name: ");
            String oldName = scanner.nextLine().trim();
            System.out.print("Enter new name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter new profession: ");
            String profession = scanner.nextLine().trim();
            System.out.print("Enter new location: ");
            String location = scanner.nextLine().trim();
            boolean edited = scheduler.editHealthProfessional(oldName, new HealthProfessional(name, profession, location));
            System.out.println(edited ? "Updated." : "Failed to edit.");
        } catch (Exception e) {
            System.out.println("Error editing professional: " + e.getMessage());
        }
    }

    private static void deleteHealthProfessional() {
        System.out.print("Enter name: ");
        String name = scanner.nextLine().trim();
        boolean deleted = scheduler.removeHealthProfessional(name);
        System.out.println(deleted ? "Deleted." : "Not found.");
    }

    private static void viewDiary() {
        System.out.print("Enter professional name: ");
        String name = scanner.nextLine().trim();
        Diary diary = scheduler.getDiary(name);
        if (diary != null) diary.printAllAppointments();
        else System.out.println("Professional not found.");
    }

    private static void addAppointment() {
        try {
            System.out.print("Enter date (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(scanner.nextLine().trim());
            System.out.print("Enter start time (HH:MM): ");
            LocalTime start = LocalTime.parse(scanner.nextLine().trim());
            System.out.print("Enter end time (HH:MM): ");
            LocalTime end = LocalTime.parse(scanner.nextLine().trim());
            System.out.print("Enter treatment type: ");
            String type = scanner.nextLine().trim();
            System.out.print("Enter patient ID: ");
            String patientId = scanner.nextLine().trim();
            System.out.print("Enter doctor names (comma separated): ");
            List<String> names = List.of(scanner.nextLine().split(", *"));
            TimeSlot slot = new TimeSlot(LocalDateTime.of(date, start), LocalDateTime.of(date, end));
            boolean success = scheduler.scheduleAppointment(slot, patientId, type, names);
            System.out.println(success ? "Appointment scheduled." : "Conflict found or invalid professionals.");
        } catch (Exception e) {
            System.out.println("Error adding appointment: " + e.getMessage());
        }
    }

    private static void searchAndScheduleAppointment() {
        try {
            System.out.print("Enter doctor names (comma separated): ");
            List<String> names = List.of(scanner.nextLine().split(", *"));
            System.out.print("Enter patient ID: ");
            String patientId = scanner.nextLine().trim();
            System.out.print("Enter start date (YYYY-MM-DD): ");
            LocalDate start = LocalDate.parse(scanner.nextLine().trim());

            System.out.println("NOTE: You can book appointments up to 10 days ahead.");
            System.out.print("Enter end date (YYYY-MM-DD): ");
            LocalDate end = LocalDate.parse(scanner.nextLine().trim());
            if (end.isAfter(start.plusDays(10))) {
                System.out.println("End date cannot be more than 10 days after start date. Adjusting to " + start.plusDays(10));
                end = start.plusDays(10);
            }

            System.out.print("Enter desired duration (minutes): ");
            Duration duration = Duration.ofMinutes(Long.parseLong(scanner.nextLine().trim()));

            long begin = System.currentTimeMillis();
            List<TimeSlot> slots = scheduler.findAvailableTimeSlots(names, start, end, duration);
            long timeTaken = System.currentTimeMillis() - begin;

            if (slots.isEmpty()) {
                System.out.println("No slots available.");
                return;
            }

            for (int i = 0; i < slots.size(); i++) {
                System.out.println(i + 1 + ". " + slots.get(i));
            }

            System.out.print("Choose a slot number to book or 0 to cancel: ");
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice > 0 && choice <= slots.size()) {
                System.out.print("Enter treatment type: ");
                String type = scanner.nextLine().trim();
                boolean booked = scheduler.scheduleAppointment(slots.get(choice - 1), patientId, type, names);
                System.out.println(booked ? "Booked." : "Failed to book.");
            }

            System.out.println("Search took: " + timeTaken + "ms");
        } catch (Exception e) {
            System.out.println("Error scheduling: " + e.getMessage());
        }
    }

    private static void viewAllProfessionals() {
        List<HealthProfessional> all = scheduler.getAllProfessionals();
        if (all.isEmpty()) {
            System.out.println("No professionals found.");
        } else {
            all.forEach(System.out::println);
        }
    }

    private static void undo() {
        boolean undone = scheduler.undo();
        System.out.println(undone ? "Undo complete." : "Nothing to undo.");
    }
}

