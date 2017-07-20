commit ae2c056ce69c3c5c0c45640e750989435592fe36
Author: Bernard Saulme <b92@wanadoo.fr>
Date:   Fri Mar 4 19:51:20 2016 +0100

    W3C compliance improvement for com_installer

    ** How to reproduce

    Test the page /administrator/index.php?option=com_installer with with https://validator.w3.org
    This produce: "Error : Error: Element style not allowed as child of element div in this context" (From line 431, column 11; to line 431)

    ** Proposal
    Use JFactory::getDocument()->addStyleDeclaration() to add the css, instead of adding ccs into the page content with <style type="text/css"> ...</style>

    note: after the change the information text have the same 'look' :)