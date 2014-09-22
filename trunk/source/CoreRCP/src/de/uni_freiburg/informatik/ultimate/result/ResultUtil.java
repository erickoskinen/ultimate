package de.uni_freiburg.informatik.ultimate.result;

import java.util.List;

import de.uni_freiburg.informatik.ultimate.core.services.IBacktranslationService;
import de.uni_freiburg.informatik.ultimate.model.DefaultTranslator;
import de.uni_freiburg.informatik.ultimate.model.IElement;
import de.uni_freiburg.informatik.ultimate.model.ITranslator;
import de.uni_freiburg.informatik.ultimate.model.annotation.IAnnotations;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.model.boogie.output.BoogiePrettyPrinter;

/**
 * 
 * @author Matthias Heizmann, Jan Leike
 */
public class ResultUtil {

	/**
	 * Use Ultimate's translator sequence do translate a result expression back
	 * through the toolchain.
	 * 
	 * @param expr
	 *            the resulting expression
	 * @return a string corresponding to the backtranslated expression
	 */
	public static <SE> String backtranslationWorkaround(List<ITranslator<?, ?, ?, ?>> translatorSequence, SE expr) {
		Object backExpr = DefaultTranslator.translateExpressionIteratively(expr,
				translatorSequence.toArray(new ITranslator[0]));

		// If the result is a Boogie expression, we use the Boogie pretty
		// printer
		String result;
		if (backExpr instanceof String) {
			result = (String) backExpr;
		} else if (backExpr instanceof Expression) {
			result = BoogiePrettyPrinter.print((Expression) backExpr);
		} else {
			result = backExpr.toString();
		}
		return result;
	}

	public static <SE> String translateExpressionToString(IBacktranslationService translator, Class<SE> clazz,
			SE[] expression) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < expression.length; ++i) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(translator.translateExpressionToString(expression[i], clazz));
		}
		return sb.toString();
	}

	/**
	 * Return the checked specification that is checked at the error location.
	 */
	public static <ELEM extends IElement> Check getCheckedSpecification(ELEM element) {
		if (element.getPayload().hasAnnotation()) {
			IAnnotations check = element.getPayload().getAnnotations().get(Check.getIdentifier());
			return (Check) check;
		} else {
			return element.getPayload().getLocation().getOrigin().checkedSpecification();
		}
	}
}
