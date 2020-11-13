/*
 * Copyright (C) 2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Thomas Lang
 * Copyright (C) 2015 University of Freiburg
 *
 * This file is part of the ULTIMATE CACSL2BoogieTranslator plug-in.
 *
 * The ULTIMATE CACSL2BoogieTranslator plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE CACSL2BoogieTranslator plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE CACSL2BoogieTranslator plug-in. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE CACSL2BoogieTranslator plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE CACSL2BoogieTranslator plug-in grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.base.expressiontranslation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBinaryExpression;

import de.uni_freiburg.informatik.ultimate.boogie.ExpressionFactory;
import de.uni_freiburg.informatik.ultimate.boogie.ast.ASTType;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssertStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.AssumeStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Attribute;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BinaryExpression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.BinaryExpression.Operator;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.FunctionApplication;
import de.uni_freiburg.informatik.ultimate.boogie.ast.IntegerLiteral;
import de.uni_freiburg.informatik.ultimate.boogie.ast.NamedAttribute;
import de.uni_freiburg.informatik.ultimate.boogie.ast.PrimitiveType;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.UnaryExpression;
import de.uni_freiburg.informatik.ultimate.boogie.type.BoogiePrimitiveType;
import de.uni_freiburg.informatik.ultimate.boogie.type.BoogieType;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.FlatSymbolTable;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.LocationFactory;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.base.CHandler;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.base.FunctionDeclarations;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.base.IDispatcher;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.base.TranslationSettings;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.base.chandler.MemoryHandler;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.base.chandler.TypeSizes;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.AuxVarInfoBuilder;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c.CEnum;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c.CPointer;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c.CPrimitive;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c.CPrimitive.CPrimitiveCategory;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c.CPrimitive.CPrimitives;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c.CType;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.exception.UnsupportedSyntaxException;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.result.ExpressionResult;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.result.ExpressionResultBuilder;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.result.ExpressionResultTransformer;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.result.RValue;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.result.Result;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.util.ISOIEC9899TC3;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.util.SFO;
import de.uni_freiburg.informatik.ultimate.cdt.translation.interfaces.handler.ITypeHandler;
import de.uni_freiburg.informatik.ultimate.core.lib.models.annotation.Check;
import de.uni_freiburg.informatik.ultimate.core.lib.models.annotation.Check.Spec;
import de.uni_freiburg.informatik.ultimate.core.model.models.ILocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.cacsl2boogietranslator.preferences.CACSLPreferenceInitializer.UnsignedTreatment;
import de.uni_freiburg.informatik.ultimate.util.datastructures.BitvectorConstant.SupportedBitvectorOperations;

/**
 *
 * @author Cyrus Liu
 *
 */
public class BitabsTranslation {

	public static final int STRING_OVERAPPROXIMATION_THRESHOLD = 8;

	protected final FunctionDeclarations mFunctionDeclarations;
	protected final TypeSizes mTypeSizes;
	protected final ITypeHandler mTypeHandler;
//	protected final IPointerIntegerConversion mPointerIntegerConversion;
	protected final FlatSymbolTable mSymboltable;

	protected final TranslationSettings mSettings;


	public BitabsTranslation(final TypeSizes typeSizes, final TranslationSettings translationSettings,
			final ITypeHandler typeHandler, final FlatSymbolTable symboltable, FunctionDeclarations functionDeclarations) {
		mSettings = translationSettings;
		mTypeSizes = typeSizes;
		mTypeHandler = typeHandler;
		mSymboltable = symboltable;
		mFunctionDeclarations = functionDeclarations;
		}
	
	
	public Expression abstractAnd(final ILocation loc, final int op, final Expression left,
			final CPrimitive typeLeft, final Expression right, final CPrimitive typeRight, final IASTNode hook) {
		final String funcname = "bitwiseAnd";	
		//if	decides_to_apply(CYRUS_AND_0_LEFT, left, right)
		if (left instanceof IntegerLiteral) {
			String valueLeft =((IntegerLiteral) left).getValue();
			System.out.println("-----Light side constant value:" + valueLeft.equals("1"));
			if (valueLeft.equals("1")) {
				return right;
				} else if (valueLeft.equals("0")){
					return left;
					} 
			} else if (right instanceof IntegerLiteral) {
				String valueRight = ((IntegerLiteral) right).getValue();
				System.out.println("-----Right side constant value:" + valueRight.equals("1"));
				if (valueRight.equals("1")) {
					return left;
					} else if (valueRight.equals("0")){
						return right;
						}
				} 
		// If left is equal literal 0 or right is equal literal 0. 
		Expression literal_0 = new IntegerLiteral(loc, BoogieType.TYPE_INT, "0");
		Expression literal_1 = new IntegerLiteral(loc, BoogieType.TYPE_INT, "1");
		Expression left_eq1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPEQ, left, literal_1);
		Expression left_eq0 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPEQ, left, literal_0);
		Expression right_eq1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPEQ, right, literal_1);
		Expression right_eq0 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPEQ, right, literal_0);
		
		final String prefixedFunctionName = SFO.AUXILIARY_FUNCTION_PREFIX + funcname;
				
		Expression cond_and_0 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICOR, left_eq0, right_eq0);
		declareBitvectorFunction(loc, prefixedFunctionName, false, typeLeft, typeLeft, typeRight);
		final Expression func = ExpressionFactory.constructFunctionApplication(loc, prefixedFunctionName,
				new Expression[] { left, right }, mTypeHandler.getBoogieTypeForCType(typeLeft));
		//	return func;
		
		// case a&0
		Expression and_0 = ExpressionFactory.constructIfThenElseExpression(loc, cond_and_0, literal_0, func);
		
		// for the case, a&1, if size(a) is not 1, the result would diverge: -2&1=0, 2&1=0, 3&1=1.
		
		Expression left_size1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICOR, left_eq1, left_eq0);
		Expression right_size1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICOR, right_eq1, right_eq0);	

	
		Expression cond_left_1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, left_eq1, right_size1);
		Expression cond_right_1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, left_size1, right_eq1);
		Expression right_1_ite = ExpressionFactory.constructIfThenElseExpression(loc, cond_right_1, left, and_0);
		Expression and_abs = ExpressionFactory.constructIfThenElseExpression(loc, cond_left_1, right, right_1_ite);			
		return and_abs;
		}
	
	public Expression abstractOr(final ILocation loc, final int op, final Expression left,
			final CPrimitive typeLeft, final Expression right, final CPrimitive typeRight, final IASTNode hook) {
		final String funcname = "bitwiseOr";	
		if (left instanceof IntegerLiteral) {
			String valueLeft =((IntegerLiteral) left).getValue();
			if (valueLeft.equals("1")) {
				return left;
				} else if (valueLeft.equals("0")){
					return right;
					}
			} else if (right instanceof IntegerLiteral) {
				String valueRight = ((IntegerLiteral) right).getValue();
				if (valueRight.equals("1")) {
					return right;
					} else if (valueRight.equals("0")){
						return left;
						}
				}
		Expression literal_1 = new IntegerLiteral(loc, BoogieType.TYPE_INT, "1");
		Expression literal_0 = new IntegerLiteral(loc, BoogieType.TYPE_INT, "0");
		
		Expression left_cmp1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPEQ, left, literal_1);
		Expression left_cmp0 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPEQ, left, literal_0);
		Expression right_cmp1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPEQ, right, literal_1);
		Expression right_cmp0 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPEQ, right, literal_0);
		
		
		final String prefixedFunctionName = SFO.AUXILIARY_FUNCTION_PREFIX + funcname;
		declareBitvectorFunction(loc, prefixedFunctionName, false, typeLeft, typeLeft, typeRight);
		final Expression func = ExpressionFactory.constructFunctionApplication(loc, prefixedFunctionName,
				new Expression[] { left, right }, mTypeHandler.getBoogieTypeForCType(typeLeft));
		
		// bit-size(left/right) = 1 <==> (left/right == 1) || (left/right ==0) 
		Expression left_size1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICOR, left_cmp1, left_cmp0);
		Expression right_size1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICOR, right_cmp1, right_cmp0);	
//		Expression left_right_size1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, left_size1, right_size1);
		
		//case that a is non-zero, assuming they are in the condition, that always evaluated to true?		
		
		// a|1 -> a
		Expression left_1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, left_cmp1, right_size1);
		Expression right_1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, left_size1, right_cmp1);
		Expression either_1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICOR, left_1, right_1);
		Expression or_1 = ExpressionFactory.constructIfThenElseExpression(loc, either_1, literal_1, func);	
		
	
		// for the case, a|0 = a when a is bloolean or one bit size?
		Expression left_0 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, left_cmp0, right_size1);
		Expression right_0 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, left_size1, right_cmp0);
		
		Expression left_0_ite = ExpressionFactory.constructIfThenElseExpression(loc, left_0, right, or_1);
		Expression or_0 = ExpressionFactory.constructIfThenElseExpression(loc, right_0, left, left_0_ite);	
		return or_0;			
	}
	
	
	/**
	 * Construct right shift rules.
	 * In c for the gcc compiler with defualt settings, the a>>31 && a<0, return -1.
	 * 
	 **/
	
	public Expression abstractShiftRight(final ILocation loc, final int op, final Expression left,
			final CPrimitive typeLeft, final Expression right, final CPrimitive typeRight, final IASTNode hook) {
		final String funcname = "shiftRight";		
		Expression literal_0 = new IntegerLiteral(loc, BoogieType.TYPE_INT, "0");
		Expression literal_1 = new IntegerLiteral(loc, BoogieType.TYPE_INT, "-1");
	
		Expression literal_31 = new IntegerLiteral(loc, BoogieType.TYPE_INT, "31");
		Expression left_cmp = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPEQ, left, literal_31);
		Expression right_cmp = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPEQ, right, literal_31);		
		
		// left/right operand is positive and right/left operand is 31 
		Expression left_pos = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPGEQ, left, literal_0);
		Expression right_pos = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPGEQ, right, literal_0);		
		Expression left_cond_pos = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, left_pos, right_cmp);
		Expression right_cond_pos = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, left_cmp, right_pos);
		Expression cond_pos = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICOR, left_cond_pos, right_cond_pos);
		
		final BigInteger shiftRightLiteralValue = mTypeSizes.extractIntegerValue(right, typeRight, hook);
		Expression func;
		if (shiftRightLiteralValue != null) {
			func = constructShiftWithLiteralOptimization(loc, left, typeRight, shiftRightLiteralValue,
					Operator.ARITHDIV);
		} else {
			final String prefixedFunctionName = SFO.AUXILIARY_FUNCTION_PREFIX + funcname;
			declareBitvectorFunction(loc, prefixedFunctionName, false, typeLeft, typeLeft, typeRight);
			func = ExpressionFactory.constructFunctionApplication(loc, prefixedFunctionName,
					new Expression[] { left, right }, mTypeHandler.getBoogieTypeForCType(typeLeft));
			}
		Expression pos_ite = ExpressionFactory.constructIfThenElseExpression(loc, cond_pos, literal_0, func);
		
		// shiftRight on an negative number is unconventional, but according to the evaluation from gcc compiler, a>>31 would results in -1
		Expression left_neg = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPLT, left, literal_0);
		Expression right_neg = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPLT, right, literal_0);
		Expression left_cond_neg = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, left_neg, right_cmp);
		Expression right_cond_neg = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, left_cmp, right_neg);		
		Expression cond_neg = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICOR, left_cond_neg, right_cond_neg);
		
		
		Expression shiftRight = ExpressionFactory.constructIfThenElseExpression(loc, cond_neg, literal_1, pos_ite);
		return shiftRight;
			
	}
	
	/*
	 * Still we need to decide how to judge the bit size of an expression here?
	 * solution: integer eqauls to 0 or 1
	 */
	public Expression abstractXor(final ILocation loc, final int op, final Expression left,
			final CPrimitive typeLeft, final Expression right, final CPrimitive typeRight, final IASTNode hook) {
		final String funcname = "bitwiseXOr";
			
		Expression literal_1 = new IntegerLiteral(loc, BoogieType.TYPE_INT, "1");
		Expression literal_0 = new IntegerLiteral(loc, BoogieType.TYPE_INT, "0");
		
		Expression left_cmp1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPEQ, left, literal_1);
		Expression left_cmp0 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPEQ, left, literal_0);
		
		Expression right_cmp1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPEQ, right, literal_1);
		Expression right_cmp0 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPEQ, right, literal_0);
		Expression left_right_eq = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPEQ, left, right);
		Expression left_right_neq = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.COMPNEQ, left, right);
		
		// bit-size(left/right) = 1 <==> (left/right == 1) || (left/right ==0) 
		Expression left_size1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICOR, left_cmp1, left_cmp0);
		Expression right_size1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICOR, right_cmp1, right_cmp0);
		Expression left_right_size1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, left_size1, right_size1);
		
				
		final String prefixedFunctionName = SFO.AUXILIARY_FUNCTION_PREFIX + funcname;
		declareBitvectorFunction(loc, prefixedFunctionName, false, typeLeft, typeLeft, typeRight);
		final Expression func = ExpressionFactory.constructFunctionApplication(loc, prefixedFunctionName,
				new Expression[] { left, right }, mTypeHandler.getBoogieTypeForCType(typeLeft));
		
//		// rule xor-0
//		Expression right_0 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, left_size1, right_cmp0);
//		Expression left_0 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, right_size1, left_cmp0);
//		Expression right_ite_0 = ExpressionFactory.constructIfThenElseExpression(loc, right_0, left, func);
//		Expression left_ite_0 = ExpressionFactory.constructIfThenElseExpression(loc, left_0, right, right_ite_0);
//		
//		//	return func;
//		// rule xor-1, result is the negate of the operand.
//		Expression right_1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, left_size1, right_cmp1);
//		Expression left_1 = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, right_size1, left_cmp1);
//		
//		// need to change the expression type here: Int -> Bool? 
////		right.setType(BoogieType.TYPE_BOOL);
////		left.setType(BoogieType.TYPE_BOOL);
//		Expression right_not = ExpressionFactory.constructUnaryExpression(loc, UnaryExpression.Operator.ARITHNEGATIVE, right);
//		Expression left_not = ExpressionFactory.constructUnaryExpression(loc, UnaryExpression.Operator.ARITHNEGATIVE, left);
//	
//		
//		Expression right_ite_1 = ExpressionFactory.constructIfThenElseExpression(loc, right_1, left_not, left_ite_0);
//		Expression left_ite_1 = ExpressionFactory.constructIfThenElseExpression(loc, left_1, right_not, right_ite_1);
//	
		
		//new implementaion approach, if both operands are size of 1, when they are equal, return 0, else return 1.
		Expression cond_eq = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, left_right_size1, left_right_eq);
		Expression cond_neq = ExpressionFactory.newBinaryExpression(loc, BinaryExpression.Operator.LOGICAND, left_right_size1, left_right_neq);
		Expression xor_eq = ExpressionFactory.constructIfThenElseExpression(loc, cond_eq, literal_0, func);
		Expression xor = ExpressionFactory.constructIfThenElseExpression(loc, cond_neq, literal_1, xor_eq);
		
		return xor;			
	}
	
	/*
	 * method to decide if an expression has bitwise operator  
	 * @param bexpr
	 *        for now we consider all binary cases, because the unary complement rule is not clear yet.
	 * 	
	 */  
	public static boolean containBitwise(final IASTBinaryExpression bexpr) {
		IASTExpression opr1 = bexpr.getOperand1();
		IASTExpression opr2 = bexpr.getOperand2();
		switch(bexpr.getOperator()) {
		case IASTBinaryExpression.op_binaryAnd:
		case IASTBinaryExpression.op_binaryAndAssign:
		case IASTBinaryExpression.op_binaryOr:
		case IASTBinaryExpression.op_binaryOrAssign:
		case IASTBinaryExpression.op_binaryXor:
		case IASTBinaryExpression.op_binaryXorAssign:
			return true;
		default: {
			if (opr1 instanceof IASTBinaryExpression) 
				return containBitwise((IASTBinaryExpression) opr1);
			else if (opr2 instanceof IASTBinaryExpression)
				return containBitwise((IASTBinaryExpression) opr2);
			else return false;
			}
			
		}	
	}
	
	public static Result abstractAssgin(CHandler chandler, final ExpressionResultTransformer exprResultTransformer,
			final IDispatcher main, final LocationFactory locationFactory, final IASTBinaryExpression node) {
		final ILocation loc = locationFactory.createCLocation(node);
		final ExpressionResult leftOperand = (ExpressionResult) main.dispatch(node.getOperand1());
		final ExpressionResult rightOperand = (ExpressionResult) main.dispatch(node.getOperand2());
		final ExpressionResultBuilder builder = new ExpressionResultBuilder();
		
		if (node.getOperand2() instanceof IASTBinaryExpression) {
			// for the general bitwise assignment case, we build up assume statements.
		//	System.out.println("----Is this binary expression contains bitwise operator?: " + BitabsTranslation.containBitwise(node));
			
			IASTBinaryExpression rhs_bit = (IASTBinaryExpression) node.getInitOperand2();					
			boolean bit_op = BitabsTranslation.isBitwiseOperator(rhs_bit.getOperator());
			System.out.println("----Is the binary operator a bitwise operator? " + bit_op);
			
			if (bit_op) {
				System.out.println("----The bitwise operator is: " + rhs_bit.getRawSignature());
					System.out.println("----assignment rhs expression before translation: "
						+ node.getOperand2().getRawSignature());
				System.out
						.println("----assignment rhs expression after translation: " + rightOperand.toString());
				System.out
						.println("----assignment lhs expression after translation: " + rightOperand.toString());

				builder.addAllExceptLrValue(leftOperand);
				final CType lType = leftOperand.getLrValue().getCType().getUnderlyingType();
				final ExpressionResult rightOperandSwitched = exprResultTransformer
						.makeRepresentationReadyForConversionAndRexBoolToInt(rightOperand, loc, lType, node);
				builder.addAllIncludingLrValue(rightOperandSwitched);

				Expression literal_1 = new IntegerLiteral(loc, BoogieType.TYPE_INT, "1");
				Expression literal_0 = new IntegerLiteral(loc, BoogieType.TYPE_INT, "0");

				Expression left_pos = ExpressionFactory.newBinaryExpression(loc,
						BinaryExpression.Operator.COMPGT, leftOperand.getLrValue().getValue(), literal_0);
				AssumeStatement assume_pos = new AssumeStatement(loc, left_pos);
				Expression formula_left = ExpressionFactory.newBinaryExpression(loc,
						BinaryExpression.Operator.COMPLT, leftOperand.getLrValue().getValue(), literal_1);

				AssumeStatement assume_stmt = new AssumeStatement(loc, formula_left);
				ExpressionResult assign_rv = chandler.makeAssignment(loc, leftOperand.getLrValue(),
						leftOperand.getNeighbourUnionFields(), builder.build(), node);
				// Build the new assignment with local variable
				builder.addAllExceptLrValue(assign_rv);
				/* __VERIFIER_assume(n>=0); */
				/* __VERIFIER_assume(n<n0); */
				builder.addStatement(assume_pos);
				builder.addStatement(assume_stmt);
				return builder.build();
				} else {
					// ToDo, x = 1+x&(x-3)?
					throw new UnsupportedOperationException("ToDo, x = 1+x&(x-3)?...");
					}
			}
		return builder.build();
	}
	
	
	
	// Justify if an operator is bitwise operator
	public static boolean isBitwiseOperator(int opcd) {
	
		switch(opcd) {
		case IASTBinaryExpression.op_binaryAnd:
		case IASTBinaryExpression.op_binaryAndAssign:
		case IASTBinaryExpression.op_binaryOr:
		case IASTBinaryExpression.op_binaryOrAssign:
		case IASTBinaryExpression.op_binaryXor:
		case IASTBinaryExpression.op_binaryXorAssign:
			return true;
		default: 
			return false;			
		}	
	}
	
	
	private void declareBitvectorFunction(final ILocation loc, final String prefixedFunctionName,
			final boolean boogieResultTypeBool, final CPrimitive resultCType, final CPrimitive... paramCType) {
		final String functionName = prefixedFunctionName.substring(1, prefixedFunctionName.length());
		final Attribute attribute = new NamedAttribute(loc, FunctionDeclarations.OVERAPPROX_IDENTIFIER,
				new Expression[] { ExpressionFactory.createStringLiteral(loc, functionName) });
		final Attribute[] attributes = new Attribute[] { attribute };
		mFunctionDeclarations.declareFunction(loc, SFO.AUXILIARY_FUNCTION_PREFIX + functionName, attributes,
				boogieResultTypeBool, resultCType, paramCType);
	}


	private Expression constructShiftWithLiteralOptimization(final ILocation loc, final Expression left,
			final CPrimitive typeRight, final BigInteger integerLiteralValue, final Operator op1) {
		// 2017-11-18 Matthias: this could be done analogously in the
		// bitprecise translation
		int exponent;
		try {
			exponent = integerLiteralValue.intValueExact();
		} catch (final ArithmeticException ae) {
			throw new UnsupportedOperationException("rhs of leftshift is larger than C standard allows " + ae);
		}
		final BigInteger shiftFactorBigInt = BigInteger.valueOf(2).pow(exponent);
		final Expression shiftFactorExpr = mTypeSizes.constructLiteralForIntegerType(loc, typeRight, shiftFactorBigInt);
		final Expression result = ExpressionFactory.newBinaryExpression(loc, op1, left, shiftFactorExpr);
		return result;
	}






}
