<!DOCTYPE html>
<html lang="de">
<head>
  <title>Flashheart's RLG</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.0/jquery.min.js"></script>
  <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
  <script>
		var reloading;
		function checkReloading() {
			if (window.location.hash=="#autoreload") {
				reloading=setTimeout("window.location.reload();", 10000);
				document.getElementById("reloadCB").checked=true;
			}
		}
		
		function toggleAutoRefresh(cb) {
			if (cb.checked) {
				window.location.replace("#autoreload");
				reloading=setTimeout("window.location.reload();", 10000);
			} else {
				window.location.replace("#");
				clearTimeout(reloading);
			}
		}
		
		window.onload=checkReloading;
  </script>
</head>

	<body>
		<?php		
			require("navbar.php");
			?>
		
		
		<div class="container">			
			<div class="page-header"> <h1>Flashheart's Real Life Gaming <small>Archive Statistics</small></h1>
		</div>
	
		<?php		
			require("lang-deDE.php");
			// für die Archive
			require $_GET["gamefile"];
			require("singlematch.php");
		?> 

		
		
	</body>	
</html>