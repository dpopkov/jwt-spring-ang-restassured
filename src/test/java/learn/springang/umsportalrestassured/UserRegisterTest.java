package learn.springang.umsportalrestassured;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static learn.springang.umsportalrestassured.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserRegisterTest {

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = SERVER_PORT;
    }

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
}
