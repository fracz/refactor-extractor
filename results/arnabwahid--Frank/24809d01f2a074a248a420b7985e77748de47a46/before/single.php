<?php
/**
 * @package WordPress
 * @subpackage Franklin_Street
 */
?>
<?php get_header(); ?>
<div id='content' class='span-12 last single clear'>
	<div id='content_primary' class='span-9 clear'>
		<?php while(have_posts()) : the_post(); ?>
		<article <?php post_class(); ?> class='clear'>
			<header>
					<h1><?php the_title(); ?></h1>
			</header>
			<div id='excerpt'><?php the_excerpt(); ?></div>
			<div id='content_main' class='clear'>
				<div class='post-info span-2'>
					<dl class='metadata'>
						<dt class='author'>By</dt>
						<dd class='author'><?php the_author_link(); ?></dd>
						<dt class='time'>Posted</dt>
						<dd class='time'><time datetime="<?php the_time('Y-m-d'); ?>" pubdate><?php echo human_time_diff(get_the_time('U'), current_time('timestamp')) . ' ago'; ?></time></dd>
						<dt class='categories'>Filed Under</dt>
						<dd class='categories'><?php the_category('</dd><dd>'); ?></dd>
						<dt class='tags'>Tagged</dt>
						<dd class='tags'><?php the_tags('','<dd>','</dd>'); ?></dd>
					</dl>
					<?php if ( !function_exists('dynamic_sidebar') || !dynamic_sidebar('Post Left Aside') ) : ?>
					<?php endif; ?>
				</div>
			<section class='span-7 last'>
				<?php the_content(); ?>
			</section>
			</div>
			<footer>
				<?php wp_link_pages('before=<div class="page-links"><p>Pages:&after=</p></div>'); ?>
				<div id='post_footer' class='clear'>
					<?php if ( !function_exists('dynamic_sidebar') || !dynamic_sidebar('Post Footer') ) : ?>
						<ul class='metadata clear'>
							<li class='comments'><?php comments_popup_link('No comments', '1 comment', '% comments'); ?></li>
							<li class='tweet'><a href='http://twitter.com/home?status=<?php the_title() ?> <?php the_permalink() ?>'>Retweet This Post</a></li>
							<?php if(!showSecondaryColumnHeader()) : ?><li class='permalink last'><a href='<?php the_permalink(); ?>'>Link</a></li><?php endif; ?>
						</ul>
					<?php endif; ?>
				</div>
			</footer>
		</article>
		<?php endwhile; ?>
		<?php comments_template(); ?>
	</div>
<?php get_sidebar(); ?>
</div>
<?php get_footer(); ?>