package compilator.generation.llvm;

enum Scope {
    LOCAL("%"), GLOBAL("@");

    private final String prefix;

    Scope(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return prefix;
    }
}
