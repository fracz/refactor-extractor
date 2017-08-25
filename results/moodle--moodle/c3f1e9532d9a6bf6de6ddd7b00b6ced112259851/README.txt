commit c3f1e9532d9a6bf6de6ddd7b00b6ced112259851
Author: David Monllao <davidm@moodle.com>
Date:   Fri Jan 24 13:24:22 2014 +0800

    MDL-43738 behat: Normalization and major refactoring of getters and setters

    Every single step that sets or gets a value from a field
    has been updated to follow the same behaviour both when
    using it through a single step or through generic steps
    like "I fill the moodle form with:", to resume all the
    changes:
    - Created a behat_form_group to re-guess the field type
      and act appropriately setting and getting it's value
    - Normalize all getters and setters to use behat_form_field
      children
    - Complete behat_form_checkbox to trigger the appropiate JS
      event needed to perform some JS scripts that are listening
    - Refactor MDL-43713 multi-select changes and remove
      the two new steps introduced there as that behaviour can
      be managed from the generic getter
    - Added a new step definition to check a capability permission
      as we changed the way radio buttons gets it's value