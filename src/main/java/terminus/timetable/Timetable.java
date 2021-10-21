package terminus.timetable;

import terminus.common.DaysOfWeekEnum;
import terminus.common.Messages;
import terminus.common.TerminusLogger;
import terminus.content.ContentManager;
import terminus.content.Link;
import terminus.module.ModuleManager;
import terminus.module.NusModule;
import terminus.ui.Ui;

import java.util.Arrays;
import java.util.stream.Stream;

import static terminus.common.CommonUtils.isStringNullOrEmpty;

public class Timetable {

    private ModuleManager moduleManager;
    private int index = 0;

    public Timetable(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    /**
     * Lists all the schedule for a particular day.
     *
     * @param contentManager ContentManager object containing all user's links.
     * @param currentDay     The particular day at which the schedules are selected from.
     * @return String String object containing all the schedules for the particular day.
     */
    private String listDailySchedule(ContentManager<Link> contentManager, String currentDay) {
        StringBuilder dailySchedule = new StringBuilder();

        contentManager.getContents()
                .stream()
                .filter(x -> x.getDay().equalsIgnoreCase(currentDay))
                .forEach(x -> {
                    index++;
                    dailySchedule.append(String.format("%d. %s\n", index, x.getViewDescription()));
                });
        return dailySchedule.toString();
    }

    /**
     * Retrieve and format all the user's schedule for the particular day.
     *
     * @param today The particular day at which the schedules are selected from.
     * @return String String object containing all the schedules for the day
     */
    public String getDailySchedule(String today) {
        StringBuilder schedule = new StringBuilder();
        String[] modules = moduleManager.getAllModules();
        Stream<String> stream = Arrays.stream(modules);

        stream.forEach(x -> {
            NusModule module = moduleManager.getModule(x);
            ContentManager<Link> contentManager = module.getContentManager(Link.class);
            assert contentManager != null;
            schedule.append(listDailySchedule(contentManager, today));
            TerminusLogger.info(String.format("Successfully acquire %s's schedule for %s", x, today));
        });
        index = 0;

        if (isStringNullOrEmpty(schedule.toString())) {
            return null;
        }
        return schedule.toString();
    }

    /**
     * Retrieve and format all the user's schedule for the week.
     *
     * @return String string object containing all the user's schedule for the week
     */
    public String getWeeklySchedule() {
        StringBuilder dailyResult = new StringBuilder();

        for (DaysOfWeekEnum currentDay : DaysOfWeekEnum.values()) {
            String today = currentDay.toString();
            String dailySchedule = getDailySchedule(today);
            if (!isStringNullOrEmpty(dailySchedule)) {
                assert dailySchedule != null;
                String header = String.format("%s:\n", today);
                dailyResult.append(header.toUpperCase());
                dailyResult.append(dailySchedule);
                assert dailyResult != null;
                TerminusLogger.info(String.format("Successfully acquire %s's schedule", today));
            }
            index = 0;
        }
        return dailyResult.toString();
    }

    /**
     * Print empty message for empty user schedule.
     *
     * @param schedule The string containing the retrieved user schedule.
     * @param day      The day corresponding to the retrieved schedule
     */
    public String checkEmptySchedule(String schedule, String day) {
        if (schedule == null) {
            TerminusLogger.info("There is no schedule in the user's timetable");
            schedule = String.format(Messages.EMPTY_SCHEDULE_FOR_THE_DAY, day);
        }
        return schedule;
    }
}
