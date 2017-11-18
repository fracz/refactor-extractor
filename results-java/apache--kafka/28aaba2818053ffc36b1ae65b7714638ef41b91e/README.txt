commit 28aaba2818053ffc36b1ae65b7714638ef41b91e
Author: Ismael Juma <ismael@juma.me.uk>
Date:   Wed Oct 4 08:06:05 2017 +0100

    MINOR: Java 9 version handling improvements

    - Upgrade Gradle to 4.2.1, which handles Azul Zulu 9's version
    correctly.
    - Add tests to our Java version handling code
    - Refactor the code to make it possible to add tests
    - Rename `isIBMJdk` method to use consistent naming
    convention.

    Author: Ismael Juma <ismael@juma.me.uk>

    Reviewers: Rajini Sivaram <rajinisivaram@googlemail.com>

    Closes #4007 from ijuma/java-9-version-handling-improvements