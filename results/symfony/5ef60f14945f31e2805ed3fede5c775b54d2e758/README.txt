commit 5ef60f14945f31e2805ed3fede5c775b54d2e758
Author: TeLiXj <telixj@gmail.com>
Date:   Wed Jan 29 22:47:07 2014 +0100

    [Translation] [Loader] Add INI_SCANNER_RAW to parse ini files

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | yes
    | Deprecations? | no
    | Tests pass?   | no
    | Fixed tickets |
    | License       | MIT
    | Doc PR        |

    INI_SCANNER_RAW change the default scanner mode of parse_ini_files to parse all values without evaluate. This allow values with single quotes, "no" and "false" and raise an error if you use the deprecated "#" as comment character.
    This change is specially good for shared translations, because a translator haven't to know that he can't use a few restricted terms.
    And has a residual improvement: it's twice fast that use the default value (INI_SCANNER_NORMAL) in my tests