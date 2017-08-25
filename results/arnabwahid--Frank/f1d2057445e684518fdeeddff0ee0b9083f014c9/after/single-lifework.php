<?php
/**
 * @package WordPress
 * @subpackage Franklin_Street
 */
?>
<?php get_header(); ?>
<div id="content" class="span-14 last single clear">
	<div id="content_primary" class='span-11 clear'>
		<?php while(have_posts()) : the_post(); ?>
		<article <?php post_class(); ?>>
			<header>
					<h1><?php the_title(); ?></h1>
					<ul class='metadata clear'>
						<li class='time iconic clock'><time datetime="<?php the_time('Y-m-d'); ?>" pubdate><?php the_time('Y-j-n, g:i a'); ?></time></li>
						<li class='comments last'><span class='iconic comment'></span> <?php comments_popup_link('No comments', '1 comment', '% comments'); ?></li>
					</ul>
			</header>
			<section>
				<?php the_content(); ?>
			</section>
			<footer>
				<div class='pagination'>
					<?php wp_link_pages(); ?>
				</div>
				<div id='post_footer' class='clear'>
					<?php if ( !function_exists('dynamic_sidebar') || !dynamic_sidebar("Post Footer") ) : ?>
					<?php endif; ?>
				</div>
			</footer>
		</article>
		<?php endwhile; ?>
		<div id="comments_container" class='clear'>
			<header>
				<h1><span class='iconic chat'></span> The Discussion</h1>
				<h2><?php comments_number('No Comments', 'One Comment', '% Comments' );?> on &#8220;<?php the_title(); ?>&#8221; <span class='comments_toggle'><a href='#comments' rel="<?php the_ID(); ?>" rev="<?php bloginfo('url'); ?>/?page_id=<?php echo get_option('fs_comment_template_id'); ?>" id="comments_toggle">Show comments</a></span></h2>
			</header>
			<div id="comments_ajax" class='clear'></div>
		</div>
	</div>
<?php get_sidebar(); ?>
</div>
<?php get_footer(); ?>