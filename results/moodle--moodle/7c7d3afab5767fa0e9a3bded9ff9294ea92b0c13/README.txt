commit 7c7d3afab5767fa0e9a3bded9ff9294ea92b0c13
Author: Petr Skoda <skodak@moodle.org>
Date:   Sat Nov 7 10:27:57 2009 +0000

    MDL-20766 message_send() used intead of events, it will enable us to improve performance and solve db transactions issues independedntly from events, hopefully this will give us more options in future too because the events are designed specifically for communication "moodle-->external systems"