grammar P3lang;

@parser::header {
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
}

@parser::members {
private void insertTokens(String fileName) {
    try {
        fileName = fileName.substring(1, fileName.length() - 1);
        File file = new File(fileName);
        InputStream additionalFile = new FileInputStream(file.getAbsolutePath());
        CharStream input = CharStreams.fromStream(additionalFile);
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

statement: PRINT ID #printStatement
         | READ ID #readStatement
         | ID '=' expr0 #assignStatement
         | INSERT STRING { insertTokens($STRING.text); } #insertCodeStatement
   ;

expr0:  expr1			#single0
      | expr1 ADD expr1		#add
      | expr1 SUB expr1 #subs
    ;

expr1:  expr2			#single1
      | expr2 MULT expr2	#mult
      | expr2 DIV expr2 #div
    ;

expr2: ID #var
     | INT #int
     | REAL #real
     | TOINT expr2 #toint
     | TOREAL expr2 #toreal
     | '(' expr0 ')' #par
     | STRING #string
   ;

TOINT:  '(int)'
    ;

TOREAL: '(real)'
    ;

ADD: '+';

SUB: '-';

MULT: '*';

DIV: '/';

INSERT: 'insert';

PRINT:	'print';

READ: 'read';

STRING :  '"' ( ~('\\'|'"') )* '"' ;

INT: '0'..'9'+ ;

REAL: '0'..'9'+'.''0'..'9'+ ;

ID:   ('a'..'z'|'A'..'Z')+ ;

NEWLINE:	'\r'? '\n' ;

WS:   (' '|'\t')+ -> skip ;

LINE_COMMENT:   '#' ~[\r\n]* -> skip ;