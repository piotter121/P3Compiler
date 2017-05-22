grammar P3lang;

@parser::header {
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
}

@parser::members {
private void insertTokens(String fileName) {
    try {
        fileName = fileName.substring(1, fileName.length() - 1);
        CharStream input = CharStreams.fromStream(getClass().getResourceAsStream(fileName));
        CommonTokenStream thatStream = new CommonTokenStream(new P3langLexer(input));
        thatStream.fill();
        List extraTokens = thatStream.getTokens();
        extraTokens.remove(extraTokens.size() - 1); // remove EOF
        CommonTokenStream thisStream = (CommonTokenStream)this.getTokenStream();
        thisStream.getTokens().addAll(thisStream.index() + 1, extraTokens); // jumpo over insert statement NEWLINE
    } catch(IOException e) {
        e.printStackTrace();
    }
}
}

program: ( statement? NEWLINE )* EOF
    ;

statement: printStatement
         | assignStatement
         | insertCodeStatement
   ;

printStatement: PRINT value
    ;

assignStatement: ID '=' STRING
    ;

value: ID
     | STRING
   ;

insertCodeStatement: INSERT STRING { insertTokens($STRING.text); }
    ;

INSERT: 'insert'
    ;

PRINT:	'print'
   ;

STRING :  '"' ( ~('\\'|'"') )* '"'
    ;

ID:   ('a'..'z'|'A'..'Z')+
    ;

NEWLINE:	'\r'? '\n'
    ;

WS:   (' '|'\t')+ -> skip
    ;

LINE_COMMENT
    :   '#' ~[\r\n]* -> skip
    ;
