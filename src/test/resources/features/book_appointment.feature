@booking @regression
Feature: Book an appointment
  As a signed-in patient
  I want to book an appointment with a provider
  So that I can be seen at a time that suits me

  Background:
    Given I am signed in as a patient

  @smoke
  Scenario: Book an in-person appointment
    When I book an appointment with:
      | department | Cardiology                    |
      | provider   | Dr. Priya Raman               |
      | date       | NEXT_WEEKDAY                  |
      | time       | 08:30                         |
      | visit type | In person                     |
      | phone      | 555-010-4477                  |
      | reason     | Follow-up on blood pressure   |
    Then the booking should be confirmed with a confirmation number
    And my new appointment should be listed as:
      | Department | Cardiology      |
      | Provider   | Dr. Priya Raman |
      | Time       | 08:30           |
      | Visit type | In person       |
      | Status     | Scheduled       |

  Scenario: Book a telehealth appointment
    When I book an appointment with:
      | department | Dermatology        |
      | provider   | Dr. Lena Ortiz     |
      | date       | WEEKDAY_IN_2_WEEKS |
      | time       | 14:45              |
      | visit type | Telehealth         |
    Then the booking should be confirmed with a confirmation number
    And my new appointment should be listed as:
      | Department | Dermatology |
      | Visit type | Telehealth  |
      | Status     | Scheduled   |

  @validation
  Scenario Outline: Booking form rejects invalid <field> (<case>)
    When I submit a valid booking but with "<field>" set to "<value>"
    Then the "<field>" booking error should read "<message>"
    And no booking confirmation should be shown

    Examples:
      | case                 | field  | value          | message                                       |
      | wrong phone format   | phone  | 5550101234     | Phone must match 555-010-0000 format          |
      | phone has letters    | phone  | 555-O10-1234   | Phone must match 555-010-0000 format          |
      | reason too short     | reason | Checkup        | Reason must be at least 10 characters         |
      | date in the past     | date   | YESTERDAY      | Date must be in the future                    |
      | same-day booking     | date   | TODAY          | Date must be in the future                    |
      | weekend date         | date   | NEXT_SATURDAY  | The clinic is closed on weekends              |
      | beyond booking range | date   | BEYOND_90_DAYS | Appointments can be booked up to 90 days ahead |
      | wrong date format    | date   | 01/15/2027     | Date must be in YYYY-MM-DD format             |
      | consent not given    | consent| false          | Please confirm to continue                    |

  @validation
  Scenario: Submitting an empty form reports every required field
    When I submit the booking form without entering anything
    Then the following booking errors should be shown:
      | field      | message                                   |
      | department | Please select a department                |
      | provider   | Please select a provider                  |
      | date       | Please enter a date                       |
      | time       | Please select a time                      |
      | phone      | Please enter a contact phone              |
      | reason     | Please describe the reason for your visit |
      | consent    | Please confirm to continue                |

  Scenario Outline: Provider list depends on the selected department (<department>)
    When I select the "<department>" department
    Then the available providers should be "<providers>"

    Examples:
      | department      | providers                                         |
      | Cardiology      | Dr. Avery Stone, Dr. Priya Raman                  |
      | Dermatology     | Dr. Lena Ortiz                                    |
      | Family Medicine | Dr. Sam Whitaker, Dr. Grace Liu, Dr. Omar Haddad |
      | Pediatrics      | Dr. Nina Patel                                    |

  Scenario: The same provider slot cannot be double-booked
    Given I have booked a valid appointment
    When I book the same provider, date and time again
    Then the "time" booking error should read "That time slot is already booked"
