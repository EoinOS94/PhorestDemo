UI test suite for Phorest gift voucher demo.
Covers sending a voucher to yourself, others and editing vouchers.

# ✅ Prerequisites 
- Java 17+ 
- Maven 3.8+ 
- Git 
- A MailSlurp account (for email testing)

## Setup Instructions 
### 1. Clone the repo 
```bash git clone https://github.com/your-username/phorest-ui-tests.git cd phorest-ui-tests```
### 2. Set Up Environment Variables

To run the tests successfully, you need to provide your MailSlurp API key as an environment variable. This allows the test suite to create and read test inboxes for email verification.

---

#### 2.1 Get Your MailSlurp API Key

1. Go to [https://app.mailslurp.com](https://app.mailslurp.com)
2. Sign in or create a free account
3. Navigate to **API Keys** in the dashboard
4. Copy your **default API key**

---

#### 2.2 Set the API Key in Your Environment

```export MAILSLURP_API_KEY=your-api-key-here```
```set MAILSLURP_API_KEY=your-api-key-here```
#### 3 Test Structure

VoucherTests.java — main test class for the gift card flow
  - a string is waiting for the API key ```private static final String MAILSLURP_API_KEY = System.getenv("MAILSLURP_API_KEY");```
  - 3 tests can be found in this test class
BaseTest.java — handles Playwright setup and teardown
InboxApi.java — wrapper for MailSlurp inbox creation and email polling
Vouchage, Summary, Receipt pages to handle page locators & actions

> ⚠️ **Note on CI Execution**

Due to access restrictions on the production Phorest gift card site, the UI tests may fail when run in CI environments such as GitHub Actions. The site returns a `403 Forbidden` response when accessed from GitHub-hosted runners. This does not affect local execution — all tests pass consistently when run locally.

If needed, tests can be configured to run on a self-hosted runner or against a staging environment with proper access.

