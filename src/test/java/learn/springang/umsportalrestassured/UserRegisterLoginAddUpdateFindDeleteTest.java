package learn.springang.umsportalrestassured;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Map;

import static learn.springang.umsportalrestassured.TestConstants.*;
import static learn.springang.umsportalrestassured.Util.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRegisterLoginAddUpdateFindDeleteTest {

    private static String bearerToken;
    private static int firstUserId;

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
        firstUserId = response.jsonPath().getInt("id");
        assertTrue(firstUserId > 0);
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

    @Order(4)
    @Test
    void testUpdateUser() {
        Map<String, Object> userParameters = Map.of(
                "currentUsername", USERNAME2,
                "newFirstName", USER2_FIRST_NAME_UPD,
                "newLastName", USER2_LAST_NAME_UPD,
                "newUsername", USERNAME2,
                "newEmail", EMAIL2_UPD,
                "newRole", ROLE_SUPER_ADMIN,
                "newNotLocked", "true",
                "newActive", "true"
        );
        final Response response = RestAssured
                .given()
                    .header(AUTHORIZATION, bearerToken)
                    .params(userParameters)
                .when()
                    .put(USER_PREFIX + "/update")
                .then()
                    .statusCode(STATUS_OK)
                    .contentType(APPLICATION_JSON)
                    .extract()
                    .response();
        String userId = response.jsonPath().getString("userId");
        assertNotNull(userId);
        String firstName = response.jsonPath().getString("firstName");
        assertEquals(USER2_FIRST_NAME_UPD, firstName);
        String lastName = response.jsonPath().getString("lastName");
        assertEquals(USER2_LAST_NAME_UPD, lastName);
        String email = response.jsonPath().getString("email");
        assertEquals(EMAIL2_UPD, email);
        String role = response.jsonPath().getString("role");
        assertEquals(ROLE_SUPER_ADMIN, role);
    }

    @Order(5)
    @Test
    void testFindByUsername() {
        final Response response = RestAssured
                .given()
                    .header(AUTHORIZATION, bearerToken)
                .when()
                    .get(USER_PREFIX + "/find/" + USERNAME)
                .then()
                    .statusCode(STATUS_OK)
                    .contentType(APPLICATION_JSON)
                    .extract()
                    .response();
        String userId = response.jsonPath().getString("userId");
        assertNotNull(userId);
        String firstName = response.jsonPath().getString("firstName");
        assertEquals(USER_FIRST_NAME, firstName);
        String lastName = response.jsonPath().getString("lastName");
        assertEquals(USER_LAST_NAME, lastName);
        String email = response.jsonPath().getString("email");
        assertEquals(EMAIL, email);
        String role = response.jsonPath().getString("role");
        assertEquals(ROLE_USER, role);
    }

    @Order(6)
    @Test
    void testDeleteUser() throws IOException {
        String superAdminPassword = readLoggedPassword(ADDED_USER_PASSWORD_PHRASE);
        Map<String, Object> loginData = Map.of(
                "username", USERNAME2,
                "password", superAdminPassword
        );
        Response response = RestAssured
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
        String username = response.jsonPath().getString("username");
        assertEquals(USERNAME2, username);
        String jwtToken = response.header("Jwt-Token");
        assertNotNull(jwtToken);
        String superAdminBearerToken = "Bearer " + jwtToken;
        RestAssured
                .given()
                    .header(AUTHORIZATION, superAdminBearerToken)
                .when()
                    .delete(USER_PREFIX + "/delete/" + firstUserId)
                .then()
                    .statusCode(STATUS_NO_CONTENT);
    }
}
