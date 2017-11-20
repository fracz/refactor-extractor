commit d7397adf6147eba6aeb57678a08737ab2d3dbc84
Author: thc202 <thc202@gmail.com>
Date:   Thu Mar 23 22:29:42 2017 +0000

    Correct creation of Git/SVN spider seeds

    Change Spider class to create the URIs using the escaped path, as the
    URI constructor used expects the contents to be already escaped,
    preventing exceptions like:
       URIException: escaped absolute path not valid

    which happened if the path contained escaped characters. The code that
    created the seeds was refactored to reduce code duplication, also, in
    case of exception while creating the URI log all data available (not
    just the base URI).