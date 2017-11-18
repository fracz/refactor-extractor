commit 99b68d19dfbdf5bf3fe58b2a3ce293dfe46a4b40
Author: lisa luo <luoser@users.noreply.github.com>
Date:   Fri Jun 16 15:24:06 2017 -0400

    [Messages] Add backing & project logic to messages (#113)

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

    * add comment