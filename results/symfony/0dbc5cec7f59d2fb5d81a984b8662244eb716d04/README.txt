commit 0dbc5cec7f59d2fb5d81a984b8662244eb716d04
Merge: 2a778c2 23590eb
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Mon Jan 2 15:50:07 2017 +0100

    Merge branch '2.8' into 3.1

    * 2.8:
      do not depend on a fixed date in layout tests
      [Console] Escape default value when dumping help
      [Console] OS X Can't call cli_set_process_title php without superuser
      Fixed @return when returning this or static #bis
      Polish translation improvement in Validator component
      [Console] Descriptors should use Helper::strlen
      [Config] Improve PHPdoc / IDE autocomplete
      [Debug] Wrap call to ->log in a try catch block
      [Debug] UndefinedMethodFatalErrorHandler - Handle anonymous classes
      [SecurityBundle] Made collection of user provider unique when injecting them to the RemberMeService