commit a793654fc4972fe6cb0f3f4434e2be854ae075f8
Author: Christopher Manning <manning@cs.stanford.edu>
Date:   Thu Jun 22 21:34:00 2017 -0700

    Document processing improvements. CleanXMLAnnoator: In flawedXML, warn not exception if extra close tag; write log.info for problems. WordsToSentences: Don't allow adding closing elements to previous sentence if divided off by forced end like XML element closing.