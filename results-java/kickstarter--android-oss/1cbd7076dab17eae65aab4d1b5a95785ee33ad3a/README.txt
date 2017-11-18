commit 1cbd7076dab17eae65aab4d1b5a95785ee33ad3a
Author: Christopher Wright <christopherwright@gmail.com>
Date:   Tue May 10 18:51:32 2016 -0400

    Reward itemization

    * Add item models

    * Add new colors for reward and mars

    * Add new reward view wip

    * Generate new id each time

    * Only show all gone if reward has not been selected

    * Reward items should probably always have an item via the API

    * Add better ids

    * wip

    * Add minimum, title

    * Show limit sections appropriately

    * Add outputs for center alignment of time/quantity limit

    * Add CoalesceTransformer

    * Add reward items to viewmodel

    * Add items recyclerview

    * Allow easier creation of rounded buttons with different colors

    * Add outputs for disabled state

    * Use white overlay instead of manually changing text color

    * Show minimum in title if reward title isn't present

    * misc

    * Extract item, quantity string

    * Organize colors

    * Fix tests relying on hardcoded ids

    * Trim whitespace

    * Fix lint checks

    * Use static import

    * Remove adapter properly

    Can't use the lifecycle because binding on destroy means it will
    never be called

    * Improve comment

    * Make headers invisible by default, visible in tools

    This way we don't see a flash of colors pop out on initial load

    * Allow static import of main thread

    * Alphabetize order

    * Add test for minimum title

    * Alphabetize

    * Fix static import

    * wip

    * Add back items hidden output

    * Make note about extracting strings

    * wip

    * wip

    * Hook up goToViewPledge for completed projects

    * Optimize imports

    * Add test for isBacked

    * Fix bad renaming during refactory

    * Move override annotation to same line

    * wip

    * Tighten spacing

    * Fix comment

    * Synchronize id generation method

    * Add docs, test for isCompleted

    * Use static import

    * Fix test

    * Add landscape view for reward delivery

    * Rename coalesceTransformer method to coalesce

    * Remove unused import

    * Swap goTo behavior subjects for publish subjects

    * Actually use the instantiated layout manager

    * Update strings

    * Remove explicit onNext

    * Add distinct until changed optimization