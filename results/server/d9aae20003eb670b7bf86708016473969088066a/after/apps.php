<ul id="leftcontent">
	<?php foreach($_['apps'] as $app):?>
		<li <?php if($app['active']) echo 'class="active"'?> data-id="<?php echo $app['id'] ?>">
			<?php  echo $app['name'] ?>
			<span class="hidden">
				<?php echo json_encode($app) ?>
			</span>
		</li>
	<?php endforeach;?>
</ul>
<div id="rightcontent">
	<h3><strong><span class="name"><?php echo $l->t('Select an App');?></span></strong><span class="version"></span></h3>
	<p class="description"></p>
	<p class="hidden"><span class="licence"></span><?php echo $l->t('-licensed');?> <?php echo $l->t('by');?> <span class="author"></span></p>
	<input class="enable hidden" type="submit" />
</div>