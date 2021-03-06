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

package org.mustbe.consulo.csharp.ide.actions.generate.memberChoose;

import javax.swing.JTree;

import org.consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.MemberChooserObject;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.ui.SimpleColoredComponent;

/**
 * @author VISTALL
 * @since 25.06.14
 */
@ArrayFactoryFields
public abstract class CSharpMemberChooseObject<T extends DotNetElement> implements MemberChooserObject, ClassMember
{
	protected T myDeclaration;

	public CSharpMemberChooseObject(T declaration)
	{
		myDeclaration = declaration;
	}

	@NotNull
	public T getDeclaration()
	{
		return myDeclaration;
	}

	@Override
	public void renderTreeNode(SimpleColoredComponent component, JTree tree)
	{
		component.setIcon(IconDescriptorUpdaters.getIconWithoutCache(myDeclaration, Iconable.ICON_FLAG_VISIBILITY));
		component.append(getPresentationText());
	}

	public abstract String getPresentationText();

	@Override
	public boolean equals(Object obj)
	{
		if(obj == null)
		{
			return false;
		}
		if(obj.getClass() != getClass())
		{
			return false;
		}
		return ((CSharpMemberChooseObject)obj).myDeclaration.isEquivalentTo(myDeclaration);
	}

	@Override
	public int hashCode()
	{
		return myDeclaration.hashCode() ^ getClass().hashCode();
	}

	@Override
	public MemberChooserObject getParentNodeDelegate()
	{
		final PsiElement parent = myDeclaration.getParent();
		if(parent == null)
		{
			return null;
		}
		return new ClassChooseObject((DotNetTypeDeclaration) parent);
	}
}
