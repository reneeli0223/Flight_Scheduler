import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ActionFlight {

    // list all flights
    public static void listAllFlights(List<Flight> flightList) {
        System.out.println("Flights");
        for (int i = 0; i < 55; i++) {
            System.out.print("-");
        }
        System.out.println();
        Flight.showInfoHeader();
        for (int i = 0; i < 55; i++) {
            System.out.print("-");
        }
        System.out.println();
        List<Flight> tempList = new ArrayList<>(flightList);
        Collections.sort(tempList, (o1, o2) -> {
            if (o1.getDepartureTime() != o2.getDepartureTime()) {
                return Integer.compare(o1.getDepartureTime(), o2.getDepartureTime());
            } else {
                return o1.getSource().getName().compareTo(o2.getSource().getName());
            }
        });
        if (tempList.size() > 0) {
            for (Flight flight : tempList) {
                flight.showInfo();
            }
        } else {
            System.out.println("(None)");
        }
    }

    // inner function, find flight by id
    private static Flight getFlightById(String[] words, List<Flight> flightList) {
        int id;
        try {
            id = Integer.parseInt(words[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Flight ID.");
            return null;
        }
        Flight flight = MyHelper.findFlightById(flightList, id);
        if (flight == null) {
            System.out.println("Invalid Flight ID.");
        }
        return flight;
    }

    // add a flight, if failed return -1, else return new flightId
    public static int addFlight(String[] words, List<Flight> flightList,
                                int flightId, List<Location> locationList) {
        if (words.length < 7) {
            System.out.println("Usage:   FLIGHT ADD <departure time> <from> <to> <capacity>");
            System.out.println("Example: FLIGHT ADD Monday 18:00 Sydney Melbourne 120");
            return -1;
        }
        String date = words[2];
        String hourminute = words[3];
        String start = words[4];
        String end = words[5];
        String space = words[6];
        int departureTime = 0;
        try {
            date = date.substring(0, 1).toUpperCase() + date.substring(1).toLowerCase();
            MyHelper.Weekday weekday = MyHelper.Weekday.valueOf(date);
            departureTime = MyHelper.getMinuteInWeek(weekday, hourminute);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid departure time. Use the format <day_of_week> <hour:minute>, with 24h time.");
            return -1;
        }
        if (start.equalsIgnoreCase(end)) {
            System.out.println("Source and destination cannot be the same place.");
            return -1;
        }
        Location source = MyHelper.findLocationByName(locationList, start);
        if (source == null) {
            System.out.println("Invalid starting location.");
            return -1;
        }
        Location destination = MyHelper.findLocationByName(locationList, end);
        if (destination == null) {
            System.out.println("Invalid ending location.");
            return -1;
        }
        int capacity = 0;
        try {
            capacity = Integer.parseInt(space);
            if (capacity <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid positive integer capacity.");
            return -1;
        }
        Flight flight = new Flight(flightId, departureTime, source, destination, capacity, 0);
        if (checkConflict(flight) == -1) {
            return -1;
        }
        flightList.add(flight);
        source.addDeparture(flight);
        destination.addArrival(flight);
        System.out.printf("Successfully added Flight %d.%n", flightId);
        return flightId + 1;
    }

    // check conflict
    private static int checkConflict(Flight flight) {
        int flightId = flight.getId();
        Location source = flight.getSource();
        Location destination = flight.getDestination();
        Location.FlightInfo departureInfo = new Location.FlightInfo(flightId,
                flight.getDepartureTime(), "Departure to", flight.getDestination().getName());
        Location.FlightInfo arriveInfo = new Location.FlightInfo(flightId,
                flight.getArriveTime(), "Arrival from", flight.getSource().getName());
        List<Location.FlightInfo> sourceDepartures = source.getDeparturesInfos();
        List<Location.FlightInfo> sourceArrivals = source.getArrivalsInfos();
        List<Location.FlightInfo> destinationDepartures = destination.getDeparturesInfos();
        List<Location.FlightInfo> destinationArrivals = destination.getArrivalsInfos();
        Location.FlightInfo check1 = hasConflict(departureInfo, sourceDepartures);
        if (check1 != null) {
            System.out.printf("Scheduling conflict! This flight clashes with Flight %d departing from %s on %s.%n",
                    check1.id, source.getName(), MyHelper.minuteInWeekToTimeStr(check1.time));
            return -1;
        }
        check1 = hasConflict(departureInfo, sourceArrivals);
        if (check1 != null) {
            System.out.printf("Scheduling conflict! This flight clashes with Flight %d arriving at %s on %s.%n",
                    check1.id, source.getName(), MyHelper.minuteInWeekToTimeStr(check1.time));
            return -1;
        }
        check1 = hasConflict(arriveInfo, destinationDepartures);
        if (check1 != null) {
            System.out.printf("Scheduling conflict! This flight clashes with Flight %d departing from %s on %s.%n",
                    check1.id, destination.getName(), MyHelper.minuteInWeekToTimeStr(check1.time));
            return -1;
        }
        check1 = hasConflict(arriveInfo, destinationArrivals);
        if (check1 != null) {
            System.out.printf("Scheduling conflict! This flight clashes with Flight %d arriving at %s on %s.%n",
                    check1.id, destination.getName(), MyHelper.minuteInWeekToTimeStr(check1.time));
            return -1;
        }
        return 0;
    }

    private static Location.FlightInfo hasConflict(Location.FlightInfo info,
                                                   List<Location.FlightInfo> infoList) {
        List<Location.FlightInfo> tempList = new ArrayList<>(infoList);
        tempList.add(info);
        Collections.sort(tempList);
        int size = tempList.size(), index = tempList.indexOf(info);
        for (int i = 0; i < size; i++) {
            Location.FlightInfo data = tempList.get(i).copy();
            data.minusWeek();
            tempList.add(data);
            data = tempList.get(i).copy();
            data.plusWeek();
            tempList.add(data);
        }
        Collections.sort(tempList);
        if (index == 0) {
            if (tempList.get(size + 1).time - info.time < 60) {
                return tempList.get(size + 1);
            }
            //todo
            if (info.time - tempList.get(size - 1).time < 60) {
                return tempList.get(size - 1);
            }
        } else if (index == size - 1) {
            //todo
            if (tempList.get(2 * size).time - info.time < 60) {
                return tempList.get(2 * size);
            }
            if (info.time - tempList.get(2 * size - 2).time < 60) {
                return tempList.get(2 * size - 2);
            }
        } else {
            if (tempList.get(size + index + 1).time - info.time < 60) {
                return tempList.get(size + index + 1);
            }
            if (info.time - tempList.get(size + index - 1).time < 60) {
                return tempList.get(size + index - 1);
            }
        }
        return null;
    }

    // import flights from file
    public static int importFile(String[] words, List<Flight> flightList,
                                 int flightId, List<Location> locationList) {
        if (words.length < 3) {
            System.out.println("Error reading file.");
            return -1;
        }
        String filename = words[2];
        int err = 0; // invalid num
        int success = 0; // success num
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine();
            while (line != null) {
                String[] infos = Flight.fromFileStr(line);
                // departureTime source destination capacity passengersBooked
                if (infos == null) {
//                    err++;
                } else {
                    int departureTime = Integer.parseInt(infos[0]);
                    Location source = MyHelper.findLocationByName(locationList, infos[1]);
                    Location destination = MyHelper.findLocationByName(locationList, infos[2]);
                    int capacity = Integer.parseInt(infos[3]);
                    int passengersBooked = Integer.parseInt(infos[4]);
                    if (source == null || destination == null) {
                        err++;
                    } else {
                        Flight flight = new Flight(flightId, departureTime, source, destination, capacity, passengersBooked);
                        flightId++;
                        success++;
                        flightList.add(flight);
                        source.addDeparture(flight);
                        destination.addArrival(flight);
                    }
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            System.out.println("Error reading file.");
            return flightId;
        }
        if (success == 1) {
            System.out.println("Imported 1 flight.");
        } else {
            System.out.printf("Imported %d flights.%n", success);
        }
        if (err == 1) {
            System.out.println("1 line was invalid.");
        } else if (err > 1) {
            System.out.printf("%d lines were invalid.%n", err);
        }
        return flightId;
    }

    // export flights to file
    public static void exportFile(String[] words, List<Flight> flightList) {
        if (words.length < 3) {
            System.out.println("Error writing file.");
            return;
        }
        String filename = words[2];
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for (Flight flight : flightList) {
                bw.write(flight.toFileStr());
                bw.newLine();
                bw.flush();
            }
        } catch (IOException e) {
            System.out.println("Error writing file.");
            return;
        }
        if (flightList.size() == 1) {
            System.out.println("Exported 1 flight.");
        } else if (flightList.size() > 1) {
            System.out.printf("Exported %d flights.%n", flightList.size());
        }
    }

    // view flight info
    public static void viewFlight(String[] words, List<Flight> flightList) {
        Flight flight = getFlightById(words, flightList);
        if (flight != null) {
            flight.showFullInfo();
        }
    }

    // book flight
    public static void bookFlight(String[] words, List<Flight> flightList) {
        Flight flight = getFlightById(words, flightList);
        if (flight != null) {
            int bookNum;
            if (words.length == 3) {
                bookNum = 1;
            } else {
                try {
                    bookNum = Integer.parseInt(words[3]);
                    if (bookNum < 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number of passengers to book.");
                    return;
                }
            }
            int canBook = flight.canBook(bookNum);
            double costAll = 0;
            for (int i = 0; i < canBook; i++) {
                costAll += flight.getTicketPrice();
                flight.book();
                if (flight.isFull()) {
                    break;
                }
            }
            System.out.printf("Booked %d passengers on flight %d for a total cost of $%.2f%n",
                    canBook, flight.getId(), costAll);
            if (flight.isFull()) {
                System.out.println("Flight is now full.");
            }
        }
    }

    // remove flight
    public static void removeFlight(String[] words, List<Flight> flightList) {
        Flight flight = getFlightById(words, flightList);
        if (flight != null) {
            boolean result = flightList.remove(flight);
            flight.getSource().removeDeparture(flight);
            flight.getDestination().removeArrival(flight);
            System.out.printf("Removed Flight %d, %s %s --> %s, from the flight schedule.%n",
                    flight.getId(), MyHelper.minuteInWeekToTimeStrSimple(flight.getDepartureTime()),
                    flight.getSource().getName(), flight.getDestination().getName());
        }
    }

    // reset flight
    public static void resetFlight(String[] words, List<Flight> flightList) {
        Flight flight = getFlightById(words, flightList);
        if (flight != null) {
            flight.setPassengersBooked(0);
            System.out.printf("Reset passengers booked to 0 for Flight %d, %s %s --> %s.%n",
                    flight.getId(), MyHelper.minuteInWeekToTimeStrSimple(flight.getDepartureTime()),
                    flight.getSource().getName(), flight.getDestination().getName());
        }
    }
}
