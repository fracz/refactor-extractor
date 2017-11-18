commit d402780a638da1662b0190b918a578c391ddeb01
Author: Bhargav Mogra <bhargav521@gmail.com>
Date:   Tue Nov 7 10:38:12 2017 +0700

    Use long value so that cache sizes greater than 2Gigabytes can be supported. (#2558)

    * Use long value so that cache sizes greater than 2Gigabytes can be supported
    resign

    * refactor to long diskcache sizes