commit aea2e901a93f6923417d01fe03f6910137ae557c
Author: Ryan Weaver <ryan@thatsquality.com>
Date:   Mon Jul 14 21:25:44 2014 -0500

    Extracting logic into a new class related to selecting the latest version

    Also refactored InitCommand slightly so that you can use this "latest version"
    functionality when searching for a package as well.