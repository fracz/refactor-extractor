commit 4ace1ceb9cee28549c5da06c72699cdfd5b62e20
Author: lisa luo <luoser@users.noreply.github.com>
Date:   Tue Jun 27 17:36:12 2017 -0400

    [Messages] Toolbar UI updates (#123)

    * Enable send button when message body is present

    * xml updates

    * disable when message is posting

    * first naive pass at center timestamps

    * Refactor message view with isolated recipient/sender logic

    * first pass at delivery status textview

    * Add longDate to utils, add test

    * Refactor to group messages by start date

    * Add shortTime util, setup timestamp output

    * update strings and config from server

    * Use i18n string

    * remove tabled timestamp feature

    * update strings and config from server

    * lil merge refactor

    * add basic fade to expanded and collapsed titles

    * One-line collapsed message toolbar

    * RV tweaking

    * wip

    * take off pledge toolbar elevation

    * rv should wrap_content

    * Add ToolbarUtils for consistent fade effect

    * make new output for toolbar text