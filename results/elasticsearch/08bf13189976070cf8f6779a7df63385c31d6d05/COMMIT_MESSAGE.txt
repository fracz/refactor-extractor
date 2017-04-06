commit 08bf13189976070cf8f6779a7df63385c31d6d05
Author: Costin Leau <costin.leau@gmail.com>
Date:   Mon Sep 16 12:57:55 2013 +0300

    rework script to handle path with spaces
    use service id for pid name
    disable filtering on *.exe (caused corruption)
    rename exe names and add more options to .bat
    start/stop operations are now supported (and expected to be called) by service.bat
    add more variables from the env to customize default behavior prior to installing the service
    add manager option
    fixes regarding batch flow
    specify service id in description
    minor readability improvement
    include .exe only in ZIP archive
    rename x64 service id to make it work out of the box
    add elasticsearch as a service for Windows platforms
    based on Apace Commons Daemon
    supports both x64 and x86