commit 5df281a570555ee132a12a4aac9aec96e8618270
Author: Jason Monk <jmonk@google.com>
Date:   Fri May 9 09:53:08 2014 -0400

    Fix badness from proxy refactoring.

    When no PAC file getPacFileUrl() can return null now, which you
    cannot call toString() on.

    Change-Id: Ife00f641c2c17fbc1bde17017d9af59d23cb9182