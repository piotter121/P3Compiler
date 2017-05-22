/**
 * Created by piotr on 08.05.17.
 */
public class LLVMGenerator {

    private static String header_text = "";
    private static String main_text = "";
    private static int str_i = 0;

    static void print(String text) {
        int str_len = text.length();
        String str_type = "[" + (str_len + 2) + " x i8]";
        header_text += "@str" + str_i + " = constant" + str_type + " c\"" + text + "\\0A\\00\"\n";
        main_text += "%strp" + str_i + " = getelementptr " + str_type + "* @str" + str_i + ", i32 0, i32 0\n";
        main_text += "call i32 (i8*, ...)* @printf(i8* %strp" + str_i + ")\n";
        str_i++;
    }

    static String generate() {
        return "declare i32 @printf(i8*, ...)\n" +
                header_text +
                "define i32 @main() nounwind{\n" +
                main_text +
                "ret i32 0 }\n";
    }

}

