commit 4b2eac3de7e1dbf5c2dd742fd9989974a83972cb
Author: Dominic Gannaway <trueadm@users.noreply.github.com>
Date:   Wed Apr 5 16:47:29 2017 +0100

    Convert current build system to Rollup and adopt flat bundles (#9327)

    * WIP

    * fbjs support

    * WIP

    * dev/prod mode WIP

    * More WIP

    * builds a cjs bundle

    * adding forwarding modules

    * more progress on forwarding modules and FB config

    * improved how certain modules get inlined for fb and cjs

    * more forwarding modules

    * added comments to the module aliasing code

    * made ReactPerf and ReactTestUtils bundle again

    * Use -core suffix for all bundles

    This makes it easier to override things in www.

    * Add a lazy shim for ReactPerf

    This prevents a circular dependency between ReactGKJSModule and ReactDOM

    * Fix forwarding module for ReactCurrentOwner

    * Revert "Add a lazy shim for ReactPerf"

    This reverts commit 723b402c07116a70ce8ff1e43a1f4d92052e8f43.

    * Rename -core suffix to -fb for clarity

    * Change forwarding modules to import from -fb

    This is another, more direct fix for ReactPerf circular dependency

    * should fix fb and cjs bundles for ReactCurrentOwner

    * added provides module for ReactCurrentOwner

    * should improve console output

    * fixed typo with argument passing on functon call

    * Revert "should improve console output"

    This breaks the FB bundles.

    This reverts commit 65f11ee64f678c387cb3cfef9a8b28b89a6272b9.

    * Work around internal FB transform require() issue

    * moved  ReactInstanceMap out of React and into ReactDOM and ReactDOMFiber

    * Expose more internal modules to www

    * Add missing modules to Stack ReactDOM to fix UFI

    * Fix onlyChild module

    * improved the build tool

    * Add a rollup npm script

    * Rename ReactDOM-fb to ReactDOMStack-fb

    * Fix circular dependencies now that ReactDOM-fb is a GK switch

    * Revert "Work around internal FB transform require() issue"

    This reverts commit 0a50b6a90bffc59f8f5416ef36000b5e3a44d253.

    * Bump rollup-plugin-commonjs to include a fix for rollup/rollup-plugin-commonjs#176

    * Add more forwarding modules that are used on www

    * Add even more forwarding modules that are used on www

    * Add DOMProperty to hidden exports

    * Externalize feature flags

    This lets www specify them dynamically.

    * Remove forwarding modules with implementations

    Instead I'm adding them to react-fb in my diff.

    * Add all injection necessary for error logging

    * Add missing forwarding module (oops)

    * Add ReactART builds

    * Add ReactDOMServer bundle

    * Fix UMD build of ReactDOMFiber

    * Work in progress: start adding ReactNative bundle

    * tidied up the options for bundles, so they can define what types they output and exclude

    * Add a working RN build

    * further improved and tidied up build process

    * improved how bundles are built by exposing externals and making the process less "magical", also tidied up code and added more comments

    * better handling of bundling ReactCurrentOwner and accessing it from renderer modules

    * added NODE_DEV and NODE_PROD

    * added NPM package creation and copying into build chain

    * Improved UMD bundles, added better fixture testing and doc plus prod builds

    * updated internal modules (WIP)

    * removed all react/lib/* dependencies from appearing in bundles created on build

    * added react-test-renderer bundles

    * renamed bundles and paths

    * fixed fixture path changes

    * added extract-errors support

    * added extractErrors warning

    * moved shims to shims directory in rollup scripts

    * changed pathing to use build rather than build/rollup

    * updated release doc to reflect some rollup changes

    * Updated ReactNative findNodeHandle() to handle number case (#9238)

    * Add dynamic injection to ReactErrorUtils (#9246)

    * Fix ReactErrorUtils injection (#9247)

    * Fix Haste name

    * Move files around

    * More descriptive filenames

    * Add missing ReactErrorUtils shim

    * Tweak reactComponentExpect to make it standalone-ish in www

    * Unflowify shims

    * facebook-www shims now get copied over correctly to build

    * removed unnecessary resolve

    * building facebook-www/build is now all sync to prevent IO issues plus handles extra facebook-www src assets

    * removed react-native-renderer package and made build make a react-native build dir instead

    * 😭😭😭

    * Add more SSR unit tests for elements and children. (#9221)

    * Adding more SSR unit tests for elements and children.

    * Some of my SSR tests were testing for react-text and react-empty elements that no longer exist in Fiber. Fixed the tests so that they expect correct markup in Fiber.

    * Tweaked some test names after @gaearon review comment https://github.com/facebook/react/pull/9221#discussion_r107045673 . Also realized that one of the tests was essentially a direct copy of another, so deleted it.

    * Responding to code review https://github.com/facebook/react/pull/9221#pullrequestreview-28996315 . Thanks @spicyj!

    * ReactElementValidator uses temporary ReactNative View propTypes getter (#9256)

    * Updating packages for 16.0.0-alpha.6 release

    * Revert "😭😭😭"

    This reverts commit 7dba33b2cfc67246881f6d57633a80e628ea05ec.

    * Work around Jest issue with CurrentOwner shared state in www

    * updated error codes

    * splits FB into FB_DEV and FB_PROD

    * Remove deps on specific builds from shims

    * should no longer mangle FB_PROD output

    * Added init() dev block to ReactTestUtils

    * added shims for DEV only code so it does not get included in prod bundles

    * added a __DEV__ wrapping code to FB_DEV

    * added __DEV__ flag behind a footer/header

    * Use right haste names

    * keeps comments in prod

    * added external babel helpers plugin

    * fixed fixtures and updated cjs/umd paths

    * Fixes Jest so it run tests correctly

    * fixed an issue with stubbed modules not properly being replaced due to greedy replacement

    * added a WIP solution for ReactCurrentOwner on FB DEV

    * adds a FB_TEST bundle

    * allows both ReactCurrentOwner and react/lib/ReactCurrentOwner

    * adds -test to provides module name

    * Remove TEST env

    * Ensure requires stay at the top

    * added basic mangle support (disbaled by default)

    * per bundle property mangling added

    * moved around plugin order to try and fix deadcode requires as per https://github.com/rollup/rollup/issues/855

    * Fix flow issues

    * removed gulp and grunt and moved tasks to standalone node script

    * configured circleci to use new paths

    * Fix lint

    * removed gulp-extract-errors

    * added test_build.sh back in

    * added missing newline to flow.js

    * fixed test coverage command

    * changed permissions on test_build.sh

    * fixed test_html_generations.sh

    * temp removed html render test

    * removed the warning output from test_build, the build should do this instead

    * fixed test_build

    * fixed broken npm script

    * Remove unused ViewportMetrics shim

    * better error output

    * updated circleci to node 7 for async/await

    * Fixes

    * removed coverage test from circleci run

    * circleci run tets

    * removed build from circlci

    * made a dedicated jest script in a new process

    * moved order around of circlci tasks

    * changing path to jest in more circleci tests

    * re-enabled code coverage

    * Add file header to prod bundles

    * Remove react-dom/server.js (WIP: decide on the plan)

    * Only UMD bundles need version header

    * Merge with master

    * disabled const evaluation by uglify for <script></script> string literal

    * deal with ART modules for UMD bundles

    * improved how bundle output gets printed

    * fixed filesize difference reporting

    * added filesize dep

    * Update yarn lockfile for some reason

    * now compares against the last run branch built on

    * added react-dom-server

    * removed un-needed comment

    * results only get saved on full builds

    * moved the rollup sized plugin into a plugins directory

    * added a missing commonjs()

    * fixed missing ignore

    * Hack around to fix RN bundle

    * Partially fix RN bundles

    * added react-art bundle and a fixture for it

    * Point UMD bundle to Fiber and add EventPluginHub to exported internals

    * Make it build on Node 4

    * fixed eslint error with resolve being defined in outer scope

    * Tweak how build results are calculated and stored

    * Tweak fixtures build to work on Node 4

    * Include LICENSE/PATENTS and fix up package.json files

    * Add Node bundle for react-test-renderer

    * Revert "Hack around to fix RN bundle"

    We'll do this later.

    This reverts commit 59445a625962d7be4c7c3e98defc8a31f8761ec1.

    * Revert more RN changes

    We'll do them separately later

    * Revert more unintentional changes

    * Revert changes to error codes

    * Add accidentally deleted RN externals

    * added RN_DEV/RN_PROD bundles

    * fixed typo where RN_DEV and RN_PROD were the wrong way around

    * Delete/ignore fixture build outputs

    * Format scripts/ with Prettier

    * tidied up the Rollup build process and split functions into various different files to improve readability

    * Copy folder before files

    * updated yarn.lock

    * updated results and yarn dependencies to the latest versions