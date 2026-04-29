# SPEC.md

## Product

We want to use a product that acts as a kakebo, that is, the Japanese method of personal household expense management. The different pages of this method must be implemented:

- Home screen / Monthly summary, with the following sections:
    - Available money for the month
    - Total spent to date
    - Planned savings vs. actual savings
    - Visual alerts (for example: "you exceeded the entertainment budget")
    - Savings goals
    - Income
        - Main income (salary)
        - Extra income (freelance, gifts, refunds)
        - Recurring income option
    - Fixed expenses

- Variable expenses
    - Survival (food, transport)
    - Entertainment and treats
    - Culture and education
    - Extras / unexpected

- Expense registration screen
    - Add expense button

## Use Cases

1. On entry show the home screen
2. When clicking the add expenses button go to the variable expenses screen.
    - When clicking the add expenses button you should go to the expense registration screen
    - Once the data is saved it should return to the variable expenses screen
3. On the variable expenses screen add an option to go to the home screen

## Acceptance Criteria

- Allow navigation through the application
- Allow adding expenses
- Display new expenses
