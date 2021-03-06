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

package org.mustbe.consulo.csharp.ide.codeInsight.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefUtil;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 01.07.14
 */
public class MethodGenerateUtil
{
	@Nullable
	public static String getDefaultValueForType(@NotNull DotNetTypeRef typeRef, @NotNull PsiElement scope)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve(scope);

		if(typeResolveResult.isNullable())
		{
			return "null";
		}
		else
		{
			if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Void))
			{
				return null;
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Byte))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.SByte))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Int16))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.UInt16))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Int32))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.UInt32))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Int64))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.UInt64))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Decimal))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Single))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Double))
			{
				return "0";
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, scope, DotNetTypes.System.Boolean))
			{
				return "false";
			}
			return "default(" + CSharpTypeRefPresentationUtil.buildShortText(typeRef, scope) + ")";
		}
	}
}
