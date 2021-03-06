package learn.springang.umsportalrestassured;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Map;

import static learn.springang.umsportalrestassured.TestConstants.*;
import static learn.springang.umsportalrestassured.Util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRegisterLoginResetPasswordTest {

    private static String jwtToken;

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
        assertEquals("ROLE_USER", role);
    }

    @Order(2)
    @Test
    void testLoginUser() throws IOException {
        String password = readLoggedPassword(REGISTERED_PASSWORD_PHRASE);

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
        jwtToken = response.header("Jwt-Token");
        assertNotNull(jwtToken);
    }

    @Order(3)
    @Test
    void testResetPassword() {
        final Response response = RestAssured
                .when()
                    .put(USER_PREFIX + "/resetPassword/" + EMAIL)
                .then()
                    .statusCode(STATUS_OK)
                    .contentType(APPLICATION_JSON)
                    .extract()
                    .response();
        String httpStatus = response.jsonPath().getString("httpStatus");
        assertNotNull(httpStatus);
        assertEquals("OK", httpStatus);
    }

    @Order(4)
    @Test
    void testLoginUserAfterReset() throws IOException {
        String password = readLoggedPassword(RESET_PASSWORD_PHRASE);

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
        jwtToken = response.header("Jwt-Token");
        assertNotNull(jwtToken);
    }
}
