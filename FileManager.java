package one;

import java.io.*;

/**
 * Utility class for saving and loading the scheduler to and from a file.
 */
public class FileManager {
    private static final String FILE_NAME = "scheduler.dat";

    /**
     * Saves the scheduler object to a file.
     *
     * @param scheduler the scheduler instance to save
     */
    public static void save(OptimizedOperationScheduler scheduler) 
    {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(scheduler);
            System.out.println("Scheduler saved successfully.");
        } catch (IOException e) {
            System.out.println("Failed to save scheduler: " + e.getMessage());
        }
    }

    /**
     * Loads the scheduler object from a file.
     *
     * @return the loaded scheduler or a new instance if load fails
     */
    public static OptimizedOperationScheduler load() 
    {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            System.out.println("Scheduler loaded successfully.");
            return (OptimizedOperationScheduler) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Failed to load scheduler: " + e.getMessage());
            return new OptimizedOperationScheduler();
        }
    }
}

