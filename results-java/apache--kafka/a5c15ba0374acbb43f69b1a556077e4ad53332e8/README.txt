commit a5c15ba0374acbb43f69b1a556077e4ad53332e8
Author: Rekha Joshi <rekhajoshm@gmail.com>
Date:   Tue Dec 20 12:40:00 2016 +0000

    KAFKA-4500; Code quality improvements

    - Removed redundant modifiers, not needed String.format()
    - Removed unnecessary semicolon, additional assignment, inlined return
    - Using StringBuilder for consistency across codebase
    - Using try-with-resources

    Author: Rekha Joshi <rekhajoshm@gmail.com>
    Author: Joshi <rekhajoshm@gmail.com>

    Reviewers: Ismael Juma <ismael@juma.me.uk>

    Closes #2222 from rekhajoshm/KAFKA-4500