package com.phorestdemo.tests;

import com.phorestdemo.config.ConfigReader;
import com.phorestdemo.pages.VoucherPage;
import com.phorestdemo.pages.ReceiptPage;
import com.phorestdemo.pages.SummaryPage;
import org.junit.jupiter.api.*;

import com.mailslurp.clients.ApiClient;
import com.mailslurp.clients.Configuration;
import com.mailslurp.apis.InboxControllerApi;
import com.mailslurp.apis.WaitForControllerApi;
import com.mailslurp.models.InboxDto;
import com.mailslurp.models.Email;

import java.text.DecimalFormat;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class VoucherTests extends BaseTest {

    private static final String MAILSLURP_API_KEY = System.getenv("MAILSLURP_API_KEY");

    private InboxControllerApi inboxApi;
    private WaitForControllerApi waitApi;

    @BeforeEach
    void setupMailSlurp() {
        if (MAILSLURP_API_KEY == null || MAILSLURP_API_KEY.isBlank()) {
            throw new RuntimeException("MAILSLURP_API_KEY environment variable is not set");
        }

        ApiClient client = Configuration.getDefaultApiClient();
        client.setApiKey(MAILSLURP_API_KEY);

        inboxApi = new InboxControllerApi(client);
        waitApi = new WaitForControllerApi(client);
        }

    @Test
    void sendToMeTest() throws Exception {
        System.out.println("BASE URL = " + ConfigReader.getBaseUrl());
        page.navigate(ConfigReader.getBaseUrl());
        System.out.println("PAGE CONTENT = " + page.content());
        InboxDto inbox = inboxApi.createInboxWithDefaults().execute();

        try {
            String voucherCustomAmount = "123";
            String expectedAmount =
                    new DecimalFormat("0.00").format(Double.parseDouble(voucherCustomAmount));

            VoucherPage voucherPage = new VoucherPage(page, ConfigReader.getBaseUrl());
            SummaryPage summaryPage = new SummaryPage(page, ConfigReader.getBaseUrl());
            ReceiptPage receiptPage = new ReceiptPage(page, ConfigReader.getBaseUrl());
            page.navigate(ConfigReader.getBaseUrl());
           //voucherPage.navigate();
            voucherPage.selectGiftAmount("Other", voucherCustomAmount);
            voucherPage.clickSendToMeTab();
            voucherPage.fillPurchaserEmailInputBox(inbox.getEmailAddress());
            voucherPage.fillFirstName("John");
            voucherPage.fillLastName("Doe");

            voucherPage.clickCheckoutButton();

            summaryPage.clickConfirmDetailsButton();
            summaryPage.enterPaymentDetails("4111 1111 1111 1111", "12/26", "999");
            summaryPage.clickPayButton();

            String voucherCode = receiptPage.getVoucherCode();
            assertNotNull(voucherCode, "Voucher code should be visible on receipt page");

            Email receipt = waitForEmailBySubject(inbox, "Your Receipt for City Salon");
            assertNotNull(receipt, "Receipt email not received");
            assertTrue(receipt.getBody().contains(voucherCode));
            assertTrue(receipt.getBody().contains(expectedAmount));
        } finally {
            deleteInboxQuietly(inbox);
        }
    }

    @Test
    void sendToOtherTest() throws Exception {
        System.out.println("BASE URL = " + ConfigReader.getBaseUrl()); 
        page.navigate(ConfigReader.getBaseUrl()); 
        System.out.println("PAGE CONTENT = " + page.content());
        
        InboxDto purchaserInbox = inboxApi.createInboxWithDefaults().execute();
        InboxDto recipientInbox = inboxApi.createInboxWithDefaults().execute();

        try {
            String amount = "150";
            String expectedAmount =
                    new DecimalFormat("0.00").format(Double.parseDouble(amount));

            VoucherPage voucherPage = new VoucherPage(page, ConfigReader.getBaseUrl());
            SummaryPage summaryPage = new SummaryPage(page, ConfigReader.getBaseUrl());
            ReceiptPage receiptPage = new ReceiptPage(page, ConfigReader.getBaseUrl());

            voucherPage.navigate();

            voucherPage.selectGiftAmount("150", null);
            voucherPage.clickSendToOtherTab();
            voucherPage.fillPurchaserEmailInputBox(purchaserInbox.getEmailAddress());
            voucherPage.fillFirstName("Jane");
            voucherPage.fillLastName("Doe");
            voucherPage.fillRecipientEmailInputBox(recipientInbox.getEmailAddress());
            voucherPage.fillMessageForRecipientInputBox("Auto message for voucher");

            voucherPage.clickCheckoutButton();

            summaryPage.clickConfirmDetailsButton();
            summaryPage.enterPaymentDetails("4111 1111 1111 1111", "12/26", "999");
            summaryPage.clickPayButton();

            String voucherCode = receiptPage.getVoucherCode();
            assertNotNull(voucherCode);

            Email gift = waitForEmailBySubject(
                    recipientInbox,
                    "You've been sent a €" + expectedAmount + " gift voucher for Demo IE!"
            );

            assertNotNull(gift, "Gift email not received");
            assertTrue(gift.getBody().contains(voucherCode));

        } finally {
            deleteInboxQuietly(purchaserInbox);
            deleteInboxQuietly(recipientInbox);
        }
    }

    private Email waitForEmailBySubject(InboxDto inbox, String subject)
            throws InterruptedException {

        for (int i = 0; i < 12; i++) {
            try {
                Email email = waitApi.waitForLatestEmail()
                        .inboxId(inbox.getId())
                        .timeout(15_000L)
                        .unreadOnly(true)
                        .execute();

                if (email != null && subject.equals(email.getSubject())) {
                    return email;
                }
            } catch (Exception ignored) {}
            Thread.sleep(5000);
        }
        return null;
    }

    private void deleteInboxQuietly(InboxDto inbox) {
        if (inbox == null) return;
        try {
            inboxApi.deleteInbox(inbox.getId());
        } catch (Exception ignored) {}
    }
}