
<!DOCTYPE html>
<html lang="de">
<head>
  <title>Flashheart's RLG Archiv</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.0/jquery.min.js"></script>
  <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
 </head>

	<body>
		
		<?php		
			require("navbar.php");
			?>
			
		<div class="container">			
			<div class="page-header"> <h1>Flashheart's Real Life Gaming <small>Archive Statistics</small></h1>
		</div>
	
		<h2>OCFFLAG</h2>

		<?php		
			require("lang-deDE.php");
			
			$files = array_reverse(glob("ocfflag/archive/*.php"));
			
			$filecount = 0;
			if ($files){
 				$filecount = count($files);
			}
			
			if ($filecount == 0){
				echo "<h3>".$lang['EMPTY_ARCHIVE']."</h3>";
			} else {
				// echo "<h3>".$game['flagname']."</h3>";
				
				echo "<ul>";
				
				$protocol = $_SERVER['HTTPS'] == '' ? 'http://' : 'https://';
				$folder = $protocol . $_SERVER['HTTP_HOST']  .dirname($_SERVER['PHP_SELF'])."/ocfflag/";
								
				foreach($files AS $file){
					require($file);
					// Das mit dem "../" ist ein Trick. Da die index-archive.php ein Verzeichnis drüber ist, hat der Dateiname den falschen Pfad.
                    //                                                       VVV
					echo '<li><a href="'.$folder.'singlearchive.php?gamefile=../'.$file.'">'.$game['ts_game_started'].' ['.$game['flagname'].']'.'</a></li>';
				}
				echo "</ul>";
			}		
	?> 

	</body>	
</html>