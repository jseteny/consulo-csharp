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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroTokens;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 22.03.2015
 */
public class CSharpPreprocessorRegionDirectiveImpl extends CSharpPreprocessorDirectiveImpl
{
	public CSharpPreprocessorRegionDirectiveImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public boolean isOpen()
	{
		return findChildByType(CSharpMacroTokens.REGION_KEYWORD) != null;
	}

	@Nullable
	public String getValue()
	{
		PsiElement childByType = findChildByType(CSharpMacroTokens.VALUE);
		if(childByType == null)
		{
			return null;
		}
		return childByType.getText().trim();
	}

	@Override
	public void accept(@NotNull CSharpMacroElementVisitor visitor)
	{
		visitor.visitPreprocessorRegionDirective(this);
	}
}
