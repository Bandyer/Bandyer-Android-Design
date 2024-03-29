/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
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

package com.kaleyra.demo_collaboration_suite_ui

import android.os.Bundle
import android.text.format.DateUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite_phone_ui.chat.adapter_items.message.text.KaleyraChatTextMessage
import com.kaleyra.collaboration_suite_phone_ui.chat.adapter_items.message.text.KaleyraChatTextMessageItem
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatUnreadMessagesWidget
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import java.util.*

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        initializeUI()
        initializeListener()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPreparePanel(featureId: Int, view: View?, menu: Menu): Boolean {
        kotlin.runCatching {
            menu.javaClass.takeIf { it.simpleName == "MenuBuilder" }?.getDeclaredMethod("setOptionalIconsVisible", java.lang.Boolean.TYPE)?.apply {
                isAccessible = true
                invoke(menu, true)
            }
        }
        return super.onPreparePanel(featureId, view, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun initializeUI() {
        initializeActionBar()
        initializeChatUI()
        initializeChatInfoWidget()
        initializeChatUnreadMessagesWidget()
    }

    private fun initializeActionBar() {
        setSupportActionBar(findViewById<MaterialToolbar>(R.id.toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun initializeChatUI() {
        val messages = findViewById<RecyclerView>(R.id.message_recycler_view)
        val fastItemAdapter = ItemAdapter<KaleyraChatTextMessageItem>()
        val fastAdapter = FastAdapter.with(fastItemAdapter)
        messages.adapter = fastAdapter
        messages.layoutManager = LinearLayoutManager(this)
        messages.addItemDecoration(ChatRecyclerViewItemDecorator())

        val timestamp = Calendar.getInstance().timeInMillis - 8640000000

        fastItemAdapter.add(KaleyraChatTextMessageItem(KaleyraChatTextMessage("How is the weather today?", timestamp, mine = true, pending = true, sent = false) { true }))
        fastItemAdapter.add(KaleyraChatTextMessageItem(KaleyraChatTextMessage("It's a little cloudy", timestamp, mine = false, pending = false, sent = true) { false }))
        fastItemAdapter.add(KaleyraChatTextMessageItem(KaleyraChatTextMessage("Have a nice weekend!", timestamp, mine = true, pending = true, sent = false) { false }))

        findViewById<MaterialTextView>(R.id.timestamp_message).text = DateUtils.getRelativeTimeSpanString(timestamp)
    }

    private fun initializeChatInfoWidget() {
        val chatInfoWidget = findViewById<KaleyraChatInfoWidget>(R.id.chat_info)
        chatInfoWidget.state = KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.ONLINE()
        chatInfoWidget.setName("Keanu Reeves")
        chatInfoWidget.contactNameView?.visibility = View.VISIBLE
    }

    private fun initializeChatUnreadMessagesWidget() {
        val unreadMessageWidget = findViewById<KaleyraChatUnreadMessagesWidget>(R.id.unread_messages_widget)
        unreadMessageWidget.incrementUnreadMessages(27)
    }

    private fun initializeListener() {
        val chatInfoWidget = findViewById<KaleyraChatInfoWidget>(R.id.chat_info)
        chatInfoWidget.setOnClickListener {
            chatInfoWidget.state = when (chatInfoWidget.state) {
                is KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.WAITING_FOR_NETWORK -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.CONNECTING()
                is KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.CONNECTING -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.ONLINE()
                is KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.ONLINE -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.TYPING()
                is KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.TYPING -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.OFFLINE()
                else -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.WAITING_FOR_NETWORK()
            }
        }
    }
}