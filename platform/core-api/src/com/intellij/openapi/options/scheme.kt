/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package com.intellij.openapi.options

import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.WriteExternalException
import org.jdom.Parent

interface Scheme {
  val name: String
}

interface ExternalizableScheme : Scheme {
  override var name: String
}

abstract class SchemeManagerFactory {
  companion object {
    @JvmStatic
    fun getInstance() = ServiceManager.getService(SchemeManagerFactory::class.java)!!

    @JvmStatic
    fun getInstance(project: Project) = ServiceManager.getService(project, SchemeManagerFactory::class.java)!!
  }

  /**
   * directoryName — like "keymaps".
   */
  @JvmOverloads
  fun <SCHEME : Scheme, MUTABLE_SCHEME: SCHEME> create(directoryName: String, processor: SchemeProcessor<SCHEME, MUTABLE_SCHEME>, presentableName: String? = null, isUseOldFileNameSanitize: Boolean = false): SchemeManager<SCHEME> = create(directoryName, processor, presentableName, RoamingType.DEFAULT, isUseOldFileNameSanitize)

  protected abstract fun <SCHEME : Scheme, MUTABLE_SCHEME: SCHEME> create(directoryName: String, processor: SchemeProcessor<SCHEME, MUTABLE_SCHEME>, presentableName: String? = null, roamingType: RoamingType = RoamingType.DEFAULT, isUseOldFileNameSanitize: Boolean = false): SchemeManager<SCHEME>
}

enum class SchemeState {
  UNCHANGED, NON_PERSISTENT, POSSIBLY_CHANGED
}

abstract class SchemeProcessor<SCHEME : Scheme, in MUTABLE_SCHEME: SCHEME> {
  open fun isExternalizable(scheme: SCHEME) = scheme is ExternalizableScheme

  /**
   * Element will not be modified, it is safe to return non-cloned instance.
   */
  @Throws(WriteExternalException::class)
  abstract fun writeScheme(scheme: MUTABLE_SCHEME): Parent

  open fun initScheme(scheme: MUTABLE_SCHEME) {
  }

  open fun onSchemeAdded(scheme: MUTABLE_SCHEME) {
  }

  open fun onSchemeDeleted(scheme: MUTABLE_SCHEME) {
  }

  open fun onCurrentSchemeSwitched(oldScheme: SCHEME?, newScheme: SCHEME?) {
  }

  open fun getState(scheme: SCHEME): SchemeState = SchemeState.POSSIBLY_CHANGED
}