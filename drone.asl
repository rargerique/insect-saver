// Agent drone in project jasonTeam.mas2j

/* Initial beliefs and rules */



/* Initial goals */

!start.


/* Plans */


+!start: true <- .print("wait"); do(evaluatePlant); !test_plant.


+!test_plant: diseased_plant(X,Y)
  <- .print("is_diseasedd");
  		!start.
		
+!test_plant: safe_plant(X,Y)
	<- .print("is clean", X, Y);
		do(down);
		!start.
  
