<?php

define( 'ABSPATH', dirname(dirname(__FILE__)) );
define( 'WPINC', '/wp-includes' );

function __() {}
function add_filter() {}
function attribute_escape() {}
function apply_filters() {}
function get_option() {}
function is_lighttpd_before_150() {}
function add_action() {}
function do_action_ref_array() {}
function get_bloginfo() {}
function is_admin() {return true;}
function site_url() {}
function admin_url() {}

require(ABSPATH . '/wp-includes/script-loader.php');
require(ABSPATH . '/wp-includes/version.php');

$wp_scripts = new WP_Scripts();
wp_default_scripts($wp_scripts);

$wp_styles = new WP_Styles();
wp_default_styles($wp_styles);

$get_lang = file_exists( ABSPATH . '/wp-config.php') ? file( ABSPATH . '/wp-config.php' ) : file( dirname(ABSPATH) . '/wp-config.php' );

if ( is_array($get_lang) ) {
	foreach ( $get_lang as $val ) {
		if ( strpos( $val, "'WPLANG'" ) !== false ) {
			eval( $val );
			break;
		}
	}
}

if ( defined('WPLANG') && '' != WPLANG ) {
	if ( file_exists(ABSPATH . '/wp-content/languages') && @is_dir(ABSPATH . '/wp-content/languages') )
		$langdir = '/wp-content/languages/';
	else
		$langdir = '/wp-includes/languages/';

	$locale_file = ABSPATH . $langdir . WPLANG . '.php';
	if ( is_readable($locale_file) )
		include_once($locale_file);
}

$rtl = ( isset($text_direction) && 'rtl' == $text_direction ) ? true : false;

$defaults = $man_version = '';
foreach ( $wp_scripts->registered as $script ) {
	if ( empty($script->src) || strpos($script->src, 'tiny_mce_config.php') ) continue;
	$ver = empty($script->ver) ? $wp_version : $script->ver;
	$src = str_replace( array( '/wp-admin/', '/wp-includes/' ), array( '', '../wp-includes/' ), $script->src );
	$defaults .= '{ "url" : "' . $src . '?ver=' . $ver . '" },' . "\n";
	$man_version .= $ver;
}

foreach ( $wp_styles->registered as $style ) {
	if ( empty($style->src) ) continue;

	$ver = empty($style->ver) ? $wp_version : $style->ver;
	$src = str_replace( array( '/wp-admin/', '/wp-includes/' ), array( '', '../wp-includes/' ), $style->src );
	if ( 'colors' == $style->handle ) $src = 'css/colors-classic.css';
	$defaults .= '{ "url" : "' . $src . '?ver=' . $ver . '" },' . "\n";

	if ( $rtl && isset($style->extra['rtl']) && $style->extra['rtl'] ) {
		if ( is_bool( $style->extra['rtl'] ) )
			$rtl_href = str_replace( '.css', '-rtl.css', $src );
		else
			$rtl_href = str_replace( array( '/wp-admin/', '/wp-includes/' ), array( '', '../wp-includes/' ), $style->extra['rtl'] );

		$defaults .= '{ "url" : "' . $rtl_href . '?ver=' . $ver . '" },' . "\n";
	}
	$man_version .= $ver;
}

$man_version = md5($man_version);

header( 'Expires: Wed, 11 Jan 1984 05:00:00 GMT' );
header( 'Last-Modified: ' . gmdate( 'D, d M Y H:i:s' ) . ' GMT' );
header( 'Cache-Control: no-cache, must-revalidate, max-age=0' );
header( 'Pragma: no-cache' );
header( 'Content-Type: application/x-javascript; charset=UTF-8' );
?>
{
"betaManifestVersion" : 1,
"version" : "<?php echo $man_version; ?>_20080606",
"entries" : [
<?php echo $defaults; ?>

{ "url" : "images/align-center.png" },
{ "url" : "images/align-left.png" },
{ "url" : "images/align-none.png" },
{ "url" : "images/align-right.png" },
{ "url" : "images/browse-happy.gif" },
{ "url" : "images/bubble_bg.gif" },
{ "url" : "images/comment-grey-bubble.png" },
{ "url" : "images/comment-pill.gif" },
{ "url" : "images/comment-stalk-classic.gif" },
{ "url" : "images/comment-stalk-fresh.gif" },
{ "url" : "images/comment-stalk-rtl.gif" },
{ "url" : "images/date-button.gif" },
{ "url" : "images/fade-butt.png" },
{ "url" : "images/gear.png" },
{ "url" : "images/logo-ghost.png" },
{ "url" : "images/logo-login.gif" },
{ "url" : "images/logo.gif" },
{ "url" : "images/media-button-gallery.gif" },
{ "url" : "images/media-button-image.gif" },
{ "url" : "images/media-button-music.gif" },
{ "url" : "images/media-button-other.gif" },
{ "url" : "images/media-button-video.gif" },
{ "url" : "images/media-buttons.gif" },
{ "url" : "images/tab.png" },
{ "url" : "images/tail.gif" },
{ "url" : "images/toggle-arrow-rtl.gif" },
{ "url" : "images/toggle-arrow.gif" },
{ "url" : "images/wordpress-logo.png" },
{ "url" : "images/xit.gif" },

{ "url" : "../wp-includes/images/crystal/archive.png" },
{ "url" : "../wp-includes/images/crystal/audio.png" },
{ "url" : "../wp-includes/images/crystal/code.png" },
{ "url" : "../wp-includes/images/crystal/default.png" },
{ "url" : "../wp-includes/images/crystal/document.png" },
{ "url" : "../wp-includes/images/crystal/interactive.png" },
{ "url" : "../wp-includes/images/crystal/text.png" },
{ "url" : "../wp-includes/images/crystal/video.png" },
{ "url" : "../wp-includes/images/crystal/spreadsheet.png" },
{ "url" : "../wp-includes/images/rss.png" },
{ "url" : "../wp-includes/js/thickbox/loadingAnimation.gif" },
{ "url" : "../wp-includes/js/thickbox/tb-close.png" },
{ "url" : "../wp-includes/js/swfupload/swfupload_f9.swf" },

{ "url" : "../wp-includes/js/tinymce/tiny_mce_popup.js?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/utils/mctabs.js?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/utils/validate.js?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/utils/form_utils.js?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/utils/editable_selects.js?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/paste/js/pasteword.js?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/paste/js/pastetext.js?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/media/js/media.js?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/js/color_picker.js?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/js/charmap.js?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/js/image.js?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/js/link.js?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/js/source_editor.js?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/js/anchor.js?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/wpeditimage/js/editimage.js?ver=3091" },

{ "url" : "../wp-includes/js/tinymce/themes/advanced/source_editor.htm?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/anchor.htm?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/image.htm?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/link.htm?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/color_picker.htm?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/charmap.htm?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/media/media.htm?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/paste/pasteword.htm?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/paste/blank.htm?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/paste/pastetext.htm?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/fullscreen/fullscreen.htm?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/inlinepopups/template.htm?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/wpeditimage/editimage.html?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/wp-mce-help.php?ver=3091" },

{ "url" : "../wp-includes/js/tinymce/themes/advanced/skins/wp_theme/ui.css?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/skins/wp_theme/content.css?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/skins/wp_theme/dialog.css?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/inlinepopups/skins/clearlooks2/window.css?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/spellchecker/css/content.css?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/wordpress/css/content.css?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/media/css/content.css?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/media/css/media.css?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/paste/css/pasteword.css?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/paste/css/blank.css?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/plugins/wpeditimage/css/editimage.css?ver=3091" },
{ "url" : "../wp-includes/js/tinymce/wordpress.css?ver=3091" },

{ "url" : "../wp-includes/js/tinymce/themes/advanced/img/icons.gif" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/img/colorpicker.jpg" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/img/fm.gif" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/img/gotmoxie.png" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/img/sflogo.png" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/skins/wp_theme/img/butt2.png" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/skins/wp_theme/img/fade-butt.png" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/skins/wp_theme/img/tabs.gif" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/skins/wp_theme/img/down_arrow.gif" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/skins/default/img/progress.gif" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/skins/default/img/menu_check.gif" },
{ "url" : "../wp-includes/js/tinymce/themes/advanced/skins/default/img/menu_arrow.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/inlinepopups/skins/clearlooks2/img/drag.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/inlinepopups/skins/clearlooks2/img/corners.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/inlinepopups/skins/clearlooks2/img/buttons.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/inlinepopups/skins/clearlooks2/img/horizontal.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/inlinepopups/skins/clearlooks2/img/alert.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/inlinepopups/skins/clearlooks2/img/button.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/inlinepopups/skins/clearlooks2/img/confirm.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/inlinepopups/skins/clearlooks2/img/vertical.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/media/img/flash.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/media/img/flv_player.swf" },
{ "url" : "../wp-includes/js/tinymce/plugins/media/img/quicktime.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/media/img/realmedia.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/media/img/shockwave.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/media/img/windowsmedia.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/media/img/trans.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/wordpress/img/more.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/wordpress/img/more_bug.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/wordpress/img/page.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/wordpress/img/page_bug.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/wordpress/img/toolbars.gif" },
{ "url" : "../wp-includes/js/tinymce/plugins/wpeditimage/img/image.png" },
{ "url" : "../wp-includes/js/tinymce/plugins/wpeditimage/img/delete.png" },
{ "url" : "../wp-includes/js/tinymce/plugins/wordpress/img/help.gif" }
]}