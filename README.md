# Proiect final – DemoQA API + eMAG Selenium

Proiect Maven (Java 17, TestNG, Selenium 4, RestAssured).

## Structura

```
proiect-final/
├── pom.xml
├── testng.xml
└── src/test/java/ro/proiectfinal/
    ├── ApiMethods.java       # metode API DemoQA BookStore
    ├── SeleniumMethods.java  # metode UI pentru emag.ro
    └── TestRun.java          # flow-uri end-to-end (API + Selenium)
```

## Clase

**ApiMethods** – o metoda per actiune, intoarce `Response`:
`createUser`, `generateToken`, `getUser`, `deleteUser`, `getAllBooks`,
`createBook`, `deleteBook` + helperi de extragere (`extractUserId`, `extractToken`, `extractFirstIsbn`, ...).

**SeleniumMethods** – o metoda per actiune pe emag.ro:
`startBrowser`, `closeBrowser`, `openHomePage`, `acceptCookiesIfPresent`,
`searchProduct`, `openFirstProduct`, `addCurrentProductToCart`, `openCart` + metode de citire
(`getResultsCount`, `getResultTitles`, `getProductPageTitle`, `getCurrentUrl`).

**TestRun** – flow-uri end-to-end, fiecare cu cel putin o metoda API si una Selenium:

1. `flow1_createBook_searchOnEmag_deleteBook`
   create user -> token -> get books -> create book -> verifica in colectia userului
   -> cauta titlul cartii pe eMAG -> verifica rezultate -> delete book -> verifica stergerea.
2. `flow2_emagProduct_thenApiUserAndBook`
   cauta "carte" pe eMAG -> deschide primul produs -> create user -> create book
   -> verifica colectia -> delete user.

Userul creat este sters mereu in `@AfterMethod`, chiar daca testul pica la mijloc.

## Rulare

Cerinte: JDK 17+, Maven, Google Chrome (Selenium Manager descarca singur driverul).

```bash
mvn test                      # cu fereastra de browser
mvn test -Dheadless=true      # fara fereastra
```

## Observatii

- Locatorii din `SeleniumMethods` (ex. `button.js-accept`, `a.card-v2-title`, `h1.page-title`)
  depind de structura HTML a eMAG, care se poate schimba. Daca un test pica la un element,
  inspecteaza pagina (F12) si actualizeaza constanta de locator corespunzatoare.
- eMAG poate afisa captcha sau bloca sesiuni automatizate; in acest caz ruleaza fara `-Dheadless=true`.
- DemoQA este un API public de test si poate fi uneori lent sau indisponibil.
