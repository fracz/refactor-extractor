commit ef60722aead81e020d4effddba8ea6db97b33502
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Mon Jun 5 14:14:31 2017 +0300

    PY-8174 Always use actual arguments as default values for new parameters

    even if names referenced in them are not available at the places of
    some usages and the definition. It's user responsibility to review them
    and ensure that refactoring won't cause any errors. This approach
    follows the one used for Java.