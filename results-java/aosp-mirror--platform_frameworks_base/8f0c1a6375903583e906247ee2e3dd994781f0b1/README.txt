commit 8f0c1a6375903583e906247ee2e3dd994781f0b1
Author: Jason Monk <jmonk@google.com>
Date:   Fri May 9 09:53:08 2014 -0400

    Fix badness from proxy refactoring.

    When no PAC file getPacFileUrl() can return null now, which you
    cannot call toString() on.

    Change-Id: Ife00f641c2c17fbc1bde17017d9af59d23cb9182