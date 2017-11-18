commit aee3d7a5cc5e71ba6748011579a2863a0f2bc3ab
Author: lisa luo <luoser@users.noreply.github.com>
Date:   Thu May 11 14:38:24 2017 -0400

    [Messages] Reply to message thread (#96)

    * Modernize ProfileVM

    * Add initial ProfileVMTEst

    * Refactor VM again to make inputs testable; add tests

    * scaffold MessageThreads activity, layout, vm

    * Some new message related models

    * some new message routes

    * wip adapter / vieweHolder scaffolding

    * some initial card view msg thread rendering

    * add test and factories, cleanup a lil

    * wip refresher debugging

    * remove buggy refresh logic for now

    * Add more views, a VM, and tests

    * wip

    * Add unread count logic and tests

    * cleanup

    * temp string fix

    * lil more cleanup

    * thank you lint

    * hook up VH click to messages activity

    * A better way to hook up this interaction without Delegates

    * Lots more Messages scaffolding

    * Add new endpoint

    * add toolbar to layout, cleanup

    * add some logic and test it

    * initial stuff to get messages showing

    * some slight improvements

    * Add creator avatar logic and tests

    * Add some message formatting and tests

    * more design implementation and tests

    * get some collapsing toolbar action

    * some initial backing info logic

    * some edittext adjustments

    * Cleanup

    * Add tests

    * lint

    * creator -> participant

    * remove unneeded filter

    * don't refresh current user in VH

    * Initial send message logic

    * remove slide in animation

    * a little more styling, add initial delivery status view

    * lil layout improvements

    * lil alpha

    * Revert "lil layout improvements"

    This reverts commit cd1b2f3a8a2f7ac3dc1f78ab9a0052f8a8a3fb5c.

    * cleanup, lint

    * remove sent status text view for now

    * move reply layout to separate file

    * use onTextChanged

    * add error toast output and test

    * default expanded to false for now

    * Remove TODO for other message subjects for now

    they're not implemented yet so will update once they are

    * user better output name, setMessageEditText

    * Emit error string rather than bool for error output

    * oops, fix api methods in mocks

    * use new text color name