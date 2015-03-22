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

package org.mustbe.consulo.csharp.ide.highlight;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpFile;
import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorDefineDirective;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroReferenceExpressionImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;

/**
 * @author VISTALL
 * @since 06.02.14
 */
public class CSharpMacroHighlightVisitor extends CSharpMacroElementVisitor implements HighlightVisitor
{
	private HighlightInfoHolder myHighlightInfoHolder;

	@Override
	public boolean suitableForFile(@NotNull PsiFile psiFile)
	{
		return psiFile instanceof CSharpFile;
	}

	@Override
	public void visit(@NotNull PsiElement element)
	{
		element.accept(this);
	}

	@Override
	public boolean analyze(@NotNull PsiFile psiFile, boolean b, @NotNull HighlightInfoHolder highlightInfoHolder, @NotNull Runnable runnable)
	{
		myHighlightInfoHolder = highlightInfoHolder;
		runnable.run();
		return true;
	}

	@Override
	public void visitReferenceExpression(CSharpMacroReferenceExpressionImpl expression)
	{
		PsiElement resolve = expression.resolve();
		if(resolve != null)
		{
			highlightNamed(resolve, expression.getElement());
		}
	}

	@Override
	public void visitMacroDefine(CSharpPreprocessorDefineDirective directive)
	{
		if(directive.isUnDef())
		{
			return;
		}
		highlightNamed(directive, directive.getNameIdentifier());
	}

	public void highlightNamed(@NotNull PsiElement element, @Nullable PsiElement target)
	{
		if(element instanceof CSharpPreprocessorDefineDirective)
		{
			myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).textAttributes(CSharpPreprocessorHighlightKey
					.MACRO_VARIABLE).range(ObjectUtils.notNull(target, element)).create());
		}
	}

	@NotNull
	@Override
	public HighlightVisitor clone()
	{
		return new CSharpMacroHighlightVisitor();
	}

	@Override
	public int order()
	{
		return 0;
	}
}
