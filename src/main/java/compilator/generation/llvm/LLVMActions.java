package compilator.generation.llvm;

import org.antlr.v4.runtime.ParserRuleContext;
import p3lang.P3langBaseListener;
import p3lang.P3langParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by piotr on 08.05.17.
 */
enum VarType {
    INT, REAL, STRING, UNKNOWN
}

class Value {
    String value;
    VarType type;

    Value(String value, VarType type) {
        this.value = value;
        this.type = type;
    }
}

public class LLVMActions extends P3langBaseListener {

    private final String outputFileName;
    private final LLVMGenerator generator = new LLVMGenerator();
    private Map<String, VarType> variables = new HashMap<>();
    private Map<String, Integer> varCounters = new HashMap<>();
    private Stack<Value> stack = new Stack<>();

    public LLVMActions(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    @Override
    public void exitAssignStatement(P3langParser.AssignStatementContext ctx) {
        String ID = ctx.ID().getText();
        Value v = stack.pop();
        variables.put(ID, v.type);
        int varAssignCount = varCounters.getOrDefault(ID, -1);
        varAssignCount++;
        String newID = ID + varAssignCount;
        switch (v.type) {
            case INT:
                generator.declareI32(newID);
                generator.assignI32(newID, v.value);
                break;
            case REAL:
                generator.declareDouble(newID);
                generator.assignDouble(newID, v.value);
                break;
            case STRING:
                generator.declareString(newID);
                generator.assignString(newID, v.value);
                break;
            case UNKNOWN:
                throw new RuntimeException(String.format("Linia %d: Nieznany typ wartości", ctx.getStart().getLine()));
        }
        varCounters.put(ID, varAssignCount);
    }

    @Override
    public void exitProgram(P3langParser.ProgramContext ctx) {
        String generatedProgram = generator.generate();
        String userDir = System.getProperty("user.dir");
        try (FileWriter writer = new FileWriter(userDir + File.separator + outputFileName)) {
            writer.write(generatedProgram);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exitPrintStatement(P3langParser.PrintStatementContext ctx) {
        String ID = ctx.ID().getText();
        VarType type = variables.getOrDefault(ID, VarType.UNKNOWN);
        int c = varCounters.getOrDefault(ID, 0);
        String nid = ID + c;
        if (type != null) {
            switch (type) {
                case INT:
                    generator.printfI32(nid);
                    break;
                case REAL:
                    generator.printfDouble(nid);
                    break;
                case STRING:
                    generator.printString(nid);
                    break;
                case UNKNOWN:
                    throw new RuntimeException(
                            String.format("Linia %d: Nieznany typ zmiennej %s", ctx.getStart().getLine(), ID));
            }
        } else {
            throw new RuntimeException(String.format("Linia %d: Nieznana zmienna %s", ctx.getStart().getLine(), ID));
        }
    }

    @Override
    public void exitReadStatement(P3langParser.ReadStatementContext ctx) {
        String ID = ctx.ID().getText();

        VarType varType = variables.get(ID);
        switch (varType) {
            case INT:
                generator.scanfI32(ID);
                break;
            case REAL:
                generator.scanfDouble(ID);
                break;
            default:
                throw new UnsupportedOperationException(
                        String.format("Linia %d: nie można wczytywać innych zmiennych niż typu int i real",
                                ctx.getStart().getLine()));
        }
    }

    @Override
    public void exitAdd(P3langParser.AddContext ctx) {
        arithmeticOperation(ctx, generator::addI32, generator::addDouble,
                "Linia %d: niezgodność typów podczas operacji dodawania");
    }

    @Override
    public void exitSubs(P3langParser.SubsContext ctx) {
        arithmeticOperation(ctx, generator::subI32, generator::subDouble,
                "Linia %d: niezgodność typów podczas operacji odejmowania");
    }

    @Override
    public void exitMult(P3langParser.MultContext ctx) {
        arithmeticOperation(ctx, generator::multI32, generator::multDouble,
                "Linia %d: niezgodność typów podczas operacji mnożenia");
    }

    @Override
    public void exitDiv(P3langParser.DivContext ctx) {
        arithmeticOperation(ctx, generator::divI32, generator::divDouble,
                "Linia %d: niezgodność typów podczas operacji dzielenia");
    }

    private void arithmeticOperation(ParserRuleContext ctx, Operation intOp, Operation doubleOp, String msg) {
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if (v1.type == VarType.INT && v2.type == VarType.INT) {
            intOp.doOperation(v1.value, v2.value);
            stack.push(new Value("%" + (generator.reg - 1), VarType.INT));
        } else if (v1.type == VarType.REAL && v2.type == VarType.REAL) {
            doubleOp.doOperation(v1.value, v2.value);
            stack.push(new Value("%" + (generator.reg - 1), VarType.REAL));
        } else {
            throw new IllegalArgumentException(
                    String.format(msg, ctx.getStart().getLine()));
        }
    }

    @Override
    public void exitVar(P3langParser.VarContext ctx) {
        String ID = ctx.ID().getText();
        VarType varType = variables.getOrDefault(ID, VarType.UNKNOWN);
        if (varType == VarType.UNKNOWN) {
            throw new IllegalStateException(
                    String.format("Linia %d: nie znaleziono zmiennej o nazwie %s", ctx.getStart().getLine(), ID));
        }
        int c = varCounters.getOrDefault(ID, 0);
        stack.push(new Value("%" + ID + c, varType));
    }

    @Override
    public void exitInt(P3langParser.IntContext ctx) {
        stack.push(new Value(ctx.INT().getText(), VarType.INT));
    }

    @Override
    public void exitReal(P3langParser.RealContext ctx) {
        stack.push(new Value(ctx.REAL().getText(), VarType.REAL));
    }

    @Override
    public void exitToint(P3langParser.TointContext ctx) {
        Value v = stack.pop();
        generator.fptosi(v.value);
        stack.push(new Value("%" + (generator.reg - 1), VarType.INT));
    }

    @Override
    public void exitToreal(P3langParser.TorealContext ctx) {
        Value v = stack.pop();
        generator.sitofp(v.value);
        stack.push(new Value("%" + (generator.reg - 1), VarType.REAL));
    }

    @Override
    public void exitString(P3langParser.StringContext ctx) {
        String tmp = ctx.STRING().getText();
        String text = tmp.substring(1, tmp.length() - 1);
        stack.push(new Value(text, VarType.STRING));
    }

    @FunctionalInterface
    private interface Operation {
        void doOperation(String v1, String v2);
    }
}

