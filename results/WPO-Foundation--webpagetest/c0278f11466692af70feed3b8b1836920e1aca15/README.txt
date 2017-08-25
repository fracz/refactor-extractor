commit c0278f11466692af70feed3b8b1836920e1aca15
Author: Stefan Burnicki <stefan.burnicki@iteratec.de>
Date:   Wed Jun 22 10:47:59 2016 +0200

    refactor: Introduction of TestResults class to represent results

    The idea of this class is to encapsule $page_data and related functionality,
    so this can be tested and changed for multistep needs