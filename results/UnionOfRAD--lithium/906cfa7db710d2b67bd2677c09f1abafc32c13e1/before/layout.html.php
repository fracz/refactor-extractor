<?php
	use \lithium\util\Inflector;
	// /var_dump($request);

?>
<!doctype html>
<html>
	<head>
		<title>Lithium Unit Test Dashboard</title>
		<link rel="stylesheet" href="<?php echo $request->env('base')?>/css/debug.css" />
	</head>
	<body class="test-dashboard">
		<h1>Lithium Unit Test Dashboard</h1>

		<div style="float: left; padding: 10px 0 20px 20px; width: 20%;">
			<h2><a href="?group=\">Tests</a></h2>
			<?php echo $menu ?>
		</div>

		<div style="float:left; padding: 10px; width: 75%">
			<h2>Stats for <?php echo $report->title; ?></h2>

			<h3>Test results</h3>

			<span class="filters">
				<?php
					foreach ($filters as $i => $class) {
						$url = $request->env('REQUEST_URI') . "&amp;filters[]={$class}";
						$name = join('', array_slice(explode("\\", $class), -1));
						$key = Inflector::underscore($name);

						echo "<a class=\"{$key}\" href=\"{$url}\">{$name}</a>";
						echo ' | ';
					}
				?>
			</span>

			<?php echo $report->stats(); ?>

			<?php echo $report->filters();?>

		</div>
		<div style="clear:both"></div>
	</body>
</html>