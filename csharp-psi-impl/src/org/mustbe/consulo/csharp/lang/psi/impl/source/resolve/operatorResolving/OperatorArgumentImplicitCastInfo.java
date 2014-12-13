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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.operatorResolving;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Key;

/**
 * @author VISTALL
 * @since 13.12.14
 */
public class OperatorArgumentImplicitCastInfo
{
	public static final Key<OperatorArgumentImplicitCastInfo> IMPLICIT_CAST_INFO = Key.create("implicit.to_type.ref");

	private final DotNetTypeRef myFromTypeRef;
	private final DotNetTypeRef myToTypeRef;

	public OperatorArgumentImplicitCastInfo(@NotNull DotNetTypeRef fromTypeRef, @NotNull DotNetTypeRef toTypeRef)
	{
		myFromTypeRef = fromTypeRef;
		myToTypeRef = toTypeRef;
	}

	@NotNull
	public DotNetTypeRef getFromTypeRef()
	{
		return myFromTypeRef;
	}

	@NotNull
	public DotNetTypeRef getToTypeRef()
	{
		return myToTypeRef;
	}
}
