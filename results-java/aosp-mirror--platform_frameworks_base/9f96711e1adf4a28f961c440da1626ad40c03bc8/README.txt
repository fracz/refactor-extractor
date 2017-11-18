commit 9f96711e1adf4a28f961c440da1626ad40c03bc8
Author: Xiaohui Chen <xiaohuic@google.com>
Date:   Thu Jan 7 14:14:06 2016 -0800

    sysui: refactor out PanelHolder

    PanelHolder seems obsolete for a long time.  Now PanelBar contains only
    one PanelView.  This simplifies the code a bit.

    Change-Id: Ic4da5d4ee72ffe4e36fa084371a8cd6fd102a9bd