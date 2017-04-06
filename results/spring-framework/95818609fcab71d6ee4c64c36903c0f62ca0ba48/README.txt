commit 95818609fcab71d6ee4c64c36903c0f62ca0ba48
Author: Rossen Stoyanchev <rstoyanchev@pivotal.io>
Date:   Thu Dec 15 17:45:17 2016 -0500

    Minor refactoring

    Consolidate into one method potentially re-using UriComponentsBuilder
    for the location. Also use StringUtils#applyRelativePath.

    Issue: SPR-15020