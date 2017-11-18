commit 73b8aad9a377a396c2323fa146ea6bd03162578a
Author: Ryan Ernst <ryan@iernst.net>
Date:   Fri Apr 7 14:18:06 2017 -0700

    Settings: Disallow secure setting to exist in normal settings (#23976)

    This commit removes the "legacy" feature of secure settings, which setup
    a parallel setting that was a fallback in the insecure
    elasticsearch.yml. This was previously used to allow the new secure
    setting name to be that of the old setting name, but is now not in use
    due to other refactorings. It is much cleaner to just have all secure
    settings use new setting names. If in the future we want to reuse the
    previous setting name, once support for the insecure settings have been
    removed, we can then rename the secure setting.  This also adds a test
    for the behavior.