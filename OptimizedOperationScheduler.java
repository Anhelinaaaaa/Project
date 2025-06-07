package one;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The central scheduler that manages health professionals, their diaries,
 * and appointments. Supports undoable operations such as adding, editing,
 * or removing professionals and scheduling appointments.
 *
 * This class coordinates professional availability, conflict checking,
 * time slot searching, and supports undoing recent changes via a command stack
 */
public class OptimizedOperationScheduler implements Serializable {

    /** A map of professional names to their corresponding HealthProfessional objects. */
    private Map<String, HealthProfessional> professionals = new HashMap<>();

    /** A map of professional names to their associated Diary objects. */
    private Map<String, Diary> diaries = new HashMap<>();

    /** A stack of undoable commands used to support undo operations. */
    private Stack<Command> undoStack = new Stack<>();

    /**
     * Adds a new health professional and initializes their diary.
     *
     * @param professional the health professional to add
     * @return true if added successfully, false if they already exist or input is null
     */
    public boolean addHealthProfessional(HealthProfessional professional) 
    {
        if (professional == null || professionals.containsKey(professional.getName())) return false;
        professionals.put(professional.getName(), professional);
        diaries.put(professional.getName(), new Diary());
        recordUndo(new AddProfessionalCommand(professional.getName(), this));
        return true;
    }

    /**
     * Removes a health professional and stores their diary for undo.
     *
     * @param name the name of the professional to remove
     * @return true if removed, false if not found
     */
    public boolean removeHealthProfessional(String name) 
    {
        if (!professionals.containsKey(name)) return false;
        HealthProfessional removed = professionals.remove(name);
        Diary removedDiary = diaries.remove(name);
        recordUndo(new RemoveProfessionalCommand(removed, removedDiary, this));
        return true;
    }

    /**
     * Restores a previously removed professional and their diary (used for undo).
     *
     * @param professional the professional to restore
     * @param diary        their associated diary
     */
    public void restoreHealthProfessional(HealthProfessional professional, Diary diary) 
    {
        professionals.put(professional.getName(), professional);
        diaries.put(professional.getName(), diary);
    }

    /**
     * Edits a health professional's details and updates the mapping if the name changes.
     *
     * @param oldName         the current name
     * @param newProfessional the new professional details
     * @return true if updated, false if the old name is not found
     */
    public boolean editHealthProfessional(String oldName, HealthProfessional newProfessional) 
    {
        if (!professionals.containsKey(oldName)) return false;
        HealthProfessional oldProfessional = professionals.get(oldName);
        professionals.put(newProfessional.getName(), newProfessional);
        if (!oldName.equals(newProfessional.getName())) 
        {
            Diary diary = diaries.remove(oldName);
            diaries.put(newProfessional.getName(), diary);
            professionals.remove(oldName);
        }
        recordUndo(new EditProfessionalCommand(oldName, oldProfessional, newProfessional, this));
        return true;
    }

    /**
     * Retrieves the diary of a specific health professional.
     *
     * @param professionalName the name of the professional
     * @return the associated diary or null if not found
     */
    public Diary getDiary(String professionalName) 
    {
        return diaries.get(professionalName);
    }

    /**
     * @return a list of all registered health professionals
     */
    public List<HealthProfessional> getAllProfessionals() 
    {
        return new ArrayList<>(professionals.values());
    }

    /**
     * Schedules an appointment if all involved professionals are available during the timeslot.
     *
     * @param timeSlot         the desired appointment time
     * @param patientId        the ID of the patient
     * @param treatmentType    the type of treatment
     * @param professionalNames the list of professionals to be involved
     * @return true if scheduled successfully, false if any conflict is found
     */
    public boolean scheduleAppointment(TimeSlot timeSlot, String patientId, String treatmentType, List<String> professionalNames) 
    {
        List<Diary> involved = new ArrayList<>();
        for (String name : professionalNames) 
        {
            Diary d = diaries.get(name);
            if (d == null || d.isOverlapping(new Appointment(
                    timeSlot.getStartTime().toLocalDate(),
                    timeSlot.getStartTime().toLocalTime(),
                    timeSlot.getEndTime().toLocalTime(),
                    treatmentType,
                    null,
                    patientId))) return false;
            involved.add(d);
        }
        Appointment appointment = new Appointment(
                timeSlot.getStartTime().toLocalDate(),
                timeSlot.getStartTime().toLocalTime(),
                timeSlot.getEndTime().toLocalTime(),
                treatmentType,
                professionalNames.stream().map(professionals::get).collect(Collectors.toList()),
                patientId
        );

        for (Diary d : involved) d.addAppointment(appointment);
        recordUndo(new ScheduleAppointmentCommand(appointment, professionalNames, this));
        return true;
    }

    /**
     * Searches for time slots when all listed professionals are available
     * for the given date range and appointment duration.
     *
     * @param names    the names of professionals required
     * @param start    the start date of the search range
     * @param end      the end date of the search range
     * @param duration the desired appointment duration
     * @return list of available TimeSlots that meet the criteria
     */
    public List<TimeSlot> findAvailableTimeSlots(List<String> names, LocalDate start, LocalDate end, Duration duration) {
        if (names.isEmpty() || start.isAfter(end)) return Collections.emptyList();
        List<Diary> diariesToSearch = names.stream().map(diaries::get).filter(Objects::nonNull).collect(Collectors.toList());
        List<TimeSlot> available = new ArrayList<>();

        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) 
        {
            for (LocalTime t = LocalTime.of(8, 0); t.plus(duration).isBefore(LocalTime.of(17, 0)); t = t.plusMinutes(30)) {
                LocalDateTime startTime = LocalDateTime.of(day, t);
                LocalDateTime endTime = startTime.plus(duration);
                Appointment testApp = new Appointment(day, t, endTime.toLocalTime(), null, null, "test");
                if (diariesToSearch.stream().allMatch(d -> !d.isOverlapping(testApp))) {
                    available.add(new TimeSlot(startTime, endTime));
                }
            }
        }
        return available;
    }

    /**
     * Undoes the most recent change if possible.
     *
     * @return true if a change was undone, false if the stack was empty
     */
    public boolean undo() 
    {
        if (!undoStack.isEmpty()) 
        {
            undoStack.pop().undo();
            return true;
        }
        return false;
    }

    /**
     * Adds a command to the undo stack for future reversal.
     *
     * @param command the undoable command to record
     */
    private void recordUndo(Command command) 
    {
        undoStack.push(command);
    }
}


