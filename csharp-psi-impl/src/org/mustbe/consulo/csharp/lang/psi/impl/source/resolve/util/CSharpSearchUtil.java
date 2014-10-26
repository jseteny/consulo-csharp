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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import org.mustbe.consulo.dotnet.psi.DotNetMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetPropertyDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 18.05.14
 */
public class CSharpSearchUtil
{
	@Nullable
	public static DotNetPropertyDeclaration findPropertyByName(@NotNull final String name, @NotNull DotNetTypeRef typeRef, @NotNull PsiElement scope)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve(scope);
		PsiElement resolve = typeResolveResult.getElement();
		if(resolve == null)
		{
			return null;
		}
		DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();
		return findPropertyByName(name, resolve, genericExtractor);
	}

	@Nullable
	public static DotNetPropertyDeclaration findPropertyByName(@NotNull final String name,
			@NotNull PsiElement owner,
			@NotNull DotNetGenericExtractor extractor)
	{
		MemberResolveScopeProcessor memberResolveScopeProcessor = new MemberResolveScopeProcessor(owner.getResolveScope(),
				ResolveResult.EMPTY_ARRAY, new ExecuteTarget[]{ExecuteTarget.PROPERTY});

		ResolveState state = ResolveState.initial();
		state = state.put(CSharpResolveUtil.EXTRACTOR, extractor);
		state = state.put(CSharpResolveUtil.SELECTOR, new MemberByNameSelector(name));

		CSharpResolveUtil.walkChildren(memberResolveScopeProcessor, owner, false, null, state);

		PsiElement[] psiElements = memberResolveScopeProcessor.toPsiElements();
		return (DotNetPropertyDeclaration) ArrayUtil.getFirstElement(psiElements);
	}

	@Nullable
	public static DotNetMethodDeclaration findMethodByName(@NotNull final String name, @NotNull DotNetTypeRef typeRef, @NotNull PsiElement scope)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve(scope);
		PsiElement resolve = typeResolveResult.getElement();
		if(resolve == null)
		{
			return null;
		}
		return findMethodByName(name, resolve, typeResolveResult.getGenericExtractor());
	}

	@Nullable
	public static DotNetMethodDeclaration findMethodByName(@NotNull final String name,
			@NotNull PsiElement owner,
			@NotNull DotNetGenericExtractor extractor)
	{
		MemberResolveScopeProcessor memberResolveScopeProcessor = new MemberResolveScopeProcessor(owner.getResolveScope(),
				ResolveResult.EMPTY_ARRAY, new ExecuteTarget[]{ExecuteTarget.ELEMENT_GROUP});

		ResolveState state = ResolveState.initial();
		state = state.put(CSharpResolveUtil.EXTRACTOR, extractor);
		state = state.put(CSharpResolveUtil.SELECTOR, new MemberByNameSelector(name));

		CSharpResolveUtil.walkChildren(memberResolveScopeProcessor, owner, false, null, state);

		PsiElement[] psiElements = memberResolveScopeProcessor.toPsiElements();

		for(PsiElement psiElement : psiElements)
		{
			if(psiElement instanceof CSharpElementGroup)
			{
				for(PsiElement element : ((CSharpElementGroup<?>) psiElement).getElements())
				{
					//TODO [VISTALL] parameter handling
					if(element instanceof DotNetMethodDeclaration && ((DotNetMethodDeclaration) element).getParameters().length == 0)
					{
						return (DotNetMethodDeclaration) element;
					}
				}
			}
		}
		return null;
	}
}
