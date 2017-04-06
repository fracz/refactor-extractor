commit a96c76dcbd082fd01e29099e8f64becd5f299966
Author: javanna <cavannaluca@gmail.com>
Date:   Tue Sep 6 21:55:32 2016 +0200

    Remove FetchSubPhaseParseElement

    With the search refactoring we don't use SearchParseElement anymore to define our own parsing code but only for plugins. There was an abstract subclass called FetchSubPhaseParseElement in our production code, only used in one of our tests. We can remove that abstract class as it is not needed and not that useful for the test that depends on it.