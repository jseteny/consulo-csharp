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
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.lazy.CSharpLazyArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpWithIntValueStub;
import org.mustbe.consulo.dotnet.psi.DotNetArrayType;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;

/**
 * @author VISTALL
 * @since 13.12.13.
 */
public class CSharpArrayTypeImpl extends CSharpStubElementImpl<CSharpWithIntValueStub<CSharpArrayTypeImpl>> implements DotNetArrayType
{
	public CSharpArrayTypeImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpArrayTypeImpl(@NotNull CSharpWithIntValueStub<CSharpArrayTypeImpl> stub, @NotNull IStubElementType<? extends CSharpWithIntValueStub<CSharpArrayTypeImpl>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef()
	{
		DotNetType innerType = getInnerType();

		return new CSharpLazyArrayTypeRef(this, innerType.toTypeRef(), getDimensions());
	}

	public int getDimensions()
	{
		CSharpWithIntValueStub<CSharpArrayTypeImpl> stub = getStub();
		if(stub != null)
		{
			return stub.getValue();
		}
		return findChildrenByType(CSharpTokenSets.COMMA).size();
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitArrayType(this);
	}

	@NotNull
	@Override
	public DotNetType getInnerType()
	{
		return getRequiredStubOrPsiChildByIndex(CSharpStubElements.TYPE_SET, 0);
	}
}
