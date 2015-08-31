//#Safe
/* Tests for conversions of integers.
 * The tests are only useful if 
 *     sizeof(int) = 4 and
 *     sizeof(long long) > 4.
 * 
 * Author: langt@informatik.uni-freiburg.de
 * Date: 18.08.2015
 */

int main() {
  /* unsigned long long to unsigned int */
  if (sizeof(long long) > 4 && sizeof(int) == 4) {
    unsigned long long a = 4294967296ULL;
    unsigned int b = a;

    if (b != 0U) {
      //@assert(\false);
    }
  }

  /* signed long long to unsigned int */
  if (sizeof(long long) > 4 && sizeof(int) == 4) {
    signed long long c = -4294967296LL;
    unsigned int d = c;

    if (d != 0U) {
      //@assert(\false);
    }
  }

  /* unsigned int to signed long long */
  if (sizeof(long long) > 4 && sizeof(int) == 4) {
    unsigned int e = 2147483648U;
    signed long long f = e;

    if (f < 0) {
      //@ assert \false;
    }
  }
}
