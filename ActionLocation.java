import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ActionLocation {
    // list all locations
    public static void listAllLocations(List<Location> locationList) {
        List<Location> tempList = new ArrayList<>(locationList);
        Collections.sort(tempList);
        System.out.printf("Locations (%d):%n", tempList.size());
        if (locationList.size() > 0) {
            List<String> names = tempList.stream().map(Location::getName).collect(Collectors.toList());
            System.out.println(String.join(", ", names));
        } else {
            System.out.println("(None)");
        }
    }

    // inner function, find location by name
    private static Location getLocationByName(String[] words, List<Location> locationList) {
        String name = words[1];
        Location location = MyHelper.findLocationByName(locationList, name);
        if (location == null) {
            System.out.println("Invalid location name.");
        }
        return location;
    }

    // inner function, find location by name, different error message
    private static Location getLocationByName2(String[] words, List<Location> locationList) {
        String name = words[1];
        Location location = MyHelper.findLocationByName(locationList, name);
        if (location == null) {
            System.out.println("This location does not exist in the system.");
        }
        return location;
    }

    // add location
    public static int addLocation(String[] words, List<Location> locationList, int locationId) {
        if (words.length < 6) {
            System.out.println("Usage:   LOCATION ADD <name> <lat> <long> <demand_coefficient>");
            System.out.println("Example: LOCATION ADD Sydney -33.847927 150.651786 0.2");
            return -1;
        }
        String name = words[2];
        String latS = words[3];
        String lonS = words[4];
        String coefficientS = words[5];
        if (MyHelper.findLocationByName(locationList, name) != null) {
            System.out.println("This location already exists.");
            return -1;
        }
        double lat;
        try {
            lat = Double.parseDouble(latS);
            if (lat < -85 || lat > 85) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid latitude. It must be a number of degrees between -85 and +85.");
            return -1;
        }
        double lon;
        try {
            lon = Double.parseDouble(lonS);
            if (lon < -180 || lon > 180) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid longitude. It must be a number of degrees between -180 and +180.");
            return -1;
        }
        double coefficient;
        try {
            coefficient = Double.parseDouble(coefficientS);
            if (coefficient < -1 || coefficient > 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid demand coefficient. It must be a number between -1 and +1.");
            return -1;
        }
        Location location = new Location(name, lat, lon, coefficient);
        locationList.add(location);
        System.out.printf("Successfully added location %s.%n", name);
        return locationId + 1;
    }

    // import locations from file
    public static int importFile(String[] words, List<Location> locationList,
                                 int locationId) {
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
                String[] infos = Location.fromFileStr(line);
                // name, latitude, longitude, coefficient
                if (infos == null || MyHelper.findLocationByName(locationList, infos[0]) != null) {
                    err++;
                } else {
                    double latitude = Double.parseDouble(infos[1]);
                    double longitude = Double.parseDouble(infos[2]);
                    double coefficient = Double.parseDouble(infos[3]);
                    boolean valid = true;
                    if (latitude < -85 || latitude > 85) {
                        valid = false;
                    }
                    if (longitude < -180 || longitude > 180) {
                        valid = false;
                    }
                    if (coefficient < -1 || coefficient > 1) {
                        valid = false;
                    }
                    if (valid) {
                        Location location = new Location(infos[0], latitude, longitude, coefficient);
                        locationId++;
                        success++;
                        locationList.add(location);
                    } else {
                        err++;
                    }
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            System.out.println("Error reading file.");
            return -1;
        }
        if (success == 1) {
            System.out.println("Imported 1 location.");
        } else {
            System.out.printf("Imported %d locations.%n", success);
        }
        if (err == 1) {
            System.out.println("1 line was invalid.");
        } else if (err > 1) {
            System.out.printf("%d lines were invalid.%n", err);
        }
        return locationId;
    }

    // export locations to file
    public static void exportFile(String[] words, List<Location> locationList) {
        if (words.length < 3) {
            System.out.println("Error writing file.");
            return;
        }
        String filename = words[2];
        List<Location> tempList = new ArrayList<>(locationList);
        Collections.sort(tempList);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for (Location location : tempList) {
                bw.write(location.toFileStr());
                bw.newLine();
                bw.flush();
            }
        } catch (IOException e) {
            System.out.println("Error writing file.");
            return;
        }
        if (locationList.size() == 1) {
            System.out.println("Exported 1 location.");
        } else if (locationList.size() > 1) {
            System.out.printf("Exported %d locations.%n", locationList.size());
        }
    }

    // view location
    public static void viewLocation(String[] words, List<Location> locationList) {
        Location location = getLocationByName(words, locationList);
        if (location != null) {
            location.showFullInfo();
        }
    }

    private static void showFlightsInfo(String name, List<Location.FlightInfo> infoList) {
        System.out.println(name);
        for (int i = 0; i < 55; i++) {
            System.out.print("-");
        }
        System.out.println();
        Location.FlightInfo.showInfoHeader();
        for (int i = 0; i < 55; i++) {
            System.out.print("-");
        }
        System.out.println();
        if (infoList.size() > 0) {
            for (Location.FlightInfo info : infoList) {
                info.showInfo();
            }
        } else {
        }
    }

    // schedule
    public static void scheduleLocation(String[] words, List<Location> locationList) {
        Location location = getLocationByName2(words, locationList);
        if (location != null) {
            List<Location.FlightInfo> infoList = location.getScheduleInfos();
            showFlightsInfo(location.getName(), infoList);
        }
    }

    // arrival
    public static void arrivalsLocation(String[] words, List<Location> locationList) {
        Location location = getLocationByName2(words, locationList);
        if (location != null) {
            List<Location.FlightInfo> infoList = location.getArrivalsInfos();
            showFlightsInfo(location.getName(), infoList);
        }
    }

    // departure
    public static void departuresLocation(String[] words, List<Location> locationList) {
        Location location = getLocationByName2(words, locationList);
        if (location != null) {
            List<Location.FlightInfo> infoList = location.getDeparturesInfos();
            showFlightsInfo(location.getName(), infoList);
        }
    }
}
