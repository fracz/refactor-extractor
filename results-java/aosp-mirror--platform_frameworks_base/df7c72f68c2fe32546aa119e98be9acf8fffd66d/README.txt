commit df7c72f68c2fe32546aa119e98be9acf8fffd66d
Author: Jean Chalard <jchalard@google.com>
Date:   Thu Feb 28 15:28:54 2013 -0800

    Initial refactoring to move updateSelection to a better place

    This is step 1 of a four-step change which will move where
    updateSelection is called in regular text fields. The new
    behavior will be to call it for each actual batch of
    changes, instead of doing it in the render loop which leads
    to both false positives and true negatives.

    Bug: 8000119

    Change-Id: I17bd91a129b18d5451fe1d8e7794ae20165de309