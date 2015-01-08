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

package org.mustbe.consulo.csharp.lang.psi.msil;

import org.consulo.lombok.annotations.ModuleService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 08.01.15
 */
@ModuleService
public abstract class MsilToCSharpManager
{
	private static final MsilToCSharpManager ERROR_MANAGER = new MsilToCSharpManager()
	{
		@NotNull
		@Override
		public PsiElement wrap(@NotNull PsiElement msilElement)
		{
			return msilElement;
		}

		@NotNull
		@Override
		public PsiElement wrap(@NotNull PsiElement msilElement, @Nullable PsiElement parent)
		{
			return msilElement;
		}

		@NotNull
		@Override
		public DotNetTypeRef extractToCSharp(@NotNull DotNetTypeRef typeRef, @NotNull PsiElement scope)
		{
			return typeRef;
		}
	};

	@NotNull
	public static MsilToCSharpManager getInstance(@NotNull PsiElement element)
	{
		Module moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(element);
		if(moduleForPsiElement == null)
		{
			return ERROR_MANAGER;
		}
		return getInstance(moduleForPsiElement);
	}

	@NotNull
	public abstract PsiElement wrap(@NotNull PsiElement msilElement);

	@NotNull
	public abstract PsiElement wrap(@NotNull PsiElement msilElement, @Nullable PsiElement parent);

	@NotNull
	public abstract DotNetTypeRef extractToCSharp(@NotNull DotNetTypeRef typeRef, @NotNull PsiElement scope);
}
