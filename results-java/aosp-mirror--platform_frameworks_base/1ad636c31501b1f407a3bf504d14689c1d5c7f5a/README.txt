commit 1ad636c31501b1f407a3bf504d14689c1d5c7f5a
Author: Elliott Hughes <enh@google.com>
Date:   Thu Jul 1 16:51:48 2010 -0700

    Defer to ICU's knowledge of language-specific grammatical quantity rules.

    Also improve the documentation to make it a little less unclear what this
    is all about. In particular, explain why the original submitter's complaint
    about "zero" never being used in English, is expected behavior.

    Bug: 2663392
    Change-Id: Iade3b4f5c549ce01a95fd0e7e5c6ea394178eda3