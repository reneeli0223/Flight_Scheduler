import java.util.*;
import java.util.stream.Collectors;

public class ActionTravel {

    public static class FlightPath {
        public List<Flight> paths;

        public FlightPath() {
            this.paths = new ArrayList<>();
        }

        public FlightPath(FlightPath other) {
            this.paths = new ArrayList<>(other.paths);
        }

        public int getNum() {
            return this.paths.size();
        }

        public void addFlight(Flight flight) {
            this.paths.add(flight);
        }

        public Location getLastLocation() {
            if (paths.isEmpty()) {
                return null;
            } else {
                return paths.get(paths.size() - 1).getDestination();
            }
        }

        public double getTotalCost() {
            return paths.stream().mapToDouble(Flight::getTicketPrice).sum();
        }

        // all time
        public int getTotalDuration() {
            return getLayoverTime() + getFlightTime();
        }

        public int getStopovers() {
            return Math.max(0, paths.size() - 2);
        }

        public int getLayoverTime() {
            if (paths.size() < 2) {
                return 0; //todo
            } else {
                int layover = 0;
                for (int i = 0; i < paths.size() - 1; i++) {
                    int minute = Flight.layover(paths.get(i), paths.get(i + 1));
                    layover += minute;
                }
                return layover;
            }
        }

        public String[] getLayoverStr() {
            if (paths.size() < 2) {
                return new String[1]; //todo
            } else {
                String[] layovers = new String[paths.size() - 1];
                for (int i = 0; i < paths.size() - 1; i++) {
                    int minute = Flight.layover(paths.get(i), paths.get(i + 1));
                    layovers[i] = String.format("LAYOVER %s at %s",
                            MyHelper.minuteToHourMinute(minute), paths.get(i).getDestination().getName());
                }
                return layovers;
            }
        }

        // flight time
        public int getFlightTime() {
            return paths.stream().mapToInt(Flight::getDuration).sum();
        }

        @Override
        public String toString() {
            return "FlightPath{" +
                    "paths=" + Arrays.toString(paths.toArray()) +
                    '}';
        }

        public void showInfo() {
            System.out.printf("%-18s%d%n", "Legs:", paths.size());
            System.out.printf("%-18s%s%n", "Total Duration:", MyHelper.minuteToHourMinute(getTotalDuration()));
            System.out.printf("%-18s$%.2f%n", "Total Cost:", getTotalCost());
            System.out.println("-------------------------------------------------------------");
            System.out.println("ID   Cost      Departure   Arrival     Source --> Destination");
            System.out.println("-------------------------------------------------------------");
            String[] layovers = getLayoverStr();
            for (int i = 0; i < paths.size(); i++) {
                Flight flight = paths.get(i);
                System.out.printf("%4d $%8.2f %9s   %9s   %s --> %s%n", flight.getId(),
                        flight.getTicketPrice(),
                        MyHelper.minuteInWeekToTimeStrSimple(flight.getDepartureTime()),
                        MyHelper.minuteInWeekToTimeStrSimple(flight.getArriveTime()),
                        flight.getSource().getName(), flight.getDestination().getName());
                if (i < paths.size() - 1) {
                    System.out.println(layovers[i]);
                }
            }
        }
    }


    private static void findBest1() {
    }

    private static List<FlightPath> findAllPaths(Location start, Location end, List<Flight> flightList, List<Location> locationList) {
        //prepare
        Map<Location, List<Flight>> map = new HashMap<>();
        Queue<FlightPath> queue = new ArrayDeque<>();
        List<FlightPath> results = new ArrayList<>();
        for (Flight flight : flightList) {
            List<Flight> list = map.getOrDefault(flight.getSource(), new ArrayList<>());
            if (!list.contains(flight)) {
                list.add(flight);
            }
            map.put(flight.getSource(), list);
        }
        for (Flight flight : map.getOrDefault(start, new ArrayList<>())) {
            FlightPath fp = new FlightPath();
            fp.addFlight(flight);
            if (fp.getLastLocation().equals(end)) {
                results.add(fp);
            } else {
                //todo already arrive?
                queue.add(fp);
            }
        }
        for (int i = 0; i < 3; i++) {
            int size = queue.size();
            for (int j = 0; j < size; j++) {
                FlightPath tfp = queue.poll();
                Location des = tfp.getLastLocation();
                for (Flight flight : map.getOrDefault(des, new ArrayList<>())) {
                    FlightPath fp = new FlightPath(tfp);
                    fp.addFlight(flight);
                    if (fp.getLastLocation().equals(end)) {
                        results.add(fp);
                    } else {
                        //todo already arrive?
                        queue.add(fp);
                    }
                }
            }
        }
        return results.stream().filter(fp -> fp.getLastLocation().equals(end)).collect(Collectors.toList());
    }


    public static void sortN(String[] words, List<Flight> flightList, List<Location> locationList) {
        String[] params = checkParams(words, locationList);
        if (params == null) {
            return;
        }
        Location start = MyHelper.findLocationByName(locationList, params[0]);
        Location end = MyHelper.findLocationByName(locationList, params[1]);
        //???
        if (start == null) {
            System.out.println("Starting location not found.");
            return;
        }
        if (end == null) {
            System.out.println("Ending location not found.");
            return;
        }
        int n = 0;
        if (words.length >= 4 && words[3].equalsIgnoreCase("sort")) {
            if (words.length >= 5) {
                try {
                    n = Integer.parseInt(words[4]);
                } catch (NumberFormatException e) {
//                    e.printStackTrace();
                }
            }
        }
        List<FlightPath> list = findAllPaths(start, end, flightList, locationList);
        Collections.sort(list, Comparator.comparing(FlightPath::getTotalDuration));
        n = Math.min(n, list.size() - 1);
        if (list.size() == 0) {
            System.out.printf("Sorry, no flights with 3 or less stopovers are available from %s to %s.%n",
                    start.getName(), end.getName());
        } else {
            list.get(n).showInfo();
        }

    }

    // read source, destination, and property
    // if invalid, return null and print message
    private static String[] checkParams(String[] words, List<Location> locationList) {
        Location source = MyHelper.findLocationByName(locationList, words[1]);
        if (source == null) {
            System.out.println("Starting location not found.");
            return null;
        }
        Location ending = MyHelper.findLocationByName(locationList, words[1]);
        if (ending == null) {
            System.out.println("Ending location not found.");
            return null;
        }
        if (words.length >= 4) {
            if (!words[3].equalsIgnoreCase("sort") &&
                    !words[3].equalsIgnoreCase("cost") &&
                    !words[3].equalsIgnoreCase("duration") &&
                    !words[3].equalsIgnoreCase("stopovers") &&
                    !words[3].equalsIgnoreCase("layover") &&
                    !words[3].equalsIgnoreCase("flight_time")) {
                System.out.println("Invalid sorting property: must be either cost, duration, stopovers, layover, or flight_time.");
                return null;
            }
            return new String[]{words[1], words[2], words[3]};
        } else {
            return new String[]{words[1], words[2]};
        }
    }

    // sort by property
    public static void sortByProperty(String[] words, List<Flight> flightList, List<Location> locationList) {
        String[] params = checkParams(words, locationList);
        if (params == null) {
            return;
        }
        Location start = MyHelper.findLocationByName(locationList, params[0]);
        Location end = MyHelper.findLocationByName(locationList, params[1]);
        //???
        if (start == null) {
            System.out.println("Starting location not found.");
            return;
        }
        if (end == null) {
            System.out.println("Ending location not found.");
            return;
        }
        String property = params[2];
        List<FlightPath> list = findAllPaths(start, end, flightList, locationList);
//        System.out.println(list);
        Comparator<FlightPath> costP = (o1, o2) -> {
            double o1val = o1.getTotalCost();
            double o2val = o2.getTotalCost();
            if (o1val == o2val) {
                return o1.getTotalDuration() - o2.getTotalDuration();
            } else {
                return o1val > o2val ? 1 : -1;
            }
        };
        Comparator<FlightPath> durationP = (o1, o2) -> {
            double o1val = o1.getTotalDuration();
            double o2val = o2.getTotalDuration();
            if (o1val == o2val) {
                return Double.compare(o1.getTotalCost(), o2.getTotalCost());
            } else {
                return o1val > o2val ? 1 : -1;
            }
        };
        Comparator<FlightPath> stopoverP = (o1, o2) -> {
            int o1val = o1.getStopovers();
            int o2val = o2.getStopovers();
            if (o1val == o2val) {
                if (Double.compare(o1.getTotalDuration(), o2.getTotalDuration()) == 0) {
                    return Double.compare(o1.getTotalCost(), o2.getTotalCost());
                } else {
                    return Double.compare(o1.getTotalDuration(), o2.getTotalDuration());
                }
            } else {
                return o1val > o2val ? 1 : -1;
            }
        };
        Comparator<FlightPath> layoverP = (o1, o2) -> {
            int o1val = o1.getLayoverTime();
            int o2val = o2.getLayoverTime();
            if (o1val == o2val) {
                if (Double.compare(o1.getTotalDuration(), o2.getTotalDuration()) == 0) {
                    return Double.compare(o1.getTotalCost(), o2.getTotalCost());
                } else {
                    return Double.compare(o1.getTotalDuration(), o2.getTotalDuration());
                }
            } else {
                return o1val > o2val ? 1 : -1;
            }
        };
        Comparator<FlightPath> flightTimeP = (o1, o2) -> {
            int o1val = o1.getFlightTime();
            int o2val = o2.getFlightTime();
            if (o1val == o2val) {
                if (Double.compare(o1.getTotalDuration(), o2.getTotalDuration()) == 0) {
                    return Double.compare(o1.getTotalCost(), o2.getTotalCost());
                } else {
                    return Double.compare(o1.getTotalDuration(), o2.getTotalDuration());
                }
            } else {
                return o1val > o2val ? 1 : -1;
            }
        };
        if (property.equalsIgnoreCase("cost")) {
            Collections.sort(list, costP);
        } else if (property.equalsIgnoreCase("duration")) {
            Collections.sort(list, durationP);
        } else if (property.equalsIgnoreCase("stopovers")) {
            Collections.sort(list, stopoverP);
        } else if (property.equalsIgnoreCase("layover")) {
            Collections.sort(list, layoverP);
        } else if (property.equalsIgnoreCase("flight_time")) {
            Collections.sort(list, flightTimeP);
        }
        if (list.size() == 0) {
            System.out.printf("Sorry, no flights with 3 or less stopovers are available from %s to %s.%n",
                    start.getName(), end.getName());
        } else {
            list.get(0).showInfo();
        }
    }
}
