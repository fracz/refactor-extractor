commit b4b978ca75929a715b864f8a0aa176208bc086d2
Author: Jesús Miguel Benito Calzada <beni0888@hotmail.com>
Date:   Tue Mar 1 18:35:42 2016 +0100

    Refactor Expectation::withArgs() method

    This commit refactor the Expectation::withArgs() method to make the code more readable, understandable and easier to maintain.
    To do that, it applies the "extract method" refactoring technique.