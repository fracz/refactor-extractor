commit c1ee549d1d06008b90fe2fc8e75c274fa6c83162
Author: Jendrik Johannes <jendrik@gradle.com>
Date:   Fri Oct 6 14:33:26 2017 +0200

    Further improve binary compatibility check report

    - Allow all errors to be accepted via json file
    - @Deptrecated elements do not need @Incubating or @since
    - Improve error/accept messages in report to clarify that you
      can add annotations OR accept (for very good reasons only)