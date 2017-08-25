commit 30a85c6f52785fd922e92e96745a6754d2502955
Author: Dan Horrigan <dan@dhorrigan.com>
Date:   Wed Dec 1 15:29:37 2010 -0500

    Updated how Config::get() and Config::set() to use switch statements and fall back to a loop so that it improves performance slightly.