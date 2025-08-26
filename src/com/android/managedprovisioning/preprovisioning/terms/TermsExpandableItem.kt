/*
 * Copyright (C) 2025 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.managedprovisioning.preprovisioning.terms

import android.content.Context
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.View
import android.widget.TextView

import com.android.managedprovisioning.R
import com.google.android.setupdesign.items.ExpandableItem

class TermsExpandableItem(context: Context, attrs: AttributeSet? = null) :
  ExpandableItem(context, attrs) {

  private var termsContent: Spanned? = null

  fun setTermsContent(content: Spanned) {
    termsContent = content
  }

  override fun onBindView(view: View) {
    super.onBindView(view)
    val textView = view.findViewById<TextView>(R.id.terms_content)
    textView?.text = termsContent
    textView?.movementMethod = LinkMovementMethod.getInstance()
  }
}
