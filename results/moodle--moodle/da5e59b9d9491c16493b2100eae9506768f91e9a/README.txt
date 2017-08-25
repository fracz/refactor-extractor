commit da5e59b9d9491c16493b2100eae9506768f91e9a
Author: Andrew Robert Nicols <andrew.nicols@luns.net.uk>
Date:   Mon Mar 18 23:29:57 2013 +0000

    MDL-38391 lib: Rename moodle_metadata adder

    This is a bit of a grey area. This function does not get and return a value
    to the caller, and does not set a value taken from the caller. Instead it
    gets a value from get_moodle_metadata and adds it to the current instance
    of YUI_config. This rename will hopefully improve clarity here.