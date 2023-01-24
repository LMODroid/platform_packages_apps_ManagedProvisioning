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

import static com.android.managedprovisioning.provisioning.ProvisioningActivity.PROVISIONING_MODE_FULLY_MANAGED_DEVICE;
import static com.android.managedprovisioning.provisioning.ProvisioningActivity.PROVISIONING_MODE_WORK_PROFILE;
import static com.android.managedprovisioning.provisioning.ProvisioningActivity.PROVISIONING_MODE_WORK_PROFILE_ON_ORG_OWNED_DEVICE;

import static java.util.Objects.compare;
import static java.util.Objects.requireNonNull;

import android.content.Context;

import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;

import com.android.managedprovisioning.R;
import com.android.managedprovisioning.model.ProvisioningParams;
import com.android.managedprovisioning.provisioning.ProvisioningActivity.ProvisioningMode;
import com.google.android.setupdesign.util.DeviceHelper;

/**
 * Utility class to get the corresponding wrapper that contains provisioning transition content.
 */
public class ProvisioningModeWrapperProvider {
    private final ProvisioningParams mParams;
    private final Context mContext;

    public ProvisioningModeWrapperProvider(Context context, ProvisioningParams params) {
        mContext = context;
        mParams = requireNonNull(params);
    }

    /**
     * Return default provisioning mode wrapper depending on provisioning parameter.
     */
    public ProvisioningModeWrapper getProvisioningModeWrapper(
            @ProvisioningMode int provisioningMode) {
        CharSequence deviceName = DeviceHelper.getDeviceName(mContext);
        @VisibleForTesting
        final ProvisioningModeWrapper WORK_PROFILE_WRAPPER =
                new ProvisioningModeWrapper(new TransitionScreenWrapper[] {
                        new TransitionScreenWrapper(
                                R.string.work_profile_provisioning_step_1_header,
                                R.raw.separate_work_and_personal_animation, mContext),
                        new TransitionScreenWrapper(
                                R.string.work_profile_provisioning_step_2_header,
                                R.raw.pause_work_apps_animation, mContext),
                        new TransitionScreenWrapper(
                                R.string.work_profile_provisioning_step_3_header,
                                R.raw.not_private_animation, mContext)
                }, mContext.getString(R.string.work_profile_provisioning_summary, deviceName));

        @VisibleForTesting
        final ProvisioningModeWrapper WORK_PROFILE_ON_ORG_OWNED_DEVICE_WRAPPER =
                new ProvisioningModeWrapper(new TransitionScreenWrapper[] {
                        new TransitionScreenWrapper(R.string.cope_provisioning_step_1_header,
                                R.raw.separate_work_and_personal_animation, mContext),
                        new TransitionScreenWrapper(R.string.cope_provisioning_step_2_header,
                                /* description= */ "",
                                R.raw.personal_apps_separate_hidden_from_work_animation,
                                /* shouldLoop */ false,
                                mContext),
                        new TransitionScreenWrapper(R.string.cope_provisioning_step_3_header,
                                R.raw.it_admin_control_device_block_apps_animation, mContext)
                }, mContext.getString(R.string.cope_provisioning_summary, deviceName));

        switch (provisioningMode) {
            case PROVISIONING_MODE_WORK_PROFILE:
                return WORK_PROFILE_WRAPPER;
            case PROVISIONING_MODE_FULLY_MANAGED_DEVICE:
                return getProvisioningModeWrapperForFullyManaged();
            case PROVISIONING_MODE_WORK_PROFILE_ON_ORG_OWNED_DEVICE:
                return WORK_PROFILE_ON_ORG_OWNED_DEVICE_WRAPPER;
        }
        throw new IllegalStateException("Unexpected provisioning mode " + provisioningMode);
    }

    /** Return the provisioning mode wrapper for a fully-managed device.
     * The second screen, as well as the accessible summary, will be different, depending on whether
     * the admin can grant sensors-related permissions on this device or not.
     */
    @VisibleForTesting
    private ProvisioningModeWrapper getProvisioningModeWrapperForFullyManaged() {
        final int provisioningSummaryId;
        CharSequence deviceName = DeviceHelper.getDeviceName(mContext);
        TransitionScreenWrapper.Builder secondScreenBuilder =
                new TransitionScreenWrapper.Builder(mContext)
                        .setHeader(mContext.getString(
                                R.string.fully_managed_device_provisioning_step_2_header,
                                deviceName));

        // Admin cannot grant sensors permissions
        if (!mParams.deviceOwnerPermissionGrantOptOut) {
            secondScreenBuilder
                    .setSubHeaderTitle(
                            R.string.fully_managed_device_provisioning_permissions_header)
                    .setSubHeader(R.string.fully_managed_device_provisioning_permissions_subheader)
                    .setSubHeaderIcon(R.drawable.ic_history)
                    .setSecondarySubHeaderTitle(
                            R.string.fully_managed_device_provisioning_permissions_secondary_header)
                    .setSecondarySubHeader(mContext.getString(R.string
                            .fully_managed_device_provisioning_permissions_secondary_subheader,
                            deviceName))
                    .setSecondarySubHeaderIcon(R.drawable.ic_perm_device_information)
                    .setShouldLoop(true);
            provisioningSummaryId =
                    R.string.fully_managed_device_with_permission_control_provisioning_summary;
        } else {
            provisioningSummaryId = R.string.fully_managed_device_provisioning_summary;
            secondScreenBuilder
                    .setDescription(mContext.getString(
                            R.string.fully_managed_device_provisioning_step_2_subheader,
                            deviceName))
                    .setAnimation(R.raw.not_private_animation);
        }

        TransitionScreenWrapper firstScreen = new TransitionScreenWrapper(
                R.string.fully_managed_device_provisioning_step_1_header,
                R.raw.connect_on_the_go_animation, mContext);
        return new ProvisioningModeWrapper(new TransitionScreenWrapper[] {
                firstScreen, secondScreenBuilder.build()},
                mContext.getString(provisioningSummaryId, deviceName));
    }

    static final class ProvisioningModeWrapper {
        final TransitionScreenWrapper[] transitions;
        final String summary;

        ProvisioningModeWrapper(TransitionScreenWrapper[] transitions, String summary) {
            this.transitions = requireNonNull(transitions);
            this.summary = summary;
        }
    }
}
