commit db7602af7c26258b7f1ff5dd7ac42c6613d146df
Author: Dan Poltawski <dan@moodle.com>
Date:   Mon Jun 4 10:51:21 2012 +0800

    MDL-33501 - oauth2lib: improve redirect url handling

    Only accept PARAM_LOCALURL for state params and enforce
    use of moodle_url param in oauthlib to facilitate that.