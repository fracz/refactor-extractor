commit bb7acbe01ae4d5ad1adda44a8f99ed2ba601148e
Author: Abhiraj Butala <abhiraj.butala@gmail.com>
Date:   Tue Apr 14 22:38:50 2015 -0700

    Initial work on core refactoring. Created a 'common' module and moved all the components that are common to clients and servers to that module. Also moved the unit tests as appropriate. Linked this module to parent pom.xml file. Added a log4j.properties file for the tests.