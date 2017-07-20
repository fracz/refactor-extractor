commit c06576d410571c5e96f5384d313420879e7d790f
Author: koopersmith <koopersmith@1a063a9b-81f0-0310-95a4-ce76da25c4cd>
Date:   Thu Mar 15 04:14:05 2012 +0000

    Theme Customizer: First pass for upload controls, using background image as an example. Add a wrapper for Plupload that allows for custom upload UIs. see #19910.

    wp.Uploader is a wrapper that provides a simple way to upload an attachment (using the wp_ajax_upload_attachment handler). It is intentionally decoupled from the UI. When an upload succeeds, it will receive the attachment information (id, url, meta, etc) as a JSON response. If the upload fails, the wrapper handles both WordPress and plupload errors through a single handler.

    As todos, we should add drag classes for the uploader dropzone and account for the rough 100mb filesize limit in most browsers. The UI for the customizer upload controls could be improved as well.

    git-svn-id: http://svn.automattic.com/wordpress/trunk@20179 1a063a9b-81f0-0310-95a4-ce76da25c4cd