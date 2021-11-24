package com.unibo.ci.ast.stmt;

import java.util.ArrayList;

import com.unibo.ci.ast.errors.EffectError;
import com.unibo.ci.ast.errors.SemanticError;
import com.unibo.ci.ast.errors.TypeError;
import com.unibo.ci.ast.exp.Exp;
import com.unibo.ci.ast.exp.LhsExp;
import com.unibo.ci.ast.types.Type;
import com.unibo.ci.ast.types.TypeVoid;
import com.unibo.ci.util.Environment;
import com.unibo.ci.util.GammaEnv;
import com.unibo.ci.util.GlobalConfig;
import com.unibo.ci.util.SigmaEnv;
import com.unibo.ci.util.TypeErrorsStorage;

/**
 * Example: x = 3;
 */
public class AssignmentStmt extends Statement {

	private final LhsExp left;
	private final Exp exp;

	public AssignmentStmt(int row, int column, LhsExp left, Exp exp) {
		super(row, column);
		this.left = left;
		this.exp = exp;
	}

    @Override
    public String toPrint(String indent) {
        return indent + "Assignment:\n" + 
                indent + "\tLeft: \n" + 
				this.left.toPrint(indent + "\t\t") +
				indent + "\tRight: \n" + 
				this.exp.toPrint(indent + "\t\t");
    }

	@Override
	public ArrayList<SemanticError> checkSemantics(GammaEnv env) {
		ArrayList<SemanticError> errors = new ArrayList<SemanticError>();
		errors.addAll(left.checkSemantics(env));
		errors.addAll(exp.checkSemantics(env));
		return errors;
	}

	@Override
	public Type typeCheck() {
		Type typeLeft = left.typeCheck();
		Type typeExp = exp.typeCheck();
		if(typeExp == null)
			return null;

		if(!typeLeft.equals(typeExp)){
			TypeErrorsStorage.add(new TypeError(super.row, super.column, 
				"Cannot assign [" + typeExp.getTypeName() + "] to [" + typeLeft.getTypeName() + "]"));
			return null;
		}
		return new TypeVoid();
	}

	@Override
	public String codeGeneration() {
        boolean debug = GlobalConfig.PRINT_COMMENTS;

        String out = (debug ? ";BEGIN ASSIGNMENT " + this.toPrint("") + "\n" : "");        
		out += exp.codeGeneration();
		out += "push $a0\n";
		out += left.codeGeneration();
		out += "lw $t1 0($sp)\n";
		out += "pop\n";
		out += "sw $t1 0($a0)\n";

        out += (debug ? ";END ASSIGNMENT\n" : "");
        return out;
	}

	@Override
	public ArrayList<EffectError> AnalyzeEffect(SigmaEnv env) {
		// TODO Auto-generated method stub
		return null;
	}

}
