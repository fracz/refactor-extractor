commit f6d8b99dc1a12ca2118cbccbe2f385bcc00d5dd7
Author: Chris Gioran <chris.gioran@neotechnology.com>
Date:   Wed Mar 5 15:49:59 2014 +0100

    First wave of refactorings disentangling Commands from operations on them

    This is the first in a series of commits refactoring how commands are
     read, written and executed and how transactions are deserialized and
     rewritten. The immediate usability of this is rolling upgrades. However,
     this is not complete, still lacking proper method names in places and some
     wires hanging out. Must be polished before merging.

    Neo store Commands no longer carry the responsibility of reading, writing and
     executing themselves. Instead, this is now moved to XaCommandReader, XaCommandWriter
     and XaCommandExecutor classes which can implement whichever way is necessary to go
     about their task. Currently there is only one, which is what the Command
     subclasses did on their own.
    Commands no longer need to be created in each usage, although they currently are. An
     init() call can bind it to a proper context, allowing future pooling of Command objects,
     or even having just a single one passed around.
    XaLogicalLog now requires and uses factories for the command reader and command writer
     to use, since eventually that will be provided from the outside so rolling upgrades
     can work. The factory is necessary since the LogExtractor must use a separate
     scratch buffer.
    Transaction reading and writing from XaLogicalLog now happens through a LogReader/LogWriter
     pair which can accept different handlers, abstracting operations in a way that allows
     for random rewriting of incoming or outgoing transactions. This way, master and slave
     operations are practically the same except that they set a different handler in each case.