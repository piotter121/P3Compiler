package compilator.generation.llvm;

enum VarType {
    INT("i32", "@strpi", "0"),
    REAL("double", "@strpd", "0.0"),
    STRING("i8*", "@strps", "null"),
    UNKNOWN("", "", "");

    private final String type;
    private final String printfFormat;
    private final String initVal;

    VarType(String type, String printfFormat, String initVal) {
        this.type = type;
        this.printfFormat = printfFormat;
        this.initVal = initVal;
    }

    @Override
    public String toString() {
        return type;
    }

    String getPrintfFormat() {
        return printfFormat;
    }

    String getInitVal() {
        return initVal;
    }
}
