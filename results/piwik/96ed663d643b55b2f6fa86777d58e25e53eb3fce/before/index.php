<?php
?><!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>piwik.js: Unit Tests</title>
<?php
require_once(dirname(__FILE__).'/SQLite.php');

if(file_exists("stub.tpl")) {
    echo file_get_contents("stub.tpl");
}
?>
 <script type="text/javascript">
function getToken() {
    return "<?php $token = md5(uniqid(mt_rand(), true)); echo $token; ?>";
}
<?php
$sqlite = false;
if (file_exists("enable_sqlite")) {
    if (class_exists('SQLite')) {
        $sqlite = true;
    }
}

if(!$sqlite) {
    echo 'alert("WARNING: Javascript integration tests require sqlite, \n1) ensure this PHP extension is enabled to make sure you run all tests \napt-get install php5-sqlite \n2) Then please create an empty file enable_sqlite in tests/javascript/enable_sqlite \n3) Re-execute this page and make sure this popup does not display ");';
}
if ($sqlite) {
  echo '
var _paq = _paq || [];

function testCallingTrackPageViewBeforeSetTrackerUrlWorks() {
    _paq.push(["setCustomData", { "token" : getToken() }]);
    _paq.push(["trackPageView", "Asynchronous Tracker ONE"]);
    _paq.push(["setSiteId", 1]);
    _paq.push(["setTrackerUrl", "piwik.php"]);
}

function testTrackPageViewAsync() {
    _paq.push(["trackPageView", "Asynchronous tracking TWO"]);
}

testCallingTrackPageViewBeforeSetTrackerUrlWorks();
testTrackPageViewAsync();

';
}
?>
 </script>
 <script src="../../js/piwik.js" type="text/javascript"></script>
 <script src="../../plugins/Overlay/client/urlnormalizer.js" type="text/javascript"></script>
 <script src="piwiktest.js" type="text/javascript"></script>
 <link rel="stylesheet" href="assets/qunit.css" type="text/css" media="screen" />
 <link rel="stylesheet" href="jash/Jash.css" type="text/css" media="screen" />
 <script src="../../libs/jquery/jquery.js" type="text/javascript"></script>
 <script src="assets/qunit.js" type="text/javascript"></script>
 <script src="jslint/jslint.js" type="text/javascript"></script>
 <script type="text/javascript">
function _e(id){
    if (document.getElementById)
        return document.getElementById(id);
    if (document.layers)
        return document[id];
    if (document.all)
        return document.all[id];
}

function loadJash() {
    var jashDiv = _e('jashDiv');

    jashDiv.innerHTML = '';
    document.body.appendChild(document.createElement('script')).src='jash/Jash.js';
}

function dropCookie(cookieName, path, domain) {
    var expiryDate = new Date();

    expiryDate.setTime(expiryDate.getTime() - 3600);
    document.cookie = cookieName + '=;expires=' + expiryDate.toGMTString() +
        ';path=' + (path ? path : '') +
        (domain ? ';domain=' + domain : '');
    document.cookie = cookieName + ';expires=' + expiryDate.toGMTString() +
        ';path=' + (path ? path : '') +
        (domain ? ';domain=' + domain : '');
}

function deleteCookies() {
    // aggressively delete cookies

    // 1. get all cookies
    var
        cookies = (document.cookie).split(';'),
        aCookie,
        cookiePattern = new RegExp('^ *([^=]*)='),
        cookieMatch,
        cookieName,
        domain,
        domains = [],
        path,
        paths = [];

    cookies.push( '=' );

    // 2. construct list of domains
    domain = document.domain;
    if (domain.substring(0, 1) !== '.') {
        domain = '.' + domain;
    }
    domains.push( domain );
    while ((i = domain.indexOf('.')) >= 0) {
        domain = domain.substring(i+1);
        domains.push( domain );
    }
    domains.push( '' );
    domains.push( null );

    // 3. construct list of paths
    path = window.location.pathname;
    while ((i = path.lastIndexOf('/')) >= 0) {
        paths.push(path + '/');
        paths.push(path);
        path = path.substring(0, i);
    }
    paths.push( '/' );
    paths.push( '' );
    paths.push( null );

    // 4. iterate through cookies
    for (aCookie in cookies) {
        if (Object.prototype.hasOwnProperty.call(cookies, aCookie)) {

            // 5. extract cookie name
            cookieMatch = cookiePattern.exec(cookies[aCookie]);
            if (cookieMatch) {
                cookieName = cookieMatch[1];

                // 6. iterate through domains
                for (i = 0; i < domains.length; i++) {

                    // 7. iterate through paths
                    for (j = 0; j < paths.length; j++) {

                        // 8. drop cookie
                        dropCookie(cookieName, paths[j], domains[i]);
                    }
                }
            }
        }
    }
}

var contentTestHtml = {};

function setupContentTrackingFixture(name) {
    var url = 'content-fixtures/' + name + '.html'

    if (!contentTestHtml[name]) {
        $.ajax({
            url: url,
            success: function( content ) { contentTestHtml[name] = content; },
            dataType: 'html',
            async: false
        });
    }

    $('#other #content').remove();
    $('#other').append($('<div id="contenttest">' + contentTestHtml[name] + '</div>'));
}

 </script>
</head>
<body>
<div style="display:none;"><a id="firstLink" href="http://piwik.org/qa">First anchor link</a></div>

 <h1 id="qunit-header">piwik.js: Unit Tests</h1>
 <h2 id="qunit-banner"></h2>
 <div id="qunit-testrunner-toolbar"></div>
 <h2 id="qunit-userAgent"></h2>

 <div id="other" style="display:none;">
  <div id="div1"></div>
  <iframe name="iframe2"></iframe>
  <iframe name="iframe3"></iframe>
  <iframe name="iframe4"></iframe>
  <iframe name="iframe5"></iframe>
  <iframe name="iframe6"></iframe>
  <iframe name="iframe7"></iframe>
  <img id="image1" src=""/> <!-- Test require this empty source attribute before image2!! -->
  <img id="image2" data-content-piece src="img.jpg"/>
  <ul>
    <li><a id="click1" href="javascript:_e('div1').innerHTML='&lt;iframe src=&quot;http://click.example.com&quot;&gt;&lt;/iframe&gt;';void(0)" class="clicktest">ignore: implicit (JavaScript href)</a></li>
    <li><a id="click2" href="http://example.org" target="iframe2" class="piwik_ignore clicktest">ignore: explicit</a></li>
    <li><a id="click3" href="example.php" target="iframe3" class="clicktest">ignore: implicit (localhost)</a></li>
    <li><a id="click4" href="http://example.net" target="iframe4" class="clicktest">outlink: implicit (outbound URL)</a></li>
    <li><a id="click5" href="example.html" target="iframe5" class="piwik_link clicktest">outlink: explicit (localhost)</a></li>
    <li><a id="click6" href="example.pdf" target="iframe6" class="clicktest">download: implicit (file extension)</a></li>
    <li><a id="click7" href="example.word" target="iframe7" class="piwik_download clicktest">download: explicit</a></li>
    <li><a id="click8" href="example.exe" target="iframe8" class="clicktest">no click handler</a></li>
  </ul>
  <div id="clickDiv"></div>
 </div>

 <ol id="qunit-tests"></ol>

 <div id="main" style="display:none;"></div>

 <script>
var hasLoaded = false;
function PiwikTest() {
    hasLoaded = true;

    module('externals');

    test("JSLint", function() {
        expect(1);
        var src = '<?php
            $src = file_get_contents('../../js/piwik.js');
            $src = strtr($src, array('\\'=>'\\\\',"'"=>"\\'",'"'=>'\\"',"\r"=>'\\r',"\n"=>'\\n','</'=>'<\/'));
            echo "$src"; ?>';
        ok( JSLINT(src), "JSLint" );
//      alert(JSLINT.report(true));
    });

    test("JSON", function() {
        expect(49);

        var tracker = Piwik.getTracker(), dummy;

        equal( typeof JSON2.stringify, 'function', 'JSON.stringify function' );
        equal( typeof JSON2.stringify(dummy), 'undefined', 'undefined' );

        equal( JSON2.stringify(null), 'null', 'null' );
        equal( JSON2.stringify(true), 'true', 'true' );
        equal( JSON2.stringify(false), 'false', 'false' );
        ok( JSON2.stringify(0) === '0', 'Number 0' );
        ok( JSON2.stringify(1) === '1', 'Number 1' );
        ok( JSON2.stringify(-1) === '-1', 'Number -1' );
        ok( JSON2.stringify(42) === '42', 'Number 42' );

        ok( JSON2.stringify(1.0) === '1.0'
            || JSON2.stringify(1.0) === '1', 'float 1.0' );

        equal( JSON2.stringify(1.1), '1.1', 'float 1.1' );
        equal( JSON2.stringify(""), '""', 'empty string' );
        equal( JSON2.stringify('"'), '"' + '\\' + '"' + '"', 'string "' );
        equal( JSON2.stringify('\\'), '"' + '\\\\' + '"', 'string \\' );

        equal( JSON2.stringify("1"), '"1"', 'string "1"' );
        equal( JSON2.stringify("ABC"), '"ABC"', 'string ABC' );
        equal( JSON2.stringify("\x40\x41\x42\x43"), '"@ABC"', '\\x hex string @ABC' );

        ok( JSON2.stringify("\u60a8\u597d") == '"您好"'
            || JSON2.stringify("\u60a8\u597d") == '"\\u60a8\\u597d"', '\\u Unicode string 您好' );

        ok( JSON2.stringify("ßéàêö您好") == '"ßéàêö您好"'
            || JSON2.stringify("ßéàêö您好") == '"\\u00df\\u00e9\\u00e0\\u00ea\\u00f6\\u60a8\\u597d"', 'string non-ASCII text' );

        equal( JSON2.stringify("20060228T08:00:00"), '"20060228T08:00:00"', 'string "20060228T08:00:00"' );

        var d = new Date();
        d.setTime(1240013340000);
        ok( JSON2.stringify(d) === '"2009-04-18T00:09:00Z"'
            || JSON2.stringify(d) === '"2009-04-18T00:09:00.000Z"', 'Date');

        equal( JSON2.stringify([1, 2, 3]), '[1,2,3]', 'Array of numbers' );
        equal( JSON2.stringify({'key' : 'value'}), '{"key":"value"}', 'Object (members)' );
        equal( JSON2.stringify(
            [ {'domains' : ['example.com', 'example.ca']},
            {'names' : ['Sean', 'Cathy'] } ]
        ), '[{"domains":["example.com","example.ca"]},{"names":["Sean","Cathy"]}]', 'Nested members' );

        equal( typeof eval('('+dummy+')'), 'undefined', 'eval undefined' );

        equal( typeof JSON2.parse, 'function', 'JSON.parse function' );

        // these throw a SyntaxError
//      equal( typeof JSON2.parse('undefined'), 'undefined', 'undefined' );
//      equal( typeof JSON2.parse(dummy), 'undefined', 'undefined' );
//      equal( JSON2.parse('undefined'), dummy, 'undefined' );
//      equal( JSON2.parse('undefined'), undefined, 'undefined' );

        strictEqual( JSON2.parse('null'), null, 'null' );
        strictEqual( JSON2.parse('true'), true, 'true' );
        strictEqual( JSON2.parse('false'), false, 'false' );

        equal( JSON2.parse('0'), 0, 'Number 0' );
        equal( JSON2.parse('1'), 1, 'Number 1' );
        equal( JSON2.parse('-1'), -1, 'Number -1' );
        equal( JSON2.parse('42'), 42, 'Number 42' );

        ok( JSON2.parse('1.0') === 1.0
            || JSON2.parse('1.0') === 1, 'float 1.0' );

        equal( JSON2.parse('1.1'), 1.1, 'float 1.1' );
        equal( JSON2.parse('""'), "", 'empty string' );
        equal( JSON2.parse('"' + '\\' + '"' + '"'), '"', 'string "' );
        equal( JSON2.parse('"\\\\"'), '\\', 'string \\' );

        equal( JSON2.parse('"1"'), "1", 'string "1"' );
        equal( JSON2.parse('"ABC"'), "ABC", 'string ABC' );
        equal( JSON2.parse('"@ABC"'), "\x40\x41\x42\x43", 'Hex string @ABC' );

        ok( JSON2.parse('"您好"') == "\u60a8\u597d"
            && JSON2.parse('"\\u60a8\\u597d"') == "您好", 'Unicode string 您好' );

        ok( JSON2.parse('"ßéàêö您好"') == "ßéàêö您好"
            && JSON2.parse('"\\u00df\\u00e9\\u00e0\\u00ea\\u00f6\\u60a8\\u597d"') == "ßéàêö您好", 'string non-ASCII text' );

        equal( JSON2.parse('"20060228T08:00:00"'), "20060228T08:00:00", 'string "20060228T08:00:00"' );

        // these aren't converted back to Date objects
        equal( JSON2.parse('"2009-04-18T00:09:00Z"'), "2009-04-18T00:09:00Z", 'string "2009-04-18T00:09:00Z"' );
        equal( JSON2.parse('"2009-04-18T00:09:00.000Z"'), "2009-04-18T00:09:00.000Z", 'string "2009-04-18T00:09:00.000Z"' );

        deepEqual( JSON2.parse('[1,2,3]'), [1, 2, 3], 'Array of numbers' );
        deepEqual( JSON2.parse('{"key":"value"}'), {'key' : 'value'}, 'Object (members)' );
        deepEqual( JSON2.parse('[{"domains":["example.com","example.ca"]},{"names":["Sean","Cathy"]}]'),
            [ {'domains' : ['example.com', 'example.ca']}, {'names' : ['Sean', 'Cathy'] } ], 'Nested members' );
    });

    module("core", {
        setup: function () {},
        teardown: function () {
            $('#other #content').remove();
        }
    });

    test("Query", function() {
        var tracker = Piwik.getTracker();
        var query   = tracker.getQuery();
        var actual;

        actual = query.findFirstNodeHavingClass();
        strictEqual(actual, undefined, "findFirstNodeHavingClass, no node set");

        actual = query.findFirstNodeHavingClass(document.body);
        strictEqual(actual, undefined, "findFirstNodeHavingClass, no classname set");

        actual = query.findFirstNodeHavingClass(document.body, 'notExistingClass');
        strictEqual(actual, undefined, "findFirstNodeHavingClass, no such classname exists");

        actual = query.findFirstNodeHavingClass(document.body, 'piwik_ignore');
        strictEqual(actual, _e('click2'), "findFirstNodeHavingClass, find matching within body");

        actual = query.findFirstNodeHavingClass(_e('other'), 'clicktest');
        strictEqual(actual, _e('click1'), "findFirstNodeHavingClass, find matching within node");

        actual = query.findFirstNodeHavingClass(_e('click1'), 'clicktest');
        strictEqual(actual, _e('click1'), "findFirstNodeHavingClass, passed node has class itself");


        actual = query.findNodesHavingCssClass();
        propEqual(actual, [], "findNodesHavingCssClass, no node set");

        actual = query.findNodesHavingCssClass(document.body);
        propEqual(actual, [], "findNodesHavingCssClass, no classname set");

        actual = query.findNodesHavingCssClass(document.body, 'piwik_ignore');
        propEqual(actual, [_e('click2')], "findNodesHavingCssClass, find matching within body");

        actual = query.findNodesHavingCssClass(_e('other'), 'piwik_ignore');
        propEqual(actual, [_e('click2')], "findNodesHavingCssClass, ffind matching within div");

        actual = query.findNodesHavingCssClass(_e('other'), 'piwik_download');
        propEqual(actual, [_e('click7')], "findNodesHavingCssClass, find matching within div different class");

        actual = query.findNodesHavingCssClass(_e('other'), 'clicktest');
        propEqual(actual, [_e('click1'), _e('click2'), _e('click3'), _e('click4'), _e('click5'), _e('click6'), _e('click7'), _e('click8')], "findNodesHavingCssClass, find many matching within div");

        actual = query.findNodesHavingCssClass(_e('click7'), 'piwik_download');
        propEqual(actual, [], "findNodesHavingCssClass, should not find if passed node has class itself");

        actual = query.findNodesHavingCssClass(_e('clickDiv'), 'clicktest');
        ok(_e('clickDiv').children.length === 0, "clickDiv should not have any children");
        propEqual(actual, [], "findNodesHavingCssClass, should not find anything");



        actual = query.hasNodeCssClass();
        strictEqual(actual, false, "hasNodeCssClass, no element set");

        actual = query.hasNodeCssClass(_e('clickDiv'));
        strictEqual(actual, false, "hasNodeCssClass, no classname set");

        actual = query.hasNodeCssClass(_e('clickDiv'), 'anyClass');
        strictEqual(actual, false, "hasNodeCssClass, element has no class at all");

        actual = query.hasNodeCssClass(_e('click3'), 'anyClass');
        strictEqual(actual, false, "hasNodeCssClass, element has one classes and it does not match");

        actual = query.hasNodeCssClass(_e('click3'), 'clicktest');
        strictEqual(actual, true, "hasNodeCssClass, element has one classes and it matches");

        actual = query.hasNodeCssClass(_e('click7'), 'anyClass');
        strictEqual(actual, false, "hasNodeCssClass, element has many classes but not this one");

        actual = query.hasNodeCssClass(_e('click7'), 'piwik_download');
        strictEqual(actual, true, "hasNodeCssClass, element has many classes and it matches");



        actual = query.hasNodeAttribute();
        strictEqual(actual, false, "hasNodeAttribute, no element set");

        actual = query.hasNodeAttribute(_e('clickDiv'));
        strictEqual(actual, false, "hasNodeAttribute, no attribute set");

        actual = query.hasNodeAttribute(document.body, 'anyAttribute');
        strictEqual(actual, false, "hasNodeAttribute, element has no attribute at all");

        actual = query.hasNodeAttribute(_e('click2'), 'anyAttribute');
        strictEqual(actual, false, "hasNodeAttribute, element has attributes and it does not match");

        actual = query.hasNodeAttribute(_e('click2'), 'href');
        strictEqual(actual, true, "hasNodeAttribute, element has attributes and it does match");

        actual = query.hasNodeAttribute(_e('image1'), 'src');
        strictEqual(actual, true, "hasNodeAttribute, element has attributes and it does match other attribute");

        actual = query.hasNodeAttribute(_e('image2'), 'data-content-piece');
        strictEqual(actual, true, "hasNodeAttribute, element has attribute and no value");



        actual = query.hasNodeAttributeWithValue();
        strictEqual(actual, false, "hasNodeAttributeWithValue, no element set");

        actual = query.hasNodeAttributeWithValue(_e('clickDiv'));
        strictEqual(actual, false, "hasNodeAttributeWithValue, no attribute set");

        actual = query.hasNodeAttributeWithValue(document.body, 'anyAttribute');
        strictEqual(actual, false, "hasNodeAttributeWithValue, element has no attribute at all");

        actual = query.hasNodeAttributeWithValue(_e('click2'), 'anyAttribute');
        strictEqual(actual, false, "hasNodeAttributeWithValue, element has attributes but not this one");

        actual = query.hasNodeAttributeWithValue(_e('click2'), 'href');
        strictEqual(actual, true, "hasNodeAttributeWithValue, element has attribute and value");

        actual = query.hasNodeAttributeWithValue(_e('image1'), 'src');
        strictEqual(actual, false, "hasNodeAttributeWithValue, element has attribute but no value");

        actual = query.hasNodeAttributeWithValue(_e('image2'), 'data-content-piece');
        strictEqual(actual, false, "hasNodeAttributeWithValue, element has attribute but no value");


        actual = query.getAttributeValueFromNode();
        strictEqual(actual, undefined, "getAttributeValueFromNode, no element set");

        actual = query.getAttributeValueFromNode(_e('clickDiv'));
        strictEqual(actual, undefined, "getAttributeValueFromNode, no attribute set");

        actual = query.getAttributeValueFromNode(document.body, 'anyAttribute');
        strictEqual(actual, undefined, "getAttributeValueFromNode, element has no attribute at all");

        actual = query.getAttributeValueFromNode(_e('click2'), 'anyAttribute');
        strictEqual(actual, undefined, "getAttributeValueFromNode, element has attributes but not this one");

        actual = query.getAttributeValueFromNode(_e('click2'), 'href');
        strictEqual(actual, 'http://example.org', "getAttributeValueFromNode, element has attribute and value");

        actual = query.getAttributeValueFromNode(_e('image1'), 'src');
        strictEqual(actual, '', "getAttributeValueFromNode, element has attribute but no value");

        actual = query.getAttributeValueFromNode(_e('image2'), 'data-content-piece');
        strictEqual(actual, '', "getAttributeValueFromNode, element has attribute but no value");

        actual = query.getAttributeValueFromNode(_e('click2'), 'class');
        strictEqual(actual, 'piwik_ignore clicktest', "getAttributeValueFromNode, element has attribute class and value");



        actual = query.findNodesHavingAttribute();
        propEqual(actual, [], "findNodesHavingAttribute, no node set");

        actual = query.findNodesHavingAttribute(document.body);
        propEqual(actual, [], "findNodesHavingAttribute, no attribute set");

        actual = query.findNodesHavingAttribute(document.body, 'anyAttribute');
        propEqual(actual, [], "findNodesHavingAttribute, should not find any such attribute within body");

        actual = query.findNodesHavingAttribute(document.body, 'style');
        strictEqual(actual.length, 3, "findNodesHavingAttribute, should find a few");

        actual = query.findNodesHavingAttribute(_e('click1'), 'href');
        propEqual(actual, [], "findNodesHavingAttribute, should not find itself if the passed element has the attribute");

        actual = query.findNodesHavingAttribute(_e('clickDiv'), 'id');
        ok(_e('clickDiv').children.length === 0, "clickDiv should not have any children");
        propEqual(actual, [], "findNodesHavingAttribute, this element does not have children");

        actual = query.findNodesHavingAttribute(document.body, 'href');
        ok(actual.length > 11, "findNodesHavingAttribute, should find many elements within body");

        actual = query.findNodesHavingAttribute(_e('other'), 'href');
        propEqual(actual, [_e('click1'), _e('click2'), _e('click3'), _e('click4'), _e('click5'), _e('click6'), _e('click7'), _e('click8')], "findNodesHavingAttribute, should find many elements within node");

        actual = query.findNodesHavingAttribute(_e('other'), 'anyAttribute');
        propEqual(actual, [], "findNodesHavingAttribute, should not find any such attribute within div");


// TODO it is a bit confusing that findNodesHavingAttribute/CssClass does not include the passed node in the search but findFirstNodeHavingAttribute/CssClass does
        actual = query.findFirstNodeHavingAttribute();
        strictEqual(actual, undefined, "findFirstNodeHavingAttribute, no node set");

        actual = query.findFirstNodeHavingAttribute(document.body);
        strictEqual(actual, undefined, "findFirstNodeHavingAttribute, no attribute set");

        actual = query.findFirstNodeHavingAttribute(document.body, 'anyAttribute');
        strictEqual(actual, undefined, "findFirstNodeHavingAttribute, should not find any such attribute within body");

        actual = query.findFirstNodeHavingAttribute(_e('click1'), 'href');
        strictEqual(actual, _e('click1'), "findFirstNodeHavingAttribute, element has the attribute itself and not a children");

        actual = query.findFirstNodeHavingAttribute(_e('clickDiv'), 'anyAttribute');
        strictEqual(actual, undefined, "findFirstNodeHavingAttribute, this element does not have children");

        actual = query.findFirstNodeHavingAttribute(document.body, 'href');
        strictEqual(actual, _e('firstLink'), "findFirstNodeHavingAttribute, should find first link within body");

        actual = query.findFirstNodeHavingAttribute(_e('other'), 'href');
        strictEqual(actual, _e('click1'), "findFirstNodeHavingAttribute, should find fist link within node");



        actual = query.findFirstNodeHavingAttributeWithValue();
        strictEqual(actual, undefined, "findFirstNodeHavingAttributeWithValue, no node set");

        actual = query.findFirstNodeHavingAttributeWithValue(document.body);
        strictEqual(actual, undefined, "findFirstNodeHavingAttributeWithValue, no attribute set");

        actual = query.findFirstNodeHavingAttributeWithValue(document.body, 'anyAttribute');
        strictEqual(actual, undefined, "findFirstNodeHavingAttributeWithValue, should not find any such attribute within body");

        actual = query.findFirstNodeHavingAttributeWithValue(_e('click2'), 'href');
        strictEqual(actual, _e('click2'), "findFirstNodeHavingAttributeWithValue, element has the attribute itself and not a children");

        actual = query.findFirstNodeHavingAttributeWithValue(_e('clickDiv'), 'anyAttribute');
        strictEqual(actual, undefined, "findFirstNodeHavingAttributeWithValue, this element does not have children");

        actual = query.findFirstNodeHavingAttributeWithValue(document.body, 'href');
        strictEqual(actual, _e('firstLink'), "findFirstNodeHavingAttributeWithValue, should find first link within body");

        actual = query.findFirstNodeHavingAttributeWithValue(document.body, 'src');
        strictEqual(actual, _e('image2'), "findFirstNodeHavingAttributeWithValue, should not return first image which has empty src attribute");



        actual = query.htmlCollectionToArray();
        propEqual(actual, [], "htmlCollectionToArray, should always return an array even if nothing given");

        actual = query.htmlCollectionToArray(5);
        propEqual(actual, [], "htmlCollectionToArray, should always return an array even if interger given"); // would still parse string to an array but we can live with that

        var htmlCollection = document.getElementsByTagName('a');
        ok(htmlCollection instanceof HTMLCollection, 'htmlCollectionToArray, we need to make sure we handle an html collection in order to make test really useful')
        actual = query.htmlCollectionToArray(htmlCollection);
        ok($.isArray(actual), 'htmlCollectionToArray, should convert to array');
        ok(actual.length === htmlCollection.length, 'htmlCollectionToArray should have same amount of elements as before');
        ok(actual.length > 10, 'htmlCollectionToArray, just make sure there are many a elements found. otherwise test is useless');
        ok(-1 !== actual.indexOf(_e('click1')), 'htmlCollectionToArray, random check to make sure it contains a link');


        actual = query.find();
        propEqual(actual, [], "find, no selector passed should return an empty array");

        actual = query.find('[data-content-piece]');
        propEqual(actual, [_e('image2')], "find, should find elements by attribute");

        actual = query.find('.piwik_link');
        propEqual(actual, [_e('click5')], "find, should find elements by class");

        actual = query.find('#image1');
        propEqual(actual, [_e('image1')], "find, should find elements by id");

        actual = query.find('[href]');
        ok(actual.length > 10, "find, should find many elements by attribute");
        ok(-1 !== actual.indexOf(_e('click1')), 'find, random check to make sure it contains a link');

        actual = query.find('.clicktest');
        ok(actual.length === 8, "find, should find many elements by class");
        ok(-1 !== actual.indexOf(_e('click1')), 'find, random check to make sure it contains a link');



        actual = query.findMultiple();
        propEqual(actual, [], "findMultiple, no selectors passed should return an empty array");

        actual = query.findMultiple([]);
        propEqual(actual, [], "findMultiple, empty selectors passed should return an empty array");

        actual = query.findMultiple(['.piwik_link']);
        propEqual(actual, [_e('click5')], "findMultiple, only one selector passed");

        actual = query.findMultiple(['.piwik_link', '[data-content-piece]']);
        propEqual(actual, [_e('image2'), _e('click5')], "findMultiple, two selectors passed");

        actual = query.findMultiple(['.piwik_link', '[data-content-piece]', '#image2', '#div1']);
        propEqual(actual, [_e('image2'), _e('div1'), _e('click5')], "findMultiple, should make nodes unique in case we select the same multiple times");
    });

    test("contentFindContentBlock", function() {
        function _s(selector) { // select node within content test scope
            $nodes = $('#contenttest ' + selector);
            if ($nodes.length) return $nodes[0];
        }

        var tracker = Piwik.getTracker();
        var content = tracker.getContent();
        var actual, expected;

        actual = content.findContentNodes();
        propEqual(actual, [], "findContentNodes, should not find any content node when there is none");

        actual = content.findContentNodesWithinNode();
        propEqual(actual, [], "findContentNodesWithinNode, should not find any content node when no node passed");

        actual = content.findContentNodesWithinNode(_e('other'));
        ok(_e('other'), "if we do not get an element here test is not useful");
        propEqual(actual, [], "findContentNodesWithinNode, should not find any content node when there is none");

        actual = content.findParentContentNode(_e('click1'));
        ok(_e('click1'), "if we do not get an element here test is not useful");
        strictEqual(actual, undefined, "findParentContentNode, should not find any content node when there is none");



        setupContentTrackingFixture('findContentBlockTest');

        expected = [_s('#isOneWithClass'), _s('#isOneWithAttribute'), _s('[href="http://www.example.com"]'), _s('#containsOneWithAttribute [data-track-content]')];
        actual = content.findContentNodes();
        propEqual(actual, expected, "findContentNodes, should find all content blocks within the DOM");

        actual = content.findContentNodesWithinNode(_s(''));
        propEqual(actual, expected, "findContentNodesWithinNode, should find all content blocks within the DOM");

        actual = content.findContentNodesWithinNode(_s('#containsOneWithAttribute'));
        propEqual(actual, [expected[3]], "findContentNodesWithinNode, should find content blocks within a node");

        actual = content.findContentNodesWithinNode(expected[0]);
        propEqual(actual, [expected[0]], "findContentNodesWithinNode, should find one content block in the node itself");

        actual = content.findParentContentNode(_s('#isOneWithClass'));
        strictEqual(actual, expected[0], "findParentContentNode, should find itself in case the passed node is a content block with class");

        actual = content.findParentContentNode(_s('#isOneWithAttribute'));
        strictEqual(actual, expected[1], "findParentContentNode, should find itself in case the passed node is a content block with attribute");

        actual = content.findParentContentNode(_s('#innerNode'));
        strictEqual(actual, expected[2], "findParentContentNode, should find parent content block");
    });

    test("contentFindContentNodes", function() {
        function ex(testNumber) { // select node within content test scope
            $nodes = $('#contenttest #ex' + testNumber);
            if ($nodes.length) return $nodes[0];
        }

        var tracker = Piwik.getTracker();
        var content = tracker.getContent();
        var actual;

        var unrelatedNode = _e('other');
        ok(unrelatedNode, 'Make sure this element exists');

        actual = content.findTargetNodeNoDefault();
        strictEqual(actual, undefined, "findTargetNodeNoDefault, should not find anything if no node set");

        actual = content.findTargetNode();
        strictEqual(actual, undefined, "findTargetNode, should not find anything if no node set");

        actual = content.findPieceNode();
        strictEqual(actual, undefined, "findPieceNode, should not find anything if no node set");



        setupContentTrackingFixture('findContentNodesTest');

        var example1 = ex(1);
        ok(example1, 'Make sure this element exists to verify setup');

        ok("test fall back to content block node");

        actual = content.findTargetNodeNoDefault(example1);
        strictEqual(actual, undefined, "findTargetNodeNoDefault, should return nothing as no target set");

        actual = content.findTargetNode(example1);
        strictEqual(actual, example1, "findTargetNode, should fall back to content block node as no target set");

        actual = content.findPieceNode(example1);
        strictEqual(actual, example1, "findPieceNode, should not find anything if no node set");



        ok("test actually detects the attributes within a content block");

        actual = content.findTargetNodeNoDefault(ex(3));
        ok(undefined !== $(actual).attr(content.CONTENT_TARGET_ATTR), "findTargetNodeNoDefault, should have the attribute");
        strictEqual(actual, ex('3 a'), "findTargetNodeNoDefault, should find actual target node via attribute");

        actual = content.findTargetNode(ex(3));
        ok(undefined !== $(actual).attr(content.CONTENT_TARGET_ATTR), "findTargetNode, should have the attribute");
        strictEqual(actual, ex('3 a'), "findTargetNode, should find actual target node via attribute");

        actual = content.findPieceNode(ex(3));
        ok(undefined !== $(actual).attr(content.CONTENT_PIECE_ATTR), "findPieceNode, should have the attribute");
        strictEqual(actual, ex('3 img'), "findPieceNode, should find actual target piece via attribute");



        ok("test actually detects the CSS class within a content block");

        actual = content.findTargetNodeNoDefault(ex(4));
        ok($(actual).hasClass(content.CONTENT_TARGET_CLASS), "findTargetNodeNoDefault, should have the CSS class");
        strictEqual(actual, ex('4 a'), "findTargetNodeNoDefault, should find actual target node via class");

        actual = content.findTargetNode(ex(4));
        ok($(actual).hasClass(content.CONTENT_TARGET_CLASS), "findTargetNode, should have the CSS class");
        strictEqual(actual, ex('4 a'), "findTargetNode, should find actual target node via class");

        actual = content.findPieceNode(ex(4));
        ok($(actual).hasClass(content.CONTENT_PIECE_CLASS), "findPieceNode, should have the CSS class");
        strictEqual(actual, ex('4 img'), "findPieceNode, should find actual target piece via class");



        ok("test actually attributes takes precendence over class");

        actual = content.findTargetNodeNoDefault(ex(5));
        ok(undefined !== $(actual).attr(content.CONTENT_TARGET_ATTR), "findTargetNodeNoDefault, should have the attribute");
        strictEqual(actual.innerText, 'Target with attribute', "findTargetNodeNoDefault, should igonre node with class and pick attribute node");

        actual = content.findTargetNode(ex(5));
        ok(undefined !== $(actual).attr(content.CONTENT_TARGET_ATTR), "findTargetNode, should have the attribute");
        strictEqual(actual.innerText, 'Target with attribute', "findTargetNode, should igonre node with class and pick attribute node");

        actual = content.findPieceNode(ex(5));
        ok(undefined !== $(actual).attr(content.CONTENT_PIECE_ATTR), "findPieceNode, should have the attribute");
        strictEqual(actual.innerText, 'Piece with attribute', "findPieceNode, should igonre node with class and pick attribute node");



        ok("make sure it picks always the first one with multiple nodes have same class or same attribute");

        actual = content.findTargetNode(ex(6));
        ok($(actual).hasClass(content.CONTENT_TARGET_CLASS), "findTargetNode, should have the CSS class");
        strictEqual(actual.innerText, 'Target with class1', "findTargetNode, should igonre node with class and pick attribute node");

        actual = content.findPieceNode(ex(6));
        ok($(actual).hasClass(content.CONTENT_PIECE_CLASS), "findPieceNode, should have the CSS class");
        strictEqual(actual.innerText, 'Piece with class1', "findPieceNode, should igonre node with class and pick attribute node");

        actual = content.findTargetNode(ex(7));
        ok(undefined !== $(actual).attr(content.CONTENT_TARGET_ATTR), "findTargetNode, should have the attribute");
        strictEqual(actual.innerText, 'Target with attribute1', "findTargetNode, should igonre node with class and pick attribute node");

        actual = content.findPieceNode(ex(7));
        ok(undefined !== $(actual).attr(content.CONTENT_PIECE_ATTR), "findPieceNode, should have the attribute");
        strictEqual(actual.innerText, 'Piece with attribute1', "findPieceNode, should igonre node with class and pick attribute node");
    });

    test("contentUtilities", function() {

        var tracker = Piwik.getTracker();
        var content = tracker.getContent();
        content.setLocation(); // clear possible previous location
        var actual, expected;

        function assertTrimmed(value, expected, message)
        {
            strictEqual(content.trim(value), expected, message);
        }

        function assertRemoveDomainKeepsValueUntouched(value, message)
        {
            strictEqual(content.removeDomainIfIsUrl(value), value, message);
        }

        function assertDomainWillBeRemoved(url, expected, message)
        {
            strictEqual(content.removeDomainIfIsUrl(url), expected, message);
        }

        function assertBuildsAbsoluteUrl(url, expected, message)
        {
            strictEqual(content.toAbsoluteUrl(url), expected, message);
        }

        function assertImpressionRequestParams(name, piece, target, expected, message) {
            strictEqual(content.buildImpressionRequestParams(name, piece, target), expected, message);
        }

        function assertInteractionRequestParams(interaction, name, piece, target, expected, message) {
            strictEqual(content.buildInteractionRequestParams(interaction, name, piece, target), expected, message);
        }

        function assertShouldIgnoreInteraction(id, message) {
            var node = content.findTargetNode(_e(id));
            strictEqual(content.shouldIgnoreInteraction(node), true, message);
            ok($(node).hasClass(content.CONTENT_IGNOREINTERACTION_CLASS) || undefined !== $(node).attr(content.CONTENT_IGNOREINTERACTION_ATTR), "needs to have either attribute or class");
        }

        function assertShouldNotIgnoreInteraction(id, message) {
            var node = content.findTargetNode(_e(id));
            strictEqual(content.shouldIgnoreInteraction(node), false, message);
        }

        var locationAlias = $.extend({}, window.location);
        var origin = locationAlias.origin;

        ok("test trim(text)");

        strictEqual(undefined, content.trim(), 'should not fail if nothing set / is undefined');
        assertTrimmed(null, null, 'should not trim if null');
        assertTrimmed(5, 5, 'should not trim a number');
        assertTrimmed('', '', 'should not change an empty string');
        assertTrimmed('   ', '', 'should remove all whitespace');
        assertTrimmed('   xxxx', 'xxxx', 'should remove left whitespace');
        assertTrimmed('   xxxx   ', 'xxxx', 'should remove left and right whitespace');
        assertTrimmed(" \t  xxxx   \t", 'xxxx', 'should remove tabs and whitespace');
        assertTrimmed('  xx    xx  ', 'xx    xx', 'should keep whitespace between text untouched');

        ok("test removeDomainIfIsUrl(url)");

        strictEqual(undefined, content.removeDomainIfIsUrl(), 'should not fail if nothing set / is undefined');
        assertRemoveDomainKeepsValueUntouched(null, 'should keep null untouched');
        assertRemoveDomainKeepsValueUntouched(5, 'should keep number untouched');
        assertRemoveDomainKeepsValueUntouched('', 'should keep empty string untouched');
        assertRemoveDomainKeepsValueUntouched('Any Text', 'should keep string untouched that is not a url');
        assertRemoveDomainKeepsValueUntouched('/path/img.jpg', 'should keep string untouched that looks like a path');
        assertRemoveDomainKeepsValueUntouched('ftp://path/img.jpg', 'should keep string untouched that looks like a path');
        assertRemoveDomainKeepsValueUntouched(origin, 'should keep string untouched as it is same domain');
        assertRemoveDomainKeepsValueUntouched(origin + '/path/img.jpg', 'should keep string untouched as it is same domain, this time with path');

        assertDomainWillBeRemoved('http://www.example.com/path/img.jpg?x=y', '/path/img.jpg?x=y', 'should trim http domain with path that is not the same as the current');
        assertDomainWillBeRemoved('https://www.example.com/path/img.jpg?x=y', '/path/img.jpg?x=y', 'should trim https domain with path that is not the same as the current');
        assertDomainWillBeRemoved('http://www.example.com', '/', 'should trim http domain without path that is not the same as the current');
        assertDomainWillBeRemoved('https://www.example.com', '/', 'should trim https domain without path that is not the same as the current');

        ok("test toAbsoluteUrl(url) we need a lot of tests for this method as this will generate the redirect url");

        strictEqual(undefined, content.toAbsoluteUrl(), 'should not fail if nothing set / is undefined');
        assertBuildsAbsoluteUrl(null, null, 'null should be untouched');
        assertBuildsAbsoluteUrl(5, 5, 'number should be untouched');
        assertBuildsAbsoluteUrl('', '', '');
        assertBuildsAbsoluteUrl('/', origin + '/', 'root path');
        assertBuildsAbsoluteUrl('/test', origin + '/test', 'absolute url');
        assertBuildsAbsoluteUrl('/test/', origin + '/test/', 'absolute url');
        assertBuildsAbsoluteUrl('?x=5', origin + '/tests/javascript/?x=5', 'absolute url');
        assertBuildsAbsoluteUrl('path', origin + '/tests/javascript/path', 'relative path');
        assertBuildsAbsoluteUrl('path/x?p=5', origin + '/tests/javascript/path/x?p=5', 'relative path');
        assertBuildsAbsoluteUrl('#test', origin + '/tests/javascript/#test', 'anchor url');
        assertBuildsAbsoluteUrl('//' + locationAlias.host + '/test/img.jpg', origin + '/test/img.jpg', 'inherit protocol url');
        assertBuildsAbsoluteUrl('mailto:test@example.com', 'mailto:test@example.com', 'mailto special url');
        assertBuildsAbsoluteUrl('tel:0123456789', 'tel:0123456789', 'tel special url');
        assertBuildsAbsoluteUrl('anythingg:test', origin + '/tests/javascript/anythingg:test', 'we do not treat this one as special url as we recognize max 8 characters currently');
        assertBuildsAbsoluteUrl('k1dm:test', origin + '/tests/javascript/k1dm:test', 'we do not treat this one as special url as it contains a number');

        locationAlias.pathname = '/test/';
        content.setLocation(locationAlias);
        assertBuildsAbsoluteUrl('?x=5', origin + '/test/?x=5', 'should add query param');
        assertBuildsAbsoluteUrl('link2', origin + '/test/link2', 'relative url in existing path');

        locationAlias.pathname = '/test';
        content.setLocation(locationAlias);
        assertBuildsAbsoluteUrl('?x=5', origin + '/test?x=5', 'should add query param');
        assertBuildsAbsoluteUrl('link2', origin + '/link2', 'relative url replaces other relative url');

        ok("test buildImpressionRequestParams(name, piece, target)");
        assertImpressionRequestParams('name', 'piece', 'target', 'c_n=name&c_p=piece&c_t=target', "all parameters set");
        assertImpressionRequestParams('name', 'piece', null, 'c_n=name&c_p=piece', "no target set");
        assertImpressionRequestParams('http://example.com.com', '/?x=1', '&target=1', 'c_n=http%3A%2F%2Fexample.com.com&c_p=%2F%3Fx%3D1&c_t=%26target%3D1', "should encode values");

        ok("test buildInteractionRequestParams(interaction, name, piece, target)");
        assertInteractionRequestParams(null, null, null, null, '', "nothing set");
        assertInteractionRequestParams('interaction', null, null, null, 'c_i=interaction', "only interaction set");
        assertInteractionRequestParams('interaction', 'name', null, null, 'c_i=interaction&c_n=name', "no piece and no target set");
        assertInteractionRequestParams('interaction', 'name', 'piece', null, 'c_i=interaction&c_n=name&c_p=piece', "no target set");
        assertInteractionRequestParams('interaction', 'name', 'piece', 'target', 'c_i=interaction&c_n=name&c_p=piece&c_t=target', "all parameters set");
        assertInteractionRequestParams(null, 'name', 'piece', null, 'c_n=name&c_p=piece', "only name and piece set");
        assertInteractionRequestParams('http://', 'http://example.com.com', '/?x=1', '&target=1', 'c_i=http%3A%2F%2F&c_n=http%3A%2F%2Fexample.com.com&c_p=%2F%3Fx%3D1&c_t=%26target%3D1', "should encode values");

        setupContentTrackingFixture('contentUtilities');

        ok("test shouldIgnoreInteraction(targetNode)");
        assertShouldIgnoreInteraction('ignoreInteraction1', 'should be ignored because of CSS class');
        assertShouldIgnoreInteraction('ignoreInteraction2', 'should be ignored because of Attribute');
        assertShouldNotIgnoreInteraction('notIgnoreInteraction1', 'should not be ignored');

        // isOrWasNodeInViewport
        // isNodeVisible
        // findInternalTargetLinkFromHref
        // replaceTargetLink
    });


    test("contentFindContentValues", function() {
        // TODO
        function ex(testNumber) { // select node within content test scope
            $nodes = $('#contenttest #ex' + testNumber);
            if ($nodes.length) return $nodes[0];
        }

        var tracker = Piwik.getTracker();
        var content = tracker.getContent();
        var actual;
    });

    test("contentCollectionTest", function() {
        function ex(testNumber) { // select node within content test scope
            $nodes = $('#contenttest #ex' + testNumber);
            if ($nodes.length) return $nodes[0];
        }

        var tracker = Piwik.getTracker();
        var content = tracker.getContent();
        var actual;

    });

    test("Basic requirements", function() {
        expect(3);

        equal( typeof encodeURIComponent, 'function', 'encodeURIComponent' );
        ok( RegExp, "RegExp" );
        ok( Piwik, "Piwik" );
    });

    test("Test API - addPlugin(), getTracker(), getHook(), and hook", function() {
        expect(6);

        ok( Piwik.addPlugin, "Piwik.addPlugin" );

        var tracker = Piwik.getTracker();

        equal( typeof tracker, 'object', "Piwik.getTracker()" );
        equal( typeof tracker.getHook, 'function', "test Tracker getHook" );
        equal( typeof tracker.hook, 'object', "test Tracker hook" );
        equal( typeof tracker.getHook('test'), 'object', "test Tracker getHook('test')" );
        equal( typeof tracker.hook.test, 'object', "test Tracker hook.test" );
    });

    test("API methods", function() {
        expect(57);

        equal( typeof Piwik.addPlugin, 'function', 'addPlugin' );
        equal( typeof Piwik.getTracker, 'function', 'getTracker' );
        equal( typeof Piwik.getAsyncTracker, 'function', 'getAsyncTracker' );

        var tracker;

        tracker = Piwik.getAsyncTracker();
        ok(tracker instanceof Object, 'getAsyncTracker');

        tracker = Piwik.getTracker();
        ok(tracker instanceof Object, 'getTracker');

        equal( typeof tracker.getVisitorId, 'function', 'getVisitorId' );
        equal( typeof tracker.getVisitorInfo, 'function', 'getVisitorInfo' );
        equal( typeof tracker.getAttributionInfo, 'function', 'getAttributionInfo' );
        equal( typeof tracker.getAttributionReferrerTimestamp, 'function', 'getAttributionReferrerTimestamp' );
        equal( typeof tracker.getAttributionReferrerUrl, 'function', 'getAttributionReferrerUrl' );
        equal( typeof tracker.getAttributionCampaignName, 'function', 'getAttributionCampaignName' );
        equal( typeof tracker.getAttributionCampaignKeyword, 'function', 'getAttributionCampaignKeyword' );
        equal( typeof tracker.setTrackerUrl, 'function', 'setTrackerUrl' );
        equal( typeof tracker.getRequest, 'function', 'getRequest' );
        equal( typeof tracker.addPlugin, 'function', 'addPlugin' );
        equal( typeof tracker.setSiteId, 'function', 'setSiteId' );
        equal( typeof tracker.setCustomData, 'function', 'setCustomData' );
        equal( typeof tracker.getCustomData, 'function', 'getCustomData' );
        equal( typeof tracker.setCustomRequestProcessing, 'function', 'setCustomRequestProcessing' );
        equal( typeof tracker.setCustomVariable, 'function', 'setCustomVariable' );
        equal( typeof tracker.getCustomVariable, 'function', 'getCustomVariable' );
        equal( typeof tracker.deleteCustomVariable, 'function', 'deleteCustomVariable' );
        equal( typeof tracker.setLinkTrackingTimer, 'function', 'setLinkTrackingTimer' );
        equal( typeof tracker.setDownloadExtensions, 'function', 'setDownloadExtensions' );
        equal( typeof tracker.addDownloadExtensions, 'function', 'addDownloadExtensions' );
        equal( typeof tracker.setDomains, 'function', 'setDomains' );
        equal( typeof tracker.setIgnoreClasses, 'function', 'setIgnoreClasses' );
        equal( typeof tracker.setRequestMethod, 'function', 'setRequestMethod' );
        equal( typeof tracker.setRequestContentType, 'function', 'setRequestContentType' );
        equal( typeof tracker.setReferrerUrl, 'function', 'setReferrerUrl' );
        equal( typeof tracker.setCustomUrl, 'function', 'setCustomUrl' );
        equal( typeof tracker.setDocumentTitle, 'function', 'setDocumentTitle' );
        equal( typeof tracker.setDownloadClasses, 'function', 'setDownloadClasses' );
        equal( typeof tracker.setLinkClasses, 'function', 'setLinkClasses' );
        equal( typeof tracker.setCampaignNameKey, 'function', 'setCampaignNameKey' );
        equal( typeof tracker.setCampaignKeywordKey, 'function', 'setCampaignKeywordKey' );
        equal( typeof tracker.discardHashTag, 'function', 'discardHashTag' );
        equal( typeof tracker.setCookieNamePrefix, 'function', 'setCookieNamePrefix' );
        equal( typeof tracker.setCookieDomain, 'function', 'setCookieDomain' );
        equal( typeof tracker.setCookiePath, 'function', 'setCookiePath' );
        equal( typeof tracker.setVisitorCookieTimeout, 'function', 'setVisitorCookieTimeout' );
        equal( typeof tracker.setSessionCookieTimeout, 'function', 'setSessionCookieTimeout' );
        equal( typeof tracker.setReferralCookieTimeout, 'function', 'setReferralCookieTimeout' );
        equal( typeof tracker.setConversionAttributionFirstReferrer, 'function', 'setConversionAttributionFirstReferrer' );
        equal( typeof tracker.addListener, 'function', 'addListener' );
        equal( typeof tracker.enableLinkTracking, 'function', 'enableLinkTracking' );
        equal( typeof tracker.setHeartBeatTimer, 'function', 'setHeartBeatTimer' );
        equal( typeof tracker.killFrame, 'function', 'killFrame' );
        equal( typeof tracker.redirectFile, 'function', 'redirectFile' );
        equal( typeof tracker.setCountPreRendered, 'function', 'setCountPreRendered' );
        equal( typeof tracker.trackGoal, 'function', 'trackGoal' );
        equal( typeof tracker.trackLink, 'function', 'trackLink' );
        equal( typeof tracker.trackPageView, 'function', 'trackPageView' );
        // ecommerce
        equal( typeof tracker.setEcommerceView, 'function', 'setEcommerceView' );
        equal( typeof tracker.addEcommerceItem, 'function', 'addEcommerceItem' );
        equal( typeof tracker.trackEcommerceOrder, 'function', 'trackEcommerceOrder' );
        equal( typeof tracker.trackEcommerceCartUpdate, 'function', 'trackEcommerceCartUpdate' );
    });

    module("API and internals");

    test("Tracker is_a functions", function() {
        expect(22);

        var tracker = Piwik.getTracker();

        equal( typeof tracker.hook.test._isDefined, 'function', 'isDefined' );
        ok( tracker.hook.test._isDefined(tracker), 'isDefined true' );
        ok( tracker.hook.test._isDefined(tracker.hook), 'isDefined(obj.exists) true' );
        ok( !tracker.hook.test._isDefined(tracker.non_existant_property), 'isDefined(obj.missing) false' );

        equal( typeof tracker.hook.test._isFunction, 'function', 'isFunction' );
        ok( tracker.hook.test._isFunction(tracker.hook.test._isFunction), 'isFunction(isFunction)' );
        ok( tracker.hook.test._isFunction(function () { }), 'isFunction(function)' );

        equal( typeof tracker.hook.test._isObject, 'function', 'isObject' );
        ok( tracker.hook.test._isObject(null), 'isObject(null)' ); // null is an object!
        ok( tracker.hook.test._isObject(new Object), 'isObject(Object)' );
        ok( tracker.hook.test._isObject(window), 'isObject(window)' );
        ok( !tracker.hook.test._isObject('string'), 'isObject("string")' );
        ok( tracker.hook.test._isObject(new String), 'isObject(String)' ); // String is an object!

        equal( typeof tracker.hook.test._isString, 'function', 'isString' );
        ok( tracker.hook.test._isString(''), 'isString(emptyString)' );
        ok( tracker.hook.test._isString('abc'), 'isString("abc")' );
        ok( tracker.hook.test._isString('123'), 'isString("123")' );
        ok( !tracker.hook.test._isString(123), 'isString(123)' );
        ok( !tracker.hook.test._isString(null), 'isString(null)' );
        ok( !tracker.hook.test._isString(window), 'isString(window)' );
        ok( !tracker.hook.test._isString(function () { }), 'isString(function)' );
        ok( tracker.hook.test._isString(new String), 'isString(String)' ); // String is a string
    });

    test("AnalyticsTracker alias", function() {
        expect(1);

        var tracker = AnalyticsTracker.getTracker();
        equal( typeof tracker.hook.test._encode, 'function', 'encodeWrapper' );
    });

    test("Tracker encode, decode, urldecode wrappers", function() {
        expect(6);

        var tracker = Piwik.getTracker();

        equal( typeof tracker.hook.test._encode, 'function', 'encodeWrapper' );
        equal( typeof tracker.hook.test._decode, 'function', 'decodeWrapper' );
        equal( typeof tracker.hook.test._urldecode, 'function', 'urldecodeWrapper' );

        equal( tracker.hook.test._encode("&=?;/#"), '%26%3D%3F%3B%2F%23', 'encodeWrapper()' );
        equal( tracker.hook.test._decode("%26%3D%3F%3B%2F%23"), '&=?;/#', 'decodeWrapper()' );
        equal( tracker.hook.test._urldecode("mailto:%69%6e%66%6f@%65%78%61%6d%70%6c%65.%63%6f%6d"), 'mailto:info@example.com', 'decodeWrapper()' );
    });

    test("Tracker getHostName(), getParameter(), urlFixup(), domainFixup(), titleFixup() and purify()", function() {
        expect(57);

        var tracker = Piwik.getTracker();

        equal( typeof tracker.hook.test._getHostName, 'function', 'getHostName' );
        equal( typeof tracker.hook.test._getParameter, 'function', 'getParameter' );

        equal( tracker.hook.test._getHostName('http://example.com'), 'example.com', 'http://example.com');
        equal( tracker.hook.test._getHostName('http://example.com/'), 'example.com', 'http://example.com/');
        equal( tracker.hook.test._getHostName('http://example.com/index'), 'example.com', 'http://example.com/index');
        equal( tracker.hook.test._getHostName('http://example.com/index?q=xyz'), 'example.com', 'http://example.com/index?q=xyz');
        equal( tracker.hook.test._getHostName('http://example.com/?q=xyz'), 'example.com', 'http://example.com/?q=xyz');
        equal( tracker.hook.test._getHostName('http://example.com/?q=xyz#hash'), 'example.com', 'http://example.com/?q=xyz#hash');
        equal( tracker.hook.test._getHostName('http://example.com#hash'), 'example.com', 'http://example.com#hash');
        equal( tracker.hook.test._getHostName('http://example.com/#hash'), 'example.com', 'http://example.com/#hash');
        equal( tracker.hook.test._getHostName('http://example.com:80'), 'example.com', 'http://example.com:80');
        equal( tracker.hook.test._getHostName('http://example.com:80/'), 'example.com', 'http://example.com:80/');
        equal( tracker.hook.test._getHostName('https://example.com/'), 'example.com', 'https://example.com/');
        equal( tracker.hook.test._getHostName('http://user@example.com/'), 'example.com', 'http://user@example.com/');
        equal( tracker.hook.test._getHostName('http://user:password@example.com/'), 'example.com', 'http://user:password@example.com/');

        equal( tracker.hook.test._getParameter('http://piwik.org/', 'q'), '', 'no query');
        equal( tracker.hook.test._getParameter('http://piwik.org/?q=test', 'q'), 'test', '?q');
        equal( tracker.hook.test._getParameter('http://piwik.org/?q=test#aq=not', 'q'), 'test', '?q');
        equal( tracker.hook.test._getParameter('http://piwik.org/?p=test1&q=test2', 'q'), 'test2', '&q');

        // getParameter in hash tag
        equal( tracker.hook.test._getParameter('http://piwik.org/?p=test1&q=test2#aq=not', 'q'), 'test2', '&q');
        equal( tracker.hook.test._getParameter('http://piwik.org/?p=test1&q=test2#aq=not', 'aq'), 'not', '#aq');
        equal( tracker.hook.test._getParameter('http://piwik.org/?p=test1&q=test2#bq=yes&aq=not', 'bq'), 'yes', '#bq');
        equal( tracker.hook.test._getParameter('http://piwik.org/?p=test1&q=test2#pk_campaign=campaign', 'pk_campaign'), 'campaign', '#pk_campaign');
        equal( tracker.hook.test._getParameter('http://piwik.org/?p=test1&q=test2#bq=yes&aq=not', 'q'), 'test2', '#q');

        // URL decoded
        equal( tracker.hook.test._getParameter('http://piwik.org/?q=http%3a%2f%2flocalhost%2f%3fr%3d1%26q%3dfalse', 'q'), 'http://localhost/?r=1&q=false', 'url');
        equal( tracker.hook.test._getParameter('http://piwik.org/?q=http%3a%2f%2flocalhost%2f%3fr%3d1%26q%3dfalse&notq=not', 'q'), 'http://localhost/?r=1&q=false', 'url');

        // non existing parameters
        equal( tracker.hook.test._getParameter('http://piwik.org/?p=test1&q=test2#bq=yes&aq=not', 'bqq'), "", '#q');
        equal( tracker.hook.test._getParameter('http://piwik.org/?p=test1&q=test2#bq=yes&aq=not', 'bq='), "", '#q');
        equal( tracker.hook.test._getParameter('http://piwik.org/?p=test1&q=test2#bq=yes&aq=not', 'sp='), "", '#q');

        equal( typeof tracker.hook.test._urlFixup, 'function', 'urlFixup' );

        deepEqual( tracker.hook.test._urlFixup( 'webcache.googleusercontent.com', 'http://webcache.googleusercontent.com/search?q=cache:CD2SncROLs4J:piwik.org/blog/2010/04/piwik-0-6-security-advisory/+piwik+security&cd=1&hl=en&ct=clnk', '' ),
                ['piwik.org', 'http://piwik.org/qa', ''], 'webcache.googleusercontent.com' );

        deepEqual( tracker.hook.test._urlFixup( 'cc.bingj.com', 'http://cc.bingj.com/cache.aspx?q=web+analytics&d=5020318678516316&mkt=en-CA&setlang=en-CA&w=6ea8ea88,ff6c44df', '' ),
                ['piwik.org', 'http://piwik.org/qa', ''], 'cc.bingj.com' );

        deepEqual( tracker.hook.test._urlFixup( '74.6.239.185', 'http://74.6.239.185/search/srpcache?ei=UTF-8&p=piwik&fr=yfp-t-964&fp_ip=ca&u=http://cc.bingj.com/cache.aspx?q=piwik&d=4770519086662477&mkt=en-US&setlang=en-US&w=f4bc05d8,8c8af2e3&icp=1&.intl=us&sig=PXmPDNqapxSQ.scsuhIpZA--', '' ),
                ['piwik.org', 'http://piwik.org/qa', ''], 'yahoo cache (1)' );

        deepEqual( tracker.hook.test._urlFixup( '74.6.239.84', 'http://74.6.239.84/search/srpcache?ei=UTF-8&p=web+analytics&fr=yfp-t-715&u=http://cc.bingj.com/cache.aspx?q=web+analytics&d=5020318680482405&mkt=en-CA&setlang=en-CA&w=a68d7af0,873cfeb0&icp=1&.intl=ca&sig=x6MgjtrDYvsxi8Zk2ZX.tw--', '' ),
                ['piwik.org', 'http://piwik.org/qa', ''], 'yahoo cache (2)' );

        deepEqual( tracker.hook.test._urlFixup( 'translate.googleusercontent.com', 'http://translate.googleusercontent.com/translate_c?hl=en&ie=UTF-8&sl=en&tl=fr&u=http://piwik.org/&prev=_t&rurl=translate.google.com&twu=1&usg=ALkJrhirI_ijXXT7Ja_aDGndEJbE7pJqpQ', '' ),
                ['piwik.org', 'http://piwik.org/', 'http://translate.googleusercontent.com/translate_c?hl=en&ie=UTF-8&sl=en&tl=fr&u=http://piwik.org/&prev=_t&rurl=translate.google.com&twu=1&usg=ALkJrhirI_ijXXT7Ja_aDGndEJbE7pJqpQ'], 'translate.googleusercontent.com' );

        equal( typeof tracker.hook.test._domainFixup, 'function', 'domainFixup' );

        equal( tracker.hook.test._domainFixup( 'localhost' ), 'localhost', 'domainFixup: localhost' );
        equal( tracker.hook.test._domainFixup( 'localhost.' ), 'localhost', 'domainFixup: localhost.' );
        equal( tracker.hook.test._domainFixup( 'localhost.localdomain' ), 'localhost.localdomain', 'domainFixup: localhost.localdomain' );
        equal( tracker.hook.test._domainFixup( 'localhost.localdomain.' ), 'localhost.localdomain', 'domainFixup: localhost.localdomain.' );
        equal( tracker.hook.test._domainFixup( '127.0.0.1' ), '127.0.0.1', 'domainFixup: 127.0.0.1' );
        equal( tracker.hook.test._domainFixup( 'www.example.com' ), 'www.example.com', 'domainFixup: www.example.com' );
        equal( tracker.hook.test._domainFixup( 'www.example.com.' ), 'www.example.com', 'domainFixup: www.example.com.' );
        equal( tracker.hook.test._domainFixup( '.example.com' ), '.example.com', 'domainFixup: .example.com' );
        equal( tracker.hook.test._domainFixup( '.example.com.' ), '.example.com', 'domainFixup: .example.com.' );
        equal( tracker.hook.test._domainFixup( '*.example.com' ), '.example.com', 'domainFixup: *.example.com' );
        equal( tracker.hook.test._domainFixup( '*.example.com.' ), '.example.com', 'domainFixup: *.example.com.' );

        equal( typeof tracker.hook.test._titleFixup, 'function', 'titleFixup' );
        equal( tracker.hook.test._titleFixup( 'hello' ), 'hello', 'hello string' );
        equal( tracker.hook.test._titleFixup( document.title ), 'piwik.js: Unit Tests', 'hello string' );

        equal( typeof tracker.hook.test._purify, 'function', 'purify' );

        equal( tracker.hook.test._purify('http://example.com'), 'http://example.com', 'http://example.com');
        equal( tracker.hook.test._purify('http://example.com#hash'), 'http://example.com#hash', 'http://example.com#hash');
        equal( tracker.hook.test._purify('http://example.com/?q=xyz#hash'), 'http://example.com/?q=xyz#hash', 'http://example.com/?q=xyz#hash');

        tracker.discardHashTag(true);

        equal( tracker.hook.test._purify('http://example.com'), 'http://example.com', 'http://example.com');
        equal( tracker.hook.test._purify('http://example.com#hash'), 'http://example.com', 'http://example.com#hash');
        equal( tracker.hook.test._purify('http://example.com/?q=xyz#hash'), 'http://example.com/?q=xyz', 'http://example.com/?q=xyz#hash');
    });

    // support for setCustomUrl( relativeURI )
    test("getProtocolScheme and resolveRelativeReference", function() {
        expect(27);

        var tracker = Piwik.getTracker();

        equal( typeof tracker.hook.test._getProtocolScheme, 'function', "getProtocolScheme" );

        ok( tracker.hook.test._getProtocolScheme('http://example.com') === 'http', 'http://' );
        ok( tracker.hook.test._getProtocolScheme('https://example.com') === 'https', 'https://' );
        ok( tracker.hook.test._getProtocolScheme('file://somefile.txt') === 'file', 'file://' );
        ok( tracker.hook.test._getProtocolScheme('mailto:somebody@example.com') === 'mailto', 'mailto:' );
        ok( tracker.hook.test._getProtocolScheme('javascript:alert(document.cookie)') === 'javascript', 'javascript:' );
        ok( tracker.hook.test._getProtocolScheme('') === null, 'empty string' );
        ok( tracker.hook.test._getProtocolScheme(':') === null, 'unspecified scheme' );
        ok( tracker.hook.test._getProtocolScheme('scheme') === null, 'missing colon' );

        equal( typeof tracker.hook.test._resolveRelativeReference, 'function', 'resolveRelativeReference' );

        var i, j, data = [
            // unsupported
//          ['http://example.com/index.php/pathinfo?query', 'test.php', 'http://example.com/test.php'],
//          ['http://example.com/subdir/index.php', '../test.php', 'http://example.com/test.php'],

            // already absolute
            ['http://example.com/', 'http://example.com', 'http://example.com'],
            ['http://example.com/', 'https://example.com/', 'https://example.com/'],
            ['http://example.com/', 'http://example.com/index', 'http://example.com/index'],

            // relative to root
            ['http://example.com/', '', 'http://example.com/'],
            ['http://example.com/', '/', 'http://example.com/'],
            ['http://example.com/', '/test.php', 'http://example.com/test.php'],
            ['http://example.com/index', '/test.php', 'http://example.com/test.php'],
            ['http://example.com/index?query=x', '/test.php', 'http://example.com/test.php'],
            ['http://example.com/index?query=x#hash', '/test.php', 'http://example.com/test.php'],
            ['http://example.com/?query', '/test.php', 'http://example.com/test.php'],
            ['http://example.com/#hash', '/test.php', 'http://example.com/test.php'],

            // relative to current document
            ['http://example.com/subdir/', 'test.php', 'http://example.com/subdir/test.php'],
            ['http://example.com/subdir/index', 'test.php', 'http://example.com/subdir/test.php'],
            ['http://example.com/subdir/index?query=x', 'test.php', 'http://example.com/subdir/test.php'],
            ['http://example.com/subdir/index?query=x#hash', 'test.php', 'http://example.com/subdir/test.php'],
            ['http://example.com/subdir/?query', 'test.php', 'http://example.com/subdir/test.php'],
            ['http://example.com/subdir/#hash', 'test.php', 'http://example.com/subdir/test.php']
        ];

        for (i = 0; i < data.length; i++) {
            j = data[i];
            equal( tracker.hook.test._resolveRelativeReference(j[0], j[1]), j[2], j[2] );
        }
    });

    test("Tracker setDomains() and isSiteHostName()", function() {
        expect(13);

        var tracker = Piwik.getTracker();

        equal( typeof tracker.hook.test._isSiteHostName, 'function', "isSiteHostName" );

        // test wildcards
        tracker.setDomains( ['*.Example.com'] );

        // skip test if testing on localhost
        ok( window.location.hostname != 'localhost' ? !tracker.hook.test._isSiteHostName('localhost') : true, '!isSiteHostName("localhost")' );

        ok( !tracker.hook.test._isSiteHostName('google.com'), '!isSiteHostName("google.com")' );
        ok( tracker.hook.test._isSiteHostName('example.com'), 'isSiteHostName("example.com")' );
        ok( tracker.hook.test._isSiteHostName('www.example.com'), 'isSiteHostName("www.example.com")' );
        ok( tracker.hook.test._isSiteHostName('www.sub.example.com'), 'isSiteHostName("www.sub.example.com")' );

        tracker.setDomains( 'dev.piwik.org' );
        ok( !tracker.hook.test._isSiteHostName('piwik.org'), '!isSiteHostName("piwik.org")' );
        ok( tracker.hook.test._isSiteHostName('dev.piwik.org'), 'isSiteHostName("dev.piwik.org")' );
        ok( !tracker.hook.test._isSiteHostName('piwik.example.org'), '!isSiteHostName("piwik.example.org")');
        ok( !tracker.hook.test._isSiteHostName('dev.piwik.org.com'), '!isSiteHostName("dev.piwik.org.com")');

        tracker.setDomains( '.piwik.org' );
        ok( tracker.hook.test._isSiteHostName('piwik.org'), 'isSiteHostName("piwik.org")' );
        ok( tracker.hook.test._isSiteHostName('dev.piwik.org'), 'isSiteHostName("dev.piwik.org")' );
        ok( !tracker.hook.test._isSiteHostName('piwik.org.com'), '!isSiteHostName("piwik.org.com")');
    });

    test("Tracker getClassesRegExp()", function() {
        expect(3);

        var tracker = Piwik.getTracker();

        equal( typeof tracker.hook.test._getClassesRegExp, 'function', "getClassesRegExp" );

        var download = tracker.hook.test._getClassesRegExp([], 'download');
        ok( download.test('piwik_download'), 'piwik_download (default)' );

        var outlink = tracker.hook.test._getClassesRegExp([], 'link');
        ok( outlink.test('piwik_link'), 'piwik_link (default)' );

    });

    test("Tracker setIgnoreClasses() and getClassesRegExp(ignore)", function() {
        expect(14);

        var tracker = Piwik.getTracker();

        var ignore = tracker.hook.test._getClassesRegExp([], 'ignore');
        ok( ignore.test('piwik_ignore'), '[1] piwik_ignore' );
        ok( !ignore.test('pk_ignore'), '[1] !pk_ignore' );
        ok( !ignore.test('apiwik_ignore'), '!apiwik_ignore' );
        ok( !ignore.test('piwik_ignorez'), '!piwik_ignorez' );
        ok( ignore.test('abc piwik_ignore xyz'), 'abc piwik_ignore xyz' );

        tracker.setIgnoreClasses( 'my_download' );
        ignore = tracker.hook.test._getClassesRegExp(['my_download'], 'ignore');
        ok( ignore.test('piwik_ignore'), '[2] piwik_ignore' );
        ok( !ignore.test('pk_ignore'), '[2] !pk_ignore' );
        ok( ignore.test('my_download'), 'my_download' );
        ok( ignore.test('abc piwik_ignore xyz'), 'abc piwik_ignore xyz' );
        ok( ignore.test('abc my_download xyz'), 'abc my_download xyz' );

        tracker.setIgnoreClasses( ['my_download', 'my_outlink'] );
        ignore = tracker.hook.test._getClassesRegExp(['my_download','my_outlink'], 'ignore');
        ok( ignore.test('piwik_ignore'), '[3] piwik_ignore' );
        ok( !ignore.test('pk_ignore'), '[3] !pk_ignore' );
        ok( ignore.test('my_download'), 'my_download' );
        ok( ignore.test('my_outlink'), 'my_outlink' );
    });

    test("Tracker hasCookies(), getCookie(), setCookie()", function() {
        expect(2);

        var tracker = Piwik.getTracker();

        ok( tracker.hook.test._hasCookies() == '1', 'hasCookies()' );

        var cookieName = '_pk_test_harness' + Math.random(),
            expectedValue = String(Math.random());
        tracker.hook.test._setCookie( cookieName, expectedValue );
        equal( tracker.hook.test._getCookie( cookieName ), expectedValue, 'getCookie(), setCookie()' );
    });

    test("Tracker setDownloadExtensions(), addDownloadExtensions(), setDownloadClasses(), setLinkClasses(), and getLinkType()", function() {
        expect(25);

        var tracker = Piwik.getTracker();

        equal( typeof tracker.hook.test._getLinkType, 'function', 'getLinkType' );

        equal( tracker.hook.test._getLinkType('something', 'goofy.html', false), 'link', 'implicit link' );
        equal( tracker.hook.test._getLinkType('something', 'goofy.pdf', false), 'download', 'external PDF files are downloads' );
        equal( tracker.hook.test._getLinkType('something', 'goofy.pdf', true), 'download', 'local PDF are downloads' );
        equal( tracker.hook.test._getLinkType('something', 'goofy-with-dash.pdf', true), 'download', 'local PDF are downloads' );

        equal( tracker.hook.test._getLinkType('piwik_download', 'piwiktest.ext', true), 'download', 'piwik_download' );
        equal( tracker.hook.test._getLinkType('abc piwik_download xyz', 'piwiktest.ext', true), 'download', 'abc piwik_download xyz' );
        equal( tracker.hook.test._getLinkType('piwik_link', 'piwiktest.asp', true), 'link', 'piwik_link' );
        equal( tracker.hook.test._getLinkType('abc piwik_link xyz', 'piwiktest.asp', true), 'link', 'abc piwik_link xyz' );
        equal( tracker.hook.test._getLinkType('something', 'piwiktest.txt', true), 'download', 'download extension' );
        equal( tracker.hook.test._getLinkType('something', 'piwiktest.ext', true), 0, '[1] link (default)' );

        equal( tracker.hook.test._getLinkType('something', 'file.zip', true), 'download', 'download file.zip' );
        equal( tracker.hook.test._getLinkType('something', 'index.php?name=file.zip#anchor', true), 'download', 'download file.zip (anchor)' );
        equal( tracker.hook.test._getLinkType('something', 'index.php?name=file.zip&redirect=yes', true), 'download', 'download file.zip (is param)' );
        equal( tracker.hook.test._getLinkType('something', 'file.zip?mirror=true', true), 'download', 'download file.zip (with param)' );

        tracker.setDownloadExtensions('pk');
        equal( tracker.hook.test._getLinkType('something', 'piwiktest.pk', true), 'download', '[1] .pk == download extension' );
        equal( tracker.hook.test._getLinkType('something', 'piwiktest.txt', true), 0, '.txt =! download extension' );

        tracker.addDownloadExtensions('xyz');
        equal( tracker.hook.test._getLinkType('something', 'piwiktest.pk', true), 'download', '[2] .pk == download extension' );
        equal( tracker.hook.test._getLinkType('something', 'piwiktest.xyz', true), 'download', '.xyz == download extension' );

        tracker.setDownloadClasses(['a', 'b']);
        equal( tracker.hook.test._getLinkType('abc piwik_download', 'piwiktest.ext', true), 'download', 'download (default)' );
        equal( tracker.hook.test._getLinkType('abc a', 'piwiktest.ext', true), 'download', 'download (a)' );
        equal( tracker.hook.test._getLinkType('b abc', 'piwiktest.ext', true), 'download', 'download (b)' );

        tracker.setLinkClasses(['c', 'd']);
        equal( tracker.hook.test._getLinkType('abc piwik_link', 'piwiktest.ext', true), 'link', 'link (default)' );
        equal( tracker.hook.test._getLinkType('abc c', 'piwiktest.ext', true), 'link', 'link (c)' );
        equal( tracker.hook.test._getLinkType('d abc', 'piwiktest.ext', true), 'link', 'link (d)' );
    });

    test("utf8_encode(), sha1()", function() {
        expect(6);

        var tracker = Piwik.getTracker();

        equal( typeof tracker.hook.test._utf8_encode, 'function', 'utf8_encode' );
        equal( tracker.hook.test._utf8_encode('hello world'), '<?php echo utf8_encode("hello world"); ?>', 'utf8_encode("hello world")' );
        equal( tracker.hook.test._utf8_encode('Gesamtgröße'), '<?php echo utf8_encode("Gesamtgröße"); ?>', 'utf8_encode("Gesamtgröße")' );
        equal( tracker.hook.test._utf8_encode('您好'), '<?php echo utf8_encode("您好"); ?>', 'utf8_encode("您好")' );

        equal( typeof tracker.hook.test._sha1, 'function', 'sha1' );
        equal( tracker.hook.test._sha1('hello world'), '<?php echo sha1("hello world"); ?>', 'sha1("hello world")' );
    });

    test("getRequest()", function() {
        expect(1);

        var tracker = Piwik.getTracker();

        tracker.setCustomData("key is X", "value is Y");
        equal( tracker.getRequest('hello=world').indexOf('hello=world&idsite=&rec=1&r='), 0);
    });

    // support for setCustomRequestProcessing( customRequestContentProcessingLogic )
    test("Tracker setCustomRequestProcessing() and getRequest()", function() {
        expect(4);

        var tracker = Piwik.getTracker("trackerUrl", "42");

        tracker.setCustomRequestProcessing(function(request){
          var pairs = request.split('&');
          var result = {};
          pairs.forEach(function(pair) {
            pair = pair.split('=');
            result[pair[0]] = decodeURIComponent(pair[1] || '');
          });
          return JSON.stringify(result);
        });

        var json = JSON.parse(tracker.getRequest('hello=world'));
        equal( json.hello, 'world');
        equal( json.idsite, '42' );
        equal( json.rec, 1);
        ok( json.r.length > 0 );
    });

    // support for addPlugin( pluginName, pluginObj )
    test("Tracker addPlugin() and getRequest()", function() {
        expect(12);

        var tracker = Piwik.getTracker();

        var litp = (function() {
          var lastInteractionType = "";
          function ecommerce() { lastInteractionType = "ecommerce"; }
          function event() { lastInteractionType = "event"; }
          function goal() { lastInteractionType = "goal"; }
          function link() { lastInteractionType = "link"; }
          function load() { lastInteractionType = "load"; }
          function log() { lastInteractionType = "log"; }
          function ping() { lastInteractionType = "ping"; }
          function sitesearch() { lastInteractionType = "sitesearch"; }
          function unload() { lastInteractionType = "unload"; }
          function getLastInteractionType() { return lastInteractionType; }

          return {
            ecommerce: ecommerce,
            event : event,
            goal : goal,
            link : link,
            load : load,
            log : log,
            ping: ping,
            sitesearch : sitesearch,
            unload : unload,
            getLastInteractionType: getLastInteractionType
          };
        })();

        tracker.addPlugin("interactionTypePlugin", litp);

        ok(litp.getLastInteractionType() !== 'ecommerce');
        tracker.trackEcommerceOrder("ORDER ID YES", 666.66);
        ok(litp.getLastInteractionType() === 'ecommerce');

        ok(litp.getLastInteractionType() !== 'event');
        tracker.trackEvent("Event Category", "Event Action");
        ok(litp.getLastInteractionType() === 'event');

        ok(litp.getLastInteractionType() !== 'goal');
        tracker.trackGoal(42);
        ok(litp.getLastInteractionType() === 'goal');

        ok(litp.getLastInteractionType() !== 'link');
        tracker.trackLink("http://example.ca", "link");
        ok(litp.getLastInteractionType() === 'link');

        ok(litp.getLastInteractionType() !== 'log');
        tracker.trackPageView();
        ok(litp.getLastInteractionType() === 'log');

        ok(litp.getLastInteractionType() !== 'sitesearch');
        tracker.trackSiteSearch("search Keyword");
        ok(litp.getLastInteractionType() === 'sitesearch');
    });

    test("prefixPropertyName()", function() {
        expect(3);

        var tracker = Piwik.getTracker();

        equal( typeof tracker.hook.test._prefixPropertyName, 'function', 'prefixPropertyName' );
        equal( tracker.hook.test._prefixPropertyName('', 'hidden'), 'hidden', 'no prefix' );
        equal( tracker.hook.test._prefixPropertyName('webkit', 'hidden'), 'webkitHidden', 'webkit prefix' );
    });

    test("Internal timers and setLinkTrackingTimer()", function() {
        expect(5);

        var tracker = Piwik.getTracker();

        ok( ! ( _paq instanceof Array ), "async tracker proxy not an array" );
        equal( typeof tracker, typeof _paq, "async tracker proxy" );

        var startTime, stopTime;

        equal( typeof tracker.hook.test._beforeUnloadHandler, 'function', 'beforeUnloadHandler' );

        startTime = new Date();
        tracker.hook.test._beforeUnloadHandler();
        stopTime = new Date();
        var msSinceStarted = (stopTime.getTime() - startTime.getTime());
        ok( msSinceStarted < 510, 'beforeUnloadHandler(): ' + msSinceStarted + ' was greater than 510 ' );

        tracker.setLinkTrackingTimer(2000);
        startTime = new Date();
        tracker.trackPageView();
        tracker.hook.test._beforeUnloadHandler();
        stopTime = new Date();
        ok( (stopTime.getTime() - startTime.getTime()) >= 2000, 'setLinkTrackingTimer()' );
    });

    test("Overlay URL Normalizer", function() {
        expect(11);

        var test = function(testCases) {
            for (var i = 0; i < testCases.length; i++) {
                var observed = Piwik_Overlay_UrlNormalizer.normalize(testCases[i][0]);
                var expected = testCases[i][1];
                equal(observed, expected, testCases[i][0]);
            }
        };

        Piwik_Overlay_UrlNormalizer.initialize();
        Piwik_Overlay_UrlNormalizer.setExcludedParameters(['excluded1', 'excluded2', 'excluded3']);

        Piwik_Overlay_UrlNormalizer.setBaseHref(false);

        Piwik_Overlay_UrlNormalizer.setCurrentDomain('example.com');
        Piwik_Overlay_UrlNormalizer.setCurrentUrl('https://www.example.com/current/test.html?asdfasdf');

        test([
            [
                'relative/path/',
                'example.com/current/relative/path/'
            ], [
                'http://www.example2.com/path/foo.html',
                'example2.com/path/foo.html'
            ]
        ]);

        Piwik_Overlay_UrlNormalizer.setCurrentDomain('www.example3.com');
        Piwik_Overlay_UrlNormalizer.setCurrentUrl('http://example3.com/current/folder/');

        test([[
            'relative.html',
            'example3.com/current/folder/relative.html'
        ]]);

        Piwik_Overlay_UrlNormalizer.setBaseHref('http://example.com/base/');

        test([
            [
                'http://www.example2.com/my/test/path.html?id=2&excluded2=foo#MyAnchor',
                'example2.com/my/test/path.html?id=2#MyAnchor'
            ], [
                '/my/test/foo/../path.html?excluded1=foo&excluded2=foo&excluded3=foo',
                'example3.com/my/test/path.html'
            ], [
                'path/./test//test///foo.bar?excluded2=foo&id=3',
                'example.com/base/path/test/test/foo.bar?id=3'
            ], [
                'path/./test//test///foo.bar?excluded2=foo#Anchor',
                'example.com/base/path/test/test/foo.bar#Anchor'
            ], [
                'https://example2.com//test.html?id=3&excluded1=foo&bar=baz#asdf',
                'example2.com/test.html?id=3&bar=baz#asdf'
            ], [
                '#',
                ''
            ], [
                '#Anchor',
                ''
            ], [
                '/',
                'example3.com/'
            ]
        ]);
    });

<?php
if ($sqlite) {
    ?>

    module("request", {
        setup: function () {
            ok(true, "request.setup");

            deleteCookies();
            ok(document.cookie === "", "deleteCookies");
        },
        teardown: function () {
            ok(true, "request.teardown");
        }
    });

    test("tracking", function() {
        expect(98);

        /*
         * Prevent Opera and HtmlUnit from performing the default action (i.e., load the href URL)
         */
        var stopEvent = function (evt) {
                evt = evt || window.event;

//              evt.cancelBubble = true;
                evt.returnValue = false;

                if (evt.preventDefault)
                    evt.preventDefault();
//              if (evt.stopPropagation)
//                  evt.stopPropagation();

//              return false;
            };

        var tracker = Piwik.getTracker();
        tracker.setTrackerUrl("piwik.php");
        tracker.setSiteId(1);

        function wait(msecs)
        {
            var start = new Date().getTime();
            var cur = start
            while(cur - start < msecs)
            {
                cur = new Date().getTime();
            }
        }

        var visitorIdStart = tracker.getVisitorId();
        // need to wait at least 1 second so that the cookie would be different, if it wasnt persisted
        wait(2000);
        var visitorIdStart2 = tracker.getVisitorId();
        ok( visitorIdStart == visitorIdStart2, "getVisitorId() same when called twice with more than 1 second delay");
        var customUrl = "http://localhost.localdomain/?utm_campaign=YEAH&utm_term=RIGHT!";
        tracker.setCustomUrl(customUrl);

        tracker.setCustomData({ "token" : getToken() });
        var data = tracker.getCustomData();
        ok( getToken() != "" && data.token == data["token"] && data.token == getToken(), "setCustomData() , getCustomData()" );

        // Custom variables with integer/float values
        tracker.setCustomVariable(1, 1, 2, "visit");
        deepEqual( tracker.getCustomVariable(1, "visit"), ["1", "2"], "setCustomVariable() with integer name/value" );
        tracker.setCustomVariable(1, 1, 0, "visit");
        deepEqual( tracker.getCustomVariable(1, "visit"), ["1", "0"], "setCustomVariable() with integer name/value" );
        tracker.setCustomVariable(2, 1.05, 2.11, "visit");
        deepEqual( tracker.getCustomVariable(2, "visit"), ["1.05", "2.11"], "setCustomVariable() with integer name/value" );

        // custom variables with undefined names or values
        tracker.setCustomVariable(5);// setting a custom variable with no name and no value should not error
        deepEqual( tracker.getCustomVariable(5), false, "getting a custom variable with no name nor value" );
        deepEqual( tracker.getCustomVariable(55), false, "getting a custom variable with no name nor value" );
        tracker.setCustomVariable(5, "new name");
        deepEqual( tracker.getCustomVariable(5), ["new name", ""], "getting a custom variable with no value" );
        tracker.deleteCustomVariable(5);

        tracker.setDocumentTitle("PiwikTest");

        var referrerUrl = "http://referrer.example.com/page/sub?query=test&test2=test3";
        tracker.setReferrerUrl(referrerUrl);

        referrerTimestamp = Math.round(new Date().getTime() / 1000);
        tracker.trackPageView();

        tracker.trackPageView("CustomTitleTest");

        var customUrlShouldNotChangeCampaign = "http://localhost.localdomain/?utm_campaign=NONONONONONONO&utm_term=PLEASE NO!";
        tracker.setCustomUrl(customUrl);

        tracker.trackPageView();

        var trackLinkCallbackFired = false;
        var trackLinkCallback = function () {
            trackLinkCallbackFired = true;
        };
        tracker.trackLink("http://example.ca", "link", { "token" : getToken() }, trackLinkCallback);

        // async tracker proxy
        _paq.push(["trackLink", "http://example.fr/async.zip", "download",  { "token" : getToken() }]);

        // push function
        _paq.push([ function(t) {
            tracker.trackLink("http://example.de", "link", { "token" : t });
        }, getToken() ]);

        tracker.setRequestMethod("POST");
        tracker.trackGoal(42, 69, { "token" : getToken(), "boy" : "Michael", "girl" : "Mandy"});

        piwik_log("CompatibilityLayer", 1, "piwik.php", { "token" : getToken() });

        tracker.hook.test._addEventListener(_e("click8"), "click", stopEvent);
        $(_e("click8")).trigger('click');

        tracker.enableLinkTracking();

        tracker.setRequestMethod("GET");
        var buttons = new Array("click1", "click2", "click3", "click4", "click5", "click6", "click7");
        for (var i=0; i < buttons.length; i++) {
            tracker.hook.test._addEventListener(_e(buttons[i]), "click", stopEvent);
            $(_e(buttons[i])).trigger('click');
        }

        var xhr = window.XMLHttpRequest ? new window.XMLHttpRequest() :
            window.ActiveXObject ? new ActiveXObject("Microsoft.XMLHTTP") :
            null;

        var clickDiv = _e("clickDiv"),
            anchor = document.createElement("a");

        anchor.id = "click9";
        anchor.href = "http://example.us";
        clickDiv.innerHTML = "";
        clickDiv.appendChild(anchor);
        tracker.addListener(anchor);
        tracker.hook.test._addEventListener(anchor, "click", stopEvent);
        $(_e("click9")).trigger("click" );

        var visitorId1, visitorId2;

        _paq.push([ function() {
            visitorId1 = Piwik.getAsyncTracker().getVisitorId();
        }]);
        visitorId2 = tracker.getVisitorId();
        ok( visitorId1 && visitorId1 != "" && visitorId2 && visitorId2 != "" && (visitorId1 == visitorId2), "getVisitorId()" );

        var visitorInfo1, visitorInfo2;

        // Visitor INFO + Attribution INFO tests
        tracker.setReferrerUrl(referrerUrl);
        _paq.push([ function() {
            visitorInfo1 = Piwik.getAsyncTracker().getVisitorInfo();
            attributionInfo1 = Piwik.getAsyncTracker().getAttributionInfo();
            referrer1 = Piwik.getAsyncTracker().getAttributionReferrerUrl();
        }]);
        visitorInfo2 = tracker.getVisitorInfo();
        ok( visitorInfo1 && visitorInfo2 && visitorInfo1.length == visitorInfo2.length, "getVisitorInfo()" );
        for (var i = 0; i < 6; i++) {
            ok( visitorInfo1[i] == visitorInfo2[i], "(loadVisitorId())["+i+"]" );
        }

        attributionInfo2 = tracker.getAttributionInfo();
        ok( attributionInfo1 && attributionInfo2 && attributionInfo1.length == attributionInfo2.length, "getAttributionInfo()" );
        referrer2 = tracker.getAttributionReferrerUrl();
        ok( referrer2 == referrerUrl, "getAttributionReferrerUrl()" );
        ok( referrer1 == referrerUrl, "async getAttributionReferrerUrl()" );
        referrerTimestamp2 = tracker.getAttributionReferrerTimestamp();
        ok( referrerTimestamp2 == referrerTimestamp, "tracker.getAttributionReferrerTimestamp()" );
        campaignName2 = tracker.getAttributionCampaignName();
        campaignKeyword2 = tracker.getAttributionCampaignKeyword();
        ok( campaignName2 == "YEAH", "getAttributionCampaignName()");
        ok( campaignKeyword2 == "RIGHT!", "getAttributionCampaignKeyword()");

        // Test visitor ID at the start is the same at the end
        var visitorIdEnd = tracker.getVisitorId();
        ok( visitorIdStart == visitorIdEnd, "tracker.getVisitorId() same at the start and end of process");

        // Custom variables
        tracker.storeCustomVariablesInCookie();
        tracker.setCookieNamePrefix("PREFIX");
        tracker.setCustomVariable(1, "cookiename", "cookievalue");
        deepEqual( tracker.getCustomVariable(1), ["cookiename", "cookievalue"], "setCustomVariable(cvarExists), getCustomVariable()" );
        tracker.setCustomVariable(2, "cookiename2", "cookievalue2", "visit");
        deepEqual( tracker.getCustomVariable(2), ["cookiename2", "cookievalue2"], "setCustomVariable(cvarExists), getCustomVariable()" );
        deepEqual( tracker.getCustomVariable(2, "visit"), ["cookiename2", "cookievalue2"], "setCustomVariable(cvarExists), getCustomVariable()" );
        deepEqual( tracker.getCustomVariable(2, 2), ["cookiename2", "cookievalue2"], "GA compability - setCustomVariable(cvarExists), getCustomVariable()" );
        tracker.setCustomVariable(2, "cookiename2PAGE", "cookievalue2PAGE", "page");
        deepEqual( tracker.getCustomVariable(2, "page"), ["cookiename2PAGE", "cookievalue2PAGE"], "setCustomVariable(cvarExists), getCustomVariable()" );
        deepEqual( tracker.getCustomVariable(2, 3), ["cookiename2PAGE", "cookievalue2PAGE"], "GA compability - setCustomVariable(cvarExists), getCustomVariable()" );
        tracker.setCustomVariable(2, "cookiename2EVENT", "cookievalue2EVENT", "event");
        deepEqual( tracker.getCustomVariable(2, "event"), ["cookiename2EVENT", "cookievalue2EVENT"], "CustomVariable and event scope" );

        tracker.trackPageView("SaveCustomVariableCookie");

        // test Site Search
        tracker.trackSiteSearch("No result keyword éà", "Search cat", 0);
        tracker.trackSiteSearch("Keyword with 10 results", false, 10);
        tracker.trackSiteSearch("search Keyword");

        // Testing Custom events
        tracker.setCustomVariable(1, "cvarEventName", "cvarEventValue", "event");
        tracker.trackEvent("Event Category", "Event Action");
        tracker.trackEvent("Event Category2", "Event Action2", "Event Name2");
        tracker.trackEvent("Event Category3", "Event Action3", "Event Name3", 3.333);

        //Ecommerce views
        tracker.setEcommerceView( "", false, ["CATEGORY1","CATEGORY2"] );
        deepEqual( tracker.getCustomVariable(3, "page"), false, "Ecommerce view SKU");
        tracker.setEcommerceView( "SKUMultiple", false, ["CATEGORY1","CATEGORY2"] );
        deepEqual( tracker.getCustomVariable(4, "page"), ["_pkn",""], "Ecommerce view Name");
        deepEqual( tracker.getCustomVariable(5, "page"), ["_pkc","[\"CATEGORY1\",\"CATEGORY2\"]"], "Ecommerce view Category");
        tracker.trackPageView("MultipleCategories");

        var tracker2 = Piwik.getTracker();
        tracker2.setTrackerUrl("piwik.php");
        tracker2.setSiteId(1);
        tracker2.storeCustomVariablesInCookie();
        tracker2.setCustomData({ "token" : getToken() });
        tracker2.setCookieNamePrefix("PREFIX");
        deepEqual( tracker2.getCustomVariable(1), ["cookiename", "cookievalue"], "getCustomVariable(cvarExists) from cookie" );
        ok( /PREFIX/.test( document.cookie ), "setCookieNamePrefix()" );

        tracker2.deleteCustomVariable(1);
        //console.log(tracker2.getCustomVariable(1));
        ok( tracker2.getCustomVariable(1) === false, "VISIT deleteCustomVariable(), getCustomVariable() === false" );
        tracker2.deleteCustomVariable(2, "page");
        //console.log(tracker2.getCustomVariable(2, "page"));
        ok( tracker2.getCustomVariable(2, "page") === false, "PAGE deleteCustomVariable(), getCustomVariable() === false" );
        tracker2.trackPageView("DeleteCustomVariableCookie");

        var tracker3 = Piwik.getTracker();
        tracker3.setTrackerUrl("piwik.php");
        tracker3.setSiteId(1);
        tracker3.setCustomData({ "token" : getToken() });
        tracker3.setCookieNamePrefix("PREFIX");
        ok( tracker3.getCustomVariable(1) === false, "getCustomVariable(cvarDeleted) from cookie  === false" );

        // Ecommerce Views
        tracker3.setEcommerceView( "SKU", "NAME HERE", "CATEGORY HERE" );
        deepEqual( tracker3.getCustomVariable(3, "page"), ["_pks","SKU"], "Ecommerce view SKU");
        deepEqual( tracker3.getCustomVariable(4, "page"), ["_pkn","NAME HERE"], "Ecommerce view Name");
        deepEqual( tracker3.getCustomVariable(5, "page"), ["_pkc","CATEGORY HERE"], "Ecommerce view Category");
        tracker3.trackPageView("EcommerceView");

        //Ecommerce tests
        tracker3.addEcommerceItem("SKU PRODUCT", "PRODUCT NAME", "PRODUCT CATEGORY", 11.1111, 2);
        tracker3.addEcommerceItem("SKU PRODUCT", "random", "random PRODUCT CATEGORY", 11.1111, 2);
        tracker3.addEcommerceItem("SKU ONLY SKU", "", "", "", "");
        tracker3.addEcommerceItem("SKU ONLY NAME", "PRODUCT NAME 2", "", "");
        tracker3.addEcommerceItem("SKU NO PRICE NO QUANTITY", "PRODUCT NAME 3", "CATEGORY", "", "" );
        tracker3.addEcommerceItem("SKU ONLY" );
        tracker3.trackEcommerceCartUpdate( 555.55 );
        tracker3.trackEcommerceOrder( "ORDER ID YES", 666.66, 333, 222, 111, 1 );

        // do not track
        tracker3.setDoNotTrack(false);

        // Append tracking url parameter
        tracker3.appendToTrackingUrl("appended=1&appended2=value");

        // Track pageview
        tracker3.trackPageView("DoTrack");

        // Firefox 9: navigator.doNotTrack is read-only
        navigator.doNotTrack = "yes";
        if (navigator.doNotTrack === "yes")
        {
            tracker3.setDoNotTrack(true);
            tracker3.trackPageView("DoNotTrack");
        }

        // Testing JavaScriptErrorTracking START
        var oldOnError = window.onerror;

        var customOnErrorInvoked = false;
        window.onerror = function (message, url, line, column, error) {
            customOnErrorInvoked = true;

            equal(message, 'Uncaught Error: The message', 'message forwarded to custom onerror handler');
            equal(url, 'http://piwik.org/path/to/file.js?cb=34343', 'url forwarded to custom onerror handler');
            equal(line, 44, 'line forwarded to custom onerror handler');
            equal(column, 12, 'column forwarded to custom onerror handler');
            ok(error instanceof Error, 'error forwarded to custom onerror handler');
        };

        tracker.enableJSErrorTracking();
        window.onerror('Uncaught Error: The message', 'http://piwik.org/path/to/file.js?cb=34343', 44, 12, new Error('The message'));
        ok(customOnErrorInvoked, "Custom onerror handler was called as expected");

        // delete existing onerror handler and setup tracking again
        window.onerror = customOnErrorInvoked = false;
        tracker2.enableJSErrorTracking();

        window.onerror('Second Error: With less data', 'http://piwik.org/path/to/file.js?cb=3kfkf', 45);
        ok(!customOnErrorInvoked, "Custom onerror handler was ignored as expected");

        window.onerror = oldOnError;
        // Testing JavaScriptErrorTracking END

        stop();
        setTimeout(function() {
            xhr.open("GET", "piwik.php?requests=" + getToken(), false);
            xhr.send(null);
            results = xhr.responseText;
            equal( (/<span\>([0-9]+)\<\/span\>/.exec(results))[1], "30", "count tracking events" );

            // firing callback
            ok( trackLinkCallbackFired, "trackLink() callback fired" );

            // tracking requests
            ok( /PiwikTest/.test( results ), "trackPageView(), setDocumentTitle()" );
            ok( results.indexOf("tests/javascript/piwik.php?action_name=Asynchronous%20Tracker%20ONE&idsite=1&rec=1") >= 0 , "async trackPageView() called before setTrackerUrl() should work" );
            ok( /Asynchronous%20tracking%20TWO/.test( results ), "async trackPageView() called after another trackPageView()" );
            ok( /CustomTitleTest/.test( results ), "trackPageView(customTitle)" );
            ok( ! /click.example.com/.test( results ), "click: ignore href=javascript" );
            ok( /example.ca/.test( results ), "trackLink()" );
            ok( /example.fr/.test( results ), "async trackLink()" );
            ok( /example.de/.test( results ), "push function" );
            ok( /example.us/.test( results ), "addListener()" );

            ok( /example.net/.test( results ), "setRequestMethod(GET), click: implicit outlink (by outbound URL)" );
            ok( /example.html/.test( results ), "click: explicit outlink" );
            ok( /example.pdf/.test( results ), "click: implicit download (by file extension)" );
            ok( /example.word/.test( results ), "click: explicit download" );

            ok( ! /example.exe/.test( results ), "enableLinkTracking()" );
            ok( ! /example.php/.test( results ), "click: ignored example.php" );
            ok( ! /example.org/.test( results ), "click: ignored example.org" );
            ok( /idgoal=42.*?revenue=69.*?Michael.*?Mandy/.test( results ), "setRequestMethod(POST), trackGoal()" );
            ok( /CompatibilityLayer/.test( results ), "piwik_log(): compatibility layer" );
            ok( /localhost.localdomain/.test( results ), "setCustomUrl()" );
            ok( /referrer.example.com/.test( results ), "setReferrerUrl()" );
            ok( /cookiename/.test( results ) && /cookievalue/.test( results ), "tracking request contains custom variable" );
            ok( /DeleteCustomVariableCookie/.test( results ), "tracking request deleting custom variable" );
            ok( /DoTrack/.test( results ), "setDoNotTrack(false)" );
            ok( ! /DoNotTrack/.test( results ), "setDoNotTrack(true)" );

            // Test Custom variables
            ok( /SaveCustomVariableCookie.*&cvar=%7B%222%22%3A%5B%22cookiename2PAGE%22%2C%22cookievalue2PAGE%22%5D%7D.*&_cvar=%7B%221%22%3A%5B%22cookiename%22%2C%22cookievalue%22%5D%2C%222%22%3A%5B%22cookiename2%22%2C%22cookievalue2%22%5D%7D/.test(results), "test custom vars are set");

            // Test campaign parameters set
            ok( /&_rcn=YEAH&_rck=RIGHT!/.test( results), "Test campaign parameters found");
            ok( /&_ref=http%3A%2F%2Freferrer.example.com%2Fpage%2Fsub%3Fquery%3Dtest%26test2%3Dtest3/.test( results), "Test cookie Ref URL found ");

            // Test site search
            ok( /search=No%20result%20keyword%20%C3%A9%C3%A0&search_cat=Search%20cat&search_count=0&idsite=1/.test(results), "site search, cat, 0 result ");
            ok( /search=Keyword%20with%2010%20results&search_count=10&idsite=1/.test(results), "site search, no cat, 10 results ");
            ok( /search=search%20Keyword&idsite=1/.test(results), "site search, no cat, no results count ");

            // Test events
            ok( /(e_c=Event%20Category&e_a=Event%20Action&idsite=1).*(&e_cvar=%7B%221%22%3A%5B%22cvarEventName%22%2C%22cvarEventValue%22%5D%2C%222%22%3A%5B%22cookiename2EVENT%22%2C%22cookievalue2EVENT%22%5D%7D)/.test(results), "event Category + Action + Custom Variable");
            ok( /e_c=Event%20Category2&e_a=Event%20Action2&e_n=Event%20Name2&idsite=1/.test(results), "event Category + Action + Name");
            ok( /e_c=Event%20Category3&e_a=Event%20Action3&e_n=Event%20Name3&e_v=3.333&idsite=1/.test(results), "event Category + Action + Name + Value");

            // ecommerce view
            ok( /(EcommerceView).*(&cvar=%7B%225%22%3A%5B%22_pkc%22%2C%22CATEGORY%20HERE%22%5D%2C%223%22%3A%5B%22_pks%22%2C%22SKU%22%5D%2C%224%22%3A%5B%22_pkn%22%2C%22NAME%20HERE%22%5D%7D)/.test(results)
             || /(EcommerceView).*(&cvar=%7B%223%22%3A%5B%22_pks%22%2C%22SKU%22%5D%2C%224%22%3A%5B%22_pkn%22%2C%22NAME%20HERE%22%5D%2C%225%22%3A%5B%22_pkc%22%2C%22CATEGORY%20HERE%22%5D%7D)/.test(results), "ecommerce view");

            // ecommerce view multiple categories
            ok( /(MultipleCategories).*(&cvar=%7B%222%22%3A%5B%22cookiename2PAGE%22%2C%22cookievalue2PAGE%22%5D%2C%225%22%3A%5B%22_pkc%22%2C%22%5B%5C%22CATEGORY1%5C%22%2C%5C%22CATEGORY2%5C%22%5D%22%5D%2C%223%22%3A%5B%22_pks%22%2C%22SKUMultiple%22%5D%2C%224%22%3A%5B%22_pkn%22%2C%22%22%5D%7D)/.test(results)
            || /(MultipleCategories).*(&cvar=%7B%222%22%3A%5B%22cookiename2PAGE%22%2C%22cookievalue2PAGE%22%5D%2C%223%22%3A%5B%22_pks%22%2C%22SKUMultiple%22%5D%2C%224%22%3A%5B%22_pkn%22%2C%22%22%5D%2C%225%22%3A%5B%22_pkc%22%2C%22%5B%5C%22CATEGORY1%5C%22%2C%5C%22CATEGORY2%5C%22%5D%22%5D%7D)/.test(results), "ecommerce view multiple categories");

            // Ecommerce order
            ok( /idgoal=0&ec_id=ORDER%20ID%20YES&revenue=666.66&ec_st=333&ec_tx=222&ec_sh=111&ec_dt=1&ec_items=%5B%5B%22SKU%20PRODUCT%22%2C%22random%22%2C%22random%20PRODUCT%20CATEGORY%22%2C11.1111%2C2%5D%2C%5B%22SKU%20ONLY%20SKU%22%2C%22%22%2C%22%22%2C0%2C1%5D%2C%5B%22SKU%20ONLY%20NAME%22%2C%22PRODUCT%20NAME%202%22%2C%22%22%2C0%2C1%5D%2C%5B%22SKU%20NO%20PRICE%20NO%20QUANTITY%22%2C%22PRODUCT%20NAME%203%22%2C%22CATEGORY%22%2C0%2C1%5D%2C%5B%22SKU%20ONLY%22%2C%22%22%2C%22%22%2C0%2C1%5D%5D/.test( results ), "logEcommerceOrder() with items" );

            // Not set for the first ecommerce order
            ok( ! /idgoal=0&ec_id=ORDER%20ID.*_ects=1/.test(results), "Ecommerce last timestamp set");

            // Ecommerce last timestamp set properly for subsequent page view
            ok( /DoTrack.*_ects=1/.test(results), "Ecommerce last timestamp set");

            // Cart update
            ok( /idgoal=0&revenue=555.55&ec_items=%5B%5B%22SKU%20PRODUCT%22%2C%22random%22%2C%22random%20PRODUCT%20CATEGORY%22%2C11.1111%2C2%5D%2C%5B%22SKU%20ONLY%20SKU%22%2C%22%22%2C%22%22%2C0%2C1%5D%2C%5B%22SKU%20ONLY%20NAME%22%2C%22PRODUCT%20NAME%202%22%2C%22%22%2C0%2C1%5D%2C%5B%22SKU%20NO%20PRICE%20NO%20QUANTITY%22%2C%22PRODUCT%20NAME%203%22%2C%22CATEGORY%22%2C0%2C1%5D%2C%5B%22SKU%20ONLY%22%2C%22%22%2C%22%22%2C0%2C1%5D%5D/.test( results ), "logEcommerceCartUpdate() with items" );

            // parameters inserted by plugin hooks
            ok( /testlog/.test( results ), "plugin hook log" );
            ok( /testlink/.test( results ), "plugin hook link" );
            ok( /testgoal/.test( results ), "plugin hook goal" );

            // Testing the Tracking URL append
            ok( /&appended=1&appended2=value/.test( results ), "appendToTrackingUrl(query) function");

            // Testing the JavaScript Error Tracking
            ok( /e_c=JavaScript%20Errors&e_a=http%3A%2F%2Fpiwik.org%2Fpath%2Fto%2Ffile.js%3Fcb%3D34343%3A44%3A12&e_n=Uncaught%20Error%3A%20The%20message&idsite=1/.test( results ), "enableJSErrorTracking() function with predefined onerror event");
            ok( /e_c=JavaScript%20Errors&e_a=http%3A%2F%2Fpiwik.org%2Fpath%2Fto%2Ffile.js%3Fcb%3D3kfkf%3A45&e_n=Second%20Error%3A%20With%20less%20data&idsite=1/.test( results ), "enableJSErrorTracking() function without predefined onerror event and less parameters");

            start();
        }, 5000);
    });
    <?php
}
?>
}

function addEventListener(element, eventType, eventHandler, useCapture) {
    if (element.addEventListener) {
        element.addEventListener(eventType, eventHandler, useCapture);
        return true;
    }
    if (element.attachEvent) {
        return element.attachEvent('on' + eventType, eventHandler);
    }
    element['on' + eventType] = eventHandler;
}

(function (f) {
    if (document.addEventListener) {
        addEventListener(document, 'DOMContentLoaded', function ready() {
            document.removeEventListener('DOMContentLoaded', ready, false);
            f();
        });
    } else if (document.attachEvent) {
        document.attachEvent('onreadystatechange', function ready() {
            if (document.readyState === 'complete') {
                document.detachEvent('onreadystatechange', ready);
                f();
            }
        });

        if (document.documentElement.doScroll && window === top) {
            (function ready() {
                if (!hasLoaded) {
                    try {
                        document.documentElement.doScroll('left');
                    } catch (error) {
                        setTimeout(ready, 0);
                        return;
                    }
                    f();
                }
            }());
        }
    }
    addEventListener(window, 'load', f, false);
})(PiwikTest);
 </script>

 <div id="jashDiv">
 <a href="#" onclick="javascript:loadJash();" title="Open JavaScript Shell"><img id="title" src="gnome-terminal.png" border="0" width="24" height="24" /></a>
 </div>

</body>
</html>