commit 13ecf9092338c3cbbb7a721d00e2fb729919bbdf
Author: David Lim <dclim@users.noreply.github.com>
Date:   Mon Jun 5 14:26:25 2017 -0600

    Report Kafka lag information in supervisor status report (#4314)

    * refactor lag reporting and report lag at status endpoint

    * refactor offset reporting logic to fetch offsets periodically vs. at request time

    * remove JavaCompatUtils

    * code review changes

    * code review changes