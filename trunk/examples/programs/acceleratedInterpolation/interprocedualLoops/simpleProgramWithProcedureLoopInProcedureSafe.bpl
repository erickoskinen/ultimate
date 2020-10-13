// #Safe
/* 
 * Simple Program for Checking PDRs interprocedual capabilities
 * Here, the the query for the recursive PDR going through inc() is sat.
 * meaning true /\ y' = y + 1 /\ y' != x' + 1 is sat
 * so that we backtrack out of the procedure to the assumption which then
 * gets unsat.
 *
 * Interpolant sequence generated by Automizer: 
 * -- 
 * true 
 *   main:          assume (y == x);
 * (= y main_x)
 *   main->inc:     call inc();
 * (= y |old(y)|)
 *   inc:           y := y + 1;
 * (= y (+ |old(y)| 1))
 *   inc->main:     return (implicit) 
 * (= (+ main_x 1) y)
 *   main:          assume !(y == x + 1);
 * false
 * -- 
 *
 */

var y : int;

procedure inc() returns () 
modifies y;
{
	while (y < 5) {
		y := y + 1;
	}
}


procedure main() returns ()
modifies y;
{
    var x : int;
    x:=0;
    assume (y == x);
    
    call inc();
    call inc();
    assert (y == 5);
}
