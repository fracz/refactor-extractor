commit 862ca7dbc1e561506ad0551b71664a968f21a5cb
Author: Frederic Massart <fred@moodle.com>
Date:   Fri Oct 25 14:54:33 2013 +0800

    MDL-41041 portfolio: Temporary solution for event portfolio_send

    As we have deprecated events_trigger(), the portfolio cannot use it
    any more. Though there is no easy solution to get rid of this event
    as it is used to register a cron job. MDL-42541 has been raised to
    refactor the portfolio code to solve this problem. In the meantime
    events_trigger_legacy() will be used.