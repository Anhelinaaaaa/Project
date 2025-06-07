package one;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a time range between a start and end date-time
 */
public class TimeSlot implements Serializable {

    /** Start time of the time slot */
    private LocalDateTime startTime;

    /** End time of the time slot */
    private LocalDateTime endTime;

    /**
     * Constructs a TimeSlot with specified start and end time
     *
     * @param startTime the start of the time slot
     * @param endTime   the end of the time slot
     */
    public TimeSlot(LocalDateTime startTime, LocalDateTime endTime) 
    {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * @return the start date-time of the slot
     */
    public LocalDateTime getStartTime() 
    {
        return startTime;
    }

    /**
     * @return the end date-time of the slot
     */
    public LocalDateTime getEndTime() 
    {
        return endTime;
    }

    /**
     * @return a string representation of the time slot
     */
    @Override
    public String toString() 
    {
        return startTime + " to " + endTime;
    }

    /**
     * Compares two TimeSlot objects for equality based on start and end times
     *
     * @param o the other object to compare
     * @return true if start and end times match, false otherwise
     */
    @Override
    public boolean equals(Object o) 
    {
        if (this == o) return true;
        if (!(o instanceof TimeSlot)) return false;
        TimeSlot that = (TimeSlot) o;
        return Objects.equals(startTime, that.startTime) &&
               Objects.equals(endTime, that.endTime);
    }

    /**
     * @return hash code based on start and end times
     */
    @Override
    public int hashCode() 
    {
        return Objects.hash(startTime, endTime);
    }
}
