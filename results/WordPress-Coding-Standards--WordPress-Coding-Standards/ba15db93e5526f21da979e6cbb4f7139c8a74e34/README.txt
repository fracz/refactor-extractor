commit ba15db93e5526f21da979e6cbb4f7139c8a74e34
Author: JDGrimes <jdg@codesymphony.co>
Date:   Fri May 1 18:09:46 2015 -0400

    Move validation logic to a method

    This makes it available to all sniffs. We also move another method to
    the main sniff from the ValidatedSantizedInput sniff.

    In addition, I’ve refactored the sanitization portion of this sniff to
    use the `is_sanitized()` method that I recently introduced.