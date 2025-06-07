package one;

import java.util.List;

/**
 * Command that adds a health professional and supports undo by removing them
 */
class AddProfessionalCommand implements Command 
{
    private final String professionalName;
    private final OptimizedOperationScheduler scheduler;

    /**
     * Constructs the command with the professional's name and the scheduler context
     *
     * @param professionalName the name of the professional to add
     * @param scheduler        the scheduler instance that performs the action
     */
    public AddProfessionalCommand(String professionalName, OptimizedOperationScheduler scheduler) 
    
    {
        this.professionalName = professionalName;
        this.scheduler = scheduler;
    }

    /**
     * Undoes the addition by removing the health professional
     */
    @Override
    public void undo() 
    {
        scheduler.removeHealthProfessional(professionalName);
    }
}

/**
 * Command that removes a health professional and supports undo by restoring them
 */
class RemoveProfessionalCommand implements Command 
{
    private final HealthProfessional professional;
    private final Diary diary;
    private final OptimizedOperationScheduler scheduler;

    /**
     * Constructs the command with the removed professional, their diary, and the scheduler
     *
     * @param professional the professional that was removed
     * @param diary        the diary associated with the professional
     * @param scheduler    the scheduler instance that performs the action
     */
    public RemoveProfessionalCommand(HealthProfessional professional, Diary diary, OptimizedOperationScheduler scheduler) 
    {
        this.professional = professional;
        this.diary = diary;
        this.scheduler = scheduler;
    }

    /**
     * Undoes the removal by restoring the health professional and their diary
     */
    @Override
    public void undo() 
    {
        scheduler.restoreHealthProfessional(professional, diary);
    }
}

/**
 * Command that edits a health professional and supports undo by reverting the change
 */
class EditProfessionalCommand implements Command 
{
    private final String oldName;
    private final HealthProfessional oldProfessional;
    private final HealthProfessional newProfessional;
    private final OptimizedOperationScheduler scheduler;

    /**
     * Constructs the command for editing a professional with both old and new versions
     *
     * @param oldName         the original name of the professional
     * @param oldProfessional the original professional details
     * @param newProfessional the updated professional details
     * @param scheduler       the scheduler instance that performs the action
     */
    public EditProfessionalCommand(String oldName, HealthProfessional oldProfessional, HealthProfessional newProfessional, OptimizedOperationScheduler scheduler) 
    {
        this.oldName = oldName;
        this.oldProfessional = oldProfessional;
        this.newProfessional = newProfessional;
        this.scheduler = scheduler;
    }

    /**
     * Undoes the edit by restoring the previous professional details
     */
    @Override
    public void undo() 
    {
        scheduler.editHealthProfessional(newProfessional.getName(), oldProfessional);
    }
}

/**
 * Command that schedules an appointment and supports undo by removing it from diaries
 */
class ScheduleAppointmentCommand implements Command 
{
    private final Appointment appointment;
    private final List<String> professionalNames;
    private final OptimizedOperationScheduler scheduler;

    /**
     * Constructs the command with the appointment and involved professional names
     *
     * @param appointment       the appointment that was scheduled
     * @param professionalNames the list of professionals involved
     * @param scheduler         the scheduler instance that performs the action
     */
    public ScheduleAppointmentCommand(Appointment appointment, List<String> professionalNames, OptimizedOperationScheduler scheduler) 
    {
        this.appointment = appointment;
        this.professionalNames = professionalNames;
        this.scheduler = scheduler;
    }

    /**
     * Undoes the appointment scheduling by removing it from each professional's diary
     */
    @Override
    public void undo() 
    {
        for (String name : professionalNames) 
        {
            Diary diary = scheduler.getDiary(name);
            if (diary != null) 
            {
                diary.removeAppointment(appointment);
            }
        }
    }
}

