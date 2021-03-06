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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.CastNArgumentToTypeRefFix;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.CreateUnresolvedConstructorFix;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.CreateUnresolvedMethodFix;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpNewExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstructorSuperCallImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpOperatorReferenceImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetUserType;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixAction;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixActionRegistrarImpl;
import com.intellij.codeInsight.quickfix.UnresolvedReferenceQuickFixProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBColor;
import com.intellij.xml.util.XmlStringUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 19.11.14
 */
public class CC0001 extends CompilerCheck<CSharpReferenceExpression>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpReferenceExpression expression)
	{
		PsiElement referenceElement = expression.getReferenceElement();
		if(referenceElement == null || expression.isSoft())
		{
			return null;
		}

		return checkReference(expression, referenceElement);
	}

	@Nullable
	public static HighlightInfoFactory checkReference(@NotNull final PsiElement callElement, @NotNull PsiElement referenceElement)
	{
		ResolveResult[] resolveResults = ResolveResult.EMPTY_ARRAY;

		if(callElement instanceof PsiPolyVariantReference)
		{
			resolveResults = ((PsiPolyVariantReference) callElement).multiResolve(false);
		}

		ResolveResult goodResult = CSharpResolveUtil.findFirstValidResult(resolveResults);

		if(goodResult == null)
		{
			if(resolveResults.length == 0)
			{
				CompilerCheckBuilder result = new CompilerCheckBuilder()
				{
					@Nullable
					@Override
					public HighlightInfo create()
					{
						HighlightInfo highlightInfo = super.create();
						if(highlightInfo != null && callElement instanceof PsiReference)
						{
							UnresolvedReferenceQuickFixProvider.registerReferenceFixes((PsiReference) callElement,
									new QuickFixActionRegistrarImpl(highlightInfo));
						}
						return highlightInfo;
					}
				};
				result.setHighlightInfoType(HighlightInfoType.WRONG_REF);
				result.setText("'" + referenceElement.getText() + "' is not resolved");
				result.setTextRange(referenceElement.getTextRange());

				return result;
			}
			else
			{
				val highlightInfo = createHighlightInfo(callElement, resolveResults[0]);
				if(highlightInfo == null)
				{
					return null;
				}

				return new HighlightInfoFactory()
				{
					@Nullable
					@Override
					public HighlightInfo create()
					{
						return highlightInfo;
					}
				};
			}
		}
		return null;
	}

	@Nullable
	private static HighlightInfo createHighlightInfo(@NotNull PsiElement element, @NotNull ResolveResult resolveResult)
	{
		if(!(resolveResult instanceof MethodResolveResult))
		{
			return null;
		}

		PsiElement resolveElement = resolveResult.getElement();

		MethodCalcResult calcResult = ((MethodResolveResult) resolveResult).getCalcResult();
		List<NCallArgument> arguments = calcResult.getArguments();

		CSharpCallArgumentListOwner callOwner = findCallOwner(element);
		if(callOwner != null)
		{
			StringBuilder tooltipBuilder = new StringBuilder();
			tooltipBuilder.append("<b>");
			// sometimes name can be null
			if(element instanceof CSharpOperatorReferenceImpl)
			{
				String canonicalText = ((CSharpOperatorReferenceImpl) element).getCanonicalText();
				tooltipBuilder.append(XmlStringUtil.escapeString(canonicalText));
			}
			else
			{
				String name = ((PsiNamedElement) resolveElement).getName();
				tooltipBuilder.append(name);
			}
			tooltipBuilder.append("&#x9;(");

			if(resolveElement instanceof DotNetVariable)
			{
				DotNetTypeRef typeRef = ((DotNetVariable) resolveElement).toTypeRef(false);
				DotNetTypeResolveResult typeResolveResult = typeRef.resolve(element);
				if(!(typeResolveResult instanceof CSharpLambdaResolveResult))
				{
					return null;
				}
				DotNetTypeRef[] parameterTypes = ((CSharpLambdaResolveResult) typeResolveResult).getParameterTypeRefs();
				for(int i = 0; i < parameterTypes.length; i++)
				{
					if(i != 0)
					{
						tooltipBuilder.append(", ");
					}
					appendType(tooltipBuilder, parameterTypes[i], element);
				}
			}
			else if(resolveElement instanceof DotNetLikeMethodDeclaration)
			{
				DotNetParameter[] parameters = ((DotNetLikeMethodDeclaration) resolveElement).getParameters();
				for(int i = 0; i < parameters.length; i++)
				{
					if(i != 0)
					{
						tooltipBuilder.append(", ");
					}
					tooltipBuilder.append(parameters[i].getName()).append(" : ");
					appendType(tooltipBuilder, parameters[i].toTypeRef(false), element);
				}
			}
			tooltipBuilder.append(")</b> cannot be applied<br>");

			tooltipBuilder.append("to&#x9;<b>(");

			for(int i = 0; i < arguments.size(); i++)
			{
				if(i != 0)
				{
					tooltipBuilder.append(", ");
				}

				NCallArgument nCallArgument = arguments.get(i);

				if(!nCallArgument.isValid())
				{
					tooltipBuilder.append("<font color=\"").append(ColorUtil.toHex(JBColor.RED)).append("\">");
				}

				String parameterName = nCallArgument.getParameterName();
				if(parameterName != null)
				{
					tooltipBuilder.append(parameterName).append(" : ");
				}

				appendType(tooltipBuilder, nCallArgument.getTypeRef(), element);
				if(!nCallArgument.isValid())
				{
					tooltipBuilder.append("</font>");
				}
			}

			tooltipBuilder.append(")</b>");

			PsiElement parameterList = callOwner.getParameterList();
			if(parameterList == null)
			{
				parameterList = callOwner;
			}

			HighlightInfo.Builder builder = HighlightInfo.newHighlightInfo(HighlightInfoType.ERROR);
			builder = builder.description("");
			builder = builder.escapedToolTip(tooltipBuilder.toString());
			builder = builder.range(parameterList);

			HighlightInfo highlightInfo = builder.create();
			if(highlightInfo != null)
			{
				if(element instanceof CSharpReferenceExpression)
				{
					if(resolveElement instanceof CSharpMethodDeclaration)
					{
						QuickFixAction.registerQuickFixAction(highlightInfo, new CreateUnresolvedMethodFix((CSharpReferenceExpression) element));
					}

					if(resolveElement instanceof CSharpConstructorDeclaration)
					{
						QuickFixAction.registerQuickFixAction(highlightInfo, new CreateUnresolvedConstructorFix((CSharpReferenceExpression)
								element));
					}

					for(NCallArgument argument : arguments)
					{
						if(!argument.isValid())
						{
							CSharpCallArgument callArgument = argument.getCallArgument();
							if(callArgument == null)
							{
								continue;
							}
							DotNetExpression argumentExpression = callArgument.getArgumentExpression();
							if(argumentExpression == null)
							{
								continue;
							}
							DotNetTypeRef parameterTypeRef = argument.getParameterTypeRef();
							if(parameterTypeRef == null)
							{
								continue;
							}
							String parameterName = argument.getParameterName();
							if(parameterName == null)
							{
								continue;
							}
							QuickFixAction.registerQuickFixAction(highlightInfo, new CastNArgumentToTypeRefFix(argumentExpression, parameterTypeRef,
									parameterName));
						}
					}
				}
			}
			return highlightInfo;
		}
		return null;
	}


	private static void appendType(@NotNull StringBuilder builder, @NotNull DotNetTypeRef typeRef, @NotNull PsiElement scope)
	{
		builder.append(XmlStringUtil.escapeString(CSharpTypeRefPresentationUtil.buildText(typeRef, scope)));
	}

	private static CSharpCallArgumentListOwner findCallOwner(PsiElement element)
	{
		PsiElement parent = element.getParent();
		if(element instanceof CSharpOperatorReferenceImpl)
		{
			return (CSharpCallArgumentListOwner) element;
		}
		else if(parent instanceof CSharpMethodCallExpressionImpl || parent instanceof CSharpConstructorSuperCallImpl || parent instanceof
				CSharpAttribute)
		{
			return (CSharpCallArgumentListOwner) parent;
		}
		else if(parent instanceof DotNetUserType && parent.getParent() instanceof CSharpNewExpression)
		{
			return (CSharpCallArgumentListOwner) parent.getParent();
		}
		return null;
	}
}
