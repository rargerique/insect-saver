// Agent drone in project jasonTeam.mas2j

/* Initial beliefs and rules */

/* Initial goals */

!start.


/* Plans */


+!start : true <- .print("wait"); !evaluate; !printDiseased.

+!printDiseased: diseased
  <- .print("is_diseasedd");
  		!start.
+!printDiseased: not diseased
	<- .print("is clean");
		!start.
		
		
+!printDiseased: true <- .print("did nothing").
		
+!evaluate : true
  <- do(evaluatePlant); +diseased; !printDiseased.
