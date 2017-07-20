commit aa45a482837a98612a9126dac45a46e556ce6f27
Author: nevvermind <adragus@inviqa.com>
Date:   Fri Jan 22 13:48:29 2016 +0000

    Refactoring

    - changed "SPI" into something more familiar, like "implementation"
    - throw exceptions on invalid implementation types or invalid class names
    - use null instead of false when querying
    - refactored the tests accordingly