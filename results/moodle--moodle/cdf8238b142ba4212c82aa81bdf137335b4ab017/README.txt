commit cdf8238b142ba4212c82aa81bdf137335b4ab017
Author: Matteo Scaramuccia <moodle@matteoscaramuccia.com>
Date:   Sun Jan 8 21:29:47 2017 +0100

    MDL-57379 Files: Improved file argument evaluation.

    get_file_argument() is responsible to extract the relative path
    of the file to be served by a specific Moodle component
    like a theme or a module.
    Some modules like scorm and imscp require slasharguments support
    and they force it when creating the URLs to serve their files.
    It should honor the slasharguments setting but this could break
    those instances where existing hard-coded links still make usage of
    the "old" format, the one when slasharguments is set to No i.e. '?file='.

    Its logic has been improved by looking at when the URL is related to
    serving a plug-in file in a "forced" slasharguments way of serving it
    i.e. using '/pluginfile.php/' and not '/pluginfile.php?'.