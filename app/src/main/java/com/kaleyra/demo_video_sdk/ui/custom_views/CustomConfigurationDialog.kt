/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
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
package com.kaleyra.demo_video_sdk.ui.custom_views

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.kaleyra.app_configuration.model.CallOptionsType
import com.kaleyra.app_utilities.storage.ConfigurationPrefsManager
import com.kaleyra.demo_video_sdk.R
import com.kaleyra.demo_video_sdk.storage.DefaultConfigurationManager
import com.kaleyra.demo_video_sdk.ui.custom_views.CallOptionsDialogView.CallOptions
import com.kaleyra.video_common_ui.CallUI

class CustomConfigurationDialog : DialogFragment() {

    private var callConfiguration: CallConfiguration? = null
    private var callOptionsType: CallOptionsType? = null
    private val appConfiguration by lazy {
        ConfigurationPrefsManager.getConfiguration(dialog!!.context)
    }

    private val lifecycleObserver: LifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun disconnectListener() {
            removeLifecycleObserver()
            dismiss()
        }
    }

    enum class CallType {
        AUDIO_ONLY, AUDIO_UPGRADABLE, AUDIO_VIDEO
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callConfiguration = DefaultConfigurationManager.getDefaultCallConfiguration()
        callOptionsType = CallOptionsType.valueOf(requireArguments().getString("call_type", "AUDIO_VIDEO"))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (Build.VERSION.SDK_INT <= 23) {
            dialog!!.setTitle(title)
        }
        addLifecycleObserver()
        return setup()
    }

    private fun addLifecycleObserver() {
        if (activity != null && activity is AppCompatActivity) requireActivity().lifecycle.addObserver(lifecycleObserver)
    }

    private fun removeLifecycleObserver() {
        if (activity != null && activity is AppCompatActivity) requireActivity().lifecycle.removeObserver(lifecycleObserver)
    }

    override fun onResume() {
        super.onResume()
        val params = dialog!!.window!!.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog!!.window!!.attributes = params
    }

    private fun setup(): View {
        val callOptionsDialogView = CallOptionsDialogView(requireContext(), callOptionsType, callConfiguration)
        val textViewTitle = callOptionsDialogView.findViewById<TextView>(R.id.title)
        if (Build.VERSION.SDK_INT >= 23) textViewTitle.text = title else textViewTitle.visibility = View.GONE

        val actionButton = callOptionsDialogView.findViewById<Button>(R.id.action)
        actionButton.setOnClickListener {
            dismiss()
            setFragmentResult(
                "customize_configuration", bundleOf("call_configuration" to getCallConfiguration(callOptionsDialogView),
                                                    "call_type" to when {
                                                        callOptionsDialogView.isAudioOnlyCallChecked       -> CallType.AUDIO_ONLY.name
                                                        callOptionsDialogView.isAudioUpgradableCallChecked -> CallType.AUDIO_UPGRADABLE.name
                                                        callOptionsDialogView.isAudioVideoCallChecked      -> CallType.AUDIO_VIDEO.name
                                                        else                                               -> null
                                                    },
                                                    "app_configuration" to appConfiguration.apply {
                                                        this.defaultCallType = when {
                                                            callOptionsDialogView.isAudioOnlyCallChecked       -> CallOptionsType.AUDIO_ONLY
                                                            callOptionsDialogView.isAudioUpgradableCallChecked -> CallOptionsType.AUDIO_UPGRADABLE
                                                            callOptionsDialogView.isAudioVideoCallChecked      -> CallOptionsType.AUDIO_VIDEO
                                                            else                                               -> this.defaultCallType
                                                        }
                                                    }
                )
            )
        }

        callOptionsDialogView.findViewById<View>(R.id.cancel_action).setOnClickListener {
            dismiss()
            setFragmentResult("customize_configuration", bundleOf())
        }

        return callOptionsDialogView
    }

    private val title: String
        get() = "Call configuration"

    override fun dismiss() {
        removeLifecycleObserver()
        if (dialog != null) dialog!!.dismiss()
    }

    private fun getCallConfiguration(callOptionsDialogView: CallOptionsDialogView) = CallConfiguration(
        when {
            callOptionsDialogView.isAudioOnlyCallChecked       -> getCallCapabilities(callOptionsDialogView.audioOnlyCallOptionsView!!)
            callOptionsDialogView.isAudioUpgradableCallChecked -> getCallCapabilities(callOptionsDialogView.audioUpgradableCallOptionsView!!)
            callOptionsDialogView.isAudioVideoCallChecked      -> getCallCapabilities(callOptionsDialogView.audioVideoCallOptionsView!!)
            else                                               -> CallUI.Action.default.mapToConfigActions()
        }, when {
            callOptionsDialogView.isAudioOnlyCallChecked       -> getOptions(callOptionsDialogView.audioOnlyCallOptionsView!!)
            callOptionsDialogView.isAudioUpgradableCallChecked -> getOptions(callOptionsDialogView.audioUpgradableCallOptionsView!!)
            callOptionsDialogView.isAudioVideoCallChecked      -> getOptions(callOptionsDialogView.audioVideoCallOptionsView!!)
            else                                               -> CallConfiguration.CallOptions()
        }
    )

    private fun getCallCapabilities(optionView: CallOptions): Set<ConfigAction> {
        val actions = mutableSetOf<CallUI.Action>()
        actions += CallUI.Action.default
        if (optionView.isChatChecked) actions += CallUI.Action.OpenChat.Full
        if (optionView.isFileShareChecked) actions += CallUI.Action.FileShare
        if (optionView.isScreenShareChecked) actions += CallUI.Action.ScreenShare
        if (optionView.isWhiteboardChecked) actions += CallUI.Action.OpenWhiteboard.Full
        return actions.mapToConfigActions()
    }

    private fun getOptions(optionView: CallOptions) = CallConfiguration.CallOptions(
        recordingEnabled = optionView.isRecordingChecked,
        backCameraAsDefault = optionView.isBackCameraChecked,
        feedbackEnabled = optionView.isFeedbackChecked
    )

    companion object {

        @JvmStatic
        fun showCallConfigurationDialog(
            context: AppCompatActivity,
            callOptionsType: CallOptionsType
        ) {
            val f = CustomConfigurationDialog()
            val args = Bundle()
            args.putString("call_type", callOptionsType.toString())
            f.arguments = args
            f.show(context.supportFragmentManager, "configuration")
        }
    }
}