commit 3352d4fc3cf2c24f7e103cceb14938af7e3d5f40
Author: Spencer Rinehart <spencer.rinehart@dominionenterprises.com>
Date:   Thu Apr 17 21:36:49 2014 -0400

    Refactor getLinesToBeCovered to use simpler code from getLinesToBeUsed.

    These two methods were very similar, but took two very different
    approaches to how they were constructed.  The getLinesToBeUsed seemed
    nicer to work with (fewer regex's specific to this function), so I
    picked it as it the better of the two.

    I'm not aware of any differences in behavior introduced by this change,
    although the "TemplateMethods" bit referred to is something I am not
    familiar with.

    The main goal of this refactoring is to make it possible to increase
    functionality of both @covers and @uses without doing so in incompatible
    ways.  The common functionality should be able to be extracted out into
    a shared method to help with this.