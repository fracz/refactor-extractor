commit b537565750f137bc3807102c11f0193fff34848f
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Thu Jul 1 19:01:31 2010 +0000

    Refs #660 Patch by Julien Moumne
     * removed {includeAssets type="css"} from
    plugins/CoreUpdater/templates/header.tpl and included required css
    (i.e. themes/default/styles.css)
     * minor changes in styles.css from CoreHome and index.tpl from
    widgetize to fix missing padding
     * size of add goal form fixed, an include was missing (goals.css)
     * refactored sparkline.js into a simple function and updated Goal,
    VisitsSummary, Referers, VisitFrequency.
     * added heuristic to avoid minifying already minified JS files

    git-svn-id: http://dev.piwik.org/svn/trunk@2413 59fd770c-687e-43c8-a1e3-f5a4ff64c105