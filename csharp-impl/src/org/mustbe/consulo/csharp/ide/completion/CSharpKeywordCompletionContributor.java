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

package org.mustbe.consulo.csharp.ide.completion;

import static com.intellij.patterns.StandardPatterns.psiElement;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeVisitor;
import org.mustbe.consulo.csharp.ide.completion.util.SpaceInsertHandler;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpressionEx;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDeclarationImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByTypeDeclaration;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetUserType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.NotNullPairFunction;
import com.intellij.util.ProcessingContext;
import lombok.val;

/**
 * @author VISTALL
 * @since 07.01.14.
 */
public class CSharpKeywordCompletionContributor extends CompletionContributor
{
	private static final TokenSet ourExpressionLiterals = TokenSet.create(CSharpTokens.NULL_LITERAL, CSharpTokens.BOOL_LITERAL,
			CSharpTokens.DEFAULT_KEYWORD, CSharpTokens.TYPEOF_KEYWORD, CSharpTokens.SIZEOF_KEYWORD, CSharpTokens.THIS_KEYWORD,
			CSharpTokens.BASE_KEYWORD, CSharpSoftTokens.AWAIT_KEYWORD, CSharpTokens.NEW_KEYWORD, CSharpTokens.__MAKEREF_KEYWORD,
			CSharpTokens.__REFTYPE_KEYWORD, CSharpTokens.__REFVALUE_KEYWORD, CSharpSoftTokens.NAMEOF_KEYWORD);

	public CSharpKeywordCompletionContributor()
	{
		extend(CompletionType.BASIC, psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpReferenceExpressionEx.class),
				new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				val parent = (CSharpReferenceExpressionEx) parameters.getPosition().getParent();
				if(parent.getQualifier() == null && parent.kind() == CSharpReferenceExpression.ResolveToKind.ANY_MEMBER)
				{
					CSharpCompletionUtil.tokenSetToLookup(result, ourExpressionLiterals, new NotNullPairFunction<LookupElementBuilder, IElementType,
									LookupElement>()
							{
								@NotNull
								@Override
								public LookupElement fun(LookupElementBuilder t, IElementType elementType)
								{
									if(elementType == CSharpTokens.DEFAULT_KEYWORD ||
											elementType == CSharpTokens.TYPEOF_KEYWORD ||
											elementType == CSharpSoftTokens.NAMEOF_KEYWORD ||
											elementType == CSharpTokens.__MAKEREF_KEYWORD ||
											elementType == CSharpTokens.__REFTYPE_KEYWORD ||
											elementType == CSharpTokens.__REFVALUE_KEYWORD ||
											elementType == CSharpTokens.SIZEOF_KEYWORD)
									{
										t = t.withTailText("(...)", true);
										t = t.withInsertHandler(ParenthesesInsertHandler.getInstance(true));
									}
									else if(elementType == CSharpTokens.NEW_KEYWORD)
									{
										t = t.withInsertHandler(SpaceInsertHandler.INSTANCE);
									}

									List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeVisitor.findExpectedTypeRefs(parent);
									if(!expectedTypeRefs.isEmpty())
									{
										if(elementType == CSharpTokens.NULL_LITERAL)
										{
											for(ExpectedTypeInfo expectedTypeRef : expectedTypeRefs)
											{
												DotNetTypeResolveResult typeResolveResult = expectedTypeRef.getTypeRef().resolve(parent);
												if(typeResolveResult.isNullable())
												{
													return PrioritizedLookupElement.withPriority(t, CSharpCompletionUtil.EXPR_KEYWORD_PRIORITY);
												}
											}
										}
										else if(elementType == CSharpTokens.NEW_KEYWORD)
										{
											return PrioritizedLookupElement.withPriority(t, CSharpCompletionUtil.EXPR_KEYWORD_PRIORITY);
										}
										else
										{
											DotNetTypeRef typeRefForToken = typeRefFromTokeType(elementType, parent);
											if(typeRefForToken == null)
											{
												return t;
											}
											for(ExpectedTypeInfo expectedTypeRef : expectedTypeRefs)
											{
												if(CSharpTypeUtil.isInheritable(expectedTypeRef.getTypeRef(), typeRefForToken, parent))
												{
													return PrioritizedLookupElement.withPriority(t, CSharpCompletionUtil.EXPR_KEYWORD_PRIORITY);
												}
											}
										}
									}
									return t;
								}
							}, new Condition<IElementType>()
							{
								@Override
								public boolean value(IElementType elementType)
								{
									if(elementType == CSharpTokens.BASE_KEYWORD || elementType == CSharpTokens.THIS_KEYWORD)
									{
										val owner = (DotNetModifierListOwner) PsiTreeUtil.getParentOfType(parent, DotNetQualifiedElement.class);
										if(owner == null || owner.hasModifier(DotNetModifier.STATIC))
										{
											return false;
										}
									}
									if(elementType == CSharpSoftTokens.AWAIT_KEYWORD)
									{
										if(!CSharpModuleUtil.findLanguageVersion(parent).isAtLeast(CSharpLanguageVersion._5_0))
										{
											return false;
										}
									}
									if(elementType == CSharpSoftTokens.NAMEOF_KEYWORD)
									{
										if(!CSharpModuleUtil.findLanguageVersion(parent).isAtLeast(CSharpLanguageVersion._6_0))
										{
											return false;
										}
									}
									return true;
								}
							}
					);
				}
			}
		});

		extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement().withElementType(CSharpTokens.USING_KEYWORD)).inside
				(CSharpUsingNamespaceStatement.class), new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull final CompletionParameters parameters,
					ProcessingContext context,
					@NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.elementToLookup(result, CSharpTokens.STATIC_KEYWORD, null, new Condition<IElementType>()
				{
					@Override
					public boolean value(IElementType elementType)
					{
						return CSharpModuleUtil.findLanguageVersion(parameters.getPosition()).isAtLeast(CSharpLanguageVersion._6_0);
					}
				});
			}
		});

		extend(CompletionType.BASIC, psiElement().inside(DotNetGenericParameter.class), new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull final CompletionParameters parameters,
					ProcessingContext context,
					@NotNull CompletionResultSet result)
			{
				CSharpCompletionUtil.tokenSetToLookup(result, TokenSet.create(CSharpTokens.IN_KEYWORD, CSharpTokens.OUT_KEYWORD), null,
						new Condition<IElementType>()
				{
					@Override
					public boolean value(IElementType elementType)
					{
						DotNetQualifiedElement qualifiedElement = PsiTreeUtil.getParentOfType(parameters.getPosition(),
								CSharpMethodDeclaration.class, DotNetTypeDeclaration.class);
						if(qualifiedElement instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) qualifiedElement).isDelegate())
						{
							return true;
						}
						if(qualifiedElement instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) qualifiedElement).isInterface())
						{
							return true;
						}
						return false;
					}
				});
			}
		});

		extend(CompletionType.BASIC, psiElement(), new CompletionProvider<CompletionParameters>()
		{
			@Override
			protected void addCompletions(@NotNull CompletionParameters completionParameters,
					ProcessingContext processingContext,
					@NotNull CompletionResultSet completionResultSet)
			{
				val position = completionParameters.getPosition();
				if(position.getParent() instanceof DotNetReferenceExpression && position.getParent().getParent() instanceof DotNetUserType)
				{
					PsiElement parent1 = position.getParent().getParent();

					// dont allow inside statements
					DotNetStatement statementParent = PsiTreeUtil.getParentOfType(parent1, DotNetStatement.class);
					if(statementParent != null)
					{
						return;
					}

					PsiElement prevSibling = PsiTreeUtil.prevVisibleLeaf(parent1);
					if(prevSibling == null ||
							prevSibling.getNode().getElementType() == CSharpTokens.LBRACE ||
							prevSibling.getNode().getElementType() == CSharpTokens.RBRACE ||
							prevSibling.getNode().getElementType() == CSharpTokens.LPAR ||
							prevSibling.getNode().getElementType() == CSharpTokens.COMMA ||
							prevSibling.getNode().getElementType() == CSharpTokens.RBRACKET ||
							prevSibling.getNode().getElementType() == CSharpTokens.SEMICOLON ||
							CSharpTokenSets.MODIFIERS.contains(prevSibling.getNode().getElementType()))
					{
						val tokenVal = TokenSet.orSet(CSharpTokenSets.MODIFIERS, CSharpTokenSets.TYPE_DECLARATION_START,
								TokenSet.create(CSharpTokens.DELEGATE_KEYWORD, CSharpTokens.NAMESPACE_KEYWORD));

						CSharpCompletionUtil.tokenSetToLookup(completionResultSet, tokenVal, new NotNullPairFunction<LookupElementBuilder,
										IElementType, LookupElement>()

								{
									@NotNull
									@Override
									public LookupElement fun(LookupElementBuilder t, IElementType v)
									{
										t = t.withInsertHandler(SpaceInsertHandler.INSTANCE);
										return t;
									}
								}, new Condition<IElementType>()
								{
									@Override
									public boolean value(IElementType elementType)
									{
										if(elementType == CSharpTokens.IN_KEYWORD)
										{
											return false;
										}
										if(elementType == CSharpSoftTokens.ASYNC_KEYWORD)
										{
											if(CSharpModuleUtil.findLanguageVersion(position).isAtLeast(CSharpLanguageVersion._5_0))
											{
												return false;
											}
										}

										boolean isParameter = PsiTreeUtil.getParentOfType(position, DotNetParameter.class) != null;

										if(elementType == CSharpTokens.REF_KEYWORD || elementType == CSharpTokens.OUT_KEYWORD || elementType ==
												CSharpTokens.PARAMS_KEYWORD)
										{
											return isParameter;
										}

										return !isParameter;
									}
								}
						);
					}
				}
			}
		});
	}

	@Nullable
	private static DotNetTypeRef typeRefFromTokeType(@NotNull IElementType e, CSharpReferenceExpressionEx parent)
	{
		if(e == CSharpTokens.BOOL_LITERAL)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Boolean);
		}
		else if(e == CSharpTokens.TYPEOF_KEYWORD)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Type);
		}
		else if(e == CSharpSoftTokens.NAMEOF_KEYWORD)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.String);
		}
		else if(e == CSharpTokens.SIZEOF_KEYWORD)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Int32);
		}
		else if(e == CSharpTokens.__MAKEREF_KEYWORD)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.TypedReference);
		}
		else if(e == CSharpTokens.__REFTYPE_KEYWORD)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Type);
		}
		else if(e == CSharpTokens.THIS_KEYWORD)
		{
			DotNetTypeDeclaration thisTypeDeclaration = PsiTreeUtil.getParentOfType(parent, DotNetTypeDeclaration.class);
			if(thisTypeDeclaration != null)
			{
				return new CSharpTypeRefByTypeDeclaration(thisTypeDeclaration);
			}
		}
		else if(e == CSharpTokens.BASE_KEYWORD)
		{
			DotNetTypeDeclaration thisTypeDeclaration = PsiTreeUtil.getParentOfType(parent, DotNetTypeDeclaration.class);
			if(thisTypeDeclaration != null)
			{
				val pair = CSharpTypeDeclarationImplUtil.resolveBaseType(thisTypeDeclaration, parent);
				if(pair != null)
				{
					return new CSharpTypeRefByTypeDeclaration(pair.getFirst(), pair.getSecond());
				}
			}
		}
		return null;
	}
}
