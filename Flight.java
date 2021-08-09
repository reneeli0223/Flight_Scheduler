import java.text.DecimalFormat;

public class Flight {
    //region basic
    private int id;
    private int departureTime;
    private Location source;
    private Location destination;
    private int capacity;
    private int passengersBooked;

    public Flight(int id, int departureTime, Location source,
                  Location destination, int capacity, int passengersBooked) {
        this.id = id;
        this.departureTime = departureTime;
        this.source = source;
        this.destination = destination;
        this.capacity = capacity;
        this.passengersBooked = passengersBooked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(int departureTime) {
        this.departureTime = departureTime;
    }

    public Location getSource() {
        return source;
    }

    public void setSource(Location source) {
        this.source = source;
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getPassengersBooked() {
        return passengersBooked;
    }

    public void setPassengersBooked(int passengersBooked) {
        this.passengersBooked = passengersBooked;
    }
    //endregion

    //region string function
    // for debug
    @Override
    public String toString() {
        return "Flight{" +
                "id=" + id +
                ", departureTime=" + departureTime +
                ", source=" + source.getName() +
                ", destination=" + destination.getName() +
                ", capacity=" + capacity +
                ", passengersBooked=" + passengersBooked +
                '}';
    }

    // export to csv file
    public String toFileStr() {
        String timeStr = MyHelper.minuteInWeekToTimeStr(departureTime);
        return String.format("%s,%s,%s,%d,%d", timeStr, source.getName(),
                destination.getName(), capacity, passengersBooked);
    }

    // read line str from csv file, and return basic params
    // departureTime source destination capacity passengersBooked
    // if invalid, return null;
    public static String[] fromFileStr(String str) {
        String[] words = str.split(",");
        String[] result = new String[5];
        if (words.length != 5) {
            return null;
        }
        String time = words[0];
        String[] timeWords = time.split(" ");
        if (timeWords.length != 2) {
            return null;
        }
        MyHelper.Weekday weekday;
        try {
            weekday = MyHelper.Weekday.valueOf(timeWords[0]);
        } catch (IllegalArgumentException e) {
            return null;
        }
        String hourminute = timeWords[1];
        int minuteInWeek = MyHelper.getMinuteInWeek(weekday, hourminute);
        result[0] = String.valueOf(minuteInWeek);
        result[1] = words[1];
        result[2] = words[2];
        result[3] = words[3];
        result[4] = words[4];
        return result;
    }

    // show header in FLIGHTS command
    public static void showInfoHeader() {
        String header = String.format("%-4s %-12s%-12s%s --> %s", "ID",
                "Departure", "Arrival", "Source", "Destination");
        System.out.println(header);
    }

    // show info in FLIGHTS command
    public void showInfo() {
        int arriveTime = getArriveTime();
        String info = String.format("%4d %-12s%-12s%s --> %s", id,
                MyHelper.minuteInWeekToTimeStrSimple(departureTime),
                MyHelper.minuteInWeekToTimeStrSimple(arriveTime),
                source.getName(), destination.getName());
        System.out.println(info);
    }

    // show info in FLIGHT <id> command
    public void showFullInfo() {
        int arriveTime = getArriveTime();
        System.out.println("Flight " + id);
        System.out.printf("%-14s%-10s%s%n", "Departure:",
                MyHelper.minuteInWeekToTimeStrSimple(departureTime), source.getName());
        System.out.printf("%-14s%-10s%s%n", "Arrival:",
                MyHelper.minuteInWeekToTimeStrSimple(arriveTime), destination.getName());
        DecimalFormat distanceFormat = new DecimalFormat("#,###");
        String distanceStr = distanceFormat.format(getDistance());
        System.out.printf("%-14s%skm%n", "Distance:", distanceStr);
        System.out.printf("%-14s%s%n", "Duration:", getDurationStr());
        DecimalFormat priceFormat = new DecimalFormat("#.##");
        System.out.printf("%-14s$%.2f%n", "Ticket Cost:", getTicketPrice());
        System.out.printf("%-14s%d/%d%n", "Passengers:", passengersBooked, capacity);
    }

    // used in FLIGHT <id> command
    public String getDurationStr() {
        int duration = getDuration();
        if (duration < 60) {
            return duration + "m";
        } else if (duration < 60 * 24) {
            int hour = duration / 60;
            int minute = duration - hour * 60;
            return hour + "h " + minute + "m";
        } else {
            int day = duration / 24 / 60;
            int hour = (duration - day * 24 * 60) / 60;
            int minute = duration - day * 24 * 60 - hour * 60;
            return day + "d " + hour + "h " + minute + "m";
        }
    }
    //endregion

    //get the number of minutes this flight takes (round to nearest whole number)
    public int getDuration() {
        double distance = getDistance();
        double hour = distance / 720d;
        double minute = hour * 60d;
        return (int) Math.round(minute);
    }

    //implement the ticket price formula
    public double getTicketPrice() {
        double coefficientDiff = destination.getCoefficient() - source.getCoefficient();
        double x = passengersBooked / (double) capacity;
        double y;
        if (x <= 0.5) {
            y = -0.4 * x + 1;
        } else if (x <= 0.7) {
            y = x + 0.3;
        } else {
            y = 0.2 / Math.PI * (Math.atan(20 * x - 14)) + 1;
        }
        double distance = getDistance();
        double per100km = 30 + 4 * coefficientDiff;
        return y * distance / 100d * per100km;
    }

    // how many passengers can book
    public int canBook(int num) {
        if (isFull()) {
            return 0;
        } else {
            return Math.min(capacity - passengersBooked, num);
        }
    }

    // book one ticket
    public void book() {
        passengersBooked++;
    }

    //return whether or not this flight is full
    public boolean isFull() {
        return passengersBooked >= capacity;
    }

    //get the distance of this flight in km
    public double getDistance() {
        return Location.distance(source, destination);
    }

    //get the layover time, in minutes, between two flights
    //todo
    public static int layover(Flight x, Flight y) {
        int start = x.getArriveTime();
        int end = y.getDepartureTime();
        int time = end - start;
        if (time < 0) {
            time += 7 * 24 * 60;
        }
        return time;
    }

    // get arrive time
    public int getArriveTime() {
        int arriveTime = departureTime + getDuration();
        if (arriveTime > 7 * 24 * 60) {
            arriveTime -= 7 * 24 * 60;
        }
        return arriveTime;
    }
}
