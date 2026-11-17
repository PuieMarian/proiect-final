package ro.proiectfinal;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Metode specifice pentru testarea UI a site-ului https://www.emag.ro.
 * Fiecare metoda executa o singura actiune; verificarile se fac in TestRun.
 *
 * Rulare fara fereastra: mvn test -Dheadless=true
 */
public class SeleniumMethods {

    private static final String EMAG_URL = "https://www.emag.ro";

    private WebDriver driver;
    private WebDriverWait wait;

    // Locatori
    private static final By COOKIE_ACCEPT = By.cssSelector("button.js-accept");
    private static final By SEARCH_INPUT = By.id("searchboxTrigger");
    private static final By SEARCH_BUTTON = By.cssSelector("button.searchbox-submit-button");
    private static final By PRODUCT_CARDS = By.cssSelector("div.card-item");
    private static final By PRODUCT_TITLES = By.cssSelector("a.card-v2-title");
    private static final By PRODUCT_PAGE_TITLE = By.cssSelector("h1.page-title");
    private static final By ADD_TO_CART = By.cssSelector("button.btn-primary.yeahIWantThisProduct, form.main-product-form button[type='submit']");
    private static final By CART_ICON = By.id("my_cart");

    // ------------------------------------------------------------- DRIVER

    public void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized", "--disable-notifications",
                "--lang=ro-RO", "--remote-allow-origins=*");
        if (Boolean.getBoolean("headless")) {
            options.addArguments("--headless=new", "--window-size=1920,1080");
        }
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    // ------------------------------------------------------------ ACTIUNI

    public void openHomePage() {
        driver.get(EMAG_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_INPUT));
    }

    /** Accepta bannerul de cookies daca apare; daca nu apare, nu face nimic. */
    public void acceptCookiesIfPresent() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(COOKIE_ACCEPT));
            btn.click();
        } catch (TimeoutException ignored) {
            // bannerul nu a aparut
        }
    }

    public void searchProduct(String text) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
        input.clear();
        input.sendKeys(text);
        wait.until(ExpectedConditions.elementToBeClickable(SEARCH_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCT_CARDS));
    }

    public void openFirstProduct() {
        List<WebElement> titles = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(PRODUCT_TITLES));
        WebElement first = titles.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", first);
        wait.until(ExpectedConditions.elementToBeClickable(first)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCT_PAGE_TITLE));
    }

    public void addCurrentProductToCart() {
        wait.until(ExpectedConditions.elementToBeClickable(ADD_TO_CART)).click();
    }

    public void openCart() {
        wait.until(ExpectedConditions.elementToBeClickable(CART_ICON)).click();
    }

    // --------------------------------------------------------------- CITIRI

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public int getResultsCount() {
        return driver.findElements(PRODUCT_CARDS).size();
    }

    public List<String> getResultTitles() {
        return driver.findElements(PRODUCT_TITLES).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toList());
    }

    public String getProductPageTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCT_PAGE_TITLE)).getText();
    }
}
