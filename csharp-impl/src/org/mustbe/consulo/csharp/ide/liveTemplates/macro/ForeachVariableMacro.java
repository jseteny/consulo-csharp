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

package org.mustbe.consulo.csharp.ide.liveTemplates.macro;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.psi.PsiElement;
import com.intellij.util.SmartList;
import lombok.val;

/**
 * @author VISTALL
 * @since 11.06.14
 */
public class ForeachVariableMacro extends VariableTypeMacroBase
{
	@Nullable
	@Override
	protected PsiElement[] getVariables(Expression[] params, ExpressionContext context)
	{
		val psiElementAtStartOffset = context.getPsiElementAtStartOffset();
		if(psiElementAtStartOffset == null)
		{
			return PsiElement.EMPTY_ARRAY;
		}

		List<DotNetVariable> variables = CSharpLiveTemplateMacroUtil.resolveAllVariables(context.getPsiElementAtStartOffset());

		List<DotNetVariable> list = new SmartList<DotNetVariable>();
		for(DotNetVariable variable : variables)
		{
			DotNetTypeRef elementTypeRef = CSharpReferenceExpressionImplUtil.toTypeRef(variable);

			DotNetTypeRef iterableTypeRef = CSharpResolveUtil.resolveIterableType(psiElementAtStartOffset, elementTypeRef);

			if(iterableTypeRef == DotNetTypeRef.ERROR_TYPE)
			{
				continue;
			}
			list.add(variable);
		}
		return list.toArray(new PsiElement[list.size()]);
	}

	@Override
	public String getName()
	{
		return "csharpForeachVariable";
	}

	@Override
	public String getPresentableName()
	{
		return "foreach variable";
	}
}
