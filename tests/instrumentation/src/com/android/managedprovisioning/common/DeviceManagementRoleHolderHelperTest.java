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

import static android.app.admin.DevicePolicyManager.ACTION_ROLE_HOLDER_PROVISION_MANAGED_DEVICE_FROM_TRUSTED_SOURCE;

import static com.android.managedprovisioning.TestUtils.assertIntentsEqual;

import static com.google.common.truth.Truth.assertThat;

import static org.junit.Assert.assertThrows;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.filters.SmallTest;

import com.android.managedprovisioning.provisioning.Constants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashSet;
import java.util.Set;


@SmallTest
@RunWith(JUnit4.class)
public class DeviceManagementRoleHolderHelperTest {
    private static final String ROLE_HOLDER_PACKAGE_NAME = "com.test.package";
    private static final String ROLE_HOLDER_EMPTY_PACKAGE_NAME = "";
    private static final String ROLE_HOLDER_NULL_PACKAGE_NAME = null;
    private final Context mContext = ApplicationProvider.getApplicationContext();
    public static final String TEST_EXTRA_KEY = "test_extra_key";
    public static final String TEST_EXTRA_VALUE = "test_extra_value";
    private static final Intent MANAGED_PROFILE_INTENT =
            new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
                    .putExtra(TEST_EXTRA_KEY, TEST_EXTRA_VALUE);
    private static final Intent MANAGED_PROFILE_ROLE_HOLDER_INTENT =
            new Intent(DevicePolicyManager.ACTION_ROLE_HOLDER_PROVISION_MANAGED_PROFILE)
                    .putExtra(TEST_EXTRA_KEY, TEST_EXTRA_VALUE)
                    .setPackage(ROLE_HOLDER_PACKAGE_NAME);
    private static final Intent PROVISION_TRUSTED_SOURCE_INTENT =
            new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_DEVICE_FROM_TRUSTED_SOURCE)
                    .putExtra(TEST_EXTRA_KEY, TEST_EXTRA_VALUE);
    private static final Intent PROVISION_TRUSTED_SOURCE_ROLE_HOLDER_INTENT =
            new Intent(ACTION_ROLE_HOLDER_PROVISION_MANAGED_DEVICE_FROM_TRUSTED_SOURCE)
                    .putExtra(TEST_EXTRA_KEY, TEST_EXTRA_VALUE)
                    .setPackage(ROLE_HOLDER_PACKAGE_NAME);
    private static final Intent FINANCED_DEVICE_INTENT =
            new Intent(DevicePolicyManager.ACTION_PROVISION_FINANCED_DEVICE)
                    .putExtra(TEST_EXTRA_KEY, TEST_EXTRA_VALUE);
    private static final Intent PROVISION_FINALIZATION_INTENT =
            new Intent(DevicePolicyManager.ACTION_PROVISION_FINALIZATION);
    private static final Intent PROVISION_FINALIZATION_ROLE_HOLDER_INTENT =
            new Intent(DevicePolicyManager.ACTION_ROLE_HOLDER_PROVISION_FINALIZATION)
                    .setPackage(ROLE_HOLDER_PACKAGE_NAME);
    private static final Intent MANAGED_PROVISIONING_INTENT = MANAGED_PROFILE_INTENT;
    private static final Intent INVALID_MANAGED_PROVISIONING_INTENT =
            new Intent("action.intent.test");
    private Set<String> mRoleHolderHandledIntents;

    @Before
    public void setUp() {
        enableRoleHolderDelegation();
        mRoleHolderHandledIntents = createRoleHolderRequiredIntentActionsSet();
    }

    @Test
    public void roleHolderHelperConstructor_roleHolderPackageNameNull_throwsException() {
        assertThrows(NullPointerException.class, () ->
                createRoleHolderHelper(ROLE_HOLDER_NULL_PACKAGE_NAME));
    }

    @Test
    public void roleHolderHelperConstructor_roleHolderPackageNameEmpty_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                createRoleHolderHelper(ROLE_HOLDER_EMPTY_PACKAGE_NAME));
    }

    @Test
    public void isRoleHolderReadyForProvisioning_works() {
        DeviceManagementRoleHolderHelper roleHolderHelper = createRoleHolderHelper();

        assertThat(roleHolderHelper.isRoleHolderReadyForProvisioning(
                mContext, MANAGED_PROVISIONING_INTENT)).isTrue();
    }

    @Test
    public void isRoleHolderReadyForProvisioning_roleHolderDelegationDisabled_returnsFalse() {
        DeviceManagementRoleHolderHelper roleHolderHelper = createRoleHolderHelper();
        disableRoleHolderDelegation();

        assertThat(roleHolderHelper.isRoleHolderReadyForProvisioning(
                mContext, MANAGED_PROVISIONING_INTENT)).isFalse();
    }

    @Test
    public void isRoleHolderReadyForProvisioning_roleHolderNotInstalled_returnsFalse() {
        DeviceManagementRoleHolderHelper roleHolderHelper =
                createRoleHolderHelperWithRoleHolderNotInstalled();

        assertThat(roleHolderHelper.isRoleHolderReadyForProvisioning(
                mContext, MANAGED_PROVISIONING_INTENT)).isFalse();
    }

    @Test
    public void isRoleHolderReadyForProvisioning_roleHolderStub_returnsFalse() {
        DeviceManagementRoleHolderHelper roleHolderHelper =
                createRoleHolderHelperWithStubRoleHolder();

        assertThat(roleHolderHelper.isRoleHolderReadyForProvisioning(
                mContext, MANAGED_PROVISIONING_INTENT)).isFalse();
    }

    @Test
    public void isRoleHolderReadyForProvisioning_roleHolderInvalid_returnsFalse() {
        DeviceManagementRoleHolderHelper roleHolderHelper =
                createRoleHolderHelperWithInvalidRoleHolder();

        assertThat(roleHolderHelper.isRoleHolderReadyForProvisioning(
                mContext, MANAGED_PROVISIONING_INTENT)).isFalse();
    }

    @Test
    public void isRoleHolderReadyForProvisioning_roleHolderResolvesRequiredIntents_returnsTrue() {
        DeviceManagementRoleHolderHelper roleHolderHelper =
                createRoleHolderHelperWithRoleHolderResolvesRequiredIntents(
                        mRoleHolderHandledIntents);

        assertThat(roleHolderHelper.isRoleHolderReadyForProvisioning(
                mContext, MANAGED_PROVISIONING_INTENT)).isTrue();
    }

    @Test
    public void isRoleHolderReadyForProvisioning_roleHolderResolvesRequiredIntentsExceptManagedProfile_returnsFalse() {
        mRoleHolderHandledIntents.remove(
                DevicePolicyManager.ACTION_ROLE_HOLDER_PROVISION_MANAGED_PROFILE);
        DeviceManagementRoleHolderHelper roleHolderHelper =
                createRoleHolderHelperWithRoleHolderResolvesRequiredIntents(
                        mRoleHolderHandledIntents);

        assertThat(roleHolderHelper.isRoleHolderReadyForProvisioning(
                mContext, MANAGED_PROVISIONING_INTENT)).isFalse();
    }

    @Test
    public void isRoleHolderReadyForProvisioning_roleHolderResolvesRequiredIntentsExceptTrustedSource_returnsFalse() {
        mRoleHolderHandledIntents.remove(
                ACTION_ROLE_HOLDER_PROVISION_MANAGED_DEVICE_FROM_TRUSTED_SOURCE);
        DeviceManagementRoleHolderHelper roleHolderHelper =
                createRoleHolderHelperWithRoleHolderResolvesRequiredIntents(
                        mRoleHolderHandledIntents);

        assertThat(roleHolderHelper.isRoleHolderReadyForProvisioning(
                mContext, MANAGED_PROVISIONING_INTENT)).isFalse();
    }

    @Test
    public void isRoleHolderReadyForProvisioning_roleHolderResolvesRequiredIntentsExceptFinalization_returnsFalse() {
        mRoleHolderHandledIntents.remove(
                DevicePolicyManager.ACTION_ROLE_HOLDER_PROVISION_FINALIZATION);
        DeviceManagementRoleHolderHelper roleHolderHelper =
                createRoleHolderHelperWithRoleHolderResolvesRequiredIntents(
                        mRoleHolderHandledIntents);

        assertThat(roleHolderHelper.isRoleHolderReadyForProvisioning(
                mContext, MANAGED_PROVISIONING_INTENT)).isFalse();
    }

    @Test
    public void isRoleHolderReadyForProvisioning_provisioningStartedViaManagedProfileIntent_returnsTrue() {
        DeviceManagementRoleHolderHelper roleHolderHelper =
                createRoleHolderHelperWithRoleHolderResolvesRequiredIntents(
                        mRoleHolderHandledIntents);

        assertThat(roleHolderHelper.isRoleHolderReadyForProvisioning(
                mContext, MANAGED_PROFILE_INTENT)).isTrue();
    }

    @Test
    public void isRoleHolderReadyForProvisioning_provisioningStartedViaTrustedSourceIntent_returnsTrue() {
        DeviceManagementRoleHolderHelper roleHolderHelper =
                createRoleHolderHelperWithRoleHolderResolvesRequiredIntents(
                        mRoleHolderHandledIntents);

        assertThat(roleHolderHelper.isRoleHolderReadyForProvisioning(
                mContext, PROVISION_TRUSTED_SOURCE_INTENT)).isTrue();
    }

    @Test
    public void isRoleHolderReadyForProvisioning_provisioningStartedViaFinancedDeviceIntent_returnsFalse() {
        DeviceManagementRoleHolderHelper roleHolderHelper =
                createRoleHolderHelperWithRoleHolderResolvesRequiredIntents(
                        mRoleHolderHandledIntents);

        assertThat(roleHolderHelper.isRoleHolderReadyForProvisioning(
                mContext, FINANCED_DEVICE_INTENT)).isFalse();
    }

    @Test
    public void isRoleHolderReadyForProvisioning_provisioningStartedViaFinalizationIntent_returnsFalse() {
        DeviceManagementRoleHolderHelper roleHolderHelper =
                createRoleHolderHelperWithRoleHolderResolvesRequiredIntents(
                        mRoleHolderHandledIntents);

        assertThat(roleHolderHelper.isRoleHolderReadyForProvisioning(
                mContext, PROVISION_FINALIZATION_INTENT)).isFalse();
    }

    @Test
    public void createRoleHolderProvisioningIntent_invalidProvisioningIntent_throwsException() {
        DeviceManagementRoleHolderHelper roleHolderHelper = createRoleHolderHelper();

        assertThrows(IllegalArgumentException.class, () ->
                roleHolderHelper.createRoleHolderProvisioningIntent(
                        INVALID_MANAGED_PROVISIONING_INTENT));
    }

    @Test
    public void createRoleHolderProvisioningIntent_managedProfileProvisioningIntent_works() {
        DeviceManagementRoleHolderHelper roleHolderHelper = createRoleHolderHelper();

        assertIntentsEqual(
                roleHolderHelper.createRoleHolderProvisioningIntent(MANAGED_PROFILE_INTENT),
                MANAGED_PROFILE_ROLE_HOLDER_INTENT);
    }

    @Test
    public void createRoleHolderProvisioningIntent_trustedSourceProvisioningIntent_works() {
        DeviceManagementRoleHolderHelper roleHolderHelper = createRoleHolderHelper();

        assertIntentsEqual(
                roleHolderHelper.createRoleHolderProvisioningIntent(
                        PROVISION_TRUSTED_SOURCE_INTENT),
                PROVISION_TRUSTED_SOURCE_ROLE_HOLDER_INTENT);
    }

    @Test
    public void createRoleHolderProvisioningIntent_financedDeviceProvisioningIntent_throwsException() {
        DeviceManagementRoleHolderHelper roleHolderHelper = createRoleHolderHelper();

        assertThrows(
                IllegalArgumentException.class,
                () -> roleHolderHelper.createRoleHolderProvisioningIntent(
                        FINANCED_DEVICE_INTENT));
    }

    @Test
    public void createRoleHolderProvisioningIntent_provisioningFinalizationIntent_works() {
        DeviceManagementRoleHolderHelper roleHolderHelper = createRoleHolderHelper();

        assertIntentsEqual(
                roleHolderHelper.createRoleHolderFinalizationIntent(),
                PROVISION_FINALIZATION_ROLE_HOLDER_INTENT);
    }

    private void enableRoleHolderDelegation() {
        Constants.FLAG_DEFER_PROVISIONING_TO_ROLE_HOLDER = true;
    }

    private void disableRoleHolderDelegation() {
        Constants.FLAG_DEFER_PROVISIONING_TO_ROLE_HOLDER = false;
    }

    private DeviceManagementRoleHolderHelper createRoleHolderHelper() {
        return new DeviceManagementRoleHolderHelper(
                ROLE_HOLDER_PACKAGE_NAME,
                /* packageInstallChecker= */ (roleHolderPackageName, packageManager) -> true,
                /* resolveIntentChecker= */ (intent, packageManager) -> true,
                /* roleHolderStubChecker= */ (packageName, packageManager) -> false);
    }

    private DeviceManagementRoleHolderHelper createRoleHolderHelper(
            String roleHolderPackageName) {
        return new DeviceManagementRoleHolderHelper(
                roleHolderPackageName,
                /* packageInstallChecker= */ (packageName, packageManager) -> true,
                /* resolveIntentChecker= */ (intent, packageManager) -> true,
                /* roleHolderStubChecker= */ (packageName, packageManager) -> false);
    }

    private DeviceManagementRoleHolderHelper createRoleHolderHelperWithRoleHolderNotInstalled() {
        return new DeviceManagementRoleHolderHelper(
                ROLE_HOLDER_PACKAGE_NAME,
                /* packageInstallChecker= */ (roleHolderPackageName, packageManager) -> false,
                /* resolveIntentChecker= */ (intent, packageManager) -> true,
                /* roleHolderStubChecker= */ (packageName, packageManager) -> false);
    }

    private DeviceManagementRoleHolderHelper createRoleHolderHelperWithStubRoleHolder() {
        return new DeviceManagementRoleHolderHelper(
                ROLE_HOLDER_PACKAGE_NAME,
                /* packageInstallChecker= */ (roleHolderPackageName, packageManager) -> true,
                /* resolveIntentChecker= */ (intent, packageManager) -> true,
                /* roleHolderStubChecker= */ (packageName, packageManager) -> true);
    }

    private DeviceManagementRoleHolderHelper createRoleHolderHelperWithInvalidRoleHolder() {
        // A role holder is considered invalid if it is not able to resolve all the required intents
        return new DeviceManagementRoleHolderHelper(
                ROLE_HOLDER_PACKAGE_NAME,
                /* packageInstallChecker= */ (roleHolderPackageName, packageManager) -> true,
                /* resolveIntentChecker= */ (intent, packageManager) -> false,
                /* roleHolderStubChecker= */ (packageName, packageManager) -> false);
    }

    private DeviceManagementRoleHolderHelper
            createRoleHolderHelperWithRoleHolderResolvesRequiredIntents(
            Set<String> roleHolderHandledIntents) {
        // A role holder is considered invalid if it is not able to resolve all the required intents
        return new DeviceManagementRoleHolderHelper(
                ROLE_HOLDER_PACKAGE_NAME,
                /* packageInstallChecker= */ (roleHolderPackageName, packageManager) -> true,
                /* resolveIntentChecker= */ (intent, packageManager) ->
                        roleHolderHandledIntents.contains(intent.getAction()),
                /* roleHolderStubChecker= */ (packageName, packageManager) -> false);
    }

    private static Set<String> createRoleHolderRequiredIntentActionsSet() {
        Set<String> result = new HashSet<>();
        result.add(DevicePolicyManager.ACTION_ROLE_HOLDER_PROVISION_MANAGED_PROFILE);
        result.add(ACTION_ROLE_HOLDER_PROVISION_MANAGED_DEVICE_FROM_TRUSTED_SOURCE);
        result.add(DevicePolicyManager.ACTION_ROLE_HOLDER_PROVISION_FINALIZATION);
        return result;
    }
}
