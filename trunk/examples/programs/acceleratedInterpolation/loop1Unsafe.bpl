//#Unsafe
/* Date: 2018-05-25
 * Author: jonaswerner95@gmail.com
 *
 * A simple program
 *
 */

procedure main() {
	var x : int;
	x := 0;
	
	while (x <= 10) {
		if (x % 2 == 0) {
			x := x + 1;
		} else {
			x := x + 2;
		}
		x := x + 2;
	}	
	assert x == 11;
}
