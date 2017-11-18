commit 445a11d1cf84853fd46af0cf6df4c38ad90683df
Author: lisa luo <luoser@users.noreply.github.com>
Date:   Mon Jun 19 14:56:26 2017 -0400

    [Messages] Mark as read (#116)

    * Modernize ViewPledgeViewModel

    * Add new button, clean up VM

    * add new output to start messages activity

    * Modernize tests, add test for new output

    * fix output and tweak margins

    * add feature flag

    * some new messages endpoints

    * mock endpoints

    * first sweep refactor

    * refactor thanks to TESTS <3

    * add tests for viewmodel intent configuration

    * audit logic for no messages, edit tests

    * add koala context for messages

    * Hide View pledge button on backer modal context; clean up tests

    * add new outputs and tests for proper back / close button logic

    * use appropriate transitions for different back buttons

    * swap text

    * Hook up messages' View Pledge button

    * fix layout ids and output type

    * hide messages button on backing screen if coming from messages activity

    * let's use .either() yaaaay and cleanup

    * add markAsRead endpoint

    * hook up output, add test

    * wip caching unread message count

    * make class for SharedPreferenceKeys

    * refactor caching logic for message unread state'

    * add new output to hide toolbar unread count when appropriate

    * refresh current user to properly display message count

    * add tests for unread card view states

    * fix tests