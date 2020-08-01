package com.gengoai.config;

import com.gengoai.parsing.ParseException;
import com.gengoai.parsing.ParserToken;


%%

%class ConfigScanner
%public
%unicode
%type com.gengoai.parsing.ParserToken
%function next
%pack
%char
%line
%yylexthrow{
    com.gengoai.parsing.ParseException
%yylexthrow}


string = (\\\"|[^\"])*
safestring = [_a-zA-Z]+([_a-zA-Z0-9\.]+)*
comment = \#[^\r\n]*
number = -?(0|[1-9][0-9]*)(\.[0-9]+)?([eE][+-]?[0-9]+)?

%%

"@type" { return new ParserToken(ConfigTokenType.KEY, yytext(),yychar);}
"@constructor" { return new ParserToken(ConfigTokenType.KEY,yytext(), yychar);}
"@import" { return new ParserToken(ConfigTokenType.IMPORT,yytext(), yychar);}
"true"|"false" { return  new ParserToken(ConfigTokenType.BOOLEAN,yytext(), yychar);}
"null" { return new ParserToken(ConfigTokenType.NULL,null,yychar);}
{comment} { return new ParserToken(ConfigTokenType.COMMENT,yytext(), yychar); }
"," { return new ParserToken(ConfigTokenType.VALUE_SEPARATOR,yytext(), yychar); }
// {map_operator} { return new ParserToken(yytext(), ConfigTokenType.MAP);}
"[" { return new ParserToken(ConfigTokenType.BEGIN_ARRAY,yytext(), yychar); }
"]" { return new ParserToken(ConfigTokenType.END_ARRAY,yytext(), yychar); }
"@{"{safestring}"}" { return new ParserToken(ConfigTokenType.BEAN,yytext(), yychar); }
"{" { return new ParserToken(ConfigTokenType.BEGIN_OBJECT,yytext(), yychar);}
"}" { return new ParserToken(ConfigTokenType.END_OBJECT,yytext(), yychar);}
":" { return new ParserToken(ConfigTokenType.KEY_VALUE_SEPARATOR,yytext(), yychar);}
"=" { return new ParserToken(ConfigTokenType.EQUAL_PROPERTY,yytext(), yychar);}
"+=" { return new ParserToken(ConfigTokenType.APPEND_PROPERTY,yytext(), yychar);}
\"{string}\" { return new ParserToken(ConfigTokenType.STRING,yytext().substring(1,yytext().length()-1),yychar);}
{safestring} { return new ParserToken(ConfigTokenType.KEY,yytext(), yychar);}
{number} { return new ParserToken(ConfigTokenType.STRING,yytext(), yychar);}

[ \t\r\n\f] { /* ignore white space. */ }
. { throw new ParseException("Illegal character: "+yytext()+"\" at line: " + yyline + " char offset: " + yychar + " state: " + yystate()); }