commit 1bbd7f45b14a657a691743e31c0bb65f8b0d52e6
Author: Bernd Ahlers <bernd@users.noreply.github.com>
Date:   Thu Oct 5 19:29:26 2017 +0200

    Implement QuickValue improvements (#4205)

    * Fix linting errors

    * Migrate FieldQuickValuesStore to js/reflux

    * First stab at making QuickValues configurable

    - Add a config form that can be toggled
    - Make order, limit and table size configurable

    There are still problems with the ordering in the data table, at least.

    * Fix sorting when bottom N is configured

    * Add stacking support to the /terms endpoints

    * Incorporate PR feedback

    - Add "Cancel" button to QuickValues configuration
    - Fix horizontal scrolling in QuickValues configuration form
    - Remove unneeded custom event handler for radio buttons
    - Add label for radio buttons
    - Remove now unused FormsUtils#inputEvent()

    * Add UI for stacked fields in quick values

    * Remove debug console.log

    * Refactor config handling in QuickValuesVisualization

    This puts the "sortOrder", "dataTableTitle", "dataTableLimit" and "limit"
    props into the existing "config" prop. This makes it way easier to
    handle both usages of the QuickValuesVisualization, on dashboards and
    on the search page.

    * Adjust dashboard handling for new config options in QuickValues

    * Implement review feedback

    - Move stylesheet code into .css files
    - Remove unneeded "e.preventDefault()"
    - Avoid Input component nesting and use FromGroup and ControlLabel
    - Use ButtonToolbar instead of manually adding spaces

    * Remove custom error handler