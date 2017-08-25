commit 332173d225502a374c4a4d13cc19dc23cf7ebd1c
Author: David Grudl <david@grudl.com>
Date:   Wed Jun 19 21:35:58 2013 +0200

    Forms: DefaultFormRenderer renders checkboxes as <label><input type=checkbox>Label</label> instead of <input type=checkbox><label>Label</label> (BC break!)

    This improves compatibility with Twitter Bootstrap etc.