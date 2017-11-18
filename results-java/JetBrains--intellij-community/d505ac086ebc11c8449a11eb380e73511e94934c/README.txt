commit d505ac086ebc11c8449a11eb380e73511e94934c
Author: irengrig <Irina.Chernushina@jetbrains.com>
Date:   Wed Jan 11 16:37:46 2017 +0100

    json schema: fix some external references navigation (both for property names into schema and from $ref)
    refactor resolve logic so that it is more clear
    this fixes WEB-24965 JSON Schema: navigation from property names in package.json to package json schema does not work
    and WEB-21364 JSON Schema:Unable to create relative references to local files