commit ddaf215b03cee506c907474578c950367b344062
Author: Isaac Salier-Hellendag <isaac@fb.com>
Date:   Wed Dec 10 11:32:53 2014 -0500

    Combine BeforeInput and Composition event plugins

    In order to improve support for Chinese and Japanese IME input in
    Internet Explorer, use a fallback composition state to determine
    inserted text. IE composition events are mostly okay, except for
    certain punctuation characters that are ignored. Using the fallback, we
    can detect these characters.

    The fallback is also useful for emitting `beforeInput` events, so it
    makes sense to simply combine these plugins.

    This change also incorporates a recent change to the Google Input Tools
    browser extension that exposes a `data` field via the `detail` object
    on the custom composition events it emits.