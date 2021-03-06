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

package org.mustbe.consulo.csharp.ide;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import com.intellij.ide.actions.QualifiedNameProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 19.01.14
 */
public class CSharpQualifiedNameProvider implements QualifiedNameProvider
{
	@Nullable
	@Override
	public PsiElement adjustElementToCopy(PsiElement element)
	{
		return element;
	}

	@Nullable
	@Override
	public String getQualifiedName(PsiElement element)
	{
		if(element instanceof DotNetQualifiedElement)
		{
			return ((DotNetQualifiedElement) element).getPresentableQName();
		}
		return null;
	}

	@Nullable
	@Override
	public PsiElement qualifiedNameToElement(String s, Project project)
	{
		return null;
	}

	@Override
	public void insertQualifiedName(String s, PsiElement element, Editor editor, Project project)
	{
		editor.getDocument().insertString(editor.getCaretModel().getOffset(), s);
	}
}
