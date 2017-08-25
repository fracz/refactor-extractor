commit 92c781cab35c42e1ec43a75bf78bcf30a1b4d896
Author: gustav_delius <gustav_delius>
Date:   Thu Feb 16 10:20:27 2006 +0000

    Update the stamp each time a question is edited. This is the outcome of a long Skype discussion with Eloy on 15/02/06. A planned improvement for the future is to set the stamp to a md5 hash of the questiondata to ensure that identical questions will have identical stamps. Currently it is possible to obtain some duplication during question restore if between backup and restore the teacher clicks on the Save button on the editing page without acutally making any changes. Admittedly not a very likely scenario and hence the current system is good enough for now.