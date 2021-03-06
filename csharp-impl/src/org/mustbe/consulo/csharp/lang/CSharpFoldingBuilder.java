/*
 * Copyright 2013-2014 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mustbe.consulo.csharp.lang;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpBodyWithBraces;
import org.mustbe.consulo.csharp.lang.psi.CSharpEventDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpRecursiveElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokensImpl;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingListChild;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUsingListImpl;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import com.intellij.codeInsight.folding.CodeFoldingSettings;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class CSharpFoldingBuilder extends CustomFoldingBuilder
{
	@Override
	protected void buildLanguageFoldRegions(@NotNull final List<FoldingDescriptor> descriptors,
			@NotNull PsiElement root,
			@NotNull Document document,
			boolean quick)
	{
		root.accept(new CSharpRecursiveElementVisitor()
		{
			@Override
			public void visitTypeDeclaration(CSharpTypeDeclaration declaration)
			{
				super.visitTypeDeclaration(declaration);

				addBodyWithBraces(descriptors, declaration);
			}

			@Override
			public void visitPropertyDeclaration(CSharpPropertyDeclaration declaration)
			{
				super.visitPropertyDeclaration(declaration);

				addBodyWithBraces(descriptors, declaration);
			}

			@Override
			public void visitEventDeclaration(CSharpEventDeclaration declaration)
			{
				super.visitEventDeclaration(declaration);

				addBodyWithBraces(descriptors, declaration);
			}

			@Override
			public void visitUsingNamespaceList(CSharpUsingListImpl list)
			{
				CSharpUsingListChild[] statements = list.getStatements();
				if(statements.length <= 1)
				{
					return;
				}

				CSharpUsingListChild statement = statements[0];
				PsiElement refElement = statement.getReferenceElement();
				if(refElement == null)
				{
					return;
				}

				ASTNode usingKeyword = statement.getNode().findChildByType(CSharpTokens.USING_KEYWORD);

				assert usingKeyword != null;

				int startOffset = usingKeyword.getTextRange().getEndOffset() + 1;
				int endOffset = statements[statements.length - 1].getLastChild().getTextRange().getEndOffset();

				descriptors.add(new FoldingDescriptor(list, new TextRange(startOffset, endOffset)));
			}

			@Override
			public void visitBlockStatement(CSharpBlockStatementImpl statement)
			{
				super.visitBlockStatement(statement);

				if(!(statement.getParent() instanceof DotNetLikeMethodDeclaration))
				{
					return;
				}
				addBodyWithBraces(descriptors, statement);
			}

			@Override
			public void visitComment(PsiComment comment)
			{
				PsiFile containingFile = comment.getContainingFile();

				if(containingFile == null)
				{
					return;
				}

				if(containingFile.getFirstChild() == comment)
				{
					TextRange textRange = comment.getTextRange();
					int startOffset = textRange.getStartOffset();

					PsiElement lastComment = findLastComment(comment, comment.getTokenType());

					descriptors.add(new FoldingDescriptor(comment, new TextRange(startOffset, lastComment.getTextRange().getEndOffset())));
				}
			}
		});
	}

	@Override
	protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range)
	{
		PsiElement psi = node.getPsi();
		if(psi instanceof CSharpUsingListImpl)
		{
			return "...";
		}
		else if(psi instanceof CSharpBlockStatementImpl || psi instanceof CSharpPropertyDeclaration || psi instanceof CSharpTypeDeclaration || psi
				instanceof CSharpEventDeclaration)
		{
			return "{...}";
		}
		else if(psi instanceof PsiComment)
		{
			IElementType tokenType = ((PsiComment) psi).getTokenType();
			if(tokenType == CSharpTokensImpl.LINE_DOC_COMMENT)
			{
				return "/// ...";
			}
			else if(tokenType == CSharpTokens.LINE_COMMENT)
			{
				return "// ...";
			}
			else if(tokenType == CSharpTokens.BLOCK_COMMENT)
			{
				return "/** ... */";
			}
		}
		return null;
	}

	private static PsiElement findLastComment(PsiElement element, IElementType elementType)
	{
		PsiElement target = element;
		PsiElement nextSibling = element.getNextSibling();
		while(nextSibling != null)
		{
			if(isAcceptableComment(nextSibling, elementType))
			{
				if(nextSibling instanceof PsiWhiteSpace)
				{
					target = nextSibling.getPrevSibling();
				}
				else
				{
					target = nextSibling;
				}
				nextSibling = nextSibling.getNextSibling();
			}
			else
			{
				return target;
			}
		}
		return element;
	}

	private static boolean isAcceptableComment(PsiElement nextSibling, IElementType elementType)
	{
		if(nextSibling == null)
		{
			return false;
		}
		return nextSibling instanceof PsiWhiteSpace || (nextSibling instanceof PsiComment && ((PsiComment) nextSibling).getTokenType() ==
				elementType);
	}

	private static void addBodyWithBraces(List<FoldingDescriptor> list, CSharpBodyWithBraces bodyWithBraces)
	{
		PsiElement leftBrace = bodyWithBraces.getLeftBrace();
		PsiElement rightBrace = bodyWithBraces.getRightBrace();
		if(leftBrace == null || rightBrace == null)
		{
			return;
		}

		list.add(new FoldingDescriptor(bodyWithBraces, new TextRange(leftBrace.getTextRange().getStartOffset(),
				rightBrace.getTextRange().getStartOffset() + rightBrace.getTextLength())));
	}

	@Override
	protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node)
	{
		PsiElement psi = node.getPsi();
		if(psi instanceof CSharpUsingListImpl)
		{
			return true;
		}
		else if(psi instanceof PsiComment)
		{
			return CodeFoldingSettings.getInstance().COLLAPSE_FILE_HEADER;
		}

		return false;
	}
}
