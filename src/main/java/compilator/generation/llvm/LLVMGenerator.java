package compilator.generation.llvm;

/**
 * Created by piotr on 08.05.17.
 */
class LLVMGenerator {

    int reg = 1;
    private String header_text = "";
    private String main_text = "";

    void printString(String id) {
        main_text += "%" + reg + " = load i8** %" + id + "\n";
        reg++;
        main_text +=
                "%" + reg + " = call i32 (i8*, ...)* @printf(i8* getelementptr inbounds ([4 x i8]* @strps, i32 0, i32 0), i8* %" + (reg - 1) + ")\n";
        reg++;
    }

    void printfI32(String id) {
        main_text += "%" + reg + " = load i32* %" + id + "\n";
        reg++;
        main_text +=
                "%" + reg + " = call i32 (i8*, ...)* @printf(i8* getelementptr inbounds ([4 x i8]* @strpi, i32 0, i32 0), i32 %" + (reg - 1) + ")\n";
        reg++;
    }

    void printfDouble(String id) {
        main_text += "%" + reg + " = load double* %" + id + "\n";
        reg++;
        main_text +=
                "%" + reg + " = call i32 (i8*, ...)* @printf(i8* getelementptr inbounds ([4 x i8]* @strpd, i32 0, i32 0), double %" + (reg - 1) + ")\n";
        reg++;
    }

    void declareI32(String id) {
        main_text += "%" + id + " = alloca i32\n";
    }

    void declareDouble(String id) {
        main_text += "%" + id + " = alloca double\n";
    }

    void assignI32(String id, String value) {
        main_text += "store i32 " + value + ", i32* %" + id + "\n";
    }

    void assignDouble(String id, String value) {
        main_text += "store double " + value + ", double* %" + id + "\n";
    }


    void load_i32(String id) {
        main_text += "%" + reg + " = load i32* %" + id + "\n";
        reg++;
    }

    void load_double(String id) {
        main_text += "%" + reg + " = load double* %" + id + "\n";
        reg++;
    }

    void addI32(String val1, String val2) {
        main_text += "%" + reg + " = add i32 " + val1 + ", " + val2 + "\n";
        reg++;
    }

    void addDouble(String val1, String val2) {
        main_text += "%" + reg + " = fadd double " + val1 + ", " + val2 + "\n";
        reg++;
    }

    void subI32(String val1, String val2) {
        main_text += "%" + reg + " = sub i32 " + val2 + ", " + val1 + "\n";
        reg++;
    }

    void subDouble(String val1, String val2) {
        main_text += "%" + reg + " = fsub double " + val2 + ", " + val1 + "\n";
        reg++;
    }

    void multI32(String val1, String val2) {
        main_text += "%" + reg + " = mul i32 " + val1 + ", " + val2 + "\n";
        reg++;
    }

    void multDouble(String val1, String val2) {
        main_text += "%" + reg + " = fmul double " + val1 + ", " + val2 + "\n";
        reg++;
    }

    void divI32(String val1, String val2) {
        main_text += "%" + reg + " = sdiv i32 " + val2 + ", " + val1 + "\n";
        reg++;
    }

    void divDouble(String val1, String val2) {
        main_text += "%" + reg + " = fdiv double " + val2 + ", " + val1 + "\n";
        reg++;
    }

    void sitofp(String id) {
        main_text += "%" + reg + " = sitofp i32 " + id + " to double\n";
        reg++;
    }

    void fptosi(String id) {
        main_text += "%" + reg + " = fptosi double " + id + " to i32\n";
        reg++;
    }


    String generate() {
        return "declare i32 @printf(i8*, ...)\n" +
                "declare i32 @__isoc99_scanf(i8*, ...)\n" +
                "@strpi = constant [4 x i8] c\"%d\\0A\\00\"\n" +
                "@strpd = constant [4 x i8] c\"%f\\0A\\00\"\n" +
                "@strps = constant [4 x i8] c\"%s\\0A\\00\"\n" +
                "@strsi =  constant [3 x i8] c\"%d\\00\"\n" +
                "@strsd = constant [4 x i8] c\"%lf\\00\"\n" +
                header_text +
                "define i32 @main() nounwind{\n" +
                main_text +
                "ret i32 0 \n}\n";
    }

    void declareString(String id) {
        main_text += "%" + id + " = alloca i8*";
    }

    void assignString(String id, String value) {
        header_text += "@str" + id + " = constant[" + (value.length() + 1) + " x i8] c\"" + value + "\\00\"\n";
        main_text +=
                "store i8* getelementptr inbounds ([" + (value.length() + 1) + " x i8]* @str" + id + ", i32 0, i32 0), i8** %" + id + "\n";
    }

    void scanfI32(String id) {
        main_text +=
                "%" + reg + " = call i32 (i8*, ...)* @scanf(i8* getelementptr inbounds ([3 x i8]* @strsi, i32 0, i32 0), i32* %" + id + ")\n";
        reg++;
    }

    void scanfDouble(String id) {
        main_text +=
                "%" + reg + " = call i32 (i8*, ...)* @scanf(i8* getelementptr inbounds ([4 x i8]* @strsd, i32 0, i32 0), double* %" + id + ")\n";
        reg++;
    }
}

