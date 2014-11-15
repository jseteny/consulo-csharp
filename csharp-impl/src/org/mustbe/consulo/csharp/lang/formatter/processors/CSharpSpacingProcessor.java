package org.mustbe.consulo.csharp.lang.formatter.processors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.formatting.ASTBlock;
import com.intellij.formatting.Spacing;
import com.intellij.lang.ASTNode;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class CSharpSpacingProcessor implements CSharpTokens, CSharpElements
{
	private final ASTNode myNode;
	private final CommonCodeStyleSettings myCodeStyleSettings;

	public CSharpSpacingProcessor(ASTNode node, CommonCodeStyleSettings codeStyleSettings)
	{
		myNode = node;
		myCodeStyleSettings = codeStyleSettings;
	}

	@Nullable
	public Spacing getSpacing(@Nullable ASTBlock child1, @NotNull ASTBlock child2)
	{
		IElementType elementType = myNode.getElementType();
		IElementType child2ElementType = child2.getNode().getElementType();
		if(child2ElementType == LBRACE || child2ElementType == BLOCK_STATEMENT)
		{
			int braceStyle = myCodeStyleSettings.BRACE_STYLE;
			if(elementType == TYPE_DECLARATION)
			{
				braceStyle = myCodeStyleSettings.CLASS_BRACE_STYLE;
			}
			else if(elementType == METHOD_DECLARATION ||
					elementType == CONSTRUCTOR_DECLARATION ||
					elementType == ARRAY_METHOD_DECLARATION)
			{
				braceStyle = myCodeStyleSettings.METHOD_BRACE_STYLE;
			}

			switch(braceStyle)
			{
				case CommonCodeStyleSettings.END_OF_LINE:
					return Spacing.createSafeSpacing(false, 1);
			}
			return null;
		}

		return null;
	}
}