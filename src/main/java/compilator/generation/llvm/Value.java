package compilator.generation.llvm;

class Value {
    String value;
    VarType type;

    Value(String value, VarType type) {
        this.value = value;
        this.type = type;
    }
}
