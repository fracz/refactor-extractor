commit 7c3a3e11cfdff8b2f35917e92db14dffd0e1f8c7
Merge: d61f492 5ef60f1
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Feb 1 17:52:05 2014 +0100

    minor #10160 [Translation] [Loader] Add INI_SCANNER_RAW to parse ini files (TeLiXj)

    This PR was merged into the 2.5-dev branch.

    Discussion
    ----------

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

    Commits
    -------

    5ef60f1 [Translation] [Loader] Add INI_SCANNER_RAW to parse ini files