commit 153fefb52baabcf51078c21e2584a7b359d942dc
Author: Michal Čihař <michal@cihar.com>
Date:   Wed Jun 28 12:06:03 2017 +0200

    Improved rendering of server variables table

    Several improvements to make it look better on various screen sizes:

    - remove fixed layout of table, that makes it consume too much space for
      some fields
    - remove using float inside the table, it is not necessary
    - make the values left aligned
    - allow to wrap long values

    Fixes #12899

    Signed-off-by: Michal Čihař <michal@cihar.com>