package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightMethodDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightParameterBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightTypeDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import com.intellij.openapi.project.Project;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class CSharpLambdaResolveResultUtil
{
	@NotNull
	public static CSharpTypeDeclaration createTypeFromDelegate(@NotNull CSharpMethodDeclaration declaration)
	{
		Project project = declaration.getProject();

		CSharpLightTypeDeclarationBuilder builder = new CSharpLightTypeDeclarationBuilder(project);
		builder.withParentQName(declaration.getPresentableParentQName());
		builder.withName(declaration.getName());
		builder.setNavigationElement(declaration);

		builder.putUserData(CSharpResolveUtil.DELEGATE_METHOD_TYPE, declaration);

		builder.addExtendType(new CSharpTypeRefByQName(DotNetTypes.System.MulticastDelegate));

		for(DotNetGenericParameter parameter : declaration.getGenericParameters())
		{
			builder.addGenericParameter(parameter);
		}

		CSharpLightMethodDeclarationBuilder invokeMethodBuilder = new CSharpLightMethodDeclarationBuilder(project);
		invokeMethodBuilder.withName("Invoke");
		invokeMethodBuilder.addModifier(CSharpModifier.PUBLIC);
		invokeMethodBuilder.withReturnType(declaration.getReturnTypeRef());

		DotNetParameter[] parameters = declaration.getParameters();
		for(int i = 0; i < parameters.length; i++)
		{
			DotNetParameter parameter = parameters[i];
			CSharpLightParameterBuilder parameterBuilder = new CSharpLightParameterBuilder(project);
			String name = parameter.getName();
			if(name == null)
			{
				parameterBuilder.withName("p" + i);
			}
			else
			{
				parameterBuilder.withName(name);
			}
			parameterBuilder.withTypeRef(parameter.toTypeRef(true));
			invokeMethodBuilder.addParameter(parameterBuilder);
		}
		builder.addMember(invokeMethodBuilder);
		return builder;
	}
}
