package learn.springang.umsportalrestassured;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static learn.springang.umsportalrestassured.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRegisterLoginAddTest {

    private static String bearerToken;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = SERVER_PORT;
    }

    @Order(1)
    @Test
    void testRegisterUser() {
        Map<String, Object> userData = Map.of(
                "firstName", USER_FIRST_NAME,
                "lastName", USER_LAST_NAME,
                "username", USERNAME,
                "email", EMAIL
        );
        final Response response = RestAssured
                .given()
                    .contentType(APPLICATION_JSON)
                    .body(userData)
                .when()
                    .post(USER_PREFIX + "/register")
                .then()
                    .statusCode(STATUS_OK)
                    .contentType(APPLICATION_JSON)
                    .extract()
                    .response();
        String userId = response.jsonPath().getString("userId");
        assertNotNull(userId);
        String role = response.jsonPath().getString("role");
        assertNotNull(role);
        assertEquals(ROLE_USER, role);
    }

    @Order(2)
    @Test
    void testLoginUser() throws IOException {
        String password = readLoggedPassword();

        Map<String, Object> loginData = Map.of(
                "username", USERNAME,
                "password", password
        );
        final Response response = RestAssured
                .given()
                    .contentType(APPLICATION_JSON)
                    .body(loginData)
                .when()
                    .post(USER_PREFIX + "/login")
                .then()
                    .statusCode(STATUS_OK)
                    .contentType(APPLICATION_JSON)
                    .extract()
                    .response();
        String userId = response.jsonPath().getString("userId");
        assertNotNull(userId);
        String jwtToken = response.header("Jwt-Token");
        assertNotNull(jwtToken);
        bearerToken = "Bearer " + jwtToken;
    }

    @Order(3)
    @Test
    void testAddUser() {
        Map<String, Object> userParameters = Map.of(
                "firstName", USER2_FIRST_NAME,
                "lastName", USER2_LAST_NAME,
                "username", USERNAME2,
                "email", EMAIL2,
                "role", ROLE_USER,
                "notLocked", "true",
                "active", "true"
        );
        final Response response = RestAssured
                .given()
                    .header(AUTHORIZATION, bearerToken)
                    .params(userParameters)
                .when()
                    .post(USER_PREFIX + "/add")
                .then()
                    .statusCode(STATUS_OK)
                    .contentType(APPLICATION_JSON)
                    .extract()
                    .response();
        String userId = response.jsonPath().getString("userId");
        assertNotNull(userId);
        String firstName = response.jsonPath().getString("firstName");
        assertEquals(USER2_FIRST_NAME, firstName);
        String lastName = response.jsonPath().getString("lastName");
        assertEquals(USER2_LAST_NAME, lastName);
    }

    private String readLoggedPassword() throws IOException {
        final String userHome = System.getProperty("user.home");
        Path logPath = Paths.get(userHome, "/logs/umsportal/spring.log");
        final String key = "Registered New User password ";
        List<String> lines = Files.readAllLines(logPath)
                .stream().filter(line -> line.contains(key)).collect(Collectors.toList());
        if (lines.isEmpty()) {
            throw new IllegalStateException("Cannot find any lines containing password");
        }
        String lastFoundLine = lines.get(lines.size() - 1);
        return lastFoundLine.substring(lastFoundLine.indexOf(key) + key.length());
    }
}
