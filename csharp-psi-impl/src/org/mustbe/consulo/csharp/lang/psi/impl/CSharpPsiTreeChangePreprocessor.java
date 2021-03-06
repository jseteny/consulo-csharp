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
import org.mustbe.consulo.csharp.lang.psi.CSharpFile;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiTreeChangePreprocessorBase;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 19.12.13.
 */
public class CSharpPsiTreeChangePreprocessor extends PsiTreeChangePreprocessorBase
{
	public CSharpPsiTreeChangePreprocessor(@NotNull Project project)
	{
		super(project);
	}

	@Override
	protected boolean isMyFile(@NotNull PsiFile file)
	{
		return file instanceof CSharpFile;
	}

	@Override
	protected boolean isInsideCodeBlock(@Nullable PsiElement element)
	{
		return PsiTreeUtil.getParentOfType(element, DotNetStatement.class, false) != null;
	}
}
