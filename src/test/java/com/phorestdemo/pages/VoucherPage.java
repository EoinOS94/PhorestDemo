package com.phorestdemo.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.text.DecimalFormat;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoucherPage {

    private final Page page;

    private static final String OTHER_VOUCHER_SELECTOR_INPUT_BOX = "input[data-target='amount.otherInput']";
    private static final String VOUCHER_ICON_AMOUNT = "text#voucher-value-text";
    private static final String TOTAL_COST_SPAN = "span[data-target='checkout.totalCost']";
    private static final String CHECK_OUT_BUTTON = "button[data-target='checkout.checkoutButton']";
    private static final String SEND_TO_ME_TAB = "a[data-action='tabs#showSendToMe'][data-target='tabs.sendToMyselfTab']";
    private static final String PURCHASER_EMAIL_INPUT_BOX = "input[data-target='email.purchaserEmailInput']";
    private static final String RECIPIENT_EMAIL_INPUT_BOX = "input[data-target='email.recipientEmailInput']";
    private static final String MESSAGE_FOR_RECIPIENT_INPUT_BOX = "textarea[data-target='email.recipientMessageInput']";
    private static final String FIRST_NAME_INPUT_BOX = "input[data-target='name.purchaserFirstNameInput']";
    private static final String LAST_NAME_INPUT_BOX = "input[data-target='name.purchaserLastNameInput']";

    public VoucherPage(Page page, String url) {
        this.page = page;
    }

    public void navigate() {
        page.navigate("https://gift-cards.phorest.com/salons/demo#");
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        // Wait until at least one radio is visible
        page.waitForSelector("input[type='radio']", new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(30_000));
    }

    private Locator getCheckoutButton() {
        return page.locator(CHECK_OUT_BUTTON);
    }

    private Locator getSendToMeTab() {
        return page.locator(SEND_TO_ME_TAB);
    }

    private Locator getPurchaserEmailInputBox() {
        return page.locator(PURCHASER_EMAIL_INPUT_BOX);
    }

    private Locator getRecipientEmailInputBox() {
        return page.locator(RECIPIENT_EMAIL_INPUT_BOX);
    }

    private Locator getFirstNameInputBox() {
        return page.locator(FIRST_NAME_INPUT_BOX);
    }

    private Locator getLastNameInputBox() {
        return page.locator(LAST_NAME_INPUT_BOX);
    }

    private Locator getMessageForRecipientInputBox() {
        return page.locator(MESSAGE_FOR_RECIPIENT_INPUT_BOX);
    }

    private Locator getOtherInputBox() {
        return page.locator(OTHER_VOUCHER_SELECTOR_INPUT_BOX);
    }

    private Locator getVoucherAmountText() {
        return page.locator(VOUCHER_ICON_AMOUNT);
    }

    /**
     * Returns the Locator for a gift voucher radio button.
     * Uses data-voucher-value attribute for preset amounts.
     * Uses a special selector for "Other".
     */
    private Locator getGiftVoucherRadioButton(String amount) {
        if (amount.equalsIgnoreCase("Other")) {
            // Use the actual HTML for the "Other" radio
            return page.locator("input#optionOther");
            // OR: return page.locator("input[data-target='amount.otherOptionButton']");
        } else {
            // Preset amounts use data-voucher-value
            String formattedAmount = new DecimalFormat("0.00").format(Double.parseDouble(amount));
            return page.locator("input[type='radio'][data-voucher-value='" + formattedAmount + "']");
        }
    }

    /**
     * Selects a gift voucher amount, either preset or custom "Other".
     */
    public void selectGiftAmount(String amount, String customAmountIfOther) {
        // Wait until all radios are rendered (CI safety)
        page.waitForSelector(
                "input[type='radio']",
                new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(30_000));

        Locator radio = getGiftVoucherRadioButton(amount);

        // Wait for this specific radio to be visible
        radio.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(30_000));

        radio.check();

        assertTrue(
                radio.isChecked(),
                "Expected gift voucher amount " + amount + " to be selected.");

        if (amount.equalsIgnoreCase("Other")) {
            if (customAmountIfOther == null) {
                throw new IllegalArgumentException(
                        "Custom amount must be provided when selecting 'Other'.");
            }

            // Wait for the "Other" input box
            getOtherInputBox().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(30_000));

            getOtherInputBox().fill(customAmountIfOther);
        }
    }

    public void clickSendToMeTab() {
        getSendToMeTab().click();
    }

    public void clickSendToOtherTab() {
        page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Send to someone else")).click();
    }

    public void fillPurchaserEmailInputBox(String purchaserEmail) {
        getPurchaserEmailInputBox().fill(purchaserEmail);
    }

    public void fillRecipientEmailInputBox(String recipientEmail) {
        getRecipientEmailInputBox().fill(recipientEmail);
    }

    public void fillFirstName(String firstName) {
        getFirstNameInputBox().fill(firstName);
    }

    public void fillLastName(String lastName) {
        getLastNameInputBox().fill(lastName);
    }

    public void fillMessageForRecipientInputBox(String message) {
        getMessageForRecipientInputBox().fill(message);
    }

    public String getTotalCost() {
        String value = page.locator(TOTAL_COST_SPAN).nth(0).textContent().trim();
        value = value.replaceAll("[^0-9.]", "");
        return new DecimalFormat("0.00")
                .format(Double.parseDouble(value));
    }

    public String getVoucherIconAmount() {
        String value = getVoucherAmountText().textContent().trim();
        value = value.replaceAll("[^0-9.]", "");
        return new DecimalFormat("0.00")
                .format(Double.parseDouble(value));
    }

    public void clickCheckoutButton() {
        getCheckoutButton().nth(0).click();
    }
}
