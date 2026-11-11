package com.test.qa.automation;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginFormTest {
    private static final String LOGINFORM_URL = "https://www.qapractice.com/practice-login-form";

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
    void UserCanLogInWithValidCredentialsTest() {
        String uniqueEmail = "user@premiumbank.com";
        String validPassword = "Bank@123";

        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(LOGINFORM_URL);

            page.getByTestId("login-email")
                    .fill(uniqueEmail);
            page.getByTestId("login-password")
                    .fill(validPassword);

            page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Sign in").setExact(true)).click();
            assertThat(page.getByTestId("login-success"))
                    .hasText("Login Successful! Welcome to Premium Banking.");
            assertThat(page.getByTestId("login-success"))
                    .isVisible();
        }
    }

    @Test
    void UserCanNotLogInWithInvalidCredentialsTest() {

        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(LOGINFORM_URL);

            for (int i = 0; i < incorrectLoginCredentials().size(); i++) {
                List<String> params = incorrectLoginCredentials().get(i);
                String emailToTest = params.getFirst();
                String passwordToTest = params.getLast();

                page.getByTestId("login-email")
                        .fill(emailToTest);
                page.getByTestId("login-password")
                        .fill(passwordToTest);

                page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Sign in").setExact(true)).click();
                assertThat(page.getByTestId("login-error"))
                        .hasText("Invalid email id and password");
                assertThat(page.getByTestId("login-error"))
                        .isVisible();
            }
        }
    }

    static List<List<String>> incorrectLoginCredentials() {
        return List.of(
                List.of("use@premiumbank.com", "Bank123"),
                List.of("user@premumbank.com", "Bank@123"),
                List.of("user@premiumbank.com", "bank@125"),
                List.of("use@premumbank.com", "Bak@123")
        );
    }


    @ParameterizedTest
    @MethodSource("incorrectCredentials")
    void UserCanNotLogInWithInvalidCredentialsTest2(List<String> params) {
        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(LOGINFORM_URL);

            String emailToTest = params.getFirst();
            String passwordToTest = params.getLast();

            page.getByTestId("login-email")
                    .fill(emailToTest);
            page.getByTestId("login-password")
                    .fill(passwordToTest);

            page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Sign in").setExact(true)).click();
            assertThat(page.getByTestId("login-error"))
                    .hasText("Invalid email id and password");
            assertThat(page.getByTestId("login-error"))
                    .isVisible();
            page.waitForTimeout(1500);
        }
    }

    static Stream<List<String>> incorrectCredentials() {
        return Stream.of(
                List.of("use@premiumbank.com", "Bank123"),
                List.of("user@premumbank.com", "Bank@123"),
                List.of("user@premiumbank.com", "bank@125"),
                List.of("use@premumbank.com", "Bak@123")
        );
    }
}
