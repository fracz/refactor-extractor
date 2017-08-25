<?php
global $frank_section_header;
global $frank_section_title;
global $frank_section_caption;
?>
<div class='row post-group oneup'>
	<?php if(isset($frank_section_header)) : ?>
	<div class='nav post-group-header'>
		<span class='label'><?php print($frank_section_title); ?></span>
		<span class='caption'><?php print($frank_section_caption) ?></span> <span class='more'><?php next_posts_link('View more&hellip;'); ?></span>
	</div>
	<?php endif; ?>
	<div class='contents'>
		<?php while ( $wp_query->have_posts() ) : $wp_query->the_post(); ?>
			<?php get_template_part('partials/posts/post'); ?>
		<?php endwhile; ?>
	</div>
</div>