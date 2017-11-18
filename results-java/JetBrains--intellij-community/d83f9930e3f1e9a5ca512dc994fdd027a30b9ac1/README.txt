commit d83f9930e3f1e9a5ca512dc994fdd027a30b9ac1
Author: irengrig <Irina.Chernushina@jetbrains.com>
Date:   Mon Feb 20 11:33:49 2017 +0100

    json schema - completion contributor to work in javascript files; webpack schema bundled,
    also some improvements to by-json-schema completion code:
    - more correctly get from context names of already used properties
    - insert comma if property is completed not in the end of the object