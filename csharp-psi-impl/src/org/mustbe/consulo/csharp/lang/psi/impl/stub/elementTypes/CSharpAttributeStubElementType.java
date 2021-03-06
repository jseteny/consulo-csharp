package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpStubAttributeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpWithStringValueStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 17.10.14
 */
public class CSharpAttributeStubElementType extends CSharpAbstractStubElementType<CSharpWithStringValueStub<CSharpAttribute>, CSharpAttribute>
{
	public CSharpAttributeStubElementType()
	{
		super("ATTRIBUTE");
	}

	@NotNull
	@Override
	public PsiElement createElement(@NotNull ASTNode astNode)
	{
		return new CSharpStubAttributeImpl(astNode);
	}

	@Override
	public CSharpAttribute createPsi(@NotNull CSharpWithStringValueStub<CSharpAttribute> stub)
	{
		return new CSharpStubAttributeImpl(stub, this);
	}

	@Override
	public CSharpWithStringValueStub<CSharpAttribute> createStub(@NotNull CSharpAttribute attribute, StubElement stubElement)
	{
		CSharpReferenceExpression referenceExpression = attribute.getReferenceExpression();
		String referenceText = referenceExpression == null ? null : referenceExpression.getText();
		return new CSharpWithStringValueStub<CSharpAttribute>(stubElement, this, referenceText);
	}

	@Override
	public void serialize(@NotNull CSharpWithStringValueStub stub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(stub.getReferenceText());
	}

	@NotNull
	@Override
	public CSharpWithStringValueStub<CSharpAttribute> deserialize(@NotNull StubInputStream stubInputStream,
			StubElement stubElement) throws IOException
	{
		StringRef referenceText = stubInputStream.readName();
		return new CSharpWithStringValueStub<CSharpAttribute>(stubElement, this, referenceText);
	}
}
