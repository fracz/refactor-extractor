commit df80e4905509c0190cebfbc64812b95e14b4e375
Author: Christopher Wright <christopherwright@gmail.com>
Date:   Thu May 12 20:45:21 2016 -0400

    View pledge itemization

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

    * Namespace ids in view pledge view

    * Remove use of view() in viewmodel

    * Move backer name to output

    * Add project name, creator name outputs

    * Add project photo, sequence

    * Add pledge info, backer avatar

    * Remove unused import

    * Add backing status

    * Add reward minimum and description

    * Swap goTo behavior subjects for publish subjects

    * Add shipping location

    * Add shipping info

    * Remove legacy backing observable

    * Add back click

    * Remove unused imports

    * Actually use the instantiated layout manager

    * Add reward items to view pledge

    * Update strings

    * Remove explicit onNext

    * Add distinct until changed optimization

    * wip

    * Add test for isShippable

    * Namespace project context layout ids

    * Add static import for nevererror

    * Use zip instead of combineLatest

    * Add missing bindToLifecycle calls