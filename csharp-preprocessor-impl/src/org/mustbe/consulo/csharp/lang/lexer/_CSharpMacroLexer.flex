package org.mustbe.consulo.csharp.lang.lexer;

import java.util.*;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroTokens;

%%

%class _CSharpMacroLexer
%extends LexerBase
%unicode
%function advanceImpl
%type IElementType
%eof{  return;
%eof}

%state WAIT_DIRECTIVE
%state MACRO_SIMPLE_VALUE
%state MACRO_EXPRESSION_VALUE

IDENTIFIER=[:jletter:] [:jletterdigit:]*

MACRO_WHITE_SPACE=[ \t\f\n]+

CHARP_FRAGMENT=([^\r\n\u2028\u2029\u000B\u000C\u0085!(\#)])+

MACRO_VALUE_UNTIL_NEW_LINE=([^\r\n\u2028\u2029\u000B\u000C\u0085!(\n)])+
%%

<WAIT_DIRECTIVE>
{
	"if"                 { yybegin(MACRO_EXPRESSION_VALUE); return CSharpMacroTokens.IF_KEYWORD; }

	"elif"               { yybegin(MACRO_EXPRESSION_VALUE); return CSharpMacroTokens.ELIF_KEYWORD; }

	"else"               { yybegin(MACRO_SIMPLE_VALUE); return CSharpMacroTokens.ELSE_KEYWORD; }

	"endif"              { yybegin(MACRO_SIMPLE_VALUE); return CSharpMacroTokens.ENDIF_KEYWORD; }

	"define"             { yybegin(MACRO_EXPRESSION_VALUE); return CSharpMacroTokens.DEFINE_KEYWORD; }

	"undef"              { yybegin(MACRO_EXPRESSION_VALUE); return CSharpMacroTokens.UNDEF_KEYWORD; }

	"region"             { yybegin(MACRO_SIMPLE_VALUE); return CSharpMacroTokens.REGION_KEYWORD; }

	"endregion"          { yybegin(MACRO_SIMPLE_VALUE); return CSharpMacroTokens.ENDREGION_KEYWORD; }

	"pragma"             { yybegin(MACRO_SIMPLE_VALUE); return CSharpMacroTokens.PRAGMA_KEYWORD; }

	\n                   { yybegin(YYINITIAL); return CSharpMacroTokens.DIRECTIVE_END; }

	{MACRO_WHITE_SPACE}  {  return CSharpMacroTokens.WHITE_SPACE; }

	.                    { return CSharpMacroTokens.VALUE; }

}

<MACRO_SIMPLE_VALUE>
{
	{MACRO_VALUE_UNTIL_NEW_LINE}   { return CSharpMacroTokens.VALUE; }

	\n                   { yybegin(YYINITIAL); return CSharpMacroTokens.DIRECTIVE_END; }
}

<MACRO_EXPRESSION_VALUE>
{
	"("                  { return CSharpMacroTokens.LPAR; }

	")"                  { return CSharpMacroTokens.RPAR; }

	"!"                  { return CSharpMacroTokens.EXCL; }

	"&&"                 { return CSharpMacroTokens.ANDAND; }

	"||"                 { return CSharpMacroTokens.OROR; }

	{IDENTIFIER}         { return CSharpMacroTokens.IDENTIFIER; }

	\n                   { yybegin(YYINITIAL); return CSharpMacroTokens.DIRECTIVE_END; }

	{MACRO_WHITE_SPACE}  { return CSharpMacroTokens.WHITE_SPACE; }

	.                    { return CSharpMacroTokens.BAD_CHARACTER; }
}

<YYINITIAL>
{
	"#"                  { yybegin(WAIT_DIRECTIVE);return CSharpMacroTokens.DIRECTIVE_START; }

	{CHARP_FRAGMENT}     { return CSharpMacroTokens.CSHARP_FRAGMENT; }

	{MACRO_WHITE_SPACE}  { return CSharpMacroTokens.CSHARP_FRAGMENT; }
}
