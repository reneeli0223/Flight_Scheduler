import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Location implements Comparable<Location> {
    //region basic
    private String name;
    private double latitude;
    private double longitude;
    private List<Flight> arrivingFlights;
    private List<Flight> departingFlights;
    private double coefficient;

    public Location(String name, double lat, double lon, double coefficient) {
        this.name = name;
        this.latitude = lat;
        this.longitude = lon;
        this.arrivingFlights = new ArrayList<>();
        this.departingFlights = new ArrayList<>();
        this.coefficient = coefficient;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<Flight> getArrivingFlights() {
        return arrivingFlights;
    }

    public void setArrivingFlights(List<Flight> arrivingFlights) {
        this.arrivingFlights = arrivingFlights;
    }

    public List<Flight> getDepartingFlights() {
        return departingFlights;
    }

    public void setDepartingFlights(List<Flight> departingFlights) {
        this.departingFlights = departingFlights;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }
    //endregion

    //region string function
    // for debug
    @Override
    public String toString() {
        return "Location{" +
                "name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", arrivingFlights=" + arrivingFlights.size() +
                ", departingFlights=" + departingFlights.size() +
                ", coefficient=" + coefficient +
                '}';
    }

    // export to csv file
    public String toFileStr() {
        String latitudeS = latitude == 0 ? String.format("%.1f", latitude) :
                new BigDecimal(String.format("%.6f", latitude)).stripTrailingZeros().toString();
        String longitudeS = longitude == 0 ? String.format("%.1f", longitude) :
                new BigDecimal(String.format("%.6f", longitude)).stripTrailingZeros().toString();
        return String.format("%s,%s,%s,%.1f", name, latitudeS,
                longitudeS, coefficient);
    }

    // read line str from csv file, and return basic params
    // name, latitude, longitude, coefficient
    // if invalid, return null;
    public static String[] fromFileStr(String str) {
        String[] words = str.split(",");
        if (words.length != 4) {
            return null;
        }
        return words;
    }

    // show info in FLIGHT <id> command
    public void showFullInfo() {
        System.out.printf("%-13s%s%n", "Location:", name);
        System.out.printf("%-13s%.6f%n", "Latitude:", latitude);
        System.out.printf("%-13s%.6f%n", "Longitude:", longitude);
        System.out.printf("%-13s%+.4f%n", "Demand:", coefficient);
    }
    //endregion

    @Override
    public int compareTo(Location o) {
        return this.name.compareTo(o.getName());
    }

    //Implement the Haversine formula - return value in kilometres
    public static double distance(Location l1, Location l2) {
        double lat1 = l1.latitude, lon1 = l1.longitude;
        double lat2 = l2.latitude, lon2 = l2.longitude;
        double dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double R = 6371; // in kilometers
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

    public void addArrival(Flight f) {
        arrivingFlights.add(f);
    }

    public void addDeparture(Flight f) {
        departingFlights.add(f);
    }

    public void removeArrival(Flight f) {
        arrivingFlights.remove(f);
    }

    public void removeDeparture(Flight f) {
        departingFlights.remove(f);
    }

    // command schedule
    public List<FlightInfo> getScheduleInfos() {
        List<FlightInfo> list = new ArrayList<>();
        for (Flight flight : arrivingFlights) {
            FlightInfo info = new FlightInfo(flight.getId(), flight.getArriveTime(),
                    "Arrival from", flight.getSource().name);
            list.add(info);
        }
        for (Flight flight : departingFlights) {
            FlightInfo info = new FlightInfo(flight.getId(), flight.getDepartureTime(),
                    "Departure to", flight.getDestination().name);
            list.add(info);
        }
        Collections.sort(list);
        return list;
    }

    // command arrivals
    public List<FlightInfo> getArrivalsInfos() {
        List<FlightInfo> list = new ArrayList<>();
        for (Flight flight : arrivingFlights) {
            FlightInfo info = new FlightInfo(flight.getId(), flight.getArriveTime(),
                    "Arrival from", flight.getSource().name);
            list.add(info);
        }
        Collections.sort(list);
        return list;
    }

    // command departures
    public List<FlightInfo> getDeparturesInfos() {
        List<FlightInfo> list = new ArrayList<>();
        for (Flight flight : departingFlights) {
            FlightInfo info = new FlightInfo(flight.getId(), flight.getDepartureTime(),
                    "Departure to", flight.getDestination().name);
            list.add(info);
        }
        Collections.sort(list);
        return list;
    }

    // simple info of flight
    public static class FlightInfo implements Comparable<FlightInfo> {
        int id;
        int time;
        String type;
        String location;

        public FlightInfo(int id, int time, String type, String location) {
            this.id = id;
            this.time = time;
            this.type = type; // Arrival from / Departure to
            this.location = location;
        }

        @Override
        public int compareTo(FlightInfo o) {
            return Integer.compare(this.time, o.time);
        }

        // header
        public static void showInfoHeader() {
            String header = String.format("%-4s %-12s%s", "ID",
                    "Time", "Departure/Arrival to/from Location");
            System.out.println(header);
        }

        // show one info
        public void showInfo() {
            String info = String.format("%4d %-12s%s %s", id,
                    MyHelper.minuteInWeekToTimeStrSimple(time),
                    type, location);
            System.out.println(info);
        }

        // deep copy
        public FlightInfo copy() {
            return new FlightInfo(id, time, type, location);
        }

        public void minusWeek() {
            this.time -= 7 * 24 * 60;
        }

        public void plusWeek() {
            this.time += 7 * 24 * 60;
        }

        @Override
        public String toString() {
            return "FlightInfo{" +
                    "id=" + id +
                    ", time=" + time +
                    ", type='" + type + '\'' +
                    ", location='" + location + '\'' +
                    '}';
        }
    }
}
