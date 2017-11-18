commit c3320dbe1c7acf040a3ec895129d8aae09c570ea
Author: Mihai Preda <preda@google.com>
Date:   Mon May 18 20:15:32 2009 +0200

    Minor performance improvement when filtering intents by package.

    Don't consider the activities that have no intent filters.