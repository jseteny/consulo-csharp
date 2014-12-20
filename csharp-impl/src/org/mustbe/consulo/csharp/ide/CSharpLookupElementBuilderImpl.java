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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.consulo.ide.eap.EarlyAccessProgramDescriptor;
import org.consulo.ide.eap.EarlyAccessProgramManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.completion.util.LtGtInsertHandler;
import org.mustbe.consulo.csharp.lang.psi.CSharpEventDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroDefine;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDefStatement;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpXXXAccessorOwner;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLabeledStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.ide.DotNetElementPresentationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeUtil;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.proximity.PsiProximityComparator;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpLookupElementBuilderImpl extends CSharpLookupElementBuilder
{
	public static class PropertyAndEventCompletionNew extends EarlyAccessProgramDescriptor
	{
		@NotNull
		@Override
		public String getName()
		{
			return "C#: property & event accessor completion";
		}

		@NotNull
		@Override
		public String getDescription()
		{
			return "";
		}

		@Override
		public boolean isRestartRequired()
		{
			return true;
		}
	}

	public static final boolean PROPERTY_AND_EVENT_ACCESSOR_COMPLETION = EarlyAccessProgramManager.is(PropertyAndEventCompletionNew.class);

	@NotNull
	@Override
	public LookupElement[] buildToLookupElements(@Nullable PsiElement sender, @NotNull PsiElement[] arguments)
	{
		if(arguments.length == 0)
		{
			return LookupElement.EMPTY_ARRAY;
		}
		if(sender != null)
		{
			Arrays.sort(arguments, new PsiProximityComparator(sender));
		}

		List<LookupElement> list = new ArrayList<LookupElement>(arguments.length);
		for(PsiElement argument : arguments)
		{
			ContainerUtil.addIfNotNull(list, buildLookupElementImpl(argument));
		}
		return list.toArray(new LookupElement[list.size()]);
	}

	@NotNull
	@Override
	public LookupElement[] buildToLookupElements(@Nullable PsiElement sender, @NotNull ResolveResult[] arguments)
	{
		if(arguments.length == 0)
		{
			return LookupElement.EMPTY_ARRAY;
		}

		//FIXME [VISTALL] sorter?

		List<LookupElement> list = new ArrayList<LookupElement>(arguments.length);
		for(ResolveResult argument : arguments)
		{
			PsiElement element = argument.getElement();

			if((element instanceof CSharpPropertyDeclaration || element instanceof CSharpEventDeclaration) && PROPERTY_AND_EVENT_ACCESSOR_COMPLETION)
			{
				for(DotNetXXXAccessor accessor : ((CSharpXXXAccessorOwner) element).getAccessors())
				{
					ContainerUtil.addIfNotNull(list, buildLookupElementImpl(accessor));
				}
			}
			else
			{
				ContainerUtil.addIfNotNull(list, buildLookupElementImpl(element));
			}
		}
		return list.toArray(new LookupElement[list.size()]);
	}

	@NotNull
	@Override
	@SuppressWarnings("unchecked")
	public LookupElement[] buildToLookupElements(@Nullable PsiElement sender, @NotNull Collection<? extends PsiElement> arguments)
	{
		if(arguments.isEmpty())
		{
			return LookupElement.EMPTY_ARRAY;
		}
		List<? extends PsiElement> elements;
		if(arguments instanceof List)
		{
			elements = (List<? extends PsiElement>) arguments;
		}
		else
		{
			elements = new ArrayList<PsiElement>(arguments);
		}

		if(sender != null)
		{
			Collections.sort(elements, new PsiProximityComparator(sender));
		}

		List<LookupElement> list = new ArrayList<LookupElement>(elements.size());
		for(PsiElement element : elements)
		{
			ContainerUtil.addIfNotNull(list, buildLookupElementImpl(element));
		}

		return list.toArray(new LookupElement[list.size()]);
	}

	@Nullable
	public LookupElement buildLookupElementImpl(PsiElement element)
	{
		LookupElementBuilder builder = buildLookupElement(element);
		if(builder == null)
		{
			return null;
		}

		if(DotNetAttributeUtil.hasAttribute(element, DotNetTypes.System.ObsoleteAttribute))
		{
			builder = builder.withStrikeoutness(true);

			return PrioritizedLookupElement.withPriority(builder, -1.0);
		}

		return builder;
	}

	@Nullable
	@Override
	public LookupElementBuilder buildLookupElement(final PsiElement element)
	{
		LookupElementBuilder builder = null;
		if(element instanceof CSharpMethodDeclaration)
		{
			final CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) element;

			builder = LookupElementBuilder.create(methodDeclaration);
			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));

			if(!methodDeclaration.isDelegate())
			{
				final DotNetTypeRef[] parameterTypes = methodDeclaration.getParameterTypeRefs();

				String genericText = DotNetElementPresentationUtil.formatGenericParameters((DotNetGenericParameterListOwner) element);

				String parameterText = genericText + "(" + StringUtil.join(parameterTypes, new Function<DotNetTypeRef, String>()
				{
					@Override
					public String fun(DotNetTypeRef parameter)
					{
						return CSharpTypeRefPresentationUtil.buildShortText(parameter, element);
					}
				}, ", ") + ")";

				builder = builder.withInsertHandler(ParenthesesInsertHandler.getInstance(parameterTypes.length > 0));

				if(CSharpMethodImplUtil.isExtensionWrapper(methodDeclaration))
				{
					builder = builder.withItemTextUnderlined(true);
				}
				builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(methodDeclaration.getReturnTypeRef(), element));
				builder = builder.withTailText(parameterText, false);
			}
			else
			{
				builder = builder.withTailText(DotNetElementPresentationUtil.formatGenericParameters((DotNetGenericParameterListOwner) element),
						true);

				builder = withGenericInsertHandler(element, builder);
			}
		}
		else if(element instanceof DotNetXXXAccessor)
		{
			DotNetNamedElement parent = (DotNetNamedElement) element.getParent();

			DotNetXXXAccessor.Kind accessorKind = ((DotNetXXXAccessor) element).getAccessorKind();
			if(accessorKind == null)
			{
				return null;
			}
			String ownerName = parent.getName();
			if(ownerName == null)
			{
				return null;
			}
			String accessorPrefix = accessorKind.name().toLowerCase();
			builder = LookupElementBuilder.create(element, ownerName);
			builder = builder.withPresentableText(accessorPrefix + "::" + parent.getName());
			builder = builder.withLookupString(accessorPrefix + "::" + parent.getName());
			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(parent, Iconable.ICON_FLAG_VISIBILITY));

			if(parent instanceof DotNetVariable)
			{
				builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(((DotNetVariable) parent).toTypeRef(true), parent));
			}
			switch(accessorKind)
			{
				case SET:
					builder = builder.withTailText(" = ;", true);
					builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
					{
						@Override
						public void handleInsert(InsertionContext context, LookupElement item)
						{
							int offset = context.getEditor().getCaretModel().getOffset();
							context.getDocument().insertString(offset, " = ;");
							context.getEditor().getCaretModel().moveToOffset(offset + 3);
						}
					});
					break;
				case ADD:
					builder = builder.withTailText(" += ;", true);
					builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
					{
						@Override
						public void handleInsert(InsertionContext context, LookupElement item)
						{
							int offset = context.getEditor().getCaretModel().getOffset();
							context.getDocument().insertString(offset, " += ;");
							context.getEditor().getCaretModel().moveToOffset(offset + 4);
						}
					});
					break;
				case REMOVE:
					builder = builder.withTailText(" -= ;", true);
					builder = builder.withInsertHandler(new InsertHandler<LookupElement>()
					{
						@Override
						public void handleInsert(InsertionContext context, LookupElement item)
						{
							int offset = context.getEditor().getCaretModel().getOffset();
							context.getDocument().insertString(offset, " -= ;");
							context.getEditor().getCaretModel().moveToOffset(offset + 4);
						}
					});
					break;
			}
		}
		else if(element instanceof DotNetNamespaceAsElement)
		{
			DotNetNamespaceAsElement namespaceAsElement = (DotNetNamespaceAsElement) element;
			String name = namespaceAsElement.getName();
			if(name == null)
			{
				return null;
			}
			builder = LookupElementBuilder.create(name);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));
		}
		else if(element instanceof CSharpTypeDefStatement)
		{
			CSharpTypeDefStatement typeDefStatement = (CSharpTypeDefStatement) element;
			String name = typeDefStatement.getName();
			if(name == null)
			{
				return null;
			}
			builder = LookupElementBuilder.create(name);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));
			builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(typeDefStatement.toTypeRef(), typeDefStatement));
		}
		else if(element instanceof CSharpLabeledStatementImpl)
		{
			CSharpLabeledStatementImpl labeledStatement = (CSharpLabeledStatementImpl) element;
			String name = labeledStatement.getName();
			if(name == null)
			{
				return null;
			}
			builder = LookupElementBuilder.create(name);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));
		}
		else if(element instanceof DotNetGenericParameter)
		{
			DotNetGenericParameter typeDefStatement = (DotNetGenericParameter) element;
			String name = typeDefStatement.getName();
			if(name == null)
			{
				return null;
			}
			builder = LookupElementBuilder.create(name);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));
		}
		else if(element instanceof DotNetVariable)
		{
			DotNetVariable dotNetVariable = (DotNetVariable) element;
			builder = LookupElementBuilder.create(dotNetVariable);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));

			builder = builder.withTypeText(CSharpTypeRefPresentationUtil.buildShortText(dotNetVariable.toTypeRef(true), dotNetVariable));
		}
		else if(element instanceof CSharpMacroDefine)
		{
			builder = LookupElementBuilder.create((CSharpMacroDefine) element);
			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));
		}
		else if(element instanceof CSharpTypeDeclaration)
		{
			CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) element;
			builder = LookupElementBuilder.create(typeDeclaration);

			builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));

			builder = builder.withTypeText(typeDeclaration.getPresentableParentQName());

			builder = builder.withTailText(DotNetElementPresentationUtil.formatGenericParameters(typeDeclaration), true);

			builder = withGenericInsertHandler(element, builder);
		}

		return builder;
	}

	private static LookupElementBuilder withGenericInsertHandler(PsiElement element, LookupElementBuilder builder)
	{
		if(!(element instanceof DotNetGenericParameterListOwner))
		{
			return builder;
		}

		int genericParametersCount = ((DotNetGenericParameterListOwner) element).getGenericParametersCount();
		if(genericParametersCount == 0)
		{
			return builder;
		}

		builder = builder.withInsertHandler(LtGtInsertHandler.getInstance(true));
		return builder;
	}
}
