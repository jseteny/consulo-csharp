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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpNativeType;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 05.12.14
 */
public class CSharpNativeTypeImpl extends CSharpTypeElementImpl implements CSharpNativeType
{
	public CSharpNativeTypeImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitNativeType(this);
	}

	@Override
	@NotNull
	public IElementType getTypeElementType()
	{
		return getTypeElement().getNode().getElementType();
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRefImpl()
	{
		return CSharpNativeTypeImplUtil.toTypeRef(this);
	}

	@NotNull
	@Override
	public PsiElement getTypeElement()
	{
		return findNotNullChildByFilter(CSharpTokenSets.NATIVE_TYPES);
	}
}
