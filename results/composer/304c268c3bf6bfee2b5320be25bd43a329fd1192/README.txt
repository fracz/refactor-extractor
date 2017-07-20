commit 304c268c3bf6bfee2b5320be25bd43a329fd1192
Author: Chris Smith <chris@cs278.org>
Date:   Sun Jan 24 18:52:09 2016 +0000

    Tidy up and general improvement of sAN handling code

    * Move OpenSSL functions into a new TlsHelper class
    * Add error when sAN certificate cannot be verified due to
      CVE-2013-6420
    * Throw exception if PHP >= 5.6 manages to use fallback code
    * Add support for wildcards in CN/sAN
    * Add tests for cert name validation
    * Check for backported security fix for CVE-2013-6420 using
      testcase from PHP tests.
    * Whitelist some disto PHP versions that have the CVE-2013-6420
      fix backported.