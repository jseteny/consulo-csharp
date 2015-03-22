/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.ide.completion;

import static com.intellij.patterns.StandardPatterns.psiElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroDefine;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightMacroDefine;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroReferenceExpressionImpl;
import org.mustbe.consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Iconable;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.HashMap;

/**
 * @author VISTALL
 * @since 29.01.15
 */
public class CSharpMacroReferenceCompletionContributor extends CompletionContributor
{
	public CSharpMacroReferenceCompletionContributor()
	{
		extend(CompletionType.BASIC, psiElement(CSharpMacroTokens.IDENTIFIER).withParent(CSharpMacroReferenceExpressionImpl.class),
				new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				CSharpMacroReferenceExpressionImpl expression = (CSharpMacroReferenceExpressionImpl) parameters.getPosition().getParent();
				Map<String, CSharpMacroDefine> map = new HashMap<String, CSharpMacroDefine>();

				DotNetSimpleModuleExtension<?> extension = ModuleUtilCore.getExtension(expression, DotNetSimpleModuleExtension.class);
				if(extension != null)
				{
					for(String varName : extension.getVariables())
					{
						map.put(varName, new CSharpLightMacroDefine(extension.getModule(), varName));
					}
				}

				for(CSharpMacroDefine macroDefine : ((CSharpMacroFileImpl) expression.getContainingFile()).getDefines())
				{
					String name = macroDefine.getName();
					if(name == null)
					{
						continue;
					}

					if(macroDefine.isUnDef())
					{
						map.remove(name);
					}
					else
					{
						map.put(name, macroDefine);
					}
				}


				result.addAllElements(buildToLookupElements(map.values()));
			}
		});
	}

	@NotNull
	private Collection<LookupElementBuilder> buildToLookupElements(Collection<CSharpMacroDefine> defines)
	{
		List<LookupElementBuilder> list = new ArrayList<LookupElementBuilder>(defines.size());
		for(CSharpMacroDefine define : defines)
		{
			LookupElementBuilder builder = LookupElementBuilder.create(define);
			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(define, Iconable.ICON_FLAG_VISIBILITY));
			list.add(builder);
		}
		return list;
	}
}
