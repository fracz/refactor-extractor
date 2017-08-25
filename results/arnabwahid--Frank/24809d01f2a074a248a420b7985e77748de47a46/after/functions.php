<?php

/*thanks to http://www.nathanrice.net/blog/wordpress-single-post-templates/ */
/*add_filter('single_template', create_function('$t', 'foreach( (array) get_the_category() as $cat ) { if ( file_exists(TEMPLATEPATH . "/single-{$cat->category_nicename}.php") ) return TEMPLATEPATH . "/single-{$cat->category_nicename}.php"; } return $t;' ));*/

add_filter('single_template', create_function('$t', 'foreach( (array) get_the_category() as $cat ) {
	if ( file_exists(TEMPLATEPATH . "/single-{$cat->category_nicename}.php") ) return TEMPLATEPATH . "/single-{$cat->category_nicename}.php";
	else if($cat->category_parent!=0){
		$pCat=$cat;
		while($pCat->cat_ID) {
			if ( file_exists(TEMPLATEPATH . "/single-{$pCat->category_nicename}.php") ) return TEMPLATEPATH . "/single-{$pCat->category_nicename}.php";
			$pCat=get_category($pCat->category_parent);
		}
	}
} return $t;' ));

function fs_register_menus() {
  register_nav_menus(array('primary' => __( 'Primary Navigation' )));
}


/**
 * @package WordPress
 * @subpackage Default_Theme
 */

if ( function_exists('register_sidebar') )
	register_sidebar(array(
	'name' => 'Sub Header',
	'before_widget' => '<div id="%1$s" class="widget %2$s">',
	'after_widget' => '</div>',
	'before_title' => '<h3 class="widgettitle">',
	'after_title' => '</h3>',
	));

if ( function_exists('register_sidebar') )
	register_sidebar(array(
	'name' => 'Navigation',
	'before_widget' => '<div id="%1$s" class="widget %2$s">',
	'after_widget' => '</div>',
	'before_title' => '<h3 class="widgettitle">',
	'after_title' => '</h3>',
	));

if ( function_exists('register_sidebar') )
	register_sidebar(array(
	'name' => 'Index Right Aside',
	'before_widget' => '<div id="%1$s" class="widget %2$s">',
	'after_widget' => '</div>',
	'before_title' => '<h3 class="widgettitle">',
	'after_title' => '</h3>',
	));

if ( function_exists('register_sidebar') ) {
	register_sidebar(array(
		'name' => 'Post Left Aside',
		'before_widget' => '<div id="%1$s" class="widget %2$s">',
		'after_widget' => '</div>',
		'before_title' => '<h3 class="widgettitle">',
		'after_title' => '</h3>',
	));
}

if ( function_exists('register_sidebar') ) {
	register_sidebar(array(
		'name' => 'Post Right Aside',
		'before_widget' => '<div id="%1$s" class="widget %2$s">',
		'after_widget' => '</div>',
		'before_title' => '<h3 class="widgettitle">',
		'after_title' => '</h3>',
	));
}

if ( function_exists('register_sidebar') )
	register_sidebar(array(
	'name' => 'Post Footer',
	'before_widget' => '<div id="%1$s" class="widget %2$s">',
	'after_widget' => '</div>',
	'before_title' => '<h3 class="widgettitle">',
	'after_title' => '</h3>',
	));

if ( function_exists('register_sidebar') )
	register_sidebar(array(
	'name' => 'Footer',
	'before_widget' => '<div id="%1$s" class="widget %2$s">',
	'after_widget' => '</div>',
	'before_title' => '<h3 class="widgettitle">',
	'after_title' => '</h3>',
	));

if ( function_exists('register_sidebar') )
	register_sidebar(array(
	'name' => 'Heel',
	'before_widget' => '<div id="%1$s" class="widget %2$s">',
	'after_widget' => '</div>',
	'before_title' => '<h3 class="widgettitle">',
	'after_title' => '</h3>',
	));

if ( function_exists('register_sidebar') )
	register_sidebar(array(
	'name' => '404',
	'before_widget' => '<div id="%1$s" class="widget %2$s">',
	'after_widget' => '</div>',
	'before_title' => '<h3 class="widgettitle">',
	'after_title' => '</h3>',
	));

function new_excerpt_more($more) { return '...'; }

function excerpt_read_more($post) { return '<a href="'. get_permalink($post->ID) . '">' . 'Read On...' . '</a>'; }


/*
function truncate_title($title, $length, $echo=true)
{
	if (strlen($title) > $length) $title=substr($title, 0, $length).'&hellip;';
	if($echo) echo $title;
	return $title;
}
*/

function content($maxLength, $read_more="Read More", $image_width=190, $image_height=120, $image_quality=80, $autolink=false, $echo=true)
{
	$content = get_the_content($read_more);

	$pattern = '/\< *[img][^\>]* src *= *[\"\']{0,1}([^\"\'\ >]*)/i';
	$replacement = '<img src="' . get_bloginfo("template_directory") . '/php/timthumb/timthumb.php.php?src=' . '$1' . '&w='.$image_width.'&h='.$image_height.'&q='.$image_quality;

	$pagecontent = preg_replace($pattern, $replacement, $content);

	if($echo) echo $pagecontent;

	return $pagecontent;
}

/**
 * Add "first" and "last" CSS classes to dynamic sidebar widgets. Also adds numeric index class for each widget (widget-1, widget-2, etc.)
 * via http://wordpress.org/support/topic/how-to-first-and-last-css-classes-for-sidebar-widgets
 */
function widget_first_last_classes($params) {


	global $my_widget_num; // Global a counter array
	$this_id = $params[0]['id']; // Get the id for the current sidebar we're processing
	$arr_registered_widgets = wp_get_sidebars_widgets(); // Get an array of ALL registered widgets

	if(!$my_widget_num) {// If the counter array doesn't exist, create it
		$my_widget_num = array();
	}

	if(!isset($arr_registered_widgets[$this_id]) || !is_array($arr_registered_widgets[$this_id])) { // Check if the current sidebar has no widgets
		return $params; // No widgets in this sidebar... bail early.
	}

	if(isset($my_widget_num[$this_id])) { // See if the counter array has an entry for this sidebar
		$my_widget_num[$this_id] ++;
	} else { // If not, create it starting with 1
		$my_widget_num[$this_id] = 1;
	}

	$class = 'class="widget-' . $my_widget_num[$this_id] . ' '; // Add a widget number class for additional styling options

	if($my_widget_num[$this_id] == 1) { // If this is the first widget
		$class .= 'widget-first ';
	} elseif($my_widget_num[$this_id] == count($arr_registered_widgets[$this_id])) { // If this is the last widget
		$class .= 'widget-last ';
	}

	$params[0]['before_widget'] = str_replace('class="', $class, $params[0]['before_widget']); // Insert our new classes into "before widget"

	return $params;

}
add_filter('dynamic_sidebar_params','widget_first_last_classes');



// ======================
// = HOME PAGE SECTIONS =
// ======================

function franklin_st_home()
{
    include 'admin/admin-options-home.php';
}

// add our menus
function franklin_street_admin_menu()
{
    add_menu_page( 'Franklin Street', 'Franklin Street', 'manage_options', 'franklin-street', 'franklin_st_home' );

    add_submenu_page( 'franklin-street', 'Franklin Street', 'Home Page Sections', 'manage_options', 'franklin-street', 'franklin_st_home');
}

add_action( 'admin_menu', 'franklin_street_admin_menu' );

function franklin_street_admin_assets()
{
    if( is_admin() )
    {
        wp_enqueue_style( 'franklin-street-admin', get_bloginfo( 'stylesheet_directory' ) . '/admin/css/franklin-street-options.css', NULL, NULL, NULL );
        wp_enqueue_script( 'franklin-street-admin', get_bloginfo( 'stylesheet_directory' ) . '/admin/js/franklin-street-utils.js', 'jquery', NULL, true );
    }
}

add_action( 'init', 'franklin_street_admin_assets' );