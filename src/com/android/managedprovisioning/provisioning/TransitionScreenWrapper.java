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

package com.android.managedprovisioning.provisioning;

import android.annotation.DrawableRes;
import android.annotation.RawRes;
import android.annotation.StringRes;
import android.content.Context;

/**
 * A wrapper describing the contents of an education screen.
 */
final class TransitionScreenWrapper {
    public final String header;
    public final String description;
    public final @RawRes int drawable;
    public final @StringRes int subHeaderTitle;
    public final String subHeader;
    public final @DrawableRes int subHeaderIcon;
    public final boolean shouldLoop;
    public final @StringRes int secondarySubHeaderTitle;
    public final String secondarySubHeader;
    public final @DrawableRes int secondarySubHeaderIcon;
    public final Context context;

    TransitionScreenWrapper(@StringRes int header, @RawRes int drawable, Context context) {
        this(header, /* description= */ "", drawable, /* shouldLoop */ true, context);
    }

    TransitionScreenWrapper(@StringRes int header, String description,
            @RawRes int drawable, boolean shouldLoop, Context context) {
        this(context.getString(header), /* description= */ description, drawable, 0, "",
                0, shouldLoop, 0, "", 0, context);
    }

    private TransitionScreenWrapper(String header, String description, int drawable,
            int subHeaderTitle, String subHeader, int subHeaderIcon, boolean shouldLoop,
            int secondarySubHeaderTitle, String secondarySubHeader, int secondarySubHeaderIcon,
            Context context) {
        this.header = header;
        this.description = description;
        this.drawable = drawable;
        this.subHeaderTitle = subHeaderTitle;
        this.subHeader = subHeader;
        this.subHeaderIcon = subHeaderIcon;
        this.shouldLoop = shouldLoop;
        this.secondarySubHeaderTitle = secondarySubHeaderTitle;
        this.secondarySubHeader = secondarySubHeader;
        this.secondarySubHeaderIcon = secondarySubHeaderIcon;
        this.context = context;
        validateFields();
    }

    private void validateFields() {
        final boolean isItemProvided =
                subHeader != null && !subHeader.isEmpty()
                        || subHeaderIcon != 0
                        || subHeaderTitle != 0
                        || secondarySubHeader != null && !secondarySubHeader.isEmpty()
                        || secondarySubHeaderIcon != 0
                        || secondarySubHeaderTitle != 0;
        if (isItemProvided && drawable != 0) {
            throw new IllegalArgumentException(
                    "Cannot show items and animation at the same time.");
        }
        if (header.isEmpty()) {
            throw new IllegalArgumentException("Header resource id must be a positive number.");
        }
    }

    public static final class Builder {
        String mHeader;
        String mDescription;
        @RawRes int mDrawable;
        @StringRes private int mSubHeaderTitle;
        String mSubHeader;
        @DrawableRes int mSubHeaderIcon;
        boolean mShouldLoop;
        @StringRes int mSecondarySubHeaderTitle;
        String mSecondarySubHeader;
        @DrawableRes int mSecondarySubHeaderIcon;
        Context mContext;

        Builder (Context context) {
            mContext = context;
        }

        public Builder setHeader(int header) {
            mHeader = mContext.getString(header);
            return this;
        }

        public Builder setHeader(String header) {
            mHeader = header;
            return this;
        }

        public Builder setDescription(int description) {
            mDescription = mContext.getString(description);
            return this;
        }

        public Builder setDescription(String description) {
            mDescription = description;
            return this;
        }

        public Builder setAnimation(int drawable) {
            mDrawable = drawable;
            return this;
        }

        public Builder setSubHeaderTitle(int subHeaderTitle) {
            mSubHeaderTitle = subHeaderTitle;
            return this;
        }

        public Builder setSubHeader(int subHeader) {
            mSubHeader = mContext.getString(subHeader);
            return this;
        }

        public Builder setSubHeader(String subHeader) {
            mSubHeader = subHeader;
            return this;
        }

        public Builder setSubHeaderIcon(int subHeaderIcon) {
            mSubHeaderIcon = subHeaderIcon;
            return this;
        }

        public Builder setShouldLoop(boolean shouldLoop) {
            mShouldLoop = shouldLoop;
            return this;
        }

        public Builder setSecondarySubHeaderTitle(int secondarySubHeaderTitle) {
            mSecondarySubHeaderTitle = secondarySubHeaderTitle;
            return this;
        }

        public Builder setSecondarySubHeader(int secondarySubHeader) {
            mSecondarySubHeader = mContext.getString(secondarySubHeader);
            return this;
        }

        public Builder setSecondarySubHeader(String secondarySubHeader) {
            mSecondarySubHeader = secondarySubHeader;
            return this;
        }

        public Builder setSecondarySubHeaderIcon(int secondarySubHeaderIcon) {
            mSecondarySubHeaderIcon = secondarySubHeaderIcon;
            return this;
        }

        public TransitionScreenWrapper build() {
            return new TransitionScreenWrapper(mHeader, mDescription, mDrawable, mSubHeaderTitle,
                    mSubHeader, mSubHeaderIcon, mShouldLoop, mSecondarySubHeaderTitle,
                    mSecondarySubHeader, mSecondarySubHeaderIcon, mContext);
        }
    }
}
