commit ae0c34baaf341bd65a1077d472e3b161cce2e704
Author: Sam Brannen <sam@sambrannen.com>
Date:   Fri Feb 1 15:40:01 2013 +0100

    Improve 3.2 migration guide re: JUnit & Hamcrest

    This commit improves the "Spring Test Dependencies" section of the 3.2
    migration guide by correctly explaining that Hamcrest Core is now a
    required transitive dependency of JUnit.

    Issue: SPR-10251