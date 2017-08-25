commit 0b4d19c194da51814674c056d493fca83cd2319a
Author: Frank Chen <frrakn@users.noreply.github.com>
Date:   Tue Nov 22 16:59:43 2016 -0600

    Fix getUsernameId

    I don't understand the point of adding the JSONMapper. Kills code readability and introduces bugs like this.