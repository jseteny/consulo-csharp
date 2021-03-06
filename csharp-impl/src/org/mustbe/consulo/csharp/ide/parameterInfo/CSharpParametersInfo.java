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

package org.mustbe.consulo.csharp.ide.parameterInfo;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethod;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UnfairTextRange;
import com.intellij.psi.PsiElement;
import com.intellij.xml.util.XmlStringUtil;

/**
 * @author VISTALL
 * @since 11.05.14
 */
public class CSharpParametersInfo
{
	private static final char[] ourBrackets = {
			'[',
			']'
	};
	private static final char[] ourParentheses = {
			'(',
			')'
	};

	private static final TextRange EMPTY = new UnfairTextRange(-1, -1);

	@NotNull
	public static CSharpParametersInfo build(@NotNull CSharpSimpleLikeMethod callable, @NotNull PsiElement scope)
	{
		CSharpSimpleParameterInfo[] parameters = callable.getParameterInfos();
		DotNetTypeRef returnType = callable.getReturnTypeRef();

		char[] bounds = callable instanceof CSharpArrayMethodDeclaration ? ourBrackets : ourParentheses;

		int length = 0;

		CSharpParametersInfo parametersInfo = new CSharpParametersInfo(parameters.length);
		if(CodeInsightSettings.getInstance().SHOW_FULL_SIGNATURES_IN_PARAMETER_INFO)
		{
			String textOfReturnType = CSharpTypeRefPresentationUtil.buildShortText(returnType, scope);
			parametersInfo.myBuilder.append(textOfReturnType);
			parametersInfo.myBuilder.append(" ").append(bounds[0]);

			length = XmlStringUtil.escapeString(textOfReturnType).length() + 2; // space and brace
		}

		if(parameters.length > 0)
		{
			for(int i = 0; i < parameters.length; i++)
			{
				if(i != 0)
				{
					length += parametersInfo.appendComma();
				}

				final int startOffset = length;
				CSharpSimpleParameterInfo parameter = parameters[i];
				length += parametersInfo.buildParameter(parameter, scope);
				parametersInfo.myParameterRanges[i] = new TextRange(startOffset, length);
			}
		}
		else
		{
			String text = "<no parameters>";
			parametersInfo.myBuilder.append(text);
			parametersInfo.myParameterRanges[0] = new TextRange(length, length + XmlStringUtil.escapeString(text).length());
		}

		if(CodeInsightSettings.getInstance().SHOW_FULL_SIGNATURES_IN_PARAMETER_INFO)
		{
			parametersInfo.myBuilder.append(bounds[1]);
		}

		return parametersInfo;
	}

	private TextRange[] myParameterRanges;
	private int myParameterCount;
	private StringBuilder myBuilder = new StringBuilder();

	public CSharpParametersInfo(int count)
	{
		myParameterCount = count;
		myParameterRanges = new TextRange[myParameterCount == 0 ? 1 : myParameterCount];
	}

	private int buildParameter(@NotNull CSharpSimpleParameterInfo o, @NotNull PsiElement scope)
	{
		String text = CSharpTypeRefPresentationUtil.buildShortText(o.getTypeRef(), scope);
		myBuilder.append(text);
		int nameOffset = 0;
		if(o.getTypeRef() != CSharpStaticTypeRef.__ARGLIST_TYPE)
		{
			myBuilder.append(" ");
			String notNullName = o.getNotNullName();
			myBuilder.append(notNullName);
			nameOffset = notNullName.length() + 1;
		}
		return XmlStringUtil.escapeString(text).length() + nameOffset;
	}

	@NotNull
	public TextRange getParameterRange(int i)
	{
		if(i == -1)
		{
			if(myParameterCount == 0)
			{
				return myParameterRanges[0];
			}
			return EMPTY;
		}
		TextRange textRange = ArrayUtil2.safeGet(myParameterRanges, i);
		return textRange == null ? EMPTY : textRange;
	}

	private int appendComma()
	{
		myBuilder.append(", ");
		return 2;
	}

	@NotNull
	public String getText()
	{
		return myBuilder.toString();
	}
}
