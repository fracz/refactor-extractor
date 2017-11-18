commit 3df9d3543608927c452979633dfe623858d4a9d2
Author: Matthias Broecheler <me@matthiasb.com>
Date:   Sun May 4 20:20:11 2014 -0700

    Refactored time management for Titan. Timepoint now has a reference to the TimestampProvider it was derived from. The serializers were removed and a default constructor added so that serialization is no longer tied to Kryo. Timepoint was refactored into an interface and some convenience methods added.
    In transaction configuration, there is a transaction start time and a commit time - the latter being configurable.