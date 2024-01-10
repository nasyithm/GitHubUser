package com.nasyith.githubuser.ui.detailuser

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.nasyith.githubuser.R
import com.nasyith.githubuser.data.response.DetailUserResponse
import com.nasyith.githubuser.data.response.UserItem
import com.nasyith.githubuser.databinding.ActivityDetailUserBinding
import com.nasyith.githubuser.ui.adapter.SectionsPagerAdapter
import com.nasyith.githubuser.ui.detailuser.DetailUserViewModel.Companion.username

class DetailUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(EXTRA_USER, UserItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_USER)
        }

        username = user?.login.toString()

        val detailUserViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[DetailUserViewModel::class.java]

        detailUserViewModel.detailUser.observe(this) { detailUser ->
            if (detailUser != null) {
                setDetailUserData(detailUser)
            }
        }

        val sectionsPagerAdapter = SectionsPagerAdapter(this)
        sectionsPagerAdapter.username = user?.login.toString()
        val viewPager: ViewPager2 = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = resources.getString(TAB_TITLES[position])
        }.attach()

        detailUserViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        detailUserViewModel.isError.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                showError(it)
            }
        }
    }

    private fun setDetailUserData(detailUser: DetailUserResponse) {
        Glide.with(this)
            .load(detailUser.avatarUrl)
            .transform(CircleCrop())
            .into(binding.detailAvatar)
        binding.detailUsername.text = detailUser.login
        binding.detailName.text = detailUser.name
        binding.detailFollowers.text = getString(R.string.followers, detailUser.followers.toString())
        binding.detailFollowing.text = getString(R.string.following, detailUser.following.toString())
    }

    private fun showLoading(isLoading: Boolean) { binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_USER = "extra_user"

        @StringRes
        private val TAB_TITLES = intArrayOf(
            R.string.tab_text_followers,
            R.string.tab_text_following
        )
    }
}