package com.phorestdemo.pages;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class SummaryPage {
    private final Page page;

    // Confirmation selectors
    private static final String CONFIRM_VOUCHER_VALUE_TEXT = "p#confirm-voucher-value";
    private static final String CONFIRM_TOTAL_COST_VALUE = "p#confirm-total-amount";
    private static final String SENDER_EMAIL = "p#confirm-purchaser-email";
    private static final String RECIPIENT_EMAIL = "p#confirm-recipient-email";
    private static final String CONFIRM_DETAILS_BUTTON = "button[data-action='confirm#confirmAction']";
    private static final String STRIPE_CARD_IFRAME = "iframe[src*='elements-inner-card']";
    private static final String PAY_BUTTON = "button[data-action='stripe-purchase#confirmPayment']";

    public SummaryPage(Page page, String url) {
        this.page = page;
    }

    private FrameLocator getCardFrame() {
        return page.frameLocator(STRIPE_CARD_IFRAME);
    }

    public String getSenderEmailText() {
        return page.locator(SENDER_EMAIL).textContent().trim();
    }

    public String getRecipientEmailText() {
        return page.locator(RECIPIENT_EMAIL).textContent().trim();
    }

    public String getConfirmVoucherValue() {
        String value = page.locator(CONFIRM_VOUCHER_VALUE_TEXT).textContent().trim();
        value = value.replaceAll("[^0-9.]", "");
        double number = Double.parseDouble(value);
        return String.valueOf((int) number);
    }

    public String getConfirmTotalCost() {
        String value = page.locator(CONFIRM_TOTAL_COST_VALUE).textContent().trim();
        value = value.replaceAll("[^0-9.]", "");
        double number = Double.parseDouble(value);
        return String.valueOf((int) number);
    }

    public Locator getConfirmDetailsButton() {
        return page.locator(CONFIRM_DETAILS_BUTTON);
    }

    public Locator getPayButton() {
        return page.locator(PAY_BUTTON);
    }

    public void clickConfirmDetailsButton() {
        getConfirmDetailsButton().click();
    }

    public void enterCardNumber(String cardNumber) {
        getCardFrame().getByRole(AriaRole.TEXTBOX,
                new FrameLocator.GetByRoleOptions()
                        .setName("Credit or debit card number")
        ).fill(cardNumber);
    }

    public void enterExpiry(String expiry) {
        getCardFrame().getByRole(AriaRole.TEXTBOX,
                new FrameLocator.GetByRoleOptions()
                        .setName("Credit or debit card expiration date")
        ).fill(expiry);
    }

    public void enterCVC(String cvc) {
        getCardFrame().getByRole(AriaRole.TEXTBOX,
                new FrameLocator.GetByRoleOptions()
                        .setName("Credit or debit card CVC/CVV")
        ).fill(cvc);
    }

    // Convenience method: fill all payment fields in correct order
    public void enterPaymentDetails(String cardNumber, String expiry, String cvc) {
        enterCardNumber(cardNumber); // MUST be first
        enterExpiry(expiry);          // now iframe exists
        enterCVC(cvc);                // now iframe exists
    }

    public void clickPayButton() {
        getPayButton().click();
    }
}
