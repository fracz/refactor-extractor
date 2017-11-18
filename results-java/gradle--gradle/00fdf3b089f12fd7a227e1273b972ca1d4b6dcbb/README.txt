commit 00fdf3b089f12fd7a227e1273b972ca1d4b6dcbb
Author: Rene Groeschke <rene@breskeby.com>
Date:   Fri Mar 2 14:15:20 2012 +0100

    GRADLE-2104: refactored SFTPServer Test fixtures implementation to use apache sshd instead of j2ssh to
    - have a much more simple implementation
    - have better diagnosis options
    - fix threading issues with j2ssh
    - get faster test execution