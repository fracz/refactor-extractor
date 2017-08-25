commit ad8b8dd62dab2e3116553a371158bf88e24c99ca
Author: Georges <Geolim4@users.noreply.github.com>
Date:   Fri Jul 1 19:54:55 2016 +0200

    Merge branch 'v5' into final (#299)

    * Starting V5

    * Updated roadmap with important info

    * Multiples improvements

    - Re-implement [in|de]crement methods in ExtendedCacheItemInterface
    - Added specific Exception if the driverCheck() has failed

    * - Updated examples
    - Updated readme
    - Re-added legacy autoload

    * Fixed missing return in autoload

    * Added exemple for driver ssdb with standalone lib

    * Cleaned up some code + indexed wrapper with constants

    * - Re-implement touch method in ExtendedCacheItemInterface: deprecated in profit CacheItemInterface::expiresAfter()
    - Added missing file headers
    - Updated interfaces extending

    * Rewrite stats method to be implemented in a driverStatistic() object, not a breaking change due to ArrayAccess use, but array use is deprecated

    * Added beautiful psr6 badge

    * Validated item on checklist

    * Improved driver name detection for throwed exceptions

    * Added item to the roadmap

    * Implemented Devnull driver (aka void)

    * Fixed namespace issue with Devnull driver

    * Fixed Redis settings

    * Added Couchbase driver

    * Removed useless flags on Couchbase driver

    * - Fixed increment/decrement params in interface
    - Added append/preprend method to the API

    * Fixed interace name issue

    * Checked apend/prepend roadmap checklist

    * Wrote couchbase example

    * Attempting to repair tests

    * Removed bin dir from gitignore

    * Set exec flag to bin file

    * Excluded composer dir of syntax checker

    * Definately fixed travis build, i guess so...

    * Added custom php.ini for tests

    * Replaced deprecated char in custom ini

    * Updated v5 roadmap

    * Added Exception to expireAt() if type is not DateTimeInterface

    * Added rawData methods to driverStatistic

    * Remove automatically expired item in case it fails on driver itself

    * Added Mongodb driver

    * Updated README.md

    * Added Devfalse and Devtrue driver

    * Updated README.md and added driver description

    * Updated README.md

    * Updated README.md

    * Updated README.md

    * Added cache extends example

    * Added Apcu driver

    * Full code cleaning

    * Grouped driver names in readme

    * Prepared ground for tags reimplementation

    * Added compression support for Memcache driver

    - Added config field: compress_data
    - If compress_data is set, enabled compression in Memcache driver

    * Improved getTtl() method + updated Memcached flag visibility

    * -Re-implemented tags features (as a replacement of global search)

    * -Re-implemented tags features (as a replacement of global search)

    * Fix auto driver

    * Never expire when time is set to 0

    * Updated phpDoc interface

    * Ohh common, there's not enough drivers :3
    - Added Leveldb driver

    * Added stub files for Leveldb driver (more IDE friendly)

    * Fixed bad behaviour with Leveldb\driver::driverClear()

    * Added Scrutinizer badge

    * Fixed typo

    * Added scrutinizer conf

    * Removed external coverage

    * Updated Roadmap + Cleaned up DriverAbstract of no-longer-used methods

    * Repaired travis build

    * Code clean up + Travis build restart

    * Removed another unused method

    * Removed unused argument

    * Adjusted codeclimate settings. They point out by-design features of phpFastCacche.

    * Fixed broken build

    * Added advanced tags features:

    - incrementItemsByTag($tagName, $step = 1) // Increment items by a tag
    - incrementItemsByTags(array $tagNames, $step = 1) // Increment items by some tags
    - decrementItemsByTag($tagName, $step = 1) // Decrement items by a tag
    - decrementItemsByTags(array $tagNames, $step = 1) // Decrement items by some tags
    - appendItemsByTag($tagName, $data) // Append items by a tag
    - appendItemsByTags(array $tagNames, $data) // Append items by some tags
    - prependItemsByTag($tagName, $data) // Prepend items by a tag
    - prependItemsByTags(array $tagNames, $data) // Prepend items by some tags

    * Repaired the build, for real this time.

    * Splitted up methods into traits to make the abstractDriver more "abstract"

    * Updated trait conception

    * $tmp property must be owned by PathSeekerTrait

    * $tmp property must be owned by PathSeekerTrait

    * Fixed phpDoc comment

    * Fixed short var + updated codeclimate settings

    * Fixed short var + updated codeclimate settings

    * Removed unused var

    * Excluded dir in codeclimate

    * Added bundle-based methods

    * Fixed cache issue with windows

    * Added dependency status badge

    * Fixed Leveldb destructor error

    * Protected some properties

    * Added getConfig() method

    * Updated psr6 badge

    * Fixed potential issue with Files Stats

    * Fixed phpDoc comments

    * Fixed Files stats

    * Fixed directory write issues

    * Fixed phpDoc comments

    * Fixed Symfony profiler crashing

    * Improved raw data for driver Files

    * Updated driverStatistic entity

    * Fixed SF exception

    * Interfaced getDriverName()

    * Fixed wrong itemInstance behaviour

    * Temporary removed linear magic methods

    * Improved driver statistics + FilePath issues fixes

    * Fixed wrong var type

    * Merged statements

    * Updated drivers statistics + added xcache stub

    * Fixed #286

    * Fixed isHit() behaviour

    * Protected driver-based methods

    * Re-enabled CacheManager warning

    * Not all driver clearing returns a boolean

    * Fixed Leveldb DriverClear notice

    * Mongodb sometimes does not return the Collstat size

    * Fixed Ssdb cache clear

    * Updated Wincache Info driver

    * Improved travis tests efficiency

    * Let's try the nightly !

    * Improved Sqlite driver check

    * Ps2 compliance + added php ext-intl suggestion

    * Removed duplicate in readme

    * Update README.md

    * Updated roadmap

    * Added phpFastCacheAbstractProxy

    * Short array syntax

    * Updated extends example with Proxy class

    * Updated phpDoc comments

    * https://github.com/PHPSocialNetwork/phpfastcache/commit/e3868acd3e90d4a085eac875483251806021457b#commitcomment-17673718

    * Added getDataAsJsonString() method to the Item API

    * Added basic API class

    * Added extended JSON methods to ItemPool API

    * Updated examples headers to includes MIT licence

    * Completed readme.md with missing API methods

    * Removed useless information on api in readme.md

    * Fixed Json formatting + tags behaviour

    * Updating trait insertion

    * Re-added clean() method and deprecated it in favor of cleanr().

    * Fixed return in deprecated alias

    * Deprecated CacheManager::setup() in favor of CacheManager::setDefaultConfig()

    * Re-implemented driver fallback

    * Re-ordered option in cache manager

    * Updated Readme

    * Specify the 7.1 for Travis

    * Specify the 7.1 for Travis (reverted from commit 8ae52bc7d1b04d512dcfd5334c9d9dabe706d36b)

    * Fixed typo

    * Fixed typo

    * Fixed #294

    * Fixed #294

    * Require the JSON extension as per new JSON methods

    * Var type adjustements