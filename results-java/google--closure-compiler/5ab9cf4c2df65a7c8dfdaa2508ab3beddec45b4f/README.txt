commit 5ab9cf4c2df65a7c8dfdaa2508ab3beddec45b4f
Author: jakubvrana <jakubvrana@google.com>
Date:   Thu May 18 10:24:43 2017 -0700

    Infer tag name from type in BanCreateDom.

    This CL improves the check in two situations:

    1. If goog.dom.TagName is aliased, e.g.:

    const TagName = goog.require('goog.dom.TagName');
    goog.dom.createDom(TagName.SCRIPT);

    2. If the tag name is not used as a literal, e.g.:

    var tag = goog.dom.TagName.SCRIPT;
    goog.dom.createDom(tag);

    This second situation is not very common but this CL prepares the ground for addressing this pattern which occurs in real code:

    var tag = foo ? goog.dom.TagName.LABEL : goog.dom.TagName.SPAN;
    goog.dom.createDom(tag);

    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=156447058