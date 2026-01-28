package com.phorestdemo.tests;

import com.microsoft.playwright.*;
import com.phorestdemo.config.ConfigReader;
import org.junit.jupiter.api.*;

public abstract class BaseTest {

    protected static Playwright playwright;
    protected static Browser browser;

    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static void globalSetup() {
        playwright = Playwright.create();

        BrowserType browserType;
        switch (ConfigReader.getBrowserType().toLowerCase()) {
            case "firefox":
                browserType = playwright.firefox();
                break;
            case "webkit":
                browserType = playwright.webkit();
                break;
            case "chromium":
            default:
                browserType = playwright.chromium();
        }

        browser = browserType.launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(ConfigReader.isHeadless()));
    }

    @AfterAll
    static void globalTeardown() {
        if (browser != null)
            browser.close();
        if (playwright != null)
            playwright.close();
    }

    @BeforeEach
    void setupTest() {
        context = browser.newContext();
        page = context.newPage();
        page.setDefaultTimeout(ConfigReader.getDefaultTimeout());
    }

    @AfterEach
    void teardownTest() {
        if (context != null)
            context.close();
    }
}
