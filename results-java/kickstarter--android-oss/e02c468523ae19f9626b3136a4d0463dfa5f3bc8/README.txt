commit e02c468523ae19f9626b3136a4d0463dfa5f3bc8
Author: lisa luo <luoser@users.noreply.github.com>
Date:   Mon Sep 18 15:41:08 2017 -0400

    [Dashboard] Creator messages (#159)

    * Hook up dashboard message button click, add test, cleanups

    * start message threads activity wip

    * Make tests better w logged in user

    * Add member data to project model

    * Hide messages button for non-creators

    * refresh project or user properly to display correct unread message counts

    * fix message edit hint, add test

    * cleanup, correct hint to BehaviorSubject

    * Refactor BackingActivity to take optional User

    * Update project share event, add share context

    * modernize ProjectVMTests, add coverage for all outputs

    * instantiate VM tests with Backer model, as per VM refactor

    * use IdFactory for user id

    * comment about data from intent, fix test

    * Specify creator id in UpdateFactory for consistent tests

    hard-coded so IdFactory doesn't override within one test