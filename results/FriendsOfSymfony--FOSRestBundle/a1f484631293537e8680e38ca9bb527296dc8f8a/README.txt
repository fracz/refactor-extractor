commit a1f484631293537e8680e38ca9bb527296dc8f8a
Author: Jonathan Chan <jc@jmccc.com>
Date:   Tue Aug 26 02:02:36 2014 -0400

    Adding ParamFetcher::addParam() functionality

    Added ability for ParamFetcher to add new params at runtime (with proper tests)

    refactoring initParams() to be called from getParam() for ParamFetcher

    adding documentation for addParam()