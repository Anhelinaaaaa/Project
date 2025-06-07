package one;
import java.io.Serializable;

/**
 * Command interface to undo operations in the scheduler.
 */
public interface Command extends Serializable {
    void undo();
}
