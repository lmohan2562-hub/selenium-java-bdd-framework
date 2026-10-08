@login @regression
Feature: Patient portal sign-in
  As a patient
  I want to sign in to the portal securely
  So that only I can manage my appointments

  Background:
    Given the patient portal sign-in page is open

  @smoke
  Scenario: Successful sign-in opens the appointments dashboard
    When I sign in as "patient.demo" with password "Demo!2026"
    Then I should see the appointments dashboard
    And the welcome message should read "Welcome, Alex Demo"

  Scenario Outline: Rejected sign-in shows an error banner (<case>)
    When I sign in as "<username>" with password "<password>"
    Then I should see the sign-in error "<message>"
    And I should remain on the sign-in page

    Examples:
      | case           | username       | password    | message                                  |
      | wrong password | patient.demo   | Wrong!0000  | Invalid username or password             |
      | unknown user   | nobody.demo    | Demo!2026   | Invalid username or password             |
      | locked account | locked.demo    | Demo!2026   | Your account is locked. Contact support. |

  Scenario Outline: Required fields are validated before submission (<case>)
    When I sign in as "<username>" with password "<password>"
    Then the "<field>" field error should read "<message>"

    Examples:
      | case           | username     | password  | field    | message              |
      | blank username |              | Demo!2026 | username | Username is required |
      | blank password | patient.demo |           | password | Password is required |

  Scenario: Signing out ends the session
    Given I sign in as "patient.demo" with password "Demo!2026"
    And I should see the appointments dashboard
    When I sign out
    Then I should see the sign-in error "You have signed out."
    And I should remain on the sign-in page

  Scenario: Protected page redirects anonymous users to sign-in
    When I open the appointments page without signing in
    Then I should see the sign-in error "Please sign in to continue."
