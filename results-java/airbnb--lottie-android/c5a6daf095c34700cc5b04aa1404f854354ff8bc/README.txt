commit c5a6daf095c34700cc5b04aa1404f854354ff8bc
Author: Felipe Lima <felipe.lima@gmail.com>
Date:   Mon Feb 20 18:32:55 2017 -0800

    Pull out LottieComposition factory methods into inner class (#137)

    This is part of issue #3. Pulled static factory methods out of LottieComposition into a static inner class. Made all fields final and initialized them all in the constructor.
    This will make it easier for follow up refactors where we'll modularize the parsing logic and make it pluggable.
    Also upgraded the Espresso tests to JUnit 4 so it no longer uses the deprecated ActivityInstrumentationTestCase2