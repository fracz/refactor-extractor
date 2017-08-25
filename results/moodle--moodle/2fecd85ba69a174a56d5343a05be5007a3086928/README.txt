commit 2fecd85ba69a174a56d5343a05be5007a3086928
Author: jamiesensei <jamiesensei>
Date:   Wed May 7 16:00:56 2008 +0000

    MDL-14734 "improve interface for deleting attempts"

    * The control for deleting attempts is a drop down box with onchange javascript to submit the form, you cannot see the label to say what the drop down box does. Better to use a button. The button label is visible so you can see what it does.
    * Checkboxes should be displayed when there is no attempt to delete.
    * The whole column for checkboxes should be removed when no attempts are displayed ie. when showing students with no attempts.