import java.text.DecimalFormat;
import java.util.List;

public final class MyHelper {
    private MyHelper() {
    }

    //region time Weekday
    public enum Weekday {
        Monday,
        Tuesday,
        Wednesday,
        Thursday,
        Friday,
        Saturday,
        Sunday
    }

    /**
     * Calculate minute in week, start at Monday, according to weekday and dayTime.
     *
     * @param weekday (sample Monday)
     * @param dayTime (sample 09:00)
     * @return long, minute in week
     */
    public static int getMinuteInWeek(Weekday weekday, String dayTime) throws IllegalArgumentException {
        int minuteInWeek = 0;
        minuteInWeek += (weekday.ordinal() - Weekday.Monday.ordinal()) * 24 * 60;
        String[] words = dayTime.split(":");
        if (words.length != 2) {
            throw new IllegalArgumentException();
        }
        int hour = Integer.parseInt(words[0]);
        if (hour < 0 || hour > 24) {
            throw new IllegalArgumentException();
        }
        int minute = Integer.parseInt(words[1]);
        if (minute < 0 || minute > 60) {
            throw new IllegalArgumentException();
        }
        minuteInWeek += hour * 60 + minute;
        return minuteInWeek;
    }

    /**
     * Generate timeStr from minuteInWeek.
     *
     * @param minuteInWeek (sample Monday 09:00)
     * @return time str
     */
    public static String minuteInWeekToTimeStr(int minuteInWeek) {
        if (minuteInWeek > 7 * 24 * 60) {
            minuteInWeek = minuteInWeek % (7 * 24 * 60);
        } else if (minuteInWeek < 0) {
            minuteInWeek = (minuteInWeek + 7 * 24 * 60) % (7 * 24 * 60);
        }
        int day = minuteInWeek / 60 / 24;
        int hour = (minuteInWeek - day * 60 * 24) / 60;
        int minute = (minuteInWeek - day * 60 * 24 - hour * 60);
        return String.format("%s %02d:%02d", Weekday.values()[day].toString(),
                hour, minute);
    }

    /**
     * Simple version of minuteInWeekToTimeStr, Weekday length only 3.
     *
     * @param minuteInWeek (sample Monday 09:00)
     * @return time str, simple version
     */
    public static String minuteInWeekToTimeStrSimple(int minuteInWeek) {
        int day = minuteInWeek / 60 / 24;
        int hour = (minuteInWeek - day * 60 * 24) / 60;
        int minute = (minuteInWeek - day * 60 * 24 - hour * 60);
        return String.format("%s %02d:%02d", Weekday.values()[day].toString().substring(0, 3),
                hour, minute);
    }

    // minute to hour+minute, example: 1h 16m
    public static String minuteToHourMinute(int minute) {
        int hour = minute / 60;
        int min = minute - hour * 60;
        if (hour == 0) {
            return min + "m";
        } else {
            return hour + "h " + min + "m";
        }
    }
    //endregion

    //region find functions
    // find flight by id, not found return null
    public static Flight findFlightById(List<Flight> flightList, int id) {
        for (Flight flight : flightList) {
            if (flight.getId() == id) {
                return flight;
            }
        }
        return null;
    }

    // find location by name, not found return null
    public static Location findLocationByName(List<Location> locationList, String name) {
        for (Location location : locationList) {
            if (location.getName().equalsIgnoreCase(name)) {
                return location;
            }
        }
        return null;
    }
    //endregion

    public static void main(String[] args) {
        //test
        int minute1 = getMinuteInWeek(Weekday.Monday, "09:00");
        int minute2 = getMinuteInWeek(Weekday.Sunday, "09:43");
        System.out.println(minuteInWeekToTimeStr(minute1));
        System.out.println(minuteInWeekToTimeStr(minute2));
        System.out.println(minuteInWeekToTimeStrSimple(minute1));
        System.out.println(minuteInWeekToTimeStrSimple(minute2));
        double number = 710159826.54;
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        String string = decimalFormat.format(number);
        System.out.println(string);
    }
}
