package de.uni_freiburg.informatik.ultimate.lib.pea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.boogie.BoogieLocation;
import de.uni_freiburg.informatik.ultimate.boogie.BoogieTransformer;
import de.uni_freiburg.informatik.ultimate.boogie.BoogieVisitor;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BooleanLiteral;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.IdentifierExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.UnaryExpression;
import de.uni_freiburg.informatik.ultimate.boogie.output.BoogiePrettyPrinter;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.normalforms.BoogieExpressionTransformer;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.normalforms.NormalFormTransformer;

/**
 * {@link Decision} expressed by a BoogieAST Expression.
 *
 * @author Vincent Langenfeld (langenfv@informatik.uni-freiburg.de)
 */
public class BoogieBooleanExpressionDecision extends Decision {

	private final Expression mExpression;
	private final static NormalFormTransformer<Expression> TRANSFORMER =
			new NormalFormTransformer<>(new BoogieExpressionTransformer());

	/**
	 *
	 * @param expression
	 *            A Boogie expression which evaluates to boolean but has no boolean expressions as children.
	 */
	public BoogieBooleanExpressionDecision(final Expression expression) {
		mExpression = expression;
	}

	public Expression getExpression() {
		return mExpression;
	}

	/**
	 * Create an boogie expression constraint and enclosing CDD
	 *
	 * @param var
	 *            the condition that must hold.
	 */
	public static CDD create(final Expression e) {
		final Expression simplifiedExpression = TRANSFORMER.simplify(e);
		if (simplifiedExpression instanceof BooleanLiteral) {
			if (((BooleanLiteral) simplifiedExpression).getValue()) {
				return CDD.TRUE;
			}
			return CDD.FALSE;
		}
		return CDD.create(new BoogieBooleanExpressionDecision(simplifiedExpression), CDD.trueChilds);
	}

	public static CDD createTrue() {
		return CDD.TRUE;
	}

	@Override
	public int compareTo(final Object o) {
		if (!(o instanceof BoogieBooleanExpressionDecision)) {
			return 1;
		}

		// TODO: is there somethin better than a string comparison for that
		return ((BoogieBooleanExpressionDecision) o).getExpression().toString().compareTo(mExpression.toString());

	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof BoogieBooleanExpressionDecision)) {
			return false;
		}
		if (!mExpression.equals(((BoogieBooleanExpressionDecision) o).getExpression())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return mExpression.hashCode();
	}

	@Override
	public Decision prime() {
		return prime(null);
	}

	@Override
	public Decision unprime() {
		return unprime(null);
	}

	@Override
	public Decision unprime(final String ignore) {
		final BoogieRemovePrimeIdentifierTransformer bpit = new BoogieRemovePrimeIdentifierTransformer();
		bpit.setIgnore(ignore);
		final Expression primed = bpit.processExpression(mExpression);
		return new BoogieBooleanExpressionDecision(primed);
	}

	@Override
	public Decision prime(final String ignore) {
		final BoogiePrimeIdentifierTransformer bpit = new BoogiePrimeIdentifierTransformer();
		bpit.setIgnore(ignore);
		final Expression primed = bpit.processExpression(mExpression);
		return new BoogieBooleanExpressionDecision(primed);
	}

	@Override
	public String toString(final int child) {
		if (child != 0) {
			final BoogieLocation loc = new BoogieLocation("", 0, 0, 0, 0);
			return BoogiePrettyPrinter.print(new UnaryExpression(loc, UnaryExpression.Operator.LOGICNEG, mExpression));
		}
		return BoogiePrettyPrinter.print(mExpression);
	}

	@Override
	public String toSmtString(final int child) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toTexString(final int child) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toUppaalString(final int child) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toUppaalStringDOM(final int child) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getVar() {
		throw new UnsupportedOperationException(
				"getVar not supported by BoogieBooleanExpressionDecision (use getVars)!");
	}

	/**
	 * Collects variable names and types from the expression.
	 *
	 * @return Map: ident -> type
	 */
	public Map<String, String> getVars() {
		final Map<String, String> vars = new HashMap<>();

		final BoogieIdentifierCollector collector = new BoogieIdentifierCollector();
		final List<IdentifierExpression> idents = collector.getIdentifiers(mExpression);

		for (final IdentifierExpression ident : idents) {
			vars.put(ident.getIdentifier(), null);
		}

		return vars;
	}

	/**
	 * Collects all identifier statements from a boogie expression
	 */
	private static final class BoogieIdentifierCollector extends BoogieVisitor {

		private final ArrayList<IdentifierExpression> mIdentifiers = new ArrayList<>();

		@Override
		protected void visit(final IdentifierExpression expr) {
			mIdentifiers.add(expr);
		}

		public List<IdentifierExpression> getIdentifiers(final Expression expr) {
			processExpression(expr);
			return mIdentifiers;
		}
	}

	/**
	 * Transforms a BoggieExpressino to a BoogieExpression with primed Variable names
	 *
	 */
	private static final class BoogiePrimeIdentifierTransformer extends BoogieTransformer {
		private String mIgnore;

		public void setIgnore(final String ignore) {
			if (ignore != null) {
				mIgnore = ignore;
			}
		}

		@Override
		protected Expression processExpression(final Expression expr) {
			if (expr instanceof IdentifierExpression) {
				if (mIgnore != null && ((IdentifierExpression) expr).getIdentifier().equals(mIgnore)) {
					return expr;
				}
				return new IdentifierExpression(expr.getLocation(),
						((IdentifierExpression) expr).getIdentifier().replaceAll("([a-zA-Z_])(\\w*)", "$1$2" + "'"));
			}
			return super.processExpression(expr);
		}

	}

	/**
	 * Transforms a BoggieExpressino to a BoogieExpression with unprimed Variable names
	 *
	 */
	private static final class BoogieRemovePrimeIdentifierTransformer extends BoogieTransformer {
		private String mIgnore;

		public void setIgnore(final String ignore) {
			if (ignore != null) {
				mIgnore = ignore;
			}
		}

		@Override
		protected Expression processExpression(final Expression expr) {
			if (expr instanceof IdentifierExpression) {
				if (mIgnore != null && ((IdentifierExpression) expr).getIdentifier().equals(mIgnore)) {
					return expr;
				}
				return new IdentifierExpression(expr.getLocation(),
						((IdentifierExpression) expr).getIdentifier().replaceAll("([a-zA-Z_])(\\w*)" + "'", "$1$2"));
			}
			return super.processExpression(expr);
		}
	}

}
