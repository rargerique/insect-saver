// Agent drone in project jasonTeam.mas2j

/* Initial beliefs and rules */

lastdir(null).
lastplant(null).
//pos(0,0).

/* rules */

//go to next 10 lines when safe
calc_new_pos(AgX, AgY, LimitX, LimitY, AgX, NewY) :-  
	lastplant(safe) & 
	AgY+10 < LimitY &
	NewY = AgY + 10.
	
//go to 1 line when diseased
calc_new_pos(AgX, AgY, LimitX, LimitY, AgX, NewY) :-  
	lastplant(diseased) & 
	AgY+1 < LimitY &
	NewY = AgY+1.
	
//when Y down is reached, go 10 to right
calc_new_pos(AgX, AgY, LimitX, LimitY, NewX, NewY) :- 
	AgY+10 > LimitY &
	AgX+10 < LimitX &
 	NewX = AgX + 10 &
	NewY = 0.
	
/* Initial goals */

!start.


/* Plans */


+!start: true
 <- +pos(0,0); 
 	do(evaluatePlant);
	!test_plant;
	!handle.

+!handle: lastplant(S) & pos(X,Y)
 <- ?calc_new_pos(X,Y,200,200, NX, NY);
 	-lastplant(S);
 	!prep_around(NX,NY).

	
+!handle: pos(X,Y) & not lastplant(_)
 <- do(evaluatePlant);
 	!test_plant;
	!handle.
	
+!handle: true
	<- !!handle.

+!prep_around(X,Y) : true
  <- -around(_,_); -last_dir(_); !around(X,Y).
 
+!around(X,Y) 
   :  // I am around to some location if I am near it or
      // the last action was skip (meaning that there are no paths to there)
      (pos(AgX,AgY) & jia.neighbour(AgX,AgY,X,Y)) | last_dir(skip) 
   <- +around(X,Y).
+!around(X,Y) : not around(X,Y)
   <- !next_step(X,Y);
      !!around(X,Y).
+!around(X,Y) : true 
   <- !!around(X,Y).
   
+!test_plant: diseased_plant(X,Y)
 <- do(isectcide);
 	-+lastplant(diseased).
		
+!test_plant: safe_plant(X,Y)
	<- -+lastplant(safe).
//wait for the result to come
+!test_plant: not safe_plant(X,Y) & not diseased_plant(X,Y)
 	<- !!test_plant.

	
	
+!next_step(X,Y)
   :  pos(AgX,AgY)
   <- jia.get_direction(AgX, AgY, X, Y, D);
      //.print("from ",AgX,"x",AgY," to ", X,"x",Y," -> ",D);
      -+last_dir(D);
	  .print("Everybody move to the ", D);
      do(D).

+!next_step(X,Y) : not pos(_,_) // I still do not know my position
   <- !next_step(X,Y).
-!next_step(X,Y) : true  // failure handling -> start again!
   <- .print("Failed next_step to ", X,"x",Y," fixing and trying again!");
      -+last_dir(null);
      !next_step(X,Y).
