@appointments @regression
Feature: Manage upcoming appointments
  As a signed-in patient
  I want to see and cancel my upcoming appointments

  Background:
    Given I am signed in as a patient

  @smoke
  Scenario: Upcoming appointments are listed
    Then the upcoming appointments should include:
      | Confirmation | Department  | Provider        | Visit type | Status    |
      | APT-100231   | Dermatology | Dr. Lena Ortiz  | Telehealth | Scheduled |
      | APT-100232   | Cardiology  | Dr. Avery Stone | In person  | Scheduled |

  Scenario: Cancelling an appointment updates its status
    When I cancel appointment "APT-100231"
    Then appointment "APT-100231" should have status "Cancelled"
    And appointment "APT-100231" can no longer be cancelled
    And appointment "APT-100232" should have status "Scheduled"

  Scenario: A newly booked appointment can be cancelled
    Given I have booked a valid appointment
    When I cancel the appointment I just booked
    Then the appointment I just booked should have status "Cancelled"
