//#Safe
/* 
 * Author: heizmann@informatik.uni-freiburg.de
 * Date: 2016-07-25
 */

procedure main() returns () {
//     var input : real;
    var output : real;
	var internal : real;
	internal := 0.0;
	output := 0.0;
	while (true) 
// 	invariant (internal * (9.0 / 10.0) + output >= 0.0 && internal * (9.0 / 10.0) + output <= 100.0);
	{
// 		input := 100.0;
// 		havoc input;
// 		assume (input >= 0.0 && input <= 100.0);
		output, internal := output + internal * 0.02, 100.0 * (2.0 / 81.0) + (output * (2.0 / 81.0) * -1.0 + internal * (43.0 / 45.0));
		assert 0.0 <= output && output <= 100.0;
	}
}
