package com.phorestdemo.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class ReceiptPage {
    private final Page page;
    private static final String VOUCHER_CODE_SELECTOR = "p[data-target='stripe-serial.serialSpan']";
    private static final String DONE_BUTTON = "button#application#doneAction";

    public ReceiptPage(Page page, String url) {
        this.page = page;
    }

    public String getVoucherCode() {
        Locator voucherLocator = page.locator(VOUCHER_CODE_SELECTOR);
        voucherLocator.waitFor(); // Wait until visible
        return voucherLocator.textContent().trim();
    }
}
