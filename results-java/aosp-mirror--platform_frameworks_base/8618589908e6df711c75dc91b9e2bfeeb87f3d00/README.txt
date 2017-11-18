commit 8618589908e6df711c75dc91b9e2bfeeb87f3d00
Author: Charlie Tsai <chartsai@google.com>
Date:   Fri May 5 11:51:47 2017 +0100

    Draw rectangle shadow fast in low elevation cases

    When the elevation is not high, the shadow is similar
    to just add some dark lines on the edges and corners of
    rectangle views. Using simple algorithm can also improve
    the performance and save more memory.

    Test: The render result of unit test is updated.
    Bug: 37906145

    Change-Id: I28758f868ee6e24e4552368bddfb7ac10fe0a205