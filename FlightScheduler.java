import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FlightScheduler {
    //region singleton
    private static FlightScheduler instance;

    public static void main(String[] args) {
        instance = new FlightScheduler(args);
        instance.run();
    }

    public static FlightScheduler getInstance() {
        return instance;
    }

    private List<Flight> flightList;
    private List<Location> locationList;
    private int flightId;
    private int locationId;

    public FlightScheduler(String[] args) {
        flightList = new ArrayList<>();
        locationList = new ArrayList<>();
    }
    //endregion

    public void run() {
        // Do not use System.exit() anywhere in your code,
        // otherwise it will also exit the auto test suite.
        // Also, do not use static attributes otherwise
        // they will maintain the same values between testcases.

        // START YOUR CODE HERE
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("User: ");
            String line = scanner.nextLine();
            String lineLower = line.toLowerCase();
            if (lineLower.startsWith("flight")) {
                // flight flights
                actionFlight(line);
            } else if (lineLower.startsWith("location") ||
                    lineLower.startsWith("schedule") ||
                    lineLower.startsWith("departures") ||
                    lineLower.startsWith("arrivals")) {
                // location locations
                actionLocation(line);
            } else if (lineLower.startsWith("travel")) {
                actionTravel(line);
            } else if (lineLower.equalsIgnoreCase("help")) {
                printHelpMessage();
            } else if (lineLower.equalsIgnoreCase("exit")) {
                System.out.println("Application closed.");
                break;
            } else {
                System.out.println("Invalid command. Type 'help' for a list of commands.");
            }
            System.out.println();
        }
    }

    private void actionTravel(String command) {
        String[] words = command.split("\\s+");
        if (words[0].equalsIgnoreCase("travel")) {
            if (words.length >= 3) {
                if (words.length == 3 || words[3].equals("sort")) {
                    ActionTravel.sortN(words, flightList, locationList);
                } else {
                    ActionTravel.sortByProperty(words, flightList, locationList);
                }
            } else {
                System.out.println("Usage: TRAVEL <from> <to> [cost/duration/stopovers/layover/flight_time]");
            }
        }
    }

    private void printHelpMessage() {
        System.out.println("FLIGHTS - list all available flights ordered by departure time, then departure location name\n" +
                "FLIGHT ADD <departure time> <from> <to> <capacity> - add a flight\n" +
                "FLIGHT IMPORT/EXPORT <filename> - import/export flights to csv file\n" +
                "FLIGHT <id> - view information about a flight (from->to, departure arrival times, current ticket price, capacity, passengers booked)\n" +
                "FLIGHT <id> BOOK <num> - book a certain number of passengers for the flight at the current ticket price, and then adjust the ticket price to reflect the reduced capacity remaining. If no number is given, book 1 passenger. If the given number of bookings is more than the remaining capacity, only accept bookings until the capacity is full.\n" +
                "FLIGHT <id> REMOVE - remove a flight from the schedule\n" +
                "FLIGHT <id> RESET - reset the number of passengers booked to 0, and the ticket price to its original state.\n" +
                "\n" +
                "LOCATIONS - list all available locations in alphabetical order\n" +
                "LOCATION ADD <name> <lat> <long> <demand_coefficient> - add a location\n" +
                "LOCATION <name> - view details about a location (it's name, coordinates, demand coefficient)\n" +
                "LOCATION IMPORT/EXPORT <filename> - import/export locations to csv file\n" +
                "SCHEDULE <location_name> - list all departing and arriving flights, in order of the time they arrive/depart\n" +
                "DEPARTURES <location_name> - list all departing flights, in order of departure time\n" +
                "ARRIVALS <location_name> - list all arriving flights, in order of arrival time\n" +
                "\n" +
                "TRAVEL <from> <to> [sort] [n] - list the nth possible flight route between a starting location and destination, with a maximum of 3 stopovers. Default ordering is for shortest overall duration. If n is not provided, display the first one in the order. If n is larger than the number of flights available, display the last one in the ordering.\n" +
                "\n" +
                "can have other orderings:\n" +
                "TRAVEL <from> <to> cost - minimum current cost\n" +
                "TRAVEL <from> <to> duration - minimum total duration\n" +
                "TRAVEL <from> <to> stopovers - minimum stopovers\n" +
                "TRAVEL <from> <to> layover - minimum layover time\n" +
                "TRAVEL <from> <to> flight_time - minimum flight time\n" +
                "\n" +
                "HELP - outputs this help string.\n" +
                "EXIT - end the program.");
    }

    // commands of LOCATION
    private void actionLocation(String command) {
        String[] words = command.split("\\s+");
        if (words[0].equalsIgnoreCase("location")) {
            if (words.length >= 2) {
                if (words[1].equals("add")) {
                    int result = ActionLocation.addLocation(words, locationList, locationId);
                    locationId = result > 0 ? result : locationId;
                } else if (words[1].equals("import")) {
                    int result = ActionLocation.importFile(words, locationList, locationId);
                    locationId = result > 0 ? result : locationId;
                } else if (words[1].equals("export")) {
                    ActionLocation.exportFile(words, locationList);
                } else {
                    ActionLocation.viewLocation(words, locationList);
                }
            } else {
                System.out.println("Usage:");
                System.out.println("LOCATION <name>");
                System.out.println("LOCATION ADD <name> <latitude> <longitude> <demand_coefficient>");
                System.out.println("LOCATION IMPORT/EXPORT <filename>");
            }
        } else if (words[0].equalsIgnoreCase("locations")) {
            ActionLocation.listAllLocations(locationList);
        } else if (words[0].equalsIgnoreCase("schedule")) {
            ActionLocation.scheduleLocation(words, locationList);
        } else if (words[0].equalsIgnoreCase("departures")) {
            ActionLocation.departuresLocation(words, locationList);
        } else if (words[0].equalsIgnoreCase("arrivals")) {
            ActionLocation.arrivalsLocation(words, locationList);
        } else {
            System.out.println("Invalid command. Type 'help' for a list of commands.");
        }
    }

    // commands of FLIGHT/FLIGHTS
    private void actionFlight(String command) {
        String[] words = command.split("\\s+");
        if (words[0].equalsIgnoreCase("flight")) {
            if (words.length >= 2) {
                if (words[1].equals("add")) {
                    int result = ActionFlight.addFlight(words, flightList, flightId, locationList);
                    flightId = result > 0 ? result : flightId;
                } else if (words[1].equals("import")) {
                    int result = ActionFlight.importFile(words, flightList, flightId, locationList);
                    flightId = result > 0 ? result : flightId;
                } else if (words[1].equals("export")) {
                    ActionFlight.exportFile(words, flightList);
                } else {
                    if (words.length > 2) {
                        if (words[2].equalsIgnoreCase("book")) {
                            ActionFlight.bookFlight(words, flightList);
                        } else if (words[2].equalsIgnoreCase("remove")) {
                            ActionFlight.removeFlight(words, flightList);
                        } else if (words[2].equalsIgnoreCase("reset")) {
                            ActionFlight.resetFlight(words, flightList);
                        } else {
                            ActionFlight.viewFlight(words, flightList);
                        }
                    } else {
                        ActionFlight.viewFlight(words, flightList);
                    }
                }
            } else {
                System.out.println("Usage:");
                System.out.println("FLIGHT <id> [BOOK/REMOVE/RESET] [num]");
                System.out.println("FLIGHT ADD <departure time> <from> <to> <capacity>");
                System.out.println("FLIGHT IMPORT/EXPORT <filename>");
            }
        } else if (words[0].equalsIgnoreCase("flights")) {
            ActionFlight.listAllFlights(flightList);
        } else {
            System.out.println("Invalid command. Type 'help' for a list of commands.");
        }
    }


}
