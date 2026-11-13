package com.test.qa.automation;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;


class RegistrationTest {

    private static final String REGISTRATION_URL = "https://www.qapractice.com/register";

    private static Playwright playwright;
    private static Browser browser;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)
                        .setSlowMo(1500));
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }


    @Test
    void userCanRegisterWithValidDetails() {
        String uniqueEmail = "playwright+" + System.currentTimeMillis() + "@example.com";
        String validPassword = "Playwright1!";

        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(REGISTRATION_URL);

            page.getByLabel("Email Address").fill(uniqueEmail);
            page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true))
                    .fill(validPassword);
            page.getByLabel("Confirm Password").fill(validPassword);
            page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Register").setExact(true)).click();

            assertThat(page.getByTestId("register-success"))
                    .hasText("Registration Successful");
            assertThat(page.getByText("Your account has been created successfully."))
                    .isVisible();

            page.waitForTimeout(3000);
        }
    }

    @Test
    void userCanRegisterWithValidPasswordAndConfirmation() {
        String uniqueEmail = "playwright" + System.currentTimeMillis() + "@example.com";
        String validPassword = "Playwright1!";

        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(REGISTRATION_URL);

            page.getByLabel("Email Address").fill(uniqueEmail);

            page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true))
                    .fill(validPassword);
            assertThat(page.getByText("At least 8 characters"))
                    .isVisible();
            assertThat(page.getByText("At least 8 characters"))
                    .containsClass("text-success list-group-item");
            assertThat(page.getByText("At least one uppercase letter (A-Z)"))
                    .isVisible();
            assertThat(page.getByText("At least one uppercase letter (A-Z)"))
                    .containsClass("text-success list-group-item");
            assertThat(page.getByText("At least one lowercase letter (a-z)"))
                    .isVisible();
            assertThat(page.getByText("At least one lowercase letter (a-z)"))
                    .containsClass("text-success list-group-item");
            assertThat(page.getByText("At least one number (0-9)"))
                    .isVisible();
            assertThat(page.getByText("At least one number (0-9)"))
                    .containsClass("text-success list-group-item");
            assertThat(page.getByText("At least one special character (!@#$%^&*()_+)"))
                    .isVisible();
            assertThat(page.getByText("At least one special character (!@#$%^&*()_+)"))
                    .containsClass("text-success list-group-item");

            page.getByLabel("Confirm Password")
                    .fill(validPassword);
            assertThat(page.getByLabel("Confirm Password"))
                    .hasValue(validPassword);

            page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Register").setExact(true)).click();
            assertThat(page.getByTestId("register-success"))
                    .hasText("Registration Successful");
            assertThat(page.getByText("Your account has been created successfully."))
                    .isVisible();

            page.waitForTimeout(3000);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test",
            "test@",
            "@gmail.com"
    })
    void userCanNotRegisterWithInvalidEmail(String invalidEmail) {
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(REGISTRATION_URL);

            page.getByLabel("Email Address").fill(invalidEmail);

            page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Register").setExact(true)).click();
            assertThat(page.getByText("Your account has been created successfully."))
                    .not().isVisible();
            assertThat(page.getByText("Register")).isVisible();

        }
    }


    @ParameterizedTest
    @MethodSource("passwordRequirements")
    void passwordRequirementsAreValidated2(List<String> params) {

        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(REGISTRATION_URL);

            String passwordToTest = params.getFirst();
            String validMeaning = params.get(1);
            String label = params.getLast();

            page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true))
                    .fill(passwordToTest);
            assertThat(page.getByText(label))
                    .isVisible();
            assertThat(page.getByText(label))
                    .containsClass("text-danger list-group-item");

            page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true))
                    .fill(validMeaning);
            assertThat(page.getByText(label))
                    .isVisible();
            assertThat(page.getByText(label))
                    .containsClass("text-success list-group-item");

        }
    }

    static List<List<String>> passwordRequirements() {
        return List.of(
                List.of("P8", "Playwright1", "At least 8 characters"),
                List.of("play", "Playwright", "At least one uppercase letter (A-Z)"),
                List.of("PLAY", "PLAYWRIGHt", "At least one lowercase letter (a-z)"),
                List.of("Play?", "Playwright7", "At least one number (0-9)"),
                List.of("pLay8", "Playwright7$", "At least one special character (!@#$%^&*()_+)")
        );
    }

    @Test
    void passwordRequirementsAreValidatedWithLoop() {

        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(REGISTRATION_URL);
            List<List<String>> passwordRequirements = passwordRequirements();
            for (List<String> params : passwordRequirements) {
                String passwordToTest = params.getFirst();
                String validMeaning = params.get(1);
                String label = params.getLast();

                page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true))
                        .fill(passwordToTest);
                assertThat(page.getByText(label))
                        .isVisible();
                assertThat(page.getByText(label))
                        .containsClass("text-danger list-group-item");

                page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true))
                        .fill(validMeaning);
                assertThat(page.getByText(label))
                        .isVisible();
                assertThat(page.getByText(label))
                        .containsClass("text-success list-group-item");

            }
        }
    }

}






