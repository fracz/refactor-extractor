commit 3de8d66a82581d9056e02cf365f48ca876435495
Author: till <till@php.net>
Date:   Thu Mar 22 17:19:10 2012 +0100

     * refactor SvnDownloader to use new Util Class
     * now supports auth all over
     * svn command generation is proxied through one place
     * still needs the 'interactive' settings and an execute method