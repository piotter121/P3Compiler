package compilator.generation.llvm;

public enum ConditionalOperand {
    EQUAL("eq"), NOT_EQUAL("ne"), GREATER_THAN("sgt"), GREATER_OR_EQUAL("sge"), LESS_THAN("slt"), LESS_OR_EQUAL("sle");

    private final String operand;

    ConditionalOperand(String operand) {
        this.operand = operand;
    }


    @Override
    public String toString() {
        return operand;
    }
}
