<div id="login">
	<img src="<?php echo image_path('', 'owncloud-logo-medium-white.png'); ?>" alt="ownCloud" />
	<form action="index.php" method="post">
		<!-- <h1>Sign in :</h1> -->
		<fieldset>
			<?php if($_['error']): ?>
				Login failed!
			<?php endif; ?>
			<input type="text" name="user" id="user" value="username" />
			<input type="password" name="password" id="password" value="password" />
			<input type="submit" value="Log in" />
		</fieldset>
	</form>
</div>
