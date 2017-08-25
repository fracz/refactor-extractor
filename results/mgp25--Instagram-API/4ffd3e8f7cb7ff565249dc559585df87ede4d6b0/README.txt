commit 4ffd3e8f7cb7ff565249dc559585df87ede4d6b0
Author: Abyr Valg <valga.github@abyrga.ru>
Date:   Sun May 21 00:54:31 2017 +0300

    RealtimeClient Implementation (#1105)

    Wow. It's finally here! Valga's incredibly awesome Realtime communication implementation!

    Squashing this pull request, since it consists of ~110 separate Work in Progress commits, which would blow up the repo history and make it unreadable.

    The whole history can be seen by viewing the pull request page:

    https://github.com/mgp25/Instagram-API/pull/1105

    Here is the list of merged commits:

    * introduce rtc

    * refactor rtc

    * cleaning up

    * rewrite websocket client

    * cleaning up

    * cleaning up

    * moar cleaning up

    * mqtt support

    * cleaning up

    * sync with 2.0 release

    * improve mqtt compatibility

    * luke, i am your styleci

    * refactor rtc

    * proxies support

    * use custom repository for pawl

    * styleci

    * fix dns lookup

    * use api_body_decode for patch op

    * keepalive for mqtt

    * handle connection timeout

    * use event emitter

    * make rtc more REACTish

    * line breaks for params

    * styleci

    * styleci

    * fix cookies handling

    * catch missing cookie error

    * add an example for rtc

    * styleci

    * save features for future use

    * check features for mqtt

    * sync mqtt features with 10.15.0

    * move some constants from mqtt to core

    * styleci

    * send commands and receive actions

    * styleci

    * fix indent

    * styleci!

    * use official package for net-mqtt-client-react

    * refactor everything

    * update examples after refactor

    * event handler for unsubscribed

    * rewrite patch processor

    * handler for notify

    * hotfix for notify

    * handler for live broadcasts

    * styleci

    * remove unneeded override

    * include live topic into ws client

    * simplify fb ua

    * thread seen event

    * thread activity event

    * screenshot notification

    * story update

    * story create

    * story badge

    * styleci

    * move CPU_ABI from Constants to GoodDevices

    * rewrite experiments handling

    * rename story to direct story

    * refactor path matcher

    * take mqtt_route from experiments

    * remove unused variable

    * send text messages to direct threads

    * rename item-ack to client-context-ack

    * styleci

    * choose best storage format for experiments

    * fix ws _sendCommand result

    * rename RTC to Realtime

    * realtimeHttp example

    * add react packages to composer.json, since we already use them indirectly

    * add hacks for 32bit platforms

    * handle must_refresh flag

    * handle auth errors for websocket client

    * styleci

    * remove magic const

    * styleci

    * improve logging

    * add patch op logger

    * use IDs for MQTT topics

    * check for human readable MQTT topics on receive

    * change mqtt keepalive to 900 seconds

    * make mqtt capabilities more readable

    * rename RealtimeClient to Realtime

    * styleci

    * add missing mqtt topic

    * add missing compression level

    * use unified json encoder

    * phpdoc

    * refactor realtimeHttp example

    * styleci

    * fix misplaced mqtt route

    * refactor experiments handling

    * sync with 10.22.0

    * check for subscription status in websocket client

    * add example for message sending

    * document usage

    * styleci

    * Do not require ext-event, it's rare and not needed

    We can't demand an unusual library like php-event. It stops most people from installing this library.

    I did a search. None of our code (in this pull request) uses any Event-classes from PHP's event library.

    So I am guessing the requirement was added by looking at something like react/event-loop. But that library only RECOMMENDS ext-event. It does not require it. And it looks for 3 different event handling systems and falls back to a PHP one if no extension is found.

    Moving non-essential extensions to "suggests" allows the package to be installed and will tell users that they CAN install the extension if they want to.

    * Oops, trailing comma...

    * Fix function arg formatting

    * Extend information about why CPU_ABI is correct

    * Randomize Mqtt magic strings