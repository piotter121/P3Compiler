package compilator.generation.llvm;

import java.util.Stack;

class LLVMGenerator {

    int reg = 1;
    private int globalReqBackup = 1;
    private int ifNo = 0;
    private String headerText = "";
    private String globals = "";
    private String mainText = "";
    private String buffer = "";
    private Stack<Integer> brstack = new Stack<>();

    int icmp(ConditionalOperand operand, VarType type, String id1, String id2) {
        buffer += "%" + reg + " = icmp " + operand + " " + type + " " + id1 + ", " + id2 + "\n";
        return reg++;
    }

    void printf(Scope prefix, String id, VarType type) {
        buffer += "%" + reg + " = load " + type + "* " + prefix + id + "\n";
        reg++;
        buffer += "%" + reg + " = call i32 (i8*, ...)* @printf(i8* getelementptr inbounds ([4 x i8]* " + type.getPrintfFormat() + ", i32 0, i32 0), " + type + " %" + (reg - 1) + ")\n";
        reg++;
    }

    void declare(Scope scope, String id, VarType type) {
        if (scope == Scope.LOCAL)
            buffer += scope + id + " = alloca " + type + "\n";
        else if (scope == Scope.GLOBAL)
            globals += scope + id + " = common global " + type + " " + type.getInitVal() + "\n";
    }

    void assignI32(Scope scope, String id, String value) {
        buffer += "store i32 " + value + ", i32* " + scope + id + "\n";
    }

    void assignDouble(Scope scope, String id, String value) {
        buffer += "store double " + value + ", double* " + scope + id + "\n";
    }

    void assignString(Scope scope, String id, String value) {
        headerText += "@str" + id + " = constant[" + (value.length() + 1) + " x i8] c\"" + value + "\\00\"\n";
        buffer += "store i8* getelementptr inbounds ([" + (value.length() + 1) + " x i8]* @str" + id + ", i32 0, i32 0), i8** " + scope + id + "\n";
    }

    int load(VarType type, Scope prefix, String id) {
        buffer += "%" + reg + " = load " + type + "* " + prefix + id + "\n";
        return reg++;
    }

    void addI32(String val1, String val2) {
        buffer += "%" + reg + " = add i32 " + val1 + ", " + val2 + "\n";
        reg++;
    }

    void addDouble(String val1, String val2) {
        buffer += "%" + reg + " = fadd double " + val1 + ", " + val2 + "\n";
        reg++;
    }

    void subI32(String val1, String val2) {
        buffer += "%" + reg + " = sub i32 " + val2 + ", " + val1 + "\n";
        reg++;
    }

    void subDouble(String val1, String val2) {
        buffer += "%" + reg + " = fsub double " + val2 + ", " + val1 + "\n";
        reg++;
    }

    void multI32(String val1, String val2) {
        buffer += "%" + reg + " = mul i32 " + val1 + ", " + val2 + "\n";
        reg++;
    }

    void multDouble(String val1, String val2) {
        buffer += "%" + reg + " = fmul double " + val1 + ", " + val2 + "\n";
        reg++;
    }

    void divI32(String val1, String val2) {
        buffer += "%" + reg + " = sdiv i32 " + val2 + ", " + val1 + "\n";
        reg++;
    }

    void divDouble(String val1, String val2) {
        buffer += "%" + reg + " = fdiv double " + val2 + ", " + val1 + "\n";
        reg++;
    }

    void sitofp(String id) {
        buffer += "%" + reg + " = sitofp i32 " + id + " to double\n";
        reg++;
    }

    void fptosi(String id) {
        buffer += "%" + reg + " = fptosi double " + id + " to i32\n";
        reg++;
    }


    String generate() {
        return "declare i32 @printf(i8*, ...)\n"
                + "declare i32 @scanf(i8*, ...)\n"
                + "@strpi = constant [4 x i8] c\"%d\\0A\\00\"\n"
                + "@strpd = constant [4 x i8] c\"%f\\0A\\00\"\n"
                + "@strps = constant [4 x i8] c\"%s\\0A\\00\"\n"
                + "@strsi =  constant [3 x i8] c\"%d\\00\"\n"
                + "@strsd = constant [4 x i8] c\"%lf\\00\"\n"
                + "@strinit = constant [1 x i8] c\"\\00\"\n"
                + headerText
                + globals
                + "define i32 @main() nounwind{\n"
                + mainText
                + "ret i32 0 \n" +
                "}\n";
    }

    void scanfI32(String id) {
        buffer += "%" + reg + " = call i32 (i8*, ...)* @scanf(i8* getelementptr inbounds ([3 x i8]* @strsi, i32 0, i32 0), i32* %" + id + ")\n";
        reg++;
    }

    void scanfDouble(String id) {
        buffer += "%" + reg + " = call i32 (i8*, ...)* @scanf(i8* getelementptr inbounds ([4 x i8]* @strsd, i32 0, i32 0), double* %" + id + ")\n";
        reg++;
    }

    void closeMain() {
        mainText += buffer;
    }

    void beginIf(String cond) {
        ifNo++;
        buffer += "br i1 " + cond + ", label %ok" + ifNo + ", label %fail" + ifNo + "\n";
        buffer += "ok" + ifNo + ": \n";
        brstack.push(ifNo);
    }

    void endIf() {
        int brNo = brstack.pop();
        buffer += "br label %fail" + brNo + "\n";
        buffer += "fail" + brNo + ":\n";
    }

    void loopHead() {
        ifNo++;
        brstack.push(ifNo);
        buffer += "br label %loop" + ifNo + "Head\n";
        buffer += "loop" + ifNo + "Head:\n";
    }

    void beginLoop(String cond) {
        buffer += "br i1 " + cond + ", label %loop" + ifNo + "Body, label %loop" + ifNo + "End\n";
        buffer += "loop" + ifNo + "Body:\n";
    }

    void endLoop() {
        int brNo = brstack.pop();
        buffer += "br label %loop" + brNo + "Head\n";
        buffer += "loop" + brNo + "End:\n";
    }

    void startFunction(String id) {
        mainText += buffer;
        globalReqBackup = reg;
        buffer = "define void @" + id + "() nounwind {\n";
        reg = 1;
    }

    void endFunction() {
        buffer += "ret void\n}\n";
        headerText += buffer;
        buffer = "";
        reg = globalReqBackup;
    }

    void callFunction(String id) {
        buffer += "call void @" + id + "()\n";
    }
}

