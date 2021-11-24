package com.unibo.ci.ast.dec;

import java.util.ArrayList;
import java.util.List;

import com.unibo.ci.ast.errors.SemanticError;
import com.unibo.ci.ast.types.Type;
import com.unibo.ci.ast.types.TypeVoid;
import com.unibo.ci.util.Environment;
import com.unibo.ci.util.Environment.DuplicateEntryException;
import com.unibo.ci.util.GammaEnv;
import com.unibo.ci.util.GlobalConfig;
import com.unibo.ci.util.SigmaEnv;
import com.unibo.ci.ast.stmt.block.BlockBase;
import com.unibo.ci.ast.types.TypeFunction;
import com.unibo.ci.util.TypeErrorsStorage;
import com.unibo.ci.ast.errors.TypeError;

public class DecFun extends Dec {
    private final String id;
    private final Type type;
    private final List<Arg> args;
    private final BlockBase block;
    private final TypeFunction typeFun;

    public DecFun(int row, int column, Type type, String id, List<Arg> args, BlockBase block) {
        super(row, column, type, id);
        this.id = id;
        this.type = type;
        this.args = args;
        this.block = block;
        this.typeFun = new TypeFunction(row, column, id, args.size(), type, args);
    }

    @Override
    public String toPrint(String indent) {
        return indent + "Declaration: Function\n" + 
                indent + "\tId: \"" + this.id + "\"\n" + 
                type.toPrint(indent + "\t") +
                printArgs(indent + "\t") + 
                block.toPrint(indent + "\t");
    }

    private String printArgs(String indent) {
        StringBuilder sb = new StringBuilder(indent + "Args: \n");
        this.args.forEach((arg) -> {
            sb.append(arg.toPrint(indent + "\t"));
        });
        return sb.toString();
    }

    @Override 
    public ArrayList<SemanticError> checkSemantics(GammaEnv env) {

        ArrayList<SemanticError> semanticErrors = new ArrayList<SemanticError>();
        try {
            // Aggiungi tipo funzione
            env.addDeclaration(id, typeFun );
            //TODO quanta memoria occupa la decfun? solo il numero deli argomenti? (args.size())

            // Nota: type dovrebbe essere T_1, ..., T_n -> T

        } catch (DuplicateEntryException e) {
            SemanticError error = new SemanticError(row, column, "Already declared [" + id + "]");
            semanticErrors.add(error);
            // return semanticErrors;
        }
        semanticErrors.addAll(block.checkSemanticsInjectArgs(env, args));
        return semanticErrors;
    }
    
    @Override
    public Type typeCheck() {
        for (Arg arg : args) {
            if (arg.typeCheck() == null)
                return null;
        }
        
        Type blockType = this.block.typeCheck() ;
        //System.out.println("DEBUG: il tipo del blocco nella funzione " + id + " è " + blockType);
        //System.out.println("DEBUG: il tipo della funzione " + id + " è " + this.type);
        if ( (blockType == null && !(this.type instanceof TypeVoid)) || (blockType != null && !this.type.equals( blockType))){
            //Errore! Tipo del blocco e tipo di ritorno della funzione incompatibili
            TypeErrorsStorage.add( new TypeError(super.row, super.column, 
                "Function [" + this.id + "] must return with type [" + type.getTypeName() +"]" ));
        }

        return typeFun; 
        //TODO quanta memoria occupa la decfun? solo il numero deli argomenti? (args.size())
    }

    @Override
    public String codeGeneration() {
        boolean debug = GlobalConfig.PRINT_COMMENTS;

        String labelFun = id;
		String skip = "end" + labelFun;
        typeFun.setLabelEndFun(skip);

        String out = (debug ? ";BEGIN DECFUN " + id + "\n" : "");        
		out += "b " + skip + "\n";
		out += labelFun + ":\n";
		out += "sw $ra -1($cl)\n";
        out += block.codeGeneration();
		out += skip + ":\n";
		out += "lw $ra -1($cl)\n";
		out += "lw $fp 1($cl)\n";
		out += "lw $sp 0($cl) \n";
        out += "addi $cl $fp 2\n";
		out += "jr $ra\n";
        out += skip + ":\n";
        out += (debug ? ";END DECFUN " + id + "\n" : "");
        return out;
    }


}