commit 662a4b374089ed4b3b05afe650c9fbedde067b05
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Mar 23 15:52:52 2011 +0100

    removed the status message from HttpException, changed the signature so that most useful arguments are first, fixed many small problems introduced with previous HTTP exception refactoring

    Quote from HTTP (bis) spec (Part 2 - 5.1.1):

    The Reason Phrase exists for the
    sole purpose of providing a textual description associated with the
    numeric status code, out of deference to earlier Internet application
    protocols that were more frequently used with interactive text
    clients.  A client SHOULD ignore the content of the Reason Phrase.