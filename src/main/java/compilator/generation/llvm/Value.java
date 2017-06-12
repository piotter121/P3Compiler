package compilator.generation.llvm;

class Value {
    final String value;
    final VarType type;

    Value(String value, VarType type) {
        this.value = value;
        this.type = type;
    }
}
