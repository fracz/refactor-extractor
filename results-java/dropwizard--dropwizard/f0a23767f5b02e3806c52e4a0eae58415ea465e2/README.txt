commit f0a23767f5b02e3806c52e4a0eae58415ea465e2
Author: Jake Swenson <jakeswenson@users.noreply.github.com>
Date:   Tue Apr 12 08:59:54 2016 -0700

    Feature/improve command reuse and inheritability (#1515)

    * Improving command reuse by making versions of run not final, adding a way to configure the file argument further (like adding a default,) and adding a protected constructor to the server command to call it differently

    * adding comments