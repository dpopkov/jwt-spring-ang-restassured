package learn.springang.umsportalrestassured;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Util {

    public static final String REGISTERED_PASSWORD_PHRASE = "Registered New User password ";
    public static final String ADDED_USER_PASSWORD_PHRASE = "Added New User password ";
    public static final String RESET_PASSWORD_PHRASE = "Password reset to ";

    private static final String LOG_FILE_NAME_IN_HOME_DIR = "/logs/umsportal/spring.log";

    public static String readLoggedPassword(String phrasePrecedingPassword) throws IOException {
        final String userHome = System.getProperty("user.home");
        Path logPath = Paths.get(userHome, LOG_FILE_NAME_IN_HOME_DIR);
        List<String> lines = Files.readAllLines(logPath)
                .stream().filter(line -> line.contains(phrasePrecedingPassword)).collect(Collectors.toList());
        if (lines.isEmpty()) {
            throw new IllegalStateException("Cannot find any lines containing password");
        }
        String lastFoundLine = lines.get(lines.size() - 1);
        return lastFoundLine.substring(lastFoundLine.indexOf(phrasePrecedingPassword) + phrasePrecedingPassword.length());
    }
}
