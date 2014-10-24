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

package org.mustbe.consulo.csharp.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDefStatement;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpChameleonTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNullType;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetGenericWrapperTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefWithInnerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 08.01.14
 */
public class CSharpTypeUtil
{
	private static final String[] ourNumberRanks = new String[]{
			DotNetTypes.System.Byte,
			DotNetTypes.System.SByte,
			DotNetTypes.System.Int16,
			DotNetTypes.System.UInt16,
			DotNetTypes.System.Int32,
			DotNetTypes.System.UInt32,
			DotNetTypes.System.Int64,
			DotNetTypes.System.UInt64,
			DotNetTypes.System.Single,
			DotNetTypes.System.Double,
	};

	public static boolean isElementIsNullable(@NotNull PsiElement element)
	{
		if(element instanceof DotNetTypeDeclaration)
		{
			return !((DotNetTypeDeclaration) element).isStruct() && !((DotNetTypeDeclaration) element).isEnum();
		}
		return true;
	}

	public static int getNumberRank(DotNetTypeRef typeRef)
	{
		return ArrayUtil.find(ourNumberRanks, typeRef.getQualifiedText());
	}

	@Nullable
	public static Pair<DotNetTypeDeclaration, DotNetGenericExtractor> findTypeInSuper(@NotNull DotNetTypeRef typeRef, @NotNull String vmQName,
			@NotNull PsiElement scope)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve(scope);
		PsiElement resolve = typeResolveResult.getElement();
		if(!(resolve instanceof DotNetTypeDeclaration))
		{
			return null;
		}

		String otherVQName = ((DotNetTypeDeclaration) resolve).getVmQName();
		if(vmQName.equals(otherVQName))
		{
			return Pair.create((DotNetTypeDeclaration)resolve, typeResolveResult.getGenericExtractor());
		}

		for(DotNetTypeRef superType : ((DotNetTypeDeclaration) resolve).getExtendTypeRefs())
		{
			Pair<DotNetTypeDeclaration, DotNetGenericExtractor> typeInSuper = findTypeInSuper(superType, vmQName, scope);
			if(typeInSuper != null)
			{
				return typeInSuper;
			}
		}
		return null;
	}
	/**
	 * We have expression
	 * int a = "test";
	 * <p/>
	 * "test" - string type, ill be 'target' parameter
	 * int - int type, ill 'top'
	 * return false due it not be casted
	 */
	public static boolean isInheritable(@NotNull DotNetTypeRef top, @NotNull DotNetTypeRef target, @NotNull PsiElement scope)
	{
		if(top == DotNetTypeRef.ERROR_TYPE || target == DotNetTypeRef.ERROR_TYPE)
		{
			return false;
		}

		if(top.equals(target))
		{
			return true;
		}

		if(target instanceof CSharpChameleonTypeRef)
		{
			target = ((CSharpChameleonTypeRef) target).doMirror(top);
		}

		int topRank = ArrayUtil.find(ourNumberRanks, top.getQualifiedText());
		int targetRank = ArrayUtil.find(ourNumberRanks, target.getQualifiedText());

		if(topRank != -1 && targetRank != -1)
		{
			if(targetRank <= topRank)
			{
				return true;
			}
		}

		if(target instanceof CSharpRefTypeRef && top instanceof CSharpRefTypeRef)
		{
			if(((CSharpRefTypeRef) target).getType() != ((CSharpRefTypeRef) top).getType())
			{
				return false;
			}
			return isInheritable(((CSharpRefTypeRef) top).getInnerTypeRef(), ((CSharpRefTypeRef) target).getInnerTypeRef(), scope);
		}

		if(target instanceof CSharpArrayTypeRef && top instanceof CSharpArrayTypeRef)
		{
			if(((CSharpArrayTypeRef) target).getDimensions() != ((CSharpArrayTypeRef) top).getDimensions())
			{
				return false;
			}
			return isInheritable(((CSharpArrayTypeRef) top).getInnerTypeRef(), ((CSharpArrayTypeRef) target).getInnerTypeRef(), scope);
		}

		DotNetTypeResolveResult topTypeResolveResult = top.resolve(scope);
		DotNetTypeResolveResult targetTypeResolveResult = target.resolve(scope);
		if(topTypeResolveResult instanceof CSharpLambdaResolveResult && targetTypeResolveResult instanceof CSharpLambdaResolveResult)
		{
			DotNetTypeRef[] targetParameters = ((CSharpLambdaResolveResult) targetTypeResolveResult).getParameterTypeRefs();
			DotNetTypeRef[] topParameters = ((CSharpLambdaResolveResult) topTypeResolveResult).getParameterTypeRefs();
			if(topParameters.length != targetParameters.length)
			{
				return false;
			}
			for(int i = 0; i < targetParameters.length; i++)
			{
				DotNetTypeRef targetParameter = targetParameters[i];
				DotNetTypeRef topParameter = topParameters[i];
				if(targetParameter == DotNetTypeRef.AUTO_TYPE)
				{
					continue;
				}
				if(!isInheritable(topParameter, targetParameter, scope))
				{
					return false;
				}
			}
			DotNetTypeRef targetReturnType = ((CSharpLambdaResolveResult) targetTypeResolveResult).getReturnTypeRef();
			DotNetTypeRef topReturnType = ((CSharpLambdaResolveResult) topTypeResolveResult).getReturnTypeRef();
			return targetReturnType == DotNetTypeRef.AUTO_TYPE || isInheritable(topReturnType, targetReturnType, scope);
		}

		PsiElement topElement = topTypeResolveResult.getElement();
		PsiElement targetElement = targetTypeResolveResult.getElement();

		if(topTypeResolveResult.isNullable() && target == CSharpNullType.INSTANCE)
		{
			return true;
		}

		if(!topTypeResolveResult.isNullable() && target == CSharpNullType.INSTANCE)
		{
			return false;
		}

		if(topElement != null && topElement.isEquivalentTo(targetElement))
		{
			return true;
		}

		if(topElement instanceof CSharpTypeDefStatement)
		{
			return isInheritable(((CSharpTypeDefStatement) topElement).toTypeRef(), target, scope);
		}

		if(topElement instanceof DotNetTypeDeclaration && targetElement instanceof DotNetTypeDeclaration)
		{
			return ((DotNetTypeDeclaration) targetElement).isInheritor((DotNetTypeDeclaration) topElement, true);
		}

		return false;
	}

	public static boolean haveErrorType(DotNetTypeRef typeRef)
	{
		if(typeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return true;
		}

		if(typeRef instanceof DotNetTypeRefWithInnerTypeRef)
		{
			if(typeRef instanceof DotNetGenericWrapperTypeRef)
			{
				for(DotNetTypeRef dotNetTypeRef : ((DotNetGenericWrapperTypeRef) typeRef).getArgumentTypeRefs())
				{
					if(haveErrorType(dotNetTypeRef))
					{
						return true;
					}
				}
			}
			return haveErrorType(((DotNetTypeRefWithInnerTypeRef) typeRef).getInnerTypeRef());
		}

		return false;
	}
}
