
<h3><?php echo $game['flagname'];?></h3>		
		<p>Letzte Aktualisierung: <?php echo $game['timestamp'];?></p>
		<p><b>Match Nr. <?php echo $game['matchid'];?></b> &nbsp; <b>Spielbeginn:</b> <?php echo $game['ts_game_started'];?>
		<?php if ($game['ts_game_ended'] != 'null'){
					echo '&nbsp; <b>Spielende:</b> '.$game['ts_game_ended'];
				}
		 ?>
		 </p>
		
		<h4>Aktueller Spielstand</h4>
		
		<?php
		
			$collapseref = "collapse".rand();
		
		    if ($game['ts_game_ended'] == 'null'){
		    
				$string_pause = "";
				
				if ($game['ts_game_paused'] != 'null'){
						$string_pause = 'AUSZEIT/PAUSE seit '.$game['ts_game_paused'].' &rarr; ';
				}
				
				if ($game['flagcolor'] == 'neutral'){
					echo '<div class="well" style="font-size:175%;background:white;color:black;font-weight:bold">'.$string_pause.'Flagge neutral</div>';
				} elseif ($game['flagcolor'] == 'red'){
					echo '<div class="well" style="font-size:175%;background:red;color:yellow;font-weight:bold">'.$string_pause.'Flagge ist rot</div>';
				} elseif ($game['flagcolor'] == 'blue'){
					echo '<div class="well" style="font-size:175%;background:royalblue;color:yellow;font-weight:bold">'.$string_pause.'Flagge ist blau</div>';
				} elseif ($game['flagcolor'] == 'green'){
					echo '<div class="well" style="font-size:175%;background:green;color:white;font-weight:bold">'.$string_pause.'Flagge ist grün</div>';
				} elseif ($game['flagcolor'] == 'yellow'){
					echo '<div class="well" style="font-size:175%;background:yellow;color:black;font-weight:bold">'.$string_pause.'Flagge ist gelb</div>';
				} else {
					echo '<div class="well" style="font-size:175%;background:black;color:white;font-weight:bold">ERROR</div>';
				}
		    		
		    		
			} else {
				// GAME OVER
				if ($game['winning_team'] == 'draw'){
					echo '<div class="well" style="font-size:175%;background:white;color:black;font-weight:bold">GAME OVER: Unentschieden</div>';
				} elseif ($game['winning_team'] == 'red'){
					echo '<div class="well" style="font-size:175%;background:red;color:yellow;font-weight:bold">GAME OVER: Rot hat gewonnen</div>';
				} elseif ($game['winning_team'] == 'blue'){
					echo '<div class="well" style="font-size:175%;background:royalblue;color:yellow;font-weight:bold">GAME OVER: Blau hat gewonnen</div>';
				} elseif ($game['winning_team'] == 'green'){
					echo '<div class="well" style="font-size:175%;background:green;color:white;font-weight:bold">GAME OVER: Grün hat gewonnen</div>';
				} elseif ($game['winning_team'] == 'yellow'){
					echo '<div class="well" style="font-size:175%;background:yellow;color:black;font-weight:bold">GAME OVER: Gelb hat gewonnen</div>';
				} elseif ($game['winning_team'] == 'not_yet'){
					echo '<div class="well" style="font-size:175%;background:grey;color:white;font-weight:bold">GAME OVER: Keiner hat gewonnen</div>';
				} else {
					echo '<div class="well" style="font-size:175%;background:black;color:white;font-weight:bold">ERROR</div>';
				}
			}
		?>
		
		<table  class="table" style="width:100%">			
			<tr>		    		
		    		<th>Restliche Spielzeit</th>		    		
		    		<th>Team Rot</th>
		    		<th>Team Blau</th>
		    		<th>Team Grün</th>
		    		<th>Team Gelb</th>
		    	</tr>
  			<tr>
			    <td style="font-size:175%;font-weight:bold"><?php echo $game['time'];?></td>
			    <td style="font-size:175%;color:red;font-weight:bold"><?php echo $game['time_red'];?></td>
   			    <td style="font-size:175%;color:royalblue;font-weight:bold"><?php echo $game['time_blue'];?></td>
                <td style="font-size:175%;color:green;font-weight:bold"><?php echo $game['time_green'];?></td>
                <td style="font-size:175%;color:yellow;font-weight:bold"><?php echo $game['time_yellow'];?></td>
			</tr>
		</table>
		<br/>


		 <div class="panel-group">
			<div class="panel panel-default">
			  <div class="panel-heading">
				<h4 class="panel-title">
				  <a data-toggle="collapse" href="#<?php echo $collapseref;?>">Ereignisse einblenden</a>
				</h4>
			  </div>
			  <div id="<?php echo $collapseref;?>" class="panel-collapse collapse">
				<div class="panel-body">
				<table  class="table">
					<tr>
						<th>Zeitpunkt</th>
						<th>Ereignis</th> 
					</tr>
					<?php
						foreach($game['events'] AS $myevent) {
							echo "<tr><td>".$myevent['pit']."</td><td>".$lang[$myevent['event']]."</td></tr>";
						}
					?>
				</table>
				</div>
				<div class="panel-footer"><b>Flag ID:</b> <i><?php echo $game['uuid'];?></i></div>
			  </div>
			</div>
		  </div>