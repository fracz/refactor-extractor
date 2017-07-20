commit 4f0e655a7c9fade57851bb539db285c8f88ae35c
Author: Benaka Moorthi <benaka.moorthi@gmail.com>
Date:   Wed Jul 10 19:21:08 2013 -0400

    Refs #4041, allow subtable template to be used if idSubtable is in request so Actions controller doesn't have to call setTemplate. Also did some mild refactoring for Piwik_ViewDataTable_HtmlTable::setRecursiveLoadDataTableIfSearchingForPattern.