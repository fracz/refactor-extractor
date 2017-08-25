commit ade03c067ad47e4ba730cbaa3b08353d0660b506
Author: David Mudrák <david@moodle.com>
Date:   Thu Nov 19 14:14:16 2015 +0100

    MDL-52214 core: Fix case sensitivity in user agent comparison

    The previous 2.9 implementation of is_web_crawler() used stripos() in
    certain cases. The unit tests re-added in the previous commit revealed
    that certain crawlers (such as BaiDuSpider) were not correctly detected
    in the new refactored implementation.

    It seems lesser evil and safe enough to use /i in the regex search even
    though it is not 100% same logic as before - as stripos() was used in
    some cases only, not always.