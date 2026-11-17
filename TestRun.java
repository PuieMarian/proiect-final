package ro.proiectfinal;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Flow-uri end-to-end. Fiecare flow combina pasi API (DemoQA BookStore)
 * cu pasi Selenium pe eMAG, folosind aceleasi date (titlul unei carti
 * obtinut din API este cautat apoi pe emag.ro).
 */
public class TestRun {

    private static final String PASSWORD = "Parola@12345!";

    private ApiMethods api;
    private SeleniumMethods selenium;

    // date partajate intre pasii unui flow (resetate la fiecare test)
    private String userId;
    private String token;

    @BeforeMethod
    public void setUp() {
        api = new ApiMethods();
        selenium = new SeleniumMethods();
        userId = null;
        token = null;
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        selenium.closeBrowser();
        // curatenie: stergem userul creat, chiar daca testul a picat la mijloc
        if (userId != null && token != null) {
            api.deleteUser(userId, token);
        }
    }

    // ------------------------------------------------------------- HELPERI

    private String uniqueUserName() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void createUserAndLogin() {
        String userName = uniqueUserName();

        Response created = api.createUser(userName, PASSWORD);
        Assert.assertEquals(created.statusCode(), 201, "Create User a esuat: " + created.asString());
        userId = api.extractUserId(created);
        Assert.assertNotNull(userId, "userID lipseste din raspuns");

        Response tokenResp = api.generateToken(userName, PASSWORD);
        Assert.assertEquals(tokenResp.statusCode(), 200, "Generate Token a esuat");
        token = api.extractToken(tokenResp);
        Assert.assertNotNull(token, "token lipseste din raspuns");
    }

    // ---------------------------------------------------------------- FLOW 1

    /**
     * Flow 1: user nou -> carte adaugata prin API -> cautam cartea pe eMAG
     * -> verificam rezultatele -> stergem cartea prin API.
     */
    @Test(description = "Flow 1: API create user + create book, Selenium search pe eMAG, API delete book")
    public void flow1_createBook_searchOnEmag_deleteBook() {
        // --- API: pregatire date
        createUserAndLogin();

        Response books = api.getAllBooks();
        Assert.assertEquals(books.statusCode(), 200);
        String isbn = api.extractFirstIsbn(books);
        String title = api.extractFirstTitle(books);
        Assert.assertNotNull(isbn);
        Assert.assertNotNull(title);

        Response added = api.createBook(userId, isbn, token);
        Assert.assertEquals(added.statusCode(), 201, "Create Book a esuat: " + added.asString());

        Response userAfterAdd = api.getUser(userId, token);
        Assert.assertEquals(userAfterAdd.statusCode(), 200);
        Assert.assertTrue(api.extractUserIsbns(userAfterAdd).contains(isbn),
                "Cartea nu apare in colectia userului");

        // --- Selenium: cautam aceeasi carte pe eMAG
        selenium.startBrowser();
        selenium.openHomePage();
        selenium.acceptCookiesIfPresent();
        selenium.searchProduct(title);

        Assert.assertTrue(selenium.getCurrentUrl().contains("emag.ro"),
                "Nu mai suntem pe emag.ro");
        Assert.assertTrue(selenium.getResultsCount() > 0,
                "Cautarea pentru '" + title + "' nu a returnat rezultate");

        // --- API: curatenie + verificare stergere
        Response deleted = api.deleteBook(userId, isbn, token);
        Assert.assertEquals(deleted.statusCode(), 204, "Delete Book a esuat");

        Response userAfterDelete = api.getUser(userId, token);
        Assert.assertFalse(api.extractUserIsbns(userAfterDelete).contains(isbn),
                "Cartea mai apare dupa stergere");
    }

    // ---------------------------------------------------------------- FLOW 2

    /**
     * Flow 2: Selenium deschide primul produs din cautarea pe eMAG, apoi
     * API-ul creeaza user + carte si verifica ca datele sunt consistente.
     * Demonstreaza ordinea inversa (Selenium inainte de API).
     */
    @Test(description = "Flow 2: Selenium deschide produs pe eMAG, apoi API create user/book + delete user")
    public void flow2_emagProduct_thenApiUserAndBook() {
        // --- Selenium
        selenium.startBrowser();
        selenium.openHomePage();
        selenium.acceptCookiesIfPresent();
        selenium.searchProduct("carte");
        selenium.openFirstProduct();

        String productTitle = selenium.getProductPageTitle();
        Assert.assertFalse(productTitle.isBlank(), "Titlul produsului este gol");
        Assert.assertTrue(selenium.getCurrentUrl().contains("emag.ro"));

        // --- API
        createUserAndLogin();

        String isbn = api.extractFirstIsbn(api.getAllBooks());
        Response added = api.createBook(userId, isbn, token);
        Assert.assertEquals(added.statusCode(), 201);

        Response user = api.getUser(userId, token);
        Assert.assertEquals(user.statusCode(), 200);
        Assert.assertEquals(api.extractUserIsbns(user).size(), 1,
                "Userul ar trebui sa aiba exact o carte");

        // stergerea userului se face si in tearDown; aici o verificam explicit
        Response deletedUser = api.deleteUser(userId, token);
        Assert.assertEquals(deletedUser.statusCode(), 204, "Delete User a esuat");
        userId = null; // nu mai incercam stergerea in tearDown
    }
}
