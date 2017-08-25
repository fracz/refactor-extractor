commit 9af0df9ff4a81a8c0686f8bafcd6caadeba9948f
Author: Mike van Riel <mike.vanriel@naenius.com>
Date:   Thu Sep 20 16:56:01 2012 +0200

    Fix breaking issue due to variable rename

    A variable rename during a refactoring session is throwing fatal errors 'No modification allowed'.
    This is caused by the overwriting of a XML node in the DocBlockExporter