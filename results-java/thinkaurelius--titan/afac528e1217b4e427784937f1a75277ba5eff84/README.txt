commit afac528e1217b4e427784937f1a75277ba5eff84
Author: Matthias Broecheler <me@matthiasb.com>
Date:   Fri Apr 10 17:01:27 2015 -0700

    Finished refactoring of Titan tests for TP3M8 and associated debugging of TitanGraphTest and TitanOLAPTest.
    There were a number of quirks to work out. A noteworthy one is the fact that TinkerPop defaults vertex property cardinality to single when adding a property whereas Titan uses the schema to determine the cardinality. For compatibility, I added a config option so that the default behavior could be configured to match TinkerPop's.