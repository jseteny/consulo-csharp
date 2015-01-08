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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import java.util.Map;

import org.jboss.netty.util.internal.ConcurrentWeakKeyHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpDelegateMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpPointerTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromGenericParameter;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.lazy.CSharpLazyGenericWrapperTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.lazy.CSharpLazyLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.lazy.CSharpLazyTypeRefByQName;
import org.mustbe.consulo.csharp.lang.psi.msil.MsilToCSharpManager;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetInheritUtil;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericWrapperTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetPointerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetRefTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.msil.lang.psi.MsilClassEntry;
import org.mustbe.consulo.msil.lang.psi.MsilEntry;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import org.mustbe.consulo.msil.lang.psi.impl.type.MsilArrayTypRefImpl;
import org.mustbe.consulo.msil.lang.psi.impl.type.MsilNativeTypeRefImpl;
import com.intellij.ProjectTopics;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootAdapter;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 08.01.15
 */
public class MsilToCSharpManagerImpl extends MsilToCSharpManager
{
	private Map<MsilEntry, PsiElement> myCache = new ConcurrentWeakKeyHashMap<MsilEntry, PsiElement>();

	public MsilToCSharpManagerImpl(Module module)
	{
		module.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootAdapter()
		{
			@Override
			public void rootsChanged(ModuleRootEvent moduleRootEvent)
			{
				myCache.clear();
			}
		});
	}

	@NotNull
	@Override
	public PsiElement wrap(@NotNull PsiElement msilElement)
	{
		return wrap(msilElement, null);
	}

	@NotNull
	@Override
	public PsiElement wrap(@NotNull PsiElement element, @Nullable PsiElement parent)
	{
		if(element instanceof MsilClassEntry)
		{
			PsiElement cache = myCache.get(element);
			if(cache != null)
			{
				return cache;
			}

			cache = wrapToDelegateMethod((DotNetTypeDeclaration) element, parent);
			if(cache == null)
			{
				cache = new MsilClassAsCSharpTypeDefinition(this, parent, (MsilClassEntry) element);
			}
			myCache.put((MsilClassEntry) element, cache);
			return cache;
		}
		return element;
	}

	@NotNull
	@Override
	public DotNetTypeRef extractToCSharp(@NotNull DotNetTypeRef typeRef, @NotNull PsiElement scope)
	{
		if(typeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		if(typeRef instanceof MsilNativeTypeRefImpl)
		{
			return new CSharpLazyTypeRefByQName(scope, typeRef.getQualifiedText());
		}
		else if(typeRef instanceof MsilArrayTypRefImpl)
		{
			int[] lowerValues = ((MsilArrayTypRefImpl) typeRef).getLowerValues();
			return new CSharpArrayTypeRef(extractToCSharp(((MsilArrayTypRefImpl) typeRef).getInnerTypeRef(), scope),
					lowerValues.length == 0 ? 0 : lowerValues.length - 1);
		}
		else if(typeRef instanceof DotNetPointerTypeRef)
		{
			return new CSharpPointerTypeRef(extractToCSharp(((DotNetPointerTypeRef) typeRef).getInnerTypeRef(), scope));
		}
		else if(typeRef instanceof DotNetRefTypeRef)
		{
			return new CSharpRefTypeRef(CSharpRefTypeRef.Type.ref, extractToCSharp(((DotNetRefTypeRef) typeRef).getInnerTypeRef(), scope));
		}
		else if(typeRef instanceof DotNetGenericWrapperTypeRef)
		{
			DotNetTypeRef innerTypeRef = ((DotNetGenericWrapperTypeRef) typeRef).getInnerTypeRef();
			DotNetTypeRef[] arguments = ((DotNetGenericWrapperTypeRef) typeRef).getArgumentTypeRefs();

			val inner = extractToCSharp(innerTypeRef, scope);
			DotNetTypeRef[] newArguments = new DotNetTypeRef[arguments.length];
			for(int i = 0; i < newArguments.length; i++)
			{
				newArguments[i] = extractToCSharp(arguments[i], scope);
			}

			return new CSharpLazyGenericWrapperTypeRef(scope, inner, newArguments);
		}

		PsiElement resolve = typeRef.resolve(scope).getElement();
		if(resolve instanceof DotNetTypeDeclaration)
		{
			CSharpDelegateMethodDeclaration delegateMethod = wrapToDelegateMethod((DotNetTypeDeclaration) resolve, null);
			if(delegateMethod != null)
			{
				return new CSharpLazyLambdaTypeRef(scope, delegateMethod);
			}
		}
		else if(resolve instanceof DotNetGenericParameter)
		{
			return new CSharpTypeRefFromGenericParameter(new MsilGenericParameterAsCSharpGenericParameter(this, null,
					(DotNetGenericParameter) resolve));
		}
		return new MsilDelegateTypeRef(this, scope, typeRef);
	}

	@Nullable
	public CSharpDelegateMethodDeclaration wrapToDelegateMethod(@NotNull DotNetTypeDeclaration typeDeclaration, @Nullable PsiElement parent)
	{
		if(DotNetInheritUtil.isInheritor(typeDeclaration, DotNetTypes.System.MulticastDelegate, true))
		{
			val msilMethodEntry = (MsilMethodEntry) ContainerUtil.find((typeDeclaration).getMembers(), new Condition<DotNetNamedElement>()
			{
				@Override
				public boolean value(DotNetNamedElement element)
				{
					return element instanceof MsilMethodEntry && Comparing.equal(element.getName(), "Invoke");
				}
			});

			assert msilMethodEntry != null : typeDeclaration.getPresentableQName();

			return new MsilClassAsCSharpDelegateMethodDeclaration(this, parent, typeDeclaration, msilMethodEntry);
		}
		else
		{
			return null;
		}
	}
}
