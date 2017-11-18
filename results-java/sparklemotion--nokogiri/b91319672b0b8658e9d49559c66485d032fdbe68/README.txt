commit b91319672b0b8658e9d49559c66485d032fdbe68
Author: kares <self@kares.org>
Date:   Mon Jan 30 12:20:07 2017 +0100

    refactor ReaderNode internals -> not Cloneables + common Hash method

    ... from JRuby's RubyHash API so that we avoid all is1_9() checks