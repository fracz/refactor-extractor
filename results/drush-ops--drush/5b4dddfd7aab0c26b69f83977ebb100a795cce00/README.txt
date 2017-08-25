commit 5b4dddfd7aab0c26b69f83977ebb100a795cce00
Author: Moshe Weitzman <weitzman@tejasa.com>
Date:   Thu Apr 24 13:11:14 2014 -0400

    self::sites is new static variable in test cases.

    Get to it with a new $this->getSites() method.

    This enabled me to refactor configTest to use separate test methods without having to reinstall Drupal for every one. Other tests will get similar mimprovement soon.