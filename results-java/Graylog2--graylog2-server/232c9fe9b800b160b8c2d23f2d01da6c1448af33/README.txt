commit 232c9fe9b800b160b8c2d23f2d01da6c1448af33
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Wed Apr 8 14:34:40 2015 +0200

    move user information into dropdown in navigation bar

    this replaces the "on/off" logout button

    refactored the top.scala.html template into a minimal navbar partial, actual content is done with react
    this is still missing some styling

    fixes #1200