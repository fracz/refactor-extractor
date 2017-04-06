commit cc408ddd452318b0ff4e20129852ce2f88815ea3
Author: javanna <cavannaluca@gmail.com>
Date:   Mon Aug 31 10:00:09 2015 +0200

    [TEST] refactor DummyQueryBuilder and corresponding parser

    DummyQueryParser properly implements now fromXContent and the corresponding builder supports converting to the corresponding lucene query through toQuery method.