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

package com.android.managedprovisioning.preprovisioning;

import static com.android.managedprovisioning.preprovisioning.RoleHolderUpdaterViewModel.LaunchRoleHolderUpdaterFailureEvent.REASON_EXCEEDED_MAXIMUM_NUMBER_UPDATER_LAUNCH_RETRIES;
import static com.android.managedprovisioning.preprovisioning.RoleHolderUpdaterViewModel.LaunchRoleHolderUpdaterFailureEvent.REASON_EXCEEDED_MAXIMUM_NUMBER_UPDATE_RETRIES;

import static java.util.Objects.requireNonNull;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.android.managedprovisioning.common.DeviceManagementRoleHolderUpdaterHelper;
import com.android.managedprovisioning.common.ProvisionLogger;
import com.android.managedprovisioning.common.Utils;
import com.android.managedprovisioning.common.ViewModelEvent;

import java.util.Objects;

final class RoleHolderUpdaterViewModel extends AndroidViewModel {
    static final int VIEW_MODEL_EVENT_LAUNCH_UPDATER = 1;
    static final int VIEW_MODEL_EVENT_LAUNCH_FAILURE = 2;

    private static final int LAUNCH_ROLE_HOLDER_UPDATER_RETRY_PERIOD_MS = 60 * 1000;
    private static final int LAUNCH_ROLE_HOLDER_UPDATER_MAX_RETRIES = 3;
    private static final int ROLE_HOLDER_UPDATE_MAX_RETRIES = 3;

    private final MutableLiveData<ViewModelEvent> mObservableEvents = new MutableLiveData<>();
    private final Runnable mRunnable = RoleHolderUpdaterViewModel.this::tryStartRoleHolderUpdater;
    private final Handler mHandler;
    private final CanLaunchRoleHolderUpdaterChecker mCanLaunchRoleHolderUpdaterChecker;
    private final Config mConfig;
    private final DeviceManagementRoleHolderUpdaterHelper mRoleHolderUpdaterHelper;

    private int mNumberOfStartUpdaterTries = 0;
    private int mNumberOfUpdateTries = 0;

    RoleHolderUpdaterViewModel(
            @NonNull Application application,
            Handler handler,
            CanLaunchRoleHolderUpdaterChecker canLaunchRoleHolderUpdaterChecker,
            Config config,
            DeviceManagementRoleHolderUpdaterHelper roleHolderUpdaterHelper) {
        super(application);
        mHandler = requireNonNull(handler);
        mCanLaunchRoleHolderUpdaterChecker = requireNonNull(canLaunchRoleHolderUpdaterChecker);
        mConfig = requireNonNull(config);
        mRoleHolderUpdaterHelper = requireNonNull(roleHolderUpdaterHelper);
    }

    MutableLiveData<ViewModelEvent> observeViewModelEvents() {
        return mObservableEvents;
    }

    /**
     * Tries to start the role holder updater.
     * <ol>
     * <li>If the role holder updater can be launched, it is launched. It then tries to download the
     * role holder. If it fails to download it, it tries {@link #ROLE_HOLDER_UPDATE_MAX_RETRIES}
     * times total.</li>
     * <li>If the role holder updater cannot be currently launched (e.g. if it's being updated
     * itself), then we schedule a retry, up to {@link #LAUNCH_ROLE_HOLDER_UPDATER_MAX_RETRIES}
     * times total.</li>
     * <li>If we exceed either of the max retry thresholds, we post a failure event.</li>
     * </ol>
     *
     * @see LaunchRoleHolderUpdaterEvent
     * @see LaunchRoleHolderUpdaterFailureEvent
     */
    void tryStartRoleHolderUpdater() {
        Intent intent = mRoleHolderUpdaterHelper.createRoleHolderUpdaterIntent();
        boolean canLaunchRoleHolderUpdater =
                mCanLaunchRoleHolderUpdaterChecker.canLaunchRoleHolderUpdater(
                        getApplication().getApplicationContext(), intent);
        if (canLaunchRoleHolderUpdater
                && canRetryUpdate(mNumberOfUpdateTries)) {
            launchRoleHolderUpdater(intent);
        } else if (canLaunchRoleHolderUpdater) {
            ProvisionLogger.loge("Exceeded maximum number of update retries.");
            mObservableEvents.postValue(
                    new LaunchRoleHolderUpdaterFailureEvent(
                            REASON_EXCEEDED_MAXIMUM_NUMBER_UPDATE_RETRIES));
        } else {
            ProvisionLogger.loge("Cannot launch role holder updater.");
            tryRescheduleRoleHolderUpdater();
        }
    }

    void stopLaunchRetries() {
        mHandler.removeCallbacks(mRunnable);
    }

    /**
     * Tries to reschedule the role holder updater launch.
     */
    private void tryRescheduleRoleHolderUpdater() {
        if (canRetryLaunchRoleHolderUpdater(mNumberOfStartUpdaterTries)) {
            scheduleRetryLaunchRoleHolderUpdater();
        } else {
            ProvisionLogger.loge("Exceeded maximum number of role holder updater launch retries.");
            mObservableEvents.postValue(
                    new LaunchRoleHolderUpdaterFailureEvent(
                            REASON_EXCEEDED_MAXIMUM_NUMBER_UPDATER_LAUNCH_RETRIES));
        }
    }

    private boolean canRetryUpdate(int numTries) {
        return numTries < mConfig.getRoleHolderUpdateMaxRetries();
    }

    private boolean canRetryLaunchRoleHolderUpdater(int numTries) {
        return numTries < mConfig.getLaunchRoleHolderMaxRetries();
    }

    private void launchRoleHolderUpdater(Intent intent) {
        mObservableEvents.postValue(new LaunchRoleHolderUpdaterEvent(intent));
        mNumberOfUpdateTries++;
    }

    private void scheduleRetryLaunchRoleHolderUpdater() {
        mHandler.postDelayed(mRunnable, mConfig.getLaunchRoleHolderUpdaterPeriodMillis());
        mNumberOfStartUpdaterTries++;
    }

    static class LaunchRoleHolderUpdaterEvent extends ViewModelEvent {
        private final Intent mIntent;

        LaunchRoleHolderUpdaterEvent(Intent intent) {
            super(VIEW_MODEL_EVENT_LAUNCH_UPDATER);
            mIntent = requireNonNull(intent);
        }

        Intent getIntent() {
            return mIntent;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LaunchRoleHolderUpdaterEvent)) return false;
            LaunchRoleHolderUpdaterEvent that = (LaunchRoleHolderUpdaterEvent) o;
            return Objects.equals(mIntent.getAction(), that.mIntent.getAction())
                    && Objects.equals(mIntent.getExtras(), that.mIntent.getExtras());
        }

        @Override
        public int hashCode() {
            return Objects.hash(mIntent.getAction(), mIntent.getExtras());
        }

        @Override
        public String toString() {
            return "LaunchRoleHolderUpdaterEvent{"
                    + "mIntent=" + mIntent + '}';
        }
    }

    static class LaunchRoleHolderUpdaterFailureEvent extends ViewModelEvent {
        static final int REASON_EXCEEDED_MAXIMUM_NUMBER_UPDATE_RETRIES = 1;
        static final int REASON_EXCEEDED_MAXIMUM_NUMBER_UPDATER_LAUNCH_RETRIES = 2;

        private final int mReason;

        LaunchRoleHolderUpdaterFailureEvent(int reason) {
            super(VIEW_MODEL_EVENT_LAUNCH_FAILURE);
            mReason = reason;
        }

        int getReason() {
            return mReason;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LaunchRoleHolderUpdaterFailureEvent)) return false;
            LaunchRoleHolderUpdaterFailureEvent that = (LaunchRoleHolderUpdaterFailureEvent) o;
            return mReason == that.mReason;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mReason);
        }

        @Override
        public String toString() {
            return "LaunchRoleHolderUpdaterFailureEvent{"
                    + "mReason=" + mReason + '}';
        }
    }

    static class RoleHolderUpdaterViewModelFactory implements ViewModelProvider.Factory {
        private final Application mApplication;
        private final Utils mUtils;
        private final DeviceManagementRoleHolderUpdaterHelper mRoleHolderUpdaterHelper;

        RoleHolderUpdaterViewModelFactory(
                Application application,
                Utils utils,
                DeviceManagementRoleHolderUpdaterHelper roleHolderUpdaterHelper) {
            mApplication = requireNonNull(application);
            mRoleHolderUpdaterHelper = requireNonNull(roleHolderUpdaterHelper);
            mUtils = requireNonNull(utils);
        }

        @Override
        public <T extends ViewModel> T create(Class<T> aClass) {
            DefaultConfig config = new DefaultConfig();
            return (T) new RoleHolderUpdaterViewModel(
                    mApplication,
                    new Handler(Looper.getMainLooper()),
                    new DefaultCanLaunchRoleHolderUpdaterChecker(mUtils),
                    config,
                    mRoleHolderUpdaterHelper);
        }
    }

    interface CanLaunchRoleHolderUpdaterChecker {
        boolean canLaunchRoleHolderUpdater(Context context, Intent intent);
    }

    interface Config {
        int getLaunchRoleHolderUpdaterPeriodMillis();

        int getLaunchRoleHolderMaxRetries();

        int getRoleHolderUpdateMaxRetries();
    }

    static class DefaultConfig implements Config {

        @Override
        public int getLaunchRoleHolderUpdaterPeriodMillis() {
            return LAUNCH_ROLE_HOLDER_UPDATER_RETRY_PERIOD_MS;
        }

        @Override
        public int getLaunchRoleHolderMaxRetries() {
            return LAUNCH_ROLE_HOLDER_UPDATER_MAX_RETRIES;
        }

        @Override
        public int getRoleHolderUpdateMaxRetries() {
            return ROLE_HOLDER_UPDATE_MAX_RETRIES;
        }
    }

    static class DefaultCanLaunchRoleHolderUpdaterChecker implements
            CanLaunchRoleHolderUpdaterChecker {

        private final Utils mUtils;

        DefaultCanLaunchRoleHolderUpdaterChecker(Utils utils) {
            mUtils = requireNonNull(utils);
        }

        @Override
        public boolean canLaunchRoleHolderUpdater(Context context, Intent intent) {
            return mUtils.canResolveIntentAsUser(context, intent, UserHandle.USER_SYSTEM);
        }

    }
}
