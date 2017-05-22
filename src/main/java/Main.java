import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by piotr on 08.05.17.
 */
public class Main {
    public static void main(String... args) throws Exception {
        File inputFile = new File(args[0]);
        InputStream inputFileStream = new FileInputStream(inputFile.getAbsolutePath());
        CharStream input = CharStreams.fromStream(inputFileStream);

        TokenSource lexer = new P3langLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        P3langParser parser = new P3langParser(tokens);

        ParseTree tree = parser.program();

        ParseTreeWalker walker = new ParseTreeWalker();
        LLVMActions llvmGenerator = new LLVMActions(args[1]);
        walker.walk(llvmGenerator, tree);
    }
}

