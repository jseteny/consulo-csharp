package org.mustbe.consulo.csharp.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.dotnet.lang.psi.impl.IndexBasedDotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.impl.IndexBasedDotNetPsiSearcher;
import com.intellij.openapi.module.Module;

/**
 * @author VISTALL
 * @since 23.09.14
 */
public class CSharpNamespaceAsElementImpl extends IndexBasedDotNetNamespaceAsElement
{
	public CSharpNamespaceAsElementImpl(@NotNull Module module,
			@NotNull String indexKey,
			@NotNull String qName,
			@NotNull IndexBasedDotNetPsiSearcher searcher)
	{
		super(module, CSharpLanguage.INSTANCE, indexKey, qName, searcher);
	}
}
