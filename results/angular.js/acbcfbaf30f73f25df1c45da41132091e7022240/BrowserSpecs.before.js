describe('browser', function(){

  var browser, location, head, xhr;

  beforeEach(function(){
    location = {href:"http://server", hash:""};
    head = {
        scripts: [],
        append: function(node){head.scripts.push(node);}
    };
    xhr = null;
    browser = new Browser(location, jqLite(window.document), head, function(){
      xhr = this;
      this.open = noop;
      this.setRequestHeader = noop;
      this.send = noop;
    });
  });

  it('should contain cookie cruncher', function() {
    expect(browser.cookies).toBeDefined();
  });

  describe('outstading requests', function(){
    it('should process callbacks immedietly with no outstanding requests', function(){
      var callback = jasmine.createSpy('callback');
      browser.notifyWhenNoOutstandingRequests(callback);
      expect(callback).wasCalled();
    });

    it('should queue callbacks with outstanding requests', function(){
      var callback = jasmine.createSpy('callback');
      browser.xhr('GET', '/url', noop);
      browser.notifyWhenNoOutstandingRequests(callback);
      expect(callback).not.wasCalled();

      xhr.readyState = 4;
      xhr.onreadystatechange();
      expect(callback).wasCalled();
    });
  });

  describe('xhr', function(){
    describe('JSON', function(){
      it('should add script tag for request', function() {
        var log = "";
        browser.xhr('JSON', 'http://example.org/path?cb=JSON_CALLBACK', function(code, data){
          log += code + ':' + data + ';';
        });
        expect(head.scripts.length).toEqual(1);
        var url = head.scripts[0].src.split('?cb=');
        expect(url[0]).toEqual('http://example.org/path');
        expect(typeof window[url[1]]).toEqual($function);
        window[url[1]]('data');
        expect(log).toEqual('200:data;');
        expect(typeof window[url[1]]).toEqual('undefined');
      });
    });
  });


  describe('cookies', function() {

    function deleteAllCookies() {
      var cookies = document.cookie.split(";");

      for (var i = 0; i < cookies.length; i++) {
        var cookie = cookies[i];
        var eqPos = cookie.indexOf("=");
        var name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
        document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT";
      }
    }

    var browser;

    beforeEach(function() {
      deleteAllCookies();
      browser = new Browser({}, jqLite(document));
      expect(document.cookie).toEqual('');
    });


    afterEach(function() {
      deleteAllCookies();
      expect(document.cookie).toEqual('');
    });


    describe('remove all via (null)', function() {

      it('should do nothing when no cookies are set', function() {
        browser.cookies(null);
        expect(document.cookie).toEqual('');
        expect(browser.cookies()).toEqual({});
      });

    });

    describe('remove via (cookieName, undefined)', function() {

      it('should remove a cookie when it is present', function() {
        document.cookie = 'foo=bar';

        browser.cookies('foo', undefined);

        expect(document.cookie).toEqual('');
        expect(browser.cookies()).toEqual({});
      });


      it('should do nothing when an nonexisting cookie is being removed', function() {
        browser.cookies('doesntexist', undefined);
        expect(document.cookie).toEqual('');
        expect(browser.cookies()).toEqual({});
      });
    });


    describe('put via (cookieName, string)', function() {

      it('should create and store a cookie', function() {
        browser.cookies('cookieName', 'cookieValue');
        expect(document.cookie).toEqual('cookieName=cookieValue');
        expect(browser.cookies()).toEqual({'cookieName':'cookieValue'});
      });


      it('should overwrite an existing unsynced cookie', function() {
        document.cookie = "cookie=new";

        var oldVal = browser.cookies('cookie', 'newer');

        expect(document.cookie).toEqual('cookie=newer');
        expect(browser.cookies()).toEqual({'cookie':'newer'});
        expect(oldVal).not.toBeDefined();
      });

      it('should escape both name and value', function() {
        browser.cookies('cookie1=', 'val;ue');
        browser.cookies('cookie2=bar;baz', 'val=ue');

        var rawCookies = document.cookie.split("; "); //order is not guaranteed, so we need to parse
        expect(rawCookies.length).toEqual(2);
        expect(rawCookies).toContain('cookie1%3D=val%3Bue');
        expect(rawCookies).toContain('cookie2%3Dbar%3Bbaz=val%3Due');
      });
    });


    describe('get via (cookieName)', function() {

      it('should return undefined for nonexistent cookie', function() {
        expect(browser.cookies('nonexistent')).not.toBeDefined();
      });


      it ('should return a value for an existing cookie', function() {
        document.cookie = "foo=bar";
        browser.cookies(true);
        expect(browser.cookies().foo).toEqual('bar');
      });


      it ('should unescape cookie values that were escaped by puts', function() {
        document.cookie = "cookie2%3Dbar%3Bbaz=val%3Due";
        browser.cookies(true);
        expect(browser.cookies()['cookie2=bar;baz']).toEqual('val=ue');
      });


      it('should preserve leading & trailing spaces in names and values', function() {
        browser.cookies(' cookie name ', ' cookie value ');
        expect(browser.cookies()[' cookie name ']).toEqual(' cookie value ');
        expect(browser.cookies()['cookie name']).not.toBeDefined();
      });
    });


    describe('getAll', function() {

      it('should return cookies as hash', function() {
        document.cookie = "foo1=bar1";
        document.cookie = "foo2=bar2";
        expect(browser.cookies()).toEqual({'foo1':'bar1', 'foo2':'bar2'});
      });


      it('should return empty hash if no cookies exist', function() {
        expect(browser.cookies()).toEqual({});
      });
    });


    it('should pick up external changes made to browser cookies', function() {
      browser.cookies('oatmealCookie', 'drool');
      expect(browser.cookies()).toEqual({'oatmealCookie':'drool'});

      document.cookie = 'oatmealCookie=changed';
      browser.cookies(true);
      expect(browser.cookies().oatmealCookie).toEqual('changed');
    });


    it('should initialize cookie cache with existing cookies', function() {
      document.cookie = "existingCookie=existingValue";
      expect(browser.cookies()).toEqual({'existingCookie':'existingValue'});
    });

  });

  describe('poll', function(){
    it('should call all fns on poll', function(){
      var log = '';
      browser.addPollFn(function(){log+='a';});
      browser.addPollFn(function(){log+='b';});
      expect(log).toEqual('');
      browser.poll();
      expect(log).toEqual('ab');
      browser.poll();
      expect(log).toEqual('abab');
    });

    it('should startPoller', function(){
      var log = '';
      var setTimeoutSpy = jasmine.createSpy('setTimeout');
      browser.addPollFn(function(){log+='.';});
      browser.startPoller(50, setTimeoutSpy);
      expect(log).toEqual('.');
      expect(setTimeoutSpy.mostRecentCall.args[1]).toEqual(50);
      setTimeoutSpy.mostRecentCall.args[0]();
      expect(log).toEqual('..');
    });
  });
});
