package ro.proiectfinal;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Metode specifice pentru API-ul DemoQA BookStore (https://demoqa.com/swagger/).
 * Fiecare metoda executa o singura actiune si intoarce Response-ul,
 * iar verificarile (assert) se fac in clasa TestRun.
 */
public class ApiMethods {

    private static final String BASE_URL = "https://demoqa.com";

    private RequestSpecification spec() {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .log().ifValidationFails();
    }

    private RequestSpecification authSpec(String token) {
        return spec().header("Authorization", "Bearer " + token);
    }

    // ---------------------------------------------------------------- USER

    /** POST /Account/v1/User -> 201 si userID in body. */
    public Response createUser(String userName, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("userName", userName);
        body.put("password", password);
        return spec().body(body).post("/Account/v1/User");
    }

    /** POST /Account/v1/GenerateToken -> 200 si token in body. */
    public Response generateToken(String userName, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("userName", userName);
        body.put("password", password);
        return spec().body(body).post("/Account/v1/GenerateToken");
    }

    /** GET /Account/v1/User/{userId} -> detalii user + lista cartilor lui. */
    public Response getUser(String userId, String token) {
        return authSpec(token).get("/Account/v1/User/" + userId);
    }

    /** DELETE /Account/v1/User/{userId} -> 204. */
    public Response deleteUser(String userId, String token) {
        return authSpec(token).delete("/Account/v1/User/" + userId);
    }

    // ---------------------------------------------------------------- BOOKS

    /** GET /BookStore/v1/Books -> toate cartile din magazin. */
    public Response getAllBooks() {
        return spec().get("/BookStore/v1/Books");
    }

    /** POST /BookStore/v1/Books -> 201, adauga o carte in colectia userului. */
    public Response createBook(String userId, String isbn, String token) {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("collectionOfIsbns", List.of(Map.of("isbn", isbn)));
        return authSpec(token).body(body).post("/BookStore/v1/Books");
    }

    /** DELETE /BookStore/v1/Book -> 204, sterge o carte din colectia userului. */
    public Response deleteBook(String userId, String isbn, String token) {
        Map<String, String> body = new HashMap<>();
        body.put("userId", userId);
        body.put("isbn", isbn);
        return authSpec(token).body(body).delete("/BookStore/v1/Book");
    }

    // -------------------------------------------------------------- HELPERI

    public String extractUserId(Response createUserResponse) {
        return createUserResponse.jsonPath().getString("userID");
    }

    public String extractToken(Response tokenResponse) {
        return tokenResponse.jsonPath().getString("token");
    }

    public String extractFirstIsbn(Response allBooksResponse) {
        return allBooksResponse.jsonPath().getString("books[0].isbn");
    }

    public String extractFirstTitle(Response allBooksResponse) {
        return allBooksResponse.jsonPath().getString("books[0].title");
    }

    public List<String> extractUserIsbns(Response getUserResponse) {
        return getUserResponse.jsonPath().getList("books.isbn");
    }
}
