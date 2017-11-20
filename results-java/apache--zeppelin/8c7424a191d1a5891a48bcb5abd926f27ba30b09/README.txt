commit 8c7424a191d1a5891a48bcb5abd926f27ba30b09
Author: Damien Corneau <corneadoug@gmail.com>
Date:   Thu Jul 2 12:01:14 2015 +0900

    Zeppelin-web Spring Cleaning

    After so much time in the Wild Wild West of Internet, It's Time for the Spring Cleaning of Zeppelin-web.
    This PR will be taking care of cleaning the code, architecture, cutting code into smaller pieces etc...

    * [x] - Change Code Folder Structure to a Folder Tree Style
    * [x] - Change original code and compiled code folder names
    * [x] - Update Contributing README.md to explain most of changes
    * [x] - Organize well the components and app folders

    We will do a first merge and handle this part in a different PR:
    * [ ] - Replace as much code as possible by their lodash.js counterpart
    * [ ] - Cut the code into more smaller components (who said paragraph.js?)
    * [ ] - Move Jquery code out of the controllers (by directive when possible or to somewhere else)

    Needs to make sure that:
    * [x] - #127 is handled

    Author: Damien Corneau <corneadoug@gmail.com>
    Author: CORNEAU Damien <corneadoug@gmail.com>

    Closes #56 from corneadoug/improvement/SpringCleaning and squashes the following commits:

    453af1a [Damien Corneau] Merge Master and Fix ports
    678c0fa [Damien Corneau] Fix RAT excluded and add Apache licenses in zeppelin-web
    0addb80 [Damien Corneau] Change AppScriptServlet configuration
    ef764fc [Damien Corneau] Improve uglifyjs options
    15cc7b1 [CORNEAU Damien] Fix README
    e3ca174 [CORNEAU Damien] Small fix in doc
    775f3ca [Damien Corneau] Remove unused ngdoc comments
    25a3a63 [Damien Corneau] Fix Interpreter Create form
    bdde389 [Damien Corneau] Set loonknfeel to default for everypage, and change only if looknfeel is different
    bdf3a8e [Damien Corneau] Include lodash + Interpreter Web refactoring Part1: reducing code
    931067a [Damien Corneau] Align form label to form input + improve form disable opacity
    75d12c3 [Damien Corneau] Fix CSS of paragraph forms
    e3f3016 [Damien Corneau] Fix ZEPPELIN-102
    a6ec901 [Damien Corneau] Fix ZEPPELIN-103
    7eccca8 [Damien Corneau] Fix navbar selected menu + small code improvement
    a1fe1c1 [Damien Corneau] Refactoring of Websocket
    a36adf9 [Damien Corneau] Move all websocket calls to a service
    b21cc69 [Damien Corneau] Refactor Navbar controller to controller pattern + data factory
    5a40c4c [Damien Corneau] Separate navbar to its own html file
    2dac138 [Damien Corneau] Move directives to solo directory
    9201360 [Damien Corneau] Fix project after git clean
    9b249ea [Damien Corneau] Clean JSHint errors except for already defined and configuration functions related errors
    ef6baa0 [CORNEAU Damien] Update Zeppelin-web CONTRIBUTING.md
    411df6a [Damien Corneau] Create Zeppelin-web CONTRIBUTING.md
    d3c22cf [CORNEAU Damien] Update Zeppelin-web README.md
    48eed51 [Damien Corneau] Move the font css
    3e28c3c [Damien Corneau] Change Zeppelin-web code and compiled code folders
    0ee04a2 [Damien Corneau] Fix Grunt watch
    15b502c [Damien Corneau] Change Zeppelin Folder Structure and its GruntFile
    9f9059e [Damien Corneau] Add spark dependency reduced pom to gitignore