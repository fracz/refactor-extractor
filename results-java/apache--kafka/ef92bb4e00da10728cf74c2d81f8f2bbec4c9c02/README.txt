commit ef92bb4e00da10728cf74c2d81f8f2bbec4c9c02
Author: Eno Thereska <eno.thereska@gmail.com>
Date:   Wed Mar 1 14:36:08 2017 -0800

    MINOR: Minor reduce unnecessary calls to time.millisecond (part 2)

    Avoid calling time.milliseconds more often than necessary. Cleaning and committing logic can use the timestamp at the start of the loop with minimal consequences. 5-10% improvements noticed with request rates of 450K records/second.

    Also tidy up benchmark code a bit more.

    Author: Eno Thereska <eno.thereska@gmail.com>
    Author: Eno Thereska <eno@confluent.io>

    Reviewers: Matthias J. Sax, Damian Guy, Guozhang Wang

    Closes #2603 from enothereska/minor-reduce-milliseconds2