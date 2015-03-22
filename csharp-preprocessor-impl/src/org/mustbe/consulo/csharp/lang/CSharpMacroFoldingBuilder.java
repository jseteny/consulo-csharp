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

import java.util.ArrayDeque;
import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroRecursiveElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPreprocessorRegionDirectiveImpl;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import lombok.val;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class CSharpMacroFoldingBuilder implements FoldingBuilder
{
	@NotNull
	@Override
	public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode astNode, @NotNull Document document)
	{
		val foldingList = new ArrayList<FoldingDescriptor>();

		PsiElement psi = astNode.getPsi();

		val open = new ArrayDeque<CSharpPreprocessorRegionDirectiveImpl>();
		psi.accept(new CSharpMacroRecursiveElementVisitor()
		{
			@Override
			public void visitPreprocessorRegionDirective(CSharpPreprocessorRegionDirectiveImpl preprocessorDirective)
			{
				if(preprocessorDirective.isOpen())
				{
					String value = preprocessorDirective.getValue();
					if(value == null)
					{
						return;
					}
					open.add(preprocessorDirective);
				}
				else
				{
					CSharpPreprocessorRegionDirectiveImpl regionDirective = open.pollLast();
					if(regionDirective == null)
					{
						return;
					}

					PsiElement startElement = regionDirective.getStartElement();
					int endOffset = preprocessorDirective.getTextRange().getEndOffset();
					PsiElement endElement = preprocessorDirective.getEndElement();
					if(endElement != null)
					{
						endOffset -= endElement.getTextLength();
					}
					foldingList.add(new FoldingDescriptor(regionDirective, new TextRange(startElement.getTextRange().getStartOffset(), endOffset)));
				}
			}
		});
		return foldingList.toArray(new FoldingDescriptor[foldingList.size()]);
	}

	@Nullable
	@Override
	public String getPlaceholderText(@NotNull ASTNode astNode)
	{
		PsiElement psi = astNode.getPsi();

		if(psi instanceof CSharpPreprocessorRegionDirectiveImpl)
		{
			return ((CSharpPreprocessorRegionDirectiveImpl) psi).getValue();
		}
		return null;
	}

	@Override
	public boolean isCollapsedByDefault(@NotNull ASTNode astNode)
	{
		PsiElement psi = astNode.getPsi();


		return false;
	}
}
