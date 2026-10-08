# Selenium Java BDD Framework

[![UI tests](https://github.com/lmohan2562-hub/selenium-java-bdd-framework/actions/workflows/ci.yml/badge.svg)](https://github.com/lmohan2562-hub/selenium-java-bdd-framework/actions/workflows/ci.yml)
![Java 17](https://img.shields.io/badge/Java-17-blue)
![License: MIT](https://img.shields.io/badge/License-MIT-green)

A behaviour-driven UI test framework built with **Selenium 4, Cucumber 7, TestNG and PicoContainer**.
It tests a small **synthetic patient appointment portal** that ships inside the repository and is
served by an embedded HTTP server during the run. The suite needs no external website, no test
environment and no real data. Tests run headless by default, can target Chrome, Firefox or Edge,
and can run on a Selenium Grid 4.

> **Representative portfolio project built independently with synthetic data. It does not contain
> code or data from any employer or client.**

---

## Business use case

Online appointment scheduling is a common patient-facing workflow in healthcare. Defects there
cause missed visits, double-booked providers and support calls. The scenarios cover what matters
to patients and to clinic operations:

| Capability | Scenarios |
|---|---|
| Secure access | Successful sign-in, wrong password, unknown user, locked account, required fields, sign-out, anonymous redirect |
| Booking | In-person and telehealth bookings driven by Gherkin data tables, with a confirmation number and the new row checked in the list |
| Business rules | Future dates only, no weekends, 90-day booking window, date format, phone format, minimum reason length, consent required, no double-booking of a provider slot |
| Reference data | Provider list depends on the selected department (async load), as a Scenario Outline |
| Manage appointments | Upcoming list matches the expected data table; cancel updates status and removes the action |

All people, providers and appointments in the demo app are fictional.

## Architecture

```mermaid
flowchart TB
    subgraph Runner["TestNG + Cucumber"]
        R[CucumberTestRunner<br/>parallel DataProvider]
        F[(Feature files<br/>Gherkin)]
    end

    subgraph Glue["Glue (PicoContainer DI, one container per scenario)"]
        H[Hooks<br/>BeforeAll / After]
        S[Step definitions<br/>Login, Booking, AppointmentList]
        C[TestContext<br/>scenario state]
    end

    subgraph Framework
        P[Page Objects<br/>LoginPage, AppointmentsPage]
        W[Waits<br/>explicit-wait wrapper]
        DM[DriverManager]
        DF[DriverFactory<br/>chrome / firefox / edge<br/>local or RemoteWebDriver]
        SS[Screenshots]
    end

    subgraph SUT["System under test"]
        SRV[DemoAppServer<br/>JDK HttpServer]
        APP[Static HTML/JS portal<br/>src/test/resources/webapp]
    end

    GRID[(Selenium Grid 4<br/>docker-compose)]

    R --> F --> S
    S --> C --> P --> W
    C --> DM --> DF
    H -- start/stop --> SRV --> APP
    H -- on failure --> SS
    DF -. remote.url .-> GRID
    P -- WebDriver --> APP
```

## Tech stack

| Concern | Choice |
|---|---|
| Language / build | Java 17, Maven 3.9 |
| Browser automation | Selenium 4.25 (Selenium Manager, RemoteWebDriver) |
| BDD | Cucumber 7.20 (Gherkin, Scenario Outlines, DataTables) |
| Runner | TestNG 7.10 through `cucumber-testng`, parallel scenarios |
| Dependency injection | `cucumber-picocontainer` |
| Assertions | AssertJ 3.26 |
| System under test | Static HTML/CSS/JS served by `com.sun.net.httpserver` (no extra dependency) |
| Grid | Selenium Grid 4 hub with Chrome and Firefox nodes (`docker-compose.yml`) |
| Reporting | Cucumber HTML/JSON/JUnit, Surefire, failure screenshots |
| CI/CD | GitHub Actions (headless Chrome), Jenkins (declarative) |

## Project structure

```
selenium-java-bdd-framework/
├── .github/workflows/ci.yml          # Headless run on push/PR, reports + screenshots as artifacts
├── Jenkinsfile                       # Browser, tags, Grid URL and thread parameters
├── docker-compose.yml                # Selenium Grid 4 (+ optional fully containerised test run)
├── pom.xml
└── src/test
    ├── java/io/github/lehamohan/bdd
    │   ├── config/    TestConfig            (-D > env > test.properties)
    │   ├── context/   TestContext           (scenario-scoped, injected by Pico)
    │   ├── driver/    Browser, DriverFactory, DriverManager
    │   ├── hooks/     Hooks                 (start app, screenshot on failure, quit browser)
    │   ├── pages/     BasePage, LoginPage, AppointmentsPage
    │   ├── runners/   CucumberTestRunner
    │   ├── server/    DemoAppServer, AppUnderTest
    │   ├── steps/     LoginSteps, BookingSteps, AppointmentListSteps
    │   └── support/   Waits, Screenshots, DateResolver, BookingDetails
    └── resources
        ├── features/  login.feature, book_appointment.feature, manage_appointments.feature
        ├── webapp/    index.html, appointments.html, app.js, styles.css
        ├── test.properties, cucumber.properties, logback-test.xml
```

## Setup

Prerequisites: **JDK 17+, Maven 3.9+**, and Chrome (or Firefox/Edge) for local runs. Selenium
Manager downloads a matching driver automatically.

```bash
git clone https://github.com/lmohan2562-hub/selenium-java-bdd-framework.git
cd selenium-java-bdd-framework
mvn -B verify
```

Demo credentials for the synthetic portal: `patient.demo` / `Demo!2026`. A second account,
`locked.demo`, exists to test the locked-account path. Both are hard-coded fake accounts in the
demo app and are not real credentials.

### Offline or locked-down machines

If Selenium Manager cannot reach the internet, point Selenium at binaries you already have:

```bash
mvn -B verify \
  -Dchrome.binary=/path/to/chrome \
  -Dwebdriver.chrome.driver=/path/to/chromedriver   # driver major version must match the browser
```

## How to run

### Local

```bash
mvn -B verify                                        # all scenarios, headless Chrome, 2 threads
mvn -B verify -Dheadless=false                       # watch the browser
mvn -B verify -Dbrowser=firefox                      # cross-browser: chrome | firefox | edge
mvn -B verify -Dcucumber.filter.tags="@smoke"        # tag filtering
mvn -B verify -Dcucumber.filter.tags="@booking and not @validation"
mvn -B verify -Dthreads=4                            # parallel scenarios
```

Every key in `src/test/resources/test.properties` can be overridden with `-Dkey=value` or an
environment variable (`WAIT_EXPLICIT_SECONDS=15`).

### Selenium Grid 4 (Docker)

```bash
# A) Grid in Docker, tests on the host
docker compose up -d
mvn -B verify -Dremote.url=http://localhost:4444 -Dapp.bind=0.0.0.0 -Dapp.host=host.docker.internal
docker compose down

# B) Everything in Docker. The tests container serves the demo app to the Grid nodes.
docker compose --profile tests up --exit-code-from tests
BROWSER=firefox docker compose --profile tests up --exit-code-from tests
```

The Grid UI is available at <http://localhost:4444/ui>.

### CI

* **GitHub Actions** runs every scenario in headless Chrome on push to `main` and on pull requests.
  Cucumber reports, Surefire results and any failure screenshots are uploaded as artifacts.
  A manual dispatch accepts a browser and a tag expression.
* **Jenkins** (`Jenkinsfile`) takes `BROWSER`, `TAGS`, `THREADS` and an optional `GRID_URL`
  parameter, publishes JUnit results and archives reports and screenshots.

### Reports

| Output | Location |
|---|---|
| Cucumber HTML report | `target/cucumber-reports/cucumber.html` |
| Cucumber JSON / JUnit XML | `target/cucumber-reports/cucumber.json`, `cucumber-junit.xml` |
| Rerun file (failed scenarios) | `target/cucumber-reports/rerun.txt` |
| Failure screenshots (also embedded in the HTML report) | `target/screenshots/` |
| Surefire / TestNG | `target/surefire-reports/` |

## Sample output

> **TODO before publishing:** paste the Cucumber `summary` block and Maven `Tests run` /
> `BUILD SUCCESS` lines from your first green `mvn -B verify` run here.

The suite has **28 scenarios** in 3 feature files: 8 sign-in, 17 booking (including 9 validation
examples and 4 department/provider examples) and 3 appointment-management scenarios. 3 of them are
tagged `@smoke`.

## Design decisions

* **The app under test lives in the repository.** A static portal served by the JDK's own
  `HttpServer` makes the suite hermetic: no shared environment, no flakiness from someone else's
  deployment, and no extra dependency. The app has real asynchronous behaviour (sign-in latency,
  provider lookup, booking spinner), so the tests have to synchronise properly.
* **Explicit waits only.** The implicit wait is set to zero. Every synchronisation point is a named
  method in `Waits` (FluentWait that ignores stale and missing elements, with a descriptive
  message), so timeouts behave the same everywhere and failures say what the test was waiting for.
* **`data-testid` locators.** They decouple tests from CSS and copy changes.
* **PicoContainer instead of static state.** `TestContext` and `DriverManager` are created per
  scenario and injected into every step class. Parallel scenarios each get their own browser
  without `ThreadLocal` plumbing. The browser starts lazily.
* **Data tables map to a domain object.** `BookingDetails.valid()` plus `with(field, value)` lets
  each validation example change exactly one field, so a failure points at one rule.
* **Relative date tokens** (`NEXT_WEEKDAY`, `NEXT_SATURDAY`, `BEYOND_90_DAYS`) keep scenarios
  valid on any day. Date-dependent tests never go stale.
* **One factory for local and remote browsers.** `DriverFactory` builds the same `Capabilities`
  for both. Setting `remote.url` switches to `RemoteWebDriver`, and nothing else changes.
* **Failures leave evidence.** An `@After` hook with a higher order (it runs before the quit hook)
  saves a PNG, attaches it to the Cucumber report and logs the URL at the time of failure.

## Security considerations

* **Synthetic data only.** Patient name, providers, phone numbers (555-010-xxxx) and appointments
  are fictional. There is no PHI and no real user accounts.
* **No secrets.** The demo credentials are hard-coded in the demo app on purpose and are documented
  above. The framework reads no secret values. If it were pointed at a real environment,
  credentials would come from environment variables or CI secrets and never from feature files.
* **Embedded server hardening.** It binds to `127.0.0.1` by default and only binds to all
  interfaces when you opt in with `-Dapp.bind=0.0.0.0` for Grid. It serves only flat files from
  `webapp/`, rejects path traversal and non-GET methods, and sends `Cache-Control: no-store`.
* **CI hygiene.** Workflow permissions are read-only, and dispatch inputs are passed through
  environment variables rather than interpolated into shell commands.
* A secret scan (detect-secrets) was run over the full git history before publishing.

## Limitations and future enhancements

* The demo app keeps state in `sessionStorage`. There is no back end, so server-side rules
  (concurrency, persistence) are out of scope.
* Date checks use the browser's local time zone. A Grid node in a different time zone than the
  test JVM could disagree about "today" near midnight.
* GitHub Actions runs Chrome by default. Firefox and Edge are available through manual dispatch
  but are not gated on every push.
* Possible additions: an accessibility scan (axe-core) per page, visual regression snapshots,
  Allure reporting, a Grid job in CI using service containers, a retry of `rerun.txt` for
  quarantine analysis, and API-level setup (seeding appointments without the UI) once the app
  has a back end.

## License

[MIT](LICENSE) © 2026 Leha Mohan
