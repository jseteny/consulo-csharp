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

package org.mustbe.consulo.csharp.lang.psi.impl.partial;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintList;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDeclarationImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;

/**
 * @author VISTALL
 * @since 01.05.2015
 */
public class CSharpCompositeTypeDeclaration extends LightElement implements CSharpTypeDeclaration
{
	@NotNull
	public static PsiElement[] wrapPartialTypes(@NotNull GlobalSearchScope scope, @NotNull Project project, @NotNull PsiElement[] psiElements)
	{
		MultiMap<String, CSharpTypeDeclaration> partialTypes = null;

		List<PsiElement> newElementList = null;

		for(int i = 0; i < psiElements.length; i++)
		{
			PsiElement psiElement = psiElements[i];
			if(psiElement instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) psiElement).hasModifier(CSharpModifier.PARTIAL))
			{
				String vmQName = ((CSharpTypeDeclaration) psiElement).getVmQName();
				if(vmQName != null)
				{
					if(partialTypes == null)
					{
						partialTypes = MultiMap.create();
					}

					if(newElementList == null)
					{
						newElementList = new ArrayList<PsiElement>(psiElements.length);
						// we need copy head to new list
						newElementList.addAll(Arrays.asList(psiElements).subList(0, i));
					}

					partialTypes.putValue(vmQName, (CSharpTypeDeclaration) psiElement);
					continue;
				}
			}

			if(newElementList != null)
			{
				newElementList.add(psiElement);
			}
		}

		if(partialTypes == null)
		{
			return psiElements;
		}

		for(Map.Entry<String, Collection<CSharpTypeDeclaration>> entry : partialTypes.entrySet())
		{
			Collection<CSharpTypeDeclaration> value = entry.getValue();
			// partial modifier is useless, only one class with name
			if(value.size() == 1)
			{
				newElementList.add(value.iterator().next());
			}
			else
			{
				CSharpTypeDeclaration compositeType = CSharpPartialElementManager.getInstance(project).getOrCreateCompositeType(scope, entry.getKey
						(), value);

				newElementList.add(compositeType);
			}
		}
		return ContainerUtil.toArray(newElementList, PsiElement.ARRAY_FACTORY);
	}

	private CSharpTypeDeclaration[] myTypeDeclarations;

	public CSharpCompositeTypeDeclaration(@NotNull CSharpTypeDeclaration[] typeDeclarations)
	{
		super(typeDeclarations[0].getManager(), typeDeclarations[0].getLanguage());
		myTypeDeclarations = typeDeclarations;
	}

	@Override
	public <T> T getUserData(@NotNull Key<T> key)
	{
		if(key == ModuleUtilCore.KEY_MODULE)
		{
			//noinspection unchecked
			return (T) ModuleUtilCore.findModuleForPsiElement(myTypeDeclarations[0]);
		}
		return super.getUserData(key);
	}

	@Override
	public PsiElement getLeftBrace()
	{
		return null;
	}

	@Override
	public PsiElement getRightBrace()
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpGenericConstraintList getGenericConstraintList()
	{
		return null;
	}

	@NotNull
	@Override
	public CSharpGenericConstraint[] getGenericConstraints()
	{
		return new CSharpGenericConstraint[0];
	}

	@Override
	public boolean canNavigate()
	{
		return true;
	}

	@Override
	public void navigate(boolean requestFocus)
	{
		((Navigatable) myTypeDeclarations[0]).navigate(requestFocus);

	}

	@Override
	public boolean isInterface()
	{
		return false;
	}

	@Override
	public boolean isStruct()
	{
		return false;
	}

	@Override
	public boolean isEnum()
	{
		return false;
	}

	@Override
	public boolean isNested()
	{
		return false;
	}

	@Nullable
	@Override
	public DotNetTypeList getExtendList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		List<DotNetTypeRef> extendTypeRefs = new SmartList<DotNetTypeRef>();
		for(DotNetTypeDeclaration type : myTypeDeclarations)
		{
			DotNetTypeList extendList = type.getExtendList();
			if(extendList != null)
			{
				DotNetTypeRef[] typeRefs = extendList.getTypeRefs();
				Collections.addAll(extendTypeRefs, typeRefs);
			}
		}

		if(extendTypeRefs.isEmpty())
		{
			Set<String> set = new THashSet<String>();
			for(DotNetTypeDeclaration type : myTypeDeclarations)
			{
				ContainerUtil.addIfNotNull(set, CSharpTypeDeclarationImplUtil.getDefaultSuperType(type));
			}

			if(set.contains(DotNetTypes.System.ValueType))
			{
				extendTypeRefs.add(new CSharpTypeRefByQName(DotNetTypes.System.ValueType));
			}
			else
			{
				extendTypeRefs.add(new CSharpTypeRefByQName(DotNetTypes.System.Object));
			}
		}
		return ContainerUtil.toArray(extendTypeRefs, DotNetTypeRef.ARRAY_FACTORY);
	}

	@Override
	public boolean isInheritor(@NotNull DotNetTypeDeclaration typeDeclaration, boolean b)
	{
		for(CSharpTypeDeclaration declaration : myTypeDeclarations)
		{
			if(declaration.isInheritor(typeDeclaration, b))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public DotNetTypeRef getTypeRefForEnumConstants()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return myTypeDeclarations[0].getName();
	}

	@Nullable
	@Override
	public String getVmQName()
	{
		return myTypeDeclarations[0].getVmQName();
	}

	@Nullable
	@Override
	public String getVmName()
	{
		return myTypeDeclarations[0].getVmName();
	}

	@Nullable
	@Override
	public DotNetGenericParameterList getGenericParameterList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetGenericParameter[] getGenericParameters()
	{
		return new DotNetGenericParameter[0];
	}

	@Override
	public int getGenericParametersCount()
	{
		return 0;
	}

	@NotNull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		List<DotNetNamedElement> elements = new ArrayList<DotNetNamedElement>();
		for(CSharpTypeDeclaration typeDeclaration : myTypeDeclarations)
		{
			Collections.addAll(elements, typeDeclaration.getMembers());
		}
		return ContainerUtil.toArray(elements, DotNetNamedElement.ARRAY_FACTORY);
	}

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		// composite type dont hold partial type
		if(modifier == CSharpModifier.PARTIAL)
		{
			return false;
		}
		for(CSharpTypeDeclaration typeDeclaration : myTypeDeclarations)
		{
			if(typeDeclaration.hasModifier(modifier))
			{
				return true;
			}
		}
		return false;
	}

	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return null;
	}

	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return myTypeDeclarations[0].getPresentableParentQName();
	}

	@Nullable
	@Override
	public String getPresentableQName()
	{
		return myTypeDeclarations[0].getPresentableQName();
	}

	@Override
	public String toString()
	{
		return "CompositeTypeDeclaration: " + getVmQName();
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		for(CSharpTypeDeclaration typeDeclaration : myTypeDeclarations)
		{
			if(typeDeclaration.isEquivalentTo(another))
			{
				return true;
			}
		}
		return false;
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException
	{
		for(CSharpTypeDeclaration typeDeclaration : myTypeDeclarations)
		{
			typeDeclaration.setName(name);
		}
		return this;
	}

	@NotNull
	public CSharpTypeDeclaration[] getTypeDeclarations()
	{
		return myTypeDeclarations;
	}
}
