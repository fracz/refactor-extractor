commit f8927a7cde6a0e47689976f5d600b7ec31940517
Author: Ruslan Kabalin <r.kabalin@lancaster.ac.uk>
Date:   Thu Apr 14 10:37:44 2016 +0100

    MDL-50888 antivirus: Unit test refactoring.

    Remove all cleanup and exception test references. We expect plugin just to
    respond with scanning result constant and notice where applicable.

    Add tests for \core\antivirus\manager.