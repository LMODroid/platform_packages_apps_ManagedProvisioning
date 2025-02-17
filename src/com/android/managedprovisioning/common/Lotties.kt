/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.managedprovisioning.common

import com.android.managedprovisioning.R;

import com.airbnb.lottie.LottieAnimationView
import com.google.android.setupdesign.util.LottieAnimationHelper

object Lotties {
  /**
   * Applies color mappings to the lottie animation if the Glif Expressive is enabled. Otherwise,
   * does nothing.
   */
  @JvmStatic
  fun LottieAnimationView.applyColorMappingsIfGlifExpressive() {
    val colorMappings =
      this.context.resources.getStringArray(R.array.lottie_color_mappings_expressive).toList()
    LottieAnimationHelper.get().applyColor(this.context, this, colorMappings)
  }
}