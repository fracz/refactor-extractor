commit 605a533d12a7f905b2aa9ae17cb25420a23942bf
Merge: 598d85c 3466601
Author: Romain Neutron <imprec@gmail.com>
Date:   Mon Jul 7 08:58:47 2014 +0200

    minor #11337 [Process] fix the return types (xabbuh)

    This PR was merged into the 2.5 branch.

    Discussion
    ----------

    [Process] fix the return types

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets |
    | License       | MIT
    | Doc PR        |

    Commits
    -------

    3466601 fix the return types
    6a41ca0 minor #11271 [Validator] Added missing Slovak translations (pulzarraider)
    585a045 Added SK translations
    a22858b Merge branch '2.4' into 2.5
    5b2e34f Merge branch '2.3' into 2.4
    85af997 bug #11259 [Config] Fixed failed config schema loads due to libxml_disable_entity_loader usage (ccorliss)
    de2bef5 Fixed failed config schema loads due to libxml_disable_entity_loader usage.
    8a68e6c bug #11234 [ClassLoader] fixed PHP warning on PHP 5.3 (fabpot)
    3b9902a enabled PHP 5.6 for tests
    cd7fe02 bug #11179 [Process] Fix ExecutableFinder with open basedir (cs278)
    b8f8c0e [Process] Fix ExecutableFinder with open basedir
    fa2d337 bug #11242 [CssSelector] Refactored the CssSelector to remove the circular object graph (stof)
    994f81f Refactored the CssSelector to remove the circular object graph
    1045adf bug #11219 [DomCrawler] properly handle buttons with single and double quotes insid... (xabbuh)
    84be8de minor #11230 Fix mocks to support >=5.5.14 and >=5.4.30 (jpauli)
    1c5c694 Fix mocks to support >=5.5.14 and >=5.4.30
    7b2e3d9 [ClassLoader] fixed PHP warning on PHP 5.3
    7b0ed91 minor #11225 [Validator] added Lithuanian translation for empty file (Tadcka)
    a954083 [Validator] added Lithuanian translation for empty file
    803b06b bug #11220 [Components][Serializer] optional constructor arguments can be omitted during the denormalization process (xabbuh)
    05c51f5 minor #11203 Added missing dutch translations (WouterJ)
    bd9283e Added missing dutch translations
    5bb2345 [Components][Serializer] optional constructor arguments can be omitted during the denormalization process
    cbbdbe4 [DomCrawler] properly handle buttons with single and double quotes inside the name attribute
    f6eb9b6 minor #11201 [Validator] Added missing pt and pt_BR translations (dcsg)
    71a2b59 Added missing pt and pt_BR translations
    4450197 Merge branch '2.4' into 2.5
    0067952 minor #11195 [Validator] Add missing ru translations (megazoll)
    71eb8a8 [Validator] Add missing ru translations
    f45f2df minor #11191 [Tests] fix tests due to recent changes in PHP's behavior (xabbuh)
    bc8042d don't disable constructor calls to mockups of classes that extend internal PHP classes
    f4a3c7a special handling for the JsonDescriptor to work around changes in PHP's JSON pretty printer
    76d3c9e Merge branch '2.4' into 2.5
    f2bdc22 fixed previous merge
    b387477 Merge branch '2.3' into 2.4
    eeeae94 minor #11187 [Tests] don't disable constructor calls to mockups of classes that extend intern... (xabbuh)
    ff00dcc bug #11186 Added missing `break` statement (apfelbox)
    5af2802 Added missing `break` statement
    2c726b8 don't disable constructor calls to mockups of classes that extend internal PHP classes
    96bc061 minor #11182 Small comment update according to PSR-2 (apfelbox)
    31b1dff Small comment update according to PSR-2
    a50aca0 bug #11168 [YAML] fix merge node (<<) (Tobion)
    cd0309f minor #11162 [Console] remove weird use statement (cordoval)
    dee1562 [Yaml] fix overwriting of keys after merged map
    8c621ab [Yaml] fix priority of sequence merges according to spec
    bebd18c bug #11170 [Console] Fixed notice in QuestionHelper (florianv)
    7d4f4f2 bug #11169 [Console] Fixed notice in DialogHelper (florianv)
    9fe4b88 [Console] Fixed notice in QuestionHelper
    ff6c65e [Console] Fixed notice in DialogHelper
    02614e0 [Yaml] refactoring of merges for performance
    c117e8e [Console] remove weird use statement
    fbf92e5 bug #11144 [HttpFoundation] Fixed Request::getPort returns incorrect value under IPv6 (kicken)
    bd11e92 minor #11136 [Filesystem] Fix test suite on OSX (romainneutron)
    2a0e8e3 [HttpFoundation] Fixed Request::getPort returns incorrect value under IPv6
    e26f08e [Filesystem] Fix test suite on OSX
    185aafa minor #11077 [TwigBundle] [Tests] Add framework-bundle (clemens-tolboom)
    a12471d Add framework-bundle