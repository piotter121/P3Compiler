import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * Created by piotr on 08.05.17.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        CharStream input = CharStreams.fromStream(Main.class.getResourceAsStream(args[0]));

        TokenSource lexer = new P3langLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        P3langParser parser = new P3langParser(tokens);

        ParseTree tree = parser.program();

        ParseTreeWalker walker = new ParseTreeWalker();
        LLVMActions llvmGenerator = new LLVMActions(args[1]);
        walker.walk(llvmGenerator, tree);
    }
}

