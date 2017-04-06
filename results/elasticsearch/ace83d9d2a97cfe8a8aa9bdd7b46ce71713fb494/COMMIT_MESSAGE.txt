commit ace83d9d2a97cfe8a8aa9bdd7b46ce71713fb494
Author: Jason Tedor <jason@tedor.me>
Date:   Mon Apr 18 21:43:48 2016 -0400

    Refactor UUID-generating methods out of Strings

    This commit refactors the UUID-generating methods out of Strings into
    their own class. The primary motive for this refactoring is to avoid a
    chain of class initializers from loading this class earlier than
    necessary. This was discovered when it was noticed that starting
    Elasticsearch without any active network interfaces leads to some
    logging statements being executed before logging had been
    initailized. Thus:
     - these UUID methods have no place being on Strings
     - removing them reduces spooky action-at-distance loading of this class
     - removed the troublesome, logging statements from MacAddressProvider,
       logging using statically-initialized instances of ESLogger are prone
       to this problem

    Relates #17837