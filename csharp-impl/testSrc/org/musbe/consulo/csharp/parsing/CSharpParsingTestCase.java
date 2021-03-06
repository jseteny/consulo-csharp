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

package org.musbe.consulo.csharp.parsing;

import java.lang.reflect.Method;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpLanguageVersionHelper;
import org.mustbe.consulo.testFramework.ParsingTestCase;
import com.intellij.lang.LanguageVersion;
import com.intellij.openapi.fileTypes.FileType;

/**
 * @author VISTALL
 * @since 22.05.2015
 */
public abstract class CSharpParsingTestCase extends ParsingTestCase
{
	public CSharpParsingTestCase(@NonNls @NotNull String dataPath)
	{
		super(dataPath, "cs");
	}

	@Override
	protected boolean checkAllPsiRoots()
	{
		return false;
	}

	@NotNull
	@Override
	public LanguageVersion<?> resolveLanguageVersion(@NotNull FileType fileType)
	{
		String name = getName();
		try
		{
			Method declaredMethod = getClass().getDeclaredMethod(name);
			SetLanguageVersion annotation = declaredMethod.getAnnotation(SetLanguageVersion.class);
			if(annotation != null)
			{
				return CSharpLanguageVersionHelper.getInstance().getWrapper(annotation.version());
			}
			else
			{
				throw new IllegalArgumentException("Missed @SetLanguageVersion");
			}
		}
		catch(NoSuchMethodException e)
		{
			throw new Error(e);
		}
	}

	@Override
	protected boolean shouldContainTempFiles()
	{
		return false;
	}

	@Override
	protected String getTestDataPath()
	{
		return "/csharp-impl/testData";
	}
}
