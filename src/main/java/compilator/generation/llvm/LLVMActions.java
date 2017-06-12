package compilator.generation.llvm;

import org.antlr.v4.runtime.ParserRuleContext;
import p3lang.P3langBaseListener;
import p3lang.P3langParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class LLVMActions extends P3langBaseListener {

    private final String outputFileName;
    private final LLVMGenerator generator = new LLVMGenerator();

    private final Map<String, VarType> globalVariables = new HashMap<>();
    private final Map<String, VarType> localVariables = new HashMap<>();

    private final Set<String> functions = new HashSet<>();

    private final Stack<Value> values = new Stack<>();
    private final Stack<String> evaluatedConditions = new Stack<>();
    private boolean global;

    public LLVMActions(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    @Override
    public void enterProgram(P3langParser.ProgramContext ctx) {
        setGlobalScope();
    }

    @Override
    public void exitProgram(P3langParser.ProgramContext ctx) {
        generator.closeMain();
        String generatedProgram = generator.generate();
        String userDir = System.getProperty("user.dir");
        try (FileWriter writer = new FileWriter(userDir + File.separator + outputFileName)) {
            writer.write(generatedProgram);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void enterFunctionDefinition(P3langParser.FunctionDefinitionContext ctx) {
        String id = ctx.ID().getText();
        if (functions.contains(id)) {
            throw new RuntimeException(
                    String.format("Linia %d: Funkcja %s jest już zadeklarowana", ctx.getStart().getLine(), id)
            );
        }
        setNonGlobalScope();
        functions.add(id);
        generator.startFunction(id);
    }

    @Override
    public void exitFunctionDefinition(P3langParser.FunctionDefinitionContext ctx) {
        setGlobalScope();
        generator.endFunction();
    }

    private void setNonGlobalScope() {
        global = false;
        localVariables.clear();
    }

    private void setGlobalScope() {
        global = true;
        localVariables.clear();
    }

    @Override
    public void exitFunctionCallStatement(P3langParser.FunctionCallStatementContext ctx) {
        String id = ctx.ID().getText();
        if (functions.contains(id)) {
            generator.callFunction(id);
        } else {
            throw new IllegalArgumentException(
                    String.format("Linia %d: Nie znaleziono funkcji %s", ctx.getStart().getLine(), id));
        }
    }

    @Override
    public void exitAssignStatement(P3langParser.AssignStatementContext ctx) {
        String ID = ctx.ID().getText();
        Value v = values.pop();
        if (v.type == VarType.UNKNOWN) {
            throw new RuntimeException(String.format("Linia %d: Nieznany typ wartości", ctx.getStart().getLine()));
        }
        if (getScopeVariables().getOrDefault(ID, VarType.UNKNOWN).equals(VarType.UNKNOWN)) {
            declareNewVariable(ID, v.type);
            assign(ID, v);
        } else if (getScopeVariables().getOrDefault(ID, VarType.UNKNOWN).equals(v.type)) {
            assign(ID, v);
        } else {
            throw new RuntimeException(
                    String.format("Linia %d: Niezgodna wartość z typem zmiennej!", ctx.getStart().getLine()));
        }
    }


    private void assign(String id, Value v) {
        switch (v.type) {
            case INT:
                generator.assignI32(getVariableScope(id), id, v.value);
                break;
            case REAL:
                generator.assignDouble(getVariableScope(id), id, v.value);
                break;
            case STRING:
                generator.assignString(getVariableScope(id), id, v.value);
                break;
        }
    }

    private void declareNewVariable(String id, VarType type) {
        getScopeVariables().put(id, type);
        generator.declare(getActualScope(), id, type);
    }

    private Map<String, VarType> getScopeVariables() {
        return global ? globalVariables : localVariables;
    }

    private VarType getVariableType(String varName) {
        Map<String, VarType> variables;
        if (global) {
            variables = getScopeVariables();
        } else {
            variables = new HashMap<>(globalVariables);
            variables.putAll(localVariables);
        }
        return variables.getOrDefault(varName, VarType.UNKNOWN);
    }

    @Override
    public void exitPrintStatement(P3langParser.PrintStatementContext ctx) {
        String ID = ctx.ID().getText();
        VarType type = getVariableType(ID);
        if (type == VarType.UNKNOWN) {
            throw new RuntimeException(String.format("Linia %d: Nieznana zmienna %s", ctx.getStart().getLine(), ID));
        } else {
            generator.printf(getVariableScope(ID), ID, type);
        }
    }

    @Override
    public void exitReadStatement(P3langParser.ReadStatementContext ctx) {
        String ID = ctx.ID().getText();

        VarType varType = getVariableType(ID);
        Scope scope = getVariableScope(ID);
        switch (varType) {
            case INT:
                generator.scanfI32(scope, ID);
                break;
            case REAL:
                generator.scanfDouble(scope, ID);
                break;
            case UNKNOWN:
                declareNewVariable(ID, VarType.REAL);
                generator.scanfDouble(getActualScope(), ID);
                break;
            case STRING:
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
        Value v1 = values.pop();
        Value v2 = values.pop();
        if (v1.type == VarType.INT && v2.type == VarType.INT) {
            intOp.doOperation(v1.value, v2.value);
            values.push(new Value("%" + (generator.reg - 1), VarType.INT));
        } else if (v1.type == VarType.REAL && v2.type == VarType.REAL) {
            doubleOp.doOperation(v1.value, v2.value);
            values.push(new Value("%" + (generator.reg - 1), VarType.REAL));
        } else {
            throw new IllegalArgumentException(
                    String.format(msg, ctx.getStart().getLine()));
        }
    }

    @Override
    public void exitEqualCondition(P3langParser.EqualConditionContext ctx) {
        doComparison(ctx, ConditionalOperand.EQUAL);
    }

    @Override
    public void exitNotEqualCondition(P3langParser.NotEqualConditionContext ctx) {
        doComparison(ctx, ConditionalOperand.NOT_EQUAL);
    }

    @Override
    public void exitLessThanCondition(P3langParser.LessThanConditionContext ctx) {
        doComparison(ctx, ConditionalOperand.LESS_THAN);
    }

    @Override
    public void exitGreaterThanCondition(P3langParser.GreaterThanConditionContext ctx) {
        doComparison(ctx, ConditionalOperand.GREATER_THAN);
    }

    @Override
    public void exitLessOrEqualCondition(P3langParser.LessOrEqualConditionContext ctx) {
        doComparison(ctx, ConditionalOperand.LESS_OR_EQUAL);
    }

    @Override
    public void exitGreaterOrEqualCondition(P3langParser.GreaterOrEqualConditionContext ctx) {
        doComparison(ctx, ConditionalOperand.GREATER_OR_EQUAL);
    }

    private void doComparison(ParserRuleContext ctx, ConditionalOperand operand) {
        Value v2 = values.pop();
        Value v1 = values.pop();
        if (v1.type == VarType.INT && v2.type == VarType.INT) {
            int reg = generator.icmp(operand, v1.type, v1.value, v2.value);
            evaluatedConditions.push("%" + reg);
        } else {
            throw new RuntimeException(
                    String.format("Linia %d: Porównywane zmienne muszą być zmiennymi typu INT!",
                            ctx.getStart().getLine()));
        }
    }

    @Override
    public void enterIfBlock(P3langParser.IfBlockContext ctx) {
        generator.beginIf(evaluatedConditions.pop());
    }

    @Override
    public void exitIfBlock(P3langParser.IfBlockContext ctx) {
        generator.endIf();
    }

    @Override
    public void enterLoopStatement(P3langParser.LoopStatementContext ctx) {
        generator.loopHead();
    }

    @Override
    public void enterLoopBlock(P3langParser.LoopBlockContext ctx) {
        generator.beginLoop(evaluatedConditions.pop());
    }

    @Override
    public void exitLoopBlock(P3langParser.LoopBlockContext ctx) {
        generator.endLoop();
    }

    @Override
    public void exitVar(P3langParser.VarContext ctx) {
        String ID = ctx.ID().getText();
        VarType varType = getVariableType(ID);
        if (varType == VarType.UNKNOWN) {
            throw new IllegalStateException(
                    String.format("Linia %d: nie znaleziono zmiennej o nazwie %s", ctx.getStart().getLine(), ID));
        } else {
            int reg = generator.load(varType, getVariableScope(ID), ID);
            values.push(new Value("%" + reg, varType));
        }
    }

    private Scope getVariableScope(String id) {
        if (globalVariables.containsKey(id)) {
            return Scope.GLOBAL;
        } else if (localVariables.containsKey(id)) {
            return Scope.LOCAL;
        }
        return null;
    }

    private Scope getActualScope() {
        return global ? Scope.GLOBAL : Scope.LOCAL;
    }

    @Override
    public void exitInt(P3langParser.IntContext ctx) {
        values.push(new Value(ctx.INT().getText(), VarType.INT));
    }

    @Override
    public void exitReal(P3langParser.RealContext ctx) {
        values.push(new Value(ctx.REAL().getText(), VarType.REAL));
    }

    @Override
    public void exitToint(P3langParser.TointContext ctx) {
        Value v = values.pop();
        generator.fptosi(v.value);
        values.push(new Value("%" + (generator.reg - 1), VarType.INT));
    }

    @Override
    public void exitToreal(P3langParser.TorealContext ctx) {
        Value v = values.pop();
        generator.sitofp(v.value);
        values.push(new Value("%" + (generator.reg - 1), VarType.REAL));
    }

    @Override
    public void exitString(P3langParser.StringContext ctx) {
        String tmp = ctx.STRING().getText();
        String text = tmp.substring(1, tmp.length() - 1);
        values.push(new Value(text, VarType.STRING));
    }

    @FunctionalInterface
    private interface Operation {
        void doOperation(String v1, String v2);
    }
}

