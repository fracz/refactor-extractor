commit 67cb6a3d933ca24c4a1d0d7d21f478fa55190341
Author: Stefan Burnicki <stefan.burnicki@iteratec.de>
Date:   Wed Jun 8 23:14:09 2016 +0200

    refactor: xmlResult run XML generation more flexible

    The code to generate the XML result for a run was moved along
    with the related functions into XmlResultGenerator.inc.

    Then the code is now encapsuled in a class and more dynamic,
    so it can generate the XML for first and repeat view.

    Dynamic generation of URLs and file paths was included.

    This will make upcoming multi-step changes much easier.

    The generated XML is very similar to the old XML.
    There are some minor changes for the cached view of each run:

      - new-line between `<rawData>` and `<headers>` tag
      - missing link for `optimization_report` as it seems to be deprecated
        and points to a file which is not even included in the git project
      - added `/cached/` suffix for `breakdown` and `domains`. These URLs
        aren't necessary, but supported by current rewrite rules and the
        target files.