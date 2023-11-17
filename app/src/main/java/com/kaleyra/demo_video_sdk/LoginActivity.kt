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
package com.kaleyra.demo_video_sdk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.kaleyra.app_configuration.activities.ConfigurationActivity.Companion.show
import com.kaleyra.app_utilities.BuildConfig
import com.kaleyra.app_utilities.storage.ConfigurationPrefsManager.getConfiguration
import com.kaleyra.app_utilities.storage.LoginManager.getLoggedUser
import com.kaleyra.app_utilities.storage.LoginManager.isUserLogged
import com.kaleyra.app_utilities.storage.LoginManager.login
import com.kaleyra.demo_video_sdk.R.drawable
import com.kaleyra.demo_video_sdk.R.id
import com.kaleyra.demo_video_sdk.R.layout
import com.kaleyra.demo_video_sdk.R.string
import com.kaleyra.demo_video_sdk.databinding.ActivityLoginBinding
import com.kaleyra.demo_video_sdk.ui.activities.CollapsingToolbarActivity
import com.kaleyra.demo_video_sdk.ui.adapter_items.UserItem
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.extensions.ExtensionsFactories.register
import com.mikepenz.fastadapter.listeners.ItemFilterListener
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter.select.SelectExtensionFactory
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * This activity will allow you to choose a user from your company to use to interact with other users.
 *
 *
 * The list of users you can choose from will be displayed using the FastAdapter library to populate the  RecyclerView
 *
 *
 * For more information about how it works FastAdapter:
 * https://github.com/mikepenz/FastAdapter
 */
class LoginActivity : CollapsingToolbarActivity(), OnQueryTextListener {
    private var binding: ActivityLoginBinding? = null
    var searchView: SearchView? = null
    private val itemAdapter = ItemAdapter.items<UserItem>()
    private val fastAdapter = FastAdapter.with(itemAdapter)

    // the userAlias is the identifier of the created user via Bandyer-server restCall see https://docs.bandyer.com/Bandyer-RESTAPI/#create-user
    private var userAlias: String? = ""
    private val usersList = ArrayList<UserItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_login)
        binding = ActivityLoginBinding.bind(window.decorView)
        val header = findViewById<ImageView>(R.id.headerView)
        if (!BuildConfig.DEBUG) header.setImageResource(drawable.landing_image)

        // customize toolbar
        val title = resources.getString(string.login_title)
        setCollapsingToolbarTitle(title, title)

        // set the recyclerView
        binding!!.listUsers.adapter = fastAdapter
        binding!!.listUsers.itemAnimator = null
        binding!!.listUsers.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding!!.listUsers.layoutManager = LinearLayoutManager(this)
        register(SelectExtensionFactory())
        val selectExtension = fastAdapter.getOrCreateExtension<SelectExtension<UserItem>>(SelectExtension::class.java)!!
        selectExtension.isSelectable = true
        fastAdapter.onPreClickListener = { view: View?, userItemIAdapter: IAdapter<UserItem>?, userItem: UserItem, integer: Int? ->
            userAlias = userItem.userAlias
            if (!isUserLogged(this@LoginActivity)) login(this@LoginActivity, userAlias!!)
            hideKeyboard(true)
            MainActivity.show(this@LoginActivity)
            finish()
            false
        }
        itemAdapter.itemFilter.filterPredicate = { userSelectionItem: UserItem, constraint: CharSequence? -> userSelectionItem.userAlias.lowercase(Locale.getDefault()).contains(constraint.toString().lowercase(Locale.getDefault())) }
        itemAdapter.itemFilter.itemFilterListener = object : ItemFilterListener<UserItem> {
            override fun itemsFiltered(constraint: CharSequence?, results: List<UserItem>?) {
                if (results?.isNotEmpty() == true) binding!!.noResults.visibility = View.GONE else binding!!.noResults.visibility = View.VISIBLE
            }

            override fun onReset() {
                binding!!.noResults.visibility = View.GONE
            }

        }
    }

    override fun onResume() {
        super.onResume()
        // the userAlias is the identifier of the created user via Bandyer-server restCall see https://docs.bandyer.com/Bandyer-RESTAPI/#create-user
        userAlias = getLoggedUser(this)

        // If the user is already logged init the call client and do not fetch the sample users again.
        if (userAlias!!.isNotBlank()) {
            MainActivity.show(this@LoginActivity)
            return
        }
        if (usersList.isEmpty()) onRefresh()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.login, menu)
        searchView = menu.findItem(id.searchLogin).actionView as SearchView?
        searchView!!.setOnSearchClickListener { v: View? -> (findViewById<View>(id.appbar_toolbar) as AppBarLayout).setExpanded(false, true) }
        searchView!!.queryHint = getString(string.search)
        searchView!!.setOnQueryTextListener(this)
        menu.findItem(id.action_settings).setOnMenuItemClickListener { item: MenuItem? ->
            val configuration = getConfiguration(this)
            show(this, configuration, configuration.isMockConfiguration())
            true
        }
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        itemAdapter.filter(newText)
        return true
    }

    override fun onRefresh() {
        itemAdapter.clear()
        binding!!.loading.visibility = View.VISIBLE
        // Fetch the sample users you can use to login with.
        lifecycleScope.launch {
            binding!!.loading.visibility = View.GONE
            usersList.clear()
            restApi.listUsers()
                .map { UserItem(it) }
                .forEach { user ->
                    // Add each user(except the logged one) to the recyclerView adapter to be displayed in the list.
                    usersList.add(user)
                    setRefreshing(false)
                    itemAdapter.set(usersList)
                    if (searchView != null) itemAdapter.filter(searchView!!.query)
                }
        }
    }

    companion object {
        @JvmStatic fun show(context: Activity) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }
}