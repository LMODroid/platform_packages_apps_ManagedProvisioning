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

package com.android.managedprovisioning.common;

import android.app.role.RoleManager;
import android.content.Context;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * A provider for the role holder package name.
 */
public interface RoleHolderProvider {
    RoleHolderProvider DEFAULT = (Context context) -> {
        RoleManager roleManager = context.getSystemService(RoleManager.class);
        List<String> roleHolders = roleManager.getRoleHolders(RoleManager.ROLE_DEVICE_MANAGER);
        if (roleHolders.isEmpty()) {
            ProvisionLogger.logi("No role holders retrieved for "
                    + RoleManager.ROLE_DEVICE_MANAGER);
            return null;
        }
        return roleHolders.get(0);
    };

    /**
     * Returns the package name of the role holder.
     */
    @Nullable
    String getPackageName(Context context);
}
