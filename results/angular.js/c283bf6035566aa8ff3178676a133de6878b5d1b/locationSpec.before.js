'use strict';

/**
 * Create jasmine.Spy on given method, but ignore calls without arguments
 * This is helpful when need to spy only setter methods and ignore getters
 */
function spyOnlyCallsWithArgs(obj, method) {
  var spy = spyOn(obj, method);
  obj[method] = function() {
    if (arguments.length) return spy.apply(this, arguments);
    return spy.originalValue.apply(this);
  };
  return spy;
}


describe('$location', function() {
  var url;

  afterEach(function() {
    // link rewriting used in html5 mode on legacy browsers binds to document.onClick, so we need
    // to clean this up after each test.
    jqLite(document).unbind('click');
  });

  describe('NewUrl', function() {
    beforeEach(function() {
      url = new LocationUrl('http://www.domain.com:9877/path/b?search=a&b=c&d#hash');
    });


    it('should provide common getters', function() {
      expect(url.absUrl()).toBe('http://www.domain.com:9877/path/b?search=a&b=c&d#hash');
      expect(url.protocol()).toBe('http');
      expect(url.host()).toBe('www.domain.com');
      expect(url.port()).toBe(9877);
      expect(url.path()).toBe('/path/b');
      expect(url.search()).toEqual({search: 'a', b: 'c', d: true});
      expect(url.hash()).toBe('hash');
      expect(url.url()).toBe('/path/b?search=a&b=c&d#hash');
    });


    it('path() should change path', function() {
      url.path('/new/path');
      expect(url.path()).toBe('/new/path');
      expect(url.absUrl()).toBe('http://www.domain.com:9877/new/path?search=a&b=c&d#hash');
    });


    it('search() should accept string', function() {
      url.search('x=y&c');
      expect(url.search()).toEqual({x: 'y', c: true});
      expect(url.absUrl()).toBe('http://www.domain.com:9877/path/b?x=y&c#hash');
    });


    it('search() should accept object', function() {
      url.search({one: 1, two: true});
      expect(url.search()).toEqual({one: 1, two: true});
      expect(url.absUrl()).toBe('http://www.domain.com:9877/path/b?one=1&two#hash');
    });


    it('search() should change single parameter', function() {
      url.search({id: 'old', preserved: true});
      url.search('id', 'new');

      expect(url.search()).toEqual({id: 'new', preserved: true});
    });


    it('search() should remove single parameter', function() {
      url.search({id: 'old', preserved: true});
      url.search('id', null);

      expect(url.search()).toEqual({preserved: true});
    });


    it('hash() should change hash fragment', function() {
      url.hash('new-hash');
      expect(url.hash()).toBe('new-hash');
      expect(url.absUrl()).toBe('http://www.domain.com:9877/path/b?search=a&b=c&d#new-hash');
    });


    it('url() should change the path, search and hash', function() {
      url.url('/some/path?a=b&c=d#hhh');
      expect(url.url()).toBe('/some/path?a=b&c=d#hhh');
      expect(url.absUrl()).toBe('http://www.domain.com:9877/some/path?a=b&c=d#hhh');
      expect(url.path()).toBe('/some/path');
      expect(url.search()).toEqual({a: 'b', c: 'd'});
      expect(url.hash()).toBe('hhh');
    });


    it('replace should set $$replace flag and return itself', function() {
      expect(url.$$replace).toBe(false);

      url.replace();
      expect(url.$$replace).toBe(true);
      expect(url.replace()).toBe(url);
    });


    it('should parse new url', function() {
      url = new LocationUrl('http://host.com/base');
      expect(url.path()).toBe('/base');

      url = new LocationUrl('http://host.com/base#');
      expect(url.path()).toBe('/base');
    });


    it('should prefix path with forward-slash', function() {
      url = new LocationUrl('http://server/a');
      url.path('b');

      expect(url.path()).toBe('/b');
      expect(url.absUrl()).toBe('http://server/b');
    });


    it('should set path to forward-slash when empty', function() {
      url = new LocationUrl('http://server');
      expect(url.path()).toBe('/');
      expect(url.absUrl()).toBe('http://server/');
    });


    it('setters should return Url object to allow chaining', function() {
      expect(url.path('/any')).toBe(url);
      expect(url.search('')).toBe(url);
      expect(url.hash('aaa')).toBe(url);
      expect(url.url('/some')).toBe(url);
    });


    it('should not preserve old properties when parsing new url', function() {
      url.$$parse('http://www.domain.com:9877/a');

      expect(url.path()).toBe('/a');
      expect(url.search()).toEqual({});
      expect(url.hash()).toBe('');
      expect(url.absUrl()).toBe('http://www.domain.com:9877/a');
    });


    it('should prepend path with basePath', function() {
      url = new LocationUrl('http://server/base/abc?a', '/base');
      expect(url.path()).toBe('/abc');
      expect(url.search()).toEqual({a: true});

      url.path('/new/path');
      expect(url.absUrl()).toBe('http://server/base/new/path?a');
    });


    it('should throw error when invalid url given', function() {
      url = new LocationUrl('http://server.org/base/abc', '/base');

      expect(function() {
        url.$$parse('http://server.org/path#/path');
      }).toThrow('Invalid url "http://server.org/path#/path", missing path prefix "/base" !');
    });


    describe('encoding', function() {

      it('should encode special characters', function() {
        url.path('/a <>#');
        url.search({'i j': '<>#'});
        url.hash('<>#');

        expect(url.path()).toBe('/a <>#');
        expect(url.search()).toEqual({'i j': '<>#'});
        expect(url.hash()).toBe('<>#');
        expect(url.absUrl()).toBe('http://www.domain.com:9877/a%20%3C%3E%23?i%20j=%3C%3E%23#%3C%3E%23');
      });


      it('should not encode !$:@', function() {
        url.path('/!$:@');
        url.search('');
        url.hash('!$:@');

        expect(url.absUrl()).toBe('http://www.domain.com:9877/!$:@#!$:@');
      });


      it('should decode special characters', function() {
        url = new LocationUrl('http://host.com/a%20%3C%3E%23?i%20j=%3C%3E%23#x%20%3C%3E%23');
        expect(url.path()).toBe('/a <>#');
        expect(url.search()).toEqual({'i j': '<>#'});
        expect(url.hash()).toBe('x <>#');
      });
    });
  });


  describe('HashbangUrl', function() {

    beforeEach(function() {
      url = new LocationHashbangUrl('http://www.server.org:1234/base#!/path?a=b&c#hash', '!');
    });


    it('should parse hashband url into path and search', function() {
      expect(url.protocol()).toBe('http');
      expect(url.host()).toBe('www.server.org');
      expect(url.port()).toBe(1234);
      expect(url.path()).toBe('/path');
      expect(url.search()).toEqual({a: 'b', c: true});
      expect(url.hash()).toBe('hash');
    });


    it('absUrl() should return hashbang url', function() {
      expect(url.absUrl()).toBe('http://www.server.org:1234/base#!/path?a=b&c#hash');

      url.path('/new/path');
      url.search({one: 1});
      url.hash('hhh');
      expect(url.absUrl()).toBe('http://www.server.org:1234/base#!/new/path?one=1#hhh');
    });


    it('should preserve query params in base', function() {
      url = new LocationHashbangUrl('http://www.server.org:1234/base?base=param#/path?a=b&c#hash', '');
      expect(url.absUrl()).toBe('http://www.server.org:1234/base?base=param#/path?a=b&c#hash');

      url.path('/new/path');
      url.search({one: 1});
      url.hash('hhh');
      expect(url.absUrl()).toBe('http://www.server.org:1234/base?base=param#/new/path?one=1#hhh');
    });


    it('should prefix path with forward-slash', function() {
      url = new LocationHashbangUrl('http://host.com/base#path', '');
      expect(url.path()).toBe('/path');
      expect(url.absUrl()).toBe('http://host.com/base#/path');

      url.path('wrong');
      expect(url.path()).toBe('/wrong');
      expect(url.absUrl()).toBe('http://host.com/base#/wrong');
    });


    it('should set path to forward-slash when empty', function() {
      url = new LocationHashbangUrl('http://server/base#!', '!');
      url.path('aaa');

      expect(url.path()).toBe('/aaa');
      expect(url.absUrl()).toBe('http://server/base#!/aaa');
    });


    it('should not preserve old properties when parsing new url', function() {
      url.$$parse('http://www.server.org:1234/base#!/');

      expect(url.path()).toBe('/');
      expect(url.search()).toEqual({});
      expect(url.hash()).toBe('');
      expect(url.absUrl()).toBe('http://www.server.org:1234/base#!/');
    });


    it('should throw error when invalid url given', function() {
      expect(function() {
        url.$$parse('http://server.org/path#/path');
      }).toThrow('Invalid url "http://server.org/path#/path", missing hash prefix "!" !');
    });


    describe('encoding', function() {

      it('should encode special characters', function() {
        url.path('/a <>#');
        url.search({'i j': '<>#'});
        url.hash('<>#');

        expect(url.path()).toBe('/a <>#');
        expect(url.search()).toEqual({'i j': '<>#'});
        expect(url.hash()).toBe('<>#');
        expect(url.absUrl()).toBe('http://www.server.org:1234/base#!/a%20%3C%3E%23?i%20j=%3C%3E%23#%3C%3E%23');
      });


      it('should not encode !$:@', function() {
        url.path('/!$:@');
        url.search('');
        url.hash('!$:@');

        expect(url.absUrl()).toBe('http://www.server.org:1234/base#!/!$:@#!$:@');
      });


      it('should decode special characters', function() {
        url = new LocationHashbangUrl('http://host.com/a#/%20%3C%3E%23?i%20j=%3C%3E%23#x%20%3C%3E%23', '');
        expect(url.path()).toBe('/ <>#');
        expect(url.search()).toEqual({'i j': '<>#'});
        expect(url.hash()).toBe('x <>#');
      });
    });
  });


  function initService(html5Mode, hashPrefix, supportHistory) {
    return function($provide){
      $provide.value('$locationConfig', {html5Mode: html5Mode, hashPrefix: hashPrefix});
      $provide.value('$sniffer', {history: supportHistory});
    };
  }
  function initBrowser(url, basePath) {
    return function($browser){
      $browser.url(url);
      $browser.$$baseHref = basePath;
    };
  }

  describe('wiring', function() {

    beforeEach(inject(initService(false, '!', true), initBrowser('http://new.com/a/b#!', '/a/b')));


    it('should update $location when browser url changes', inject(function($browser, $location) {
      spyOn($location, '$$parse').andCallThrough();
      $browser.url('http://new.com/a/b#!/aaa');
      $browser.poll();
      expect($location.absUrl()).toBe('http://new.com/a/b#!/aaa');
      expect($location.path()).toBe('/aaa');
      expect($location.$$parse).toHaveBeenCalledOnce();
    }));


    it('should update browser when $location changes', inject(function($rootScope, $browser, $location) {
      var $browserUrl = spyOnlyCallsWithArgs($browser, 'url').andCallThrough();
      $location.path('/new/path');
      expect($browserUrl).not.toHaveBeenCalled();
      $rootScope.$apply();

      expect($browserUrl).toHaveBeenCalledOnce();
      expect($browser.url()).toBe('http://new.com/a/b#!/new/path');
    }));


    it('should update browser only once per $apply cycle', inject(function($rootScope, $browser, $location) {
      var $browserUrl = spyOnlyCallsWithArgs($browser, 'url').andCallThrough();
      $location.path('/new/path');

      $rootScope.$watch(function() {
        $location.search('a=b');
      });

      $rootScope.$apply();
      expect($browserUrl).toHaveBeenCalledOnce();
      expect($browser.url()).toBe('http://new.com/a/b#!/new/path?a=b');
    }));


    it('should replace browser url when url was replaced at least once',
        inject(function($rootScope, $location, $browser) {
      var $browserUrl = spyOnlyCallsWithArgs($browser, 'url').andCallThrough();
      $location.path('/n/url').replace();
      $rootScope.$apply();

      expect($browserUrl).toHaveBeenCalledOnce();
      expect($browserUrl.mostRecentCall.args).toEqual(['http://new.com/a/b#!/n/url', true]);
    }));


    it('should update the browser if changed from within a watcher', inject(function($rootScope, $location, $browser) {
      $rootScope.$watch(function() { return true; }, function() {
        $location.path('/changed');
      });

      $rootScope.$digest();
      expect($browser.url()).toBe('http://new.com/a/b#!/changed');
    }));
  });


  // html5 history is disabled
  describe('disabled history', function() {

    it('should use hashbang url with hash prefix', inject(
      initService(false, '!'),
      initBrowser('http://domain.com/base/index.html#!/a/b', '/base/index.html'),
      function($rootScope, $location, $browser) {
        expect($browser.url()).toBe('http://domain.com/base/index.html#!/a/b');
        $location.path('/new');
        $location.search({a: true});
        $rootScope.$apply();
        expect($browser.url()).toBe('http://domain.com/base/index.html#!/new?a');
      })
    );


    it('should use hashbang url without hash prefix', inject(
      initService(false, ''),
      initBrowser('http://domain.com/base/index.html#/a/b', '/base/index.html'),
      function($rootScope, $location, $browser) {
        expect($browser.url()).toBe('http://domain.com/base/index.html#/a/b');
        $location.path('/new');
        $location.search({a: true});
        $rootScope.$apply();
        expect($browser.url()).toBe('http://domain.com/base/index.html#/new?a');
      })
    );
  });


  // html5 history enabled, but not supported by browser
  describe('history on old browser', function() {

    afterEach(inject(function($document){
      dealoc($document);
    }));

    it('should use hashbang url with hash prefix', inject(
      initService(true, '!!', false),
      initBrowser('http://domain.com/base/index.html#!!/a/b', '/base/index.html'),
      function($rootScope, $location,  $browser) {
        expect($browser.url()).toBe('http://domain.com/base/index.html#!!/a/b');
        $location.path('/new');
        $location.search({a: true});
        $rootScope.$apply();
        expect($browser.url()).toBe('http://domain.com/base/index.html#!!/new?a');
      })
    );


    it('should redirect to hashbang url when new url given', inject(
      initService(true, '!'),
      initBrowser('http://domain.com/base/new-path/index.html', '/base/index.html'),
      function($browser, $location) {
        expect($browser.url()).toBe('http://domain.com/base/index.html#!/new-path/index.html');
      })
    );
  });


  // html5 history enabled and supported by browser
  describe('history on new browser', function() {

    afterEach(inject(function($document){
      dealoc($document);
    }));

    it('should use new url', inject(
      initService(true, '', true),
      initBrowser('http://domain.com/base/old/index.html#a', '/base/index.html'),
      function($rootScope, $location, $browser) {
        expect($browser.url()).toBe('http://domain.com/base/old/index.html#a');
        $location.path('/new');
        $location.search({a: true});
        $rootScope.$apply();
        expect($browser.url()).toBe('http://domain.com/base/new?a#a');
      })
    );


    it('should rewrite when hashbang url given', inject(
      initService(true, '!', true),
      initBrowser('http://domain.com/base/index.html#!/a/b', '/base/index.html'),
      function($rootScope, $location, $browser) {
        expect($browser.url()).toBe('http://domain.com/base/a/b');
        $location.path('/new');
        $location.hash('abc');
        $rootScope.$apply();
        expect($browser.url()).toBe('http://domain.com/base/new#abc');
        expect($location.path()).toBe('/new');
      })
    );


    it('should rewrite when hashbang url given (without hash prefix)', inject(
      initService(true, '', true),
      initBrowser('http://domain.com/base/index.html#/a/b', '/base/index.html'),
      function($rootScope, $location, $browser) {
        expect($browser.url()).toBe('http://domain.com/base/a/b');
        expect($location.path()).toBe('/a/b');
      })
    );
  });


  describe('URL_MATCH', function() {

    it('should parse basic url', function() {
      var match = URL_MATCH.exec('http://www.angularjs.org/path?search#hash?x=x');

      expect(match[1]).toBe('http');
      expect(match[3]).toBe('www.angularjs.org');
      expect(match[6]).toBe('/path');
      expect(match[8]).toBe('search');
      expect(match[10]).toBe('hash?x=x');
    });


    it('should parse file://', function() {
      var match = URL_MATCH.exec('file:///Users/Shared/misko/work/angular.js/scenario/widgets.html');

      expect(match[1]).toBe('file');
      expect(match[3]).toBe('');
      expect(match[5]).toBeFalsy();
      expect(match[6]).toBe('/Users/Shared/misko/work/angular.js/scenario/widgets.html');
      expect(match[8]).toBeFalsy();
    });


    it('should parse url with "-" in host', function() {
      var match = URL_MATCH.exec('http://a-b1.c-d.09/path');

      expect(match[1]).toBe('http');
      expect(match[3]).toBe('a-b1.c-d.09');
      expect(match[5]).toBeFalsy();
      expect(match[6]).toBe('/path');
      expect(match[8]).toBeFalsy();
    });


    it('should parse host without "/" at the end', function() {
      var match = URL_MATCH.exec('http://host.org');
      expect(match[3]).toBe('host.org');

      match = URL_MATCH.exec('http://host.org#');
      expect(match[3]).toBe('host.org');

      match = URL_MATCH.exec('http://host.org?');
      expect(match[3]).toBe('host.org');
    });


    it('should match with just "/" path', function() {
      var match = URL_MATCH.exec('http://server/#?book=moby');

      expect(match[10]).toBe('?book=moby');
    });
  });


  describe('PATH_MATCH', function() {

    it('should parse just path', function() {
      var match = PATH_MATCH.exec('/path');
      expect(match[1]).toBe('/path');
    });


    it('should parse path with search', function() {
      var match = PATH_MATCH.exec('/ppp/a?a=b&c');
      expect(match[1]).toBe('/ppp/a');
      expect(match[3]).toBe('a=b&c');
    });


    it('should parse path with hash', function() {
      var match = PATH_MATCH.exec('/ppp/a#abc?');
      expect(match[1]).toBe('/ppp/a');
      expect(match[5]).toBe('abc?');
    });


    it('should parse path with both search and hash', function() {
      var match = PATH_MATCH.exec('/ppp/a?a=b&c#abc/d?');
      expect(match[3]).toBe('a=b&c');
    });
  });


  describe('link rewriting', function() {

    var root, link, originalBrowser, lastEventPreventDefault;

    function configureService(linkHref, html5Mode, supportHist, attrs, content) {
      return function($provide){
        var jqRoot = jqLite('<div></div>');
        attrs = attrs ? ' ' + attrs + ' ' : '';
        link = jqLite('<a href="' + linkHref + '"' + attrs + '>' + content + '</a>')[0];
        root = jqRoot.append(link)[0];

        jqLite(document.body).append(jqRoot);

        $provide.value('$document', jqRoot);
        $provide.value('$sniffer', {history: supportHist});
        $provide.value('$locationConfig', {html5Mode: html5Mode, hashPrefix: '!'});
      };
    }

    function initBrowser() {
      return function($browser){
        $browser.url('http://host.com/base');
        $browser.$$baseHref = '/base/index.html';
      };
    }

    function initLocation() {
      return function($browser, $location, $document) {
        originalBrowser = $browser.url();
        // we have to prevent the default operation, as we need to test absolute links (http://...)
        // and navigating to these links would kill jstd
        $document.bind('click', function(e) {
          lastEventPreventDefault = e.isDefaultPrevented();
          e.preventDefault();
        });
      };
    }

    function expectRewriteTo($browser, url) {
      expect(lastEventPreventDefault).toBe(true);
      expect($browser.url()).toBe(url);
    }

    function expectNoRewrite($browser) {
      expect(lastEventPreventDefault).toBe(false);
      expect($browser.url()).toBe(originalBrowser);
    }

    afterEach(function() {
      dealoc(root);
      dealoc(document.body);
    });


    it('should rewrite rel link to new url when history enabled on new browser', inject(
      configureService('link?a#b', true, true),
      initBrowser(),
      initLocation(),
      function($browser) {
        browserTrigger(link, 'click');
        expectRewriteTo($browser, 'http://host.com/base/link?a#b');
      })
    );


    it('should rewrite abs link to new url when history enabled on new browser', inject(
      configureService('/base/link?a#b', true, true),
      initBrowser(),
      initLocation(),
      function($browser) {
        browserTrigger(link, 'click');
        expectRewriteTo($browser, 'http://host.com/base/link?a#b');
      })
    );


    it('should rewrite rel link to hashbang url when history enabled on old browser', inject(
      configureService('link?a#b', true, false),
      initBrowser(),
      initLocation(),
      function($browser) {
        browserTrigger(link, 'click');
        expectRewriteTo($browser, 'http://host.com/base/index.html#!/link?a#b');
      })
    );


    it('should rewrite abs link to hashbang url when history enabled on old browser', inject(
      configureService('/base/link?a#b', true, false),
      initBrowser(),
      initLocation(),
      function($browser) {
        browserTrigger(link, 'click');
        expectRewriteTo($browser, 'http://host.com/base/index.html#!/link?a#b');
      })
    );


    it('should not rewrite when history disabled', inject(
      configureService('#new', false),
      initBrowser(),
      initLocation(),
      function($browser) {
        browserTrigger(link, 'click');
        expectNoRewrite($browser);
      })
    );


    it('should not rewrite ng:ext-link', inject(
      configureService('#new', true, true, 'ng:ext-link'),
      initBrowser(),
      initLocation(),
      function($browser) {
        browserTrigger(link, 'click');
        expectNoRewrite($browser);
      })
    );


    it('should not rewrite full url links do different domain', inject(
      configureService('http://www.dot.abc/a?b=c', true),
      initBrowser(),
      initLocation(),
      function($browser) {
        browserTrigger(link, 'click');
        expectNoRewrite($browser);
      })
    );


    it('should not rewrite links with target="_blank"', inject(
      configureService('/a?b=c', true, true, 'target="_blank"'),
      initBrowser(),
      initLocation(),
      function($browser) {
        browserTrigger(link, 'click');
        expectNoRewrite($browser);
      })
    );


    it('should not rewrite links with target specified', inject(
      configureService('/a?b=c', true, true, 'target="some-frame"'),
      initBrowser(),
      initLocation(),
      function($browser) {
        browserTrigger(link, 'click');
        expectNoRewrite($browser);
      })
    );


    it('should rewrite full url links to same domain and base path', inject(
      configureService('http://host.com/base/new', true),
      initBrowser(),
      initLocation(),
      function($browser) {
        browserTrigger(link, 'click');
        expectRewriteTo($browser, 'http://host.com/base/index.html#!/new');
      })
    );


    it('should rewrite when clicked span inside link', inject(
      configureService('some/link', true, true, '', '<span>link</span>'),
      initBrowser(),
      initLocation(),
      function($browser) {
        var span = jqLite(link).find('span');

        browserTrigger(span, 'click');
        expectRewriteTo($browser, 'http://host.com/base/some/link');
      })
    );


    // don't run next tests on IE<9, as browserTrigger does not simulate pressed keys
    if (!(msie < 9)) {

      it('should not rewrite when clicked with ctrl pressed', inject(
        configureService('/a?b=c', true, true),
        initBrowser(),
        initLocation(),
        function($browser) {
          browserTrigger(link, 'click', ['ctrl']);
          expectNoRewrite($browser);
        })
      );


      it('should not rewrite when clicked with meta pressed', inject(
        configureService('/a?b=c', true, true),
        initBrowser(),
        initLocation(),
        function($browser) {
          browserTrigger(link, 'click', ['meta']);
          expectNoRewrite($browser);
        })
      );
    }
  });
});