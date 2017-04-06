commit ac4318a2fa5c6d306dbc19466246292a81767fca
Author: Igor Minar <igor@angularjs.org>
Date:   Mon Mar 26 23:38:20 2012 -0700

    refactor(fromJson/date filter): move date string logic to date filter

    Breaks angular.fromJson which doesn't deserialize date strings into date objects.

    This was done to make fromJson compatible with JSON.parse.

    If you do require the old behavior - if at all neeeded then because of
    json deserialization of XHR responses - then please create a custom
    $http transform:

    $httpProvider.defaults.transformResponse.push(function(data) {
      // recursively parse dates from data object here
      // see code removed in this diff for hints
    });

    Closes #202