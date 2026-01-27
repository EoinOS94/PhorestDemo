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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class VoucherTests extends BaseTest {

    private static final String MAILSLURP_API_KEY = System.getenv("MAILSLURP_API_KEY");
    private static final Logger logger = Logger.getLogger(VoucherTests.class.getName());

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

        logger.info("MailSlurp client initialized");
    }

    @Test
    void sendToMeTest() throws Exception {

        InboxDto inbox = inboxApi.createInboxWithDefaults().execute();
        String tempEmail = inbox.getEmailAddress();
        logger.info("Temporary inbox created: " + tempEmail);

        try {
            String voucherType = "Other";
            String voucherCustomAmount = "123";
            String expectedAmount =
                    new DecimalFormat("0.00").format(Double.parseDouble(voucherCustomAmount));

            VoucherPage voucherPage = new VoucherPage(page, ConfigReader.getBaseUrl());
            SummaryPage summaryPage = new SummaryPage(page, ConfigReader.getBaseUrl());
            ReceiptPage receiptPage = new ReceiptPage(page, ConfigReader.getBaseUrl());

            voucherPage.navigate();
            voucherPage.selectGiftAmount(voucherType, voucherCustomAmount);
            voucherPage.clickSendToMeTab();
            voucherPage.fillPurchaserEmailInputBox(tempEmail);
            voucherPage.fillFirstName("John");
            voucherPage.fillLastName("Doe");

            assertEquals(expectedAmount, voucherPage.getTotalCost());
            assertEquals(expectedAmount, voucherPage.getVoucherIconAmount());

            voucherPage.clickCheckoutButton();

            assertEquals(voucherCustomAmount, summaryPage.getConfirmVoucherValue());
            assertEquals(voucherCustomAmount, summaryPage.getConfirmTotalCost());
            assertEquals(tempEmail, summaryPage.getSenderEmailText());
            assertEquals(tempEmail, summaryPage.getRecipientEmailText());

            summaryPage.clickConfirmDetailsButton();
            summaryPage.enterPaymentDetails("4111 1111 1111 1111", "12/26", "999");
            summaryPage.clickPayButton();

            String voucherCode = receiptPage.getVoucherCode();
            logger.info("Voucher code: " + voucherCode);

            Email receiptEmail =
                    waitForEmailBySubject(inbox, "Your Receipt for City Salon");
            assertNotNull(receiptEmail);
            assertTrue(receiptEmail.getBody().contains(voucherCode));

            String expectedVoucherSubject =
                    "You've been sent a €" + expectedAmount + " gift voucher for Demo IE!";

            Email giftEmail =
                    waitForEmailBySubject(inbox, expectedVoucherSubject);
            assertNotNull(giftEmail);
            assertTrue(giftEmail.getBody().contains(voucherCode));

        } finally {
            deleteInboxQuietly(inbox);
        }
    }

    @Test
    void sendToOtherTest() throws Exception {

        InboxDto purchaserInbox = inboxApi.createInboxWithDefaults().execute();
        InboxDto recipientInbox = inboxApi.createInboxWithDefaults().execute();

        String purchaserEmail = purchaserInbox.getEmailAddress();
        String recipientEmail = recipientInbox.getEmailAddress();

        logger.info("Purchaser inbox: " + purchaserEmail);
        logger.info("Recipient inbox: " + recipientEmail);

        try {
            String voucherType = "150";
            String voucherCustomAmount = "150";
            String expectedAmount =
                    new DecimalFormat("0.00").format(Double.parseDouble(voucherCustomAmount));
            String message = "Auto message for voucher";

            VoucherPage voucherPage = new VoucherPage(page, ConfigReader.getBaseUrl());
            SummaryPage summaryPage = new SummaryPage(page, ConfigReader.getBaseUrl());
            ReceiptPage receiptPage = new ReceiptPage(page, ConfigReader.getBaseUrl());

            voucherPage.navigate();
            voucherPage.selectGiftAmount(voucherType, null);
            voucherPage.clickSendToOtherTab();
            voucherPage.fillPurchaserEmailInputBox(purchaserEmail);
            voucherPage.fillFirstName("Jane");
            voucherPage.fillLastName("Doe");
            voucherPage.fillRecipientEmailInputBox(recipientEmail);
            voucherPage.fillMessageForRecipientInputBox(message);

            assertEquals(expectedAmount, voucherPage.getTotalCost());
            assertEquals(expectedAmount, voucherPage.getVoucherIconAmount());

            voucherPage.clickCheckoutButton();

            assertEquals(voucherCustomAmount, summaryPage.getConfirmVoucherValue());
            assertEquals(voucherCustomAmount, summaryPage.getConfirmTotalCost());
            assertEquals(purchaserEmail, summaryPage.getSenderEmailText());
            assertEquals(recipientEmail, summaryPage.getRecipientEmailText());

            summaryPage.clickConfirmDetailsButton();
            summaryPage.enterPaymentDetails("4111 1111 1111 1111", "12/26", "999");
            summaryPage.clickPayButton();

            String voucherCode = receiptPage.getVoucherCode();
            logger.info("Voucher code: " + voucherCode);

            // Receipt → purchaser
            Email receiptEmail =
                    waitForEmailBySubject(purchaserInbox, "Your Receipt for City Salon");
            assertNotNull(receiptEmail);
            assertTrue(receiptEmail.getBody().contains(voucherCode));

            // Gift → recipient
            String expectedVoucherSubject =
                    "You've been sent a €" + expectedAmount + " gift voucher for Demo IE!";

            Email giftEmail =
                    waitForEmailBySubject(recipientInbox, expectedVoucherSubject);
            assertNotNull(giftEmail);
            assertTrue(giftEmail.getBody().contains(voucherCode));
            assertTrue(giftEmail.getBody().contains(message));

        } finally {
            deleteInboxQuietly(purchaserInbox);
            deleteInboxQuietly(recipientInbox);
        }
    }

    private Email waitForEmailBySubject(InboxDto inbox, String subject)
            throws InterruptedException {

        int retries = 12;
        long retryDelayMs = 5000L;
        long timeoutPerAttempt = 15_000L;

        for (int i = 1; i <= retries; i++) {
            try {
                Email email = waitApi.waitForLatestEmail()
                        .inboxId(inbox.getId())
                        .timeout(timeoutPerAttempt)
                        .unreadOnly(true)
                        .execute();

                if (email != null && subject.equals(email.getSubject())) {
                    logger.info("Email received on attempt " + i + ": " + subject);
                    return email;
                }
            } catch (Exception e) {
                logger.warning("Attempt " + i + " failed: " + e.getMessage());
            }
            Thread.sleep(retryDelayMs);
        }

        logger.severe("Email with subject '" + subject + "' not received");
        return null;
    }

    private void deleteInboxQuietly(InboxDto inbox) {
        if (inbox == null) return;
        try {
            inboxApi.deleteInbox(inbox.getId());
            logger.info("Deleted inbox: " + inbox.getEmailAddress());
        } catch (Exception e) {
            logger.severe("Failed to delete inbox " + inbox.getEmailAddress() + ": " + e.getMessage());
        }
    }
}
