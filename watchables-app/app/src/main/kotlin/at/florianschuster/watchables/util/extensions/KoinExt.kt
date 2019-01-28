/*
 * Copyright 2019 Florian Schuster. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package at.florianschuster.watchables.util.extensions

import androidx.lifecycle.LifecycleOwner
import at.florianschuster.watchables.ui.base.reactor.KoinReactor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.ext.viewModel
import org.koin.core.definition.Definition
import org.koin.core.module.Module
import org.koin.core.parameter.ParametersDefinition


/**
 * Reactor DSL extension for Koin.
 */
inline fun <reified Reactor : KoinReactor<*, *, *>> Module.reactor(
        name: String? = null,
        override: Boolean = false,
        noinline definition: Definition<Reactor>
) = viewModel(name, override, definition)


/**
 * Lazily gets a reactor instance.
 */
inline fun <reified Reactor : KoinReactor<*, *, *>> LifecycleOwner.reactor(
        name: String? = null,
        noinline parameters: ParametersDefinition? = null
) = viewModel<Reactor>(name, parameters)
