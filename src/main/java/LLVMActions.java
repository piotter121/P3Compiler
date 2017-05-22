import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by piotr on 08.05.17.
 */
public class LLVMActions extends P3langBaseListener {

    private HashMap<String, String> memory = new HashMap<>();
    private String value;
    private final String outputFileName;

    LLVMActions(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    @Override
    public void exitAssignStatement(P3langParser.AssignStatementContext ctx) {
        String tmp = ctx.STRING().getText();
        tmp = tmp.substring(1, tmp.length() - 1);
        memory.put(ctx.ID().getText(), tmp);
    }

    @Override
    public void exitProgram(P3langParser.ProgramContext ctx) {
        String generatedProgram = LLVMGenerator.generate();
        String userDir = System.getProperty("user.dir");
        try (FileWriter writer = new FileWriter(userDir + File.separator + outputFileName)) {
            writer.write(generatedProgram);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exitValue(P3langParser.ValueContext ctx) {
        if (ctx.ID() != null) {
            value = memory.get(ctx.ID().getText());
        }
        if (ctx.STRING() != null) {
            String tmp = ctx.STRING().getText();
            value = tmp.substring(1, tmp.length() - 1);
        }
    }

    @Override
    public void exitPrintStatement(P3langParser.PrintStatementContext ctx) {
        LLVMGenerator.print(value);
    }

}

