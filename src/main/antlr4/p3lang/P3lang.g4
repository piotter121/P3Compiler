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

program: ((statement|functionDefinition)? NEWLINE)*
       ;

block: (statement? NEWLINE)*
     ;

statement: PRINT ID #printStatement
         | READ ID #readStatement
         | ID '=' expr0 #assignStatement
         | INSERT STRING { insertTokens($STRING.text); } #insertCodeStatement
         | IF condition THEN ifBlock ENDIF #conditionalStatement
         | WHILE condition THEN loopBlock END #loopStatement
         | CALL ID '()' #functionCallStatement
         ;

functionDefinition: DEFINE ID ':' NEWLINE funblock END
                  ;

funblock: block
        ;

ifBlock: block
       ;

loopBlock: block
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

condition: expr0 EQUAL expr0 #equalCondition
         | expr0 NOT_EQUAL expr0 #notEqualCondition
         | expr0 LESSER_THAN expr0 #lessThanCondition
         | expr0 GREATER_THAN expr0 #greaterThanCondition
         | expr0 LESSER_OR_EQUAL_THAN expr0 #lessOrEqualCondition
         | expr0 GREATER_OR_EQUAL_THAN expr0 #greaterOrEqualCondition
         ;

CALL: 'call';

WHILE: 'while';

RET: 'return';

DEFINE: 'def';

END: 'end';

THEN: 'then';

ENDIF: 'endif';

EQUAL: '==';

NOT_EQUAL: '!=';

LESSER_THAN: '<';

GREATER_THAN: '>';

LESSER_OR_EQUAL_THAN: '<=';

GREATER_OR_EQUAL_THAN: '>=';

IF: 'if';

TOINT:  '(int)';

TOREAL: '(real)';

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