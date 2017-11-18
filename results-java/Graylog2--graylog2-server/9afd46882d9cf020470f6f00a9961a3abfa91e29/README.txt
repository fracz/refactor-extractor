commit 9afd46882d9cf020470f6f00a9961a3abfa91e29
Author: Edmundo Alvarez <github@edmundoa.com>
Date:   Wed Dec 28 14:13:34 2016 +0100

    Fix alert condition validations (#3257)

    * Use human name in DropdownField placeholder

    * Wait until condition types are loaded

    * Improve alert condition descriptions

    * Move AbstractAlertCondition getNumber to Tools

    That is a more appropiate place for this method.

    * Convert alert condition configuration values

    Values coming from the API may not be using the right type, so we need
    to ensure we convert them to what we expect.

    Refs #3247

    * Verify alert condition configurations

    Check if the configuration is valid against the requested configuration
    for that alert condition, ensuring all required fields are set, and the
    right data types are being used. If that is not the case, a
    `ConfigurationException` will be raised.

    Refs #3247

    * Standarize alert condition component registration

    * Use common components to format plurals

    * Try to get condition title from form

    When using the fallback form component, the title is not stored in this
    component's state, but in the form itself.

    * Only try to close the modal when possible

    When using the fallback `ConfigurationForm` component, there is no close
    method to call.

    * Small style improvements in condition form

    * Remove pluggable alert condition forms

    Having custom pluggable forms, and falling back to using
    `ConfigurationForm` when none was provided sounded like a great idea,
    but in the end it is tricky, as forms behaved in slightly different
    ways, and they also asume the developer would follow certain implied
    conventions.

    As this way also feels inconsistent from a user's point of view, I
    think it is best at this point to only use `ConfigurationForm` components
    for now, and think of a better way of doing custom forms for alert
    conditions in future versions.

    * Add title to error message loading alert condition

    * Improve time config field description