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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpNullableType;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericWrapperTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpEmptyStub;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;

/**
 * @author VISTALL
 * @since 17.04.14
 */
public class CSharpStubNullableTypeImpl extends CSharpStubTypeElementImpl<CSharpEmptyStub<CSharpNullableType>> implements CSharpNullableType
{
	public CSharpStubNullableTypeImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpStubNullableTypeImpl(@NotNull CSharpEmptyStub<CSharpNullableType> stub,
			@NotNull IStubElementType<? extends CSharpEmptyStub<CSharpNullableType>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitNullableType(this);
	}

	@Override
	@NotNull
	public DotNetTypeRef toTypeRefImpl()
	{
		DotNetType innerType = getInnerType();
		if(innerType == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		return new CSharpGenericWrapperTypeRef(new CSharpTypeRefByQName(DotNetTypes.System.Nullable$1), innerType.toTypeRef());
	}

	@Override
	@Nullable
	public DotNetType getInnerType()
	{
		return getStubOrPsiChildByIndex(CSharpStubElements.TYPE_SET, 0);
	}

	@Override
	@NotNull
	public PsiElement getQuestElement()
	{
		return findNotNullChildByType(CSharpTokens.QUEST);
	}
}
