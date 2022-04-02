package com.su.workbox.ui.app.git

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.su.workbox.AppHelper
import com.su.workbox.R
import com.su.workbox.ui.app.lib.LibActivity
import com.su.workbox.ui.base.BaseAppCompatActivity
import com.su.workbox.utils.GeneralInfoHelper
import com.su.workbox.utils.IOUtil
import com.su.workbox.utils.SearchableHelper
import com.su.workbox.utils.ThreadUtil
import com.su.workbox.widget.ToastBuilder
import com.su.workbox.widget.recycler.BaseRecyclerAdapter
import com.su.workbox.widget.recycler.PreferenceItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*

class GitActivity : BaseAppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var mAdapter: CommitAdapter
    private val TAG: String = LibActivity::class.java.simpleName
    private lateinit var mRecyclerView: RecyclerView
    private val mAllModuleList: MutableList<GitCommit> = ArrayList()
    private val mFilterModuleItems: MutableList<GitCommit> = ArrayList()
    private val mSearchableHelper = SearchableHelper(GitCommit::class.java)
    private val mScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.workbox_template_recycler_list)
        mRecyclerView = findViewById(R.id.recycler_view)
        val decoration = PreferenceItemDecoration(this, 0, 0)
        mRecyclerView.addItemDecoration(decoration)
        mAdapter = CommitAdapter(this, mFilterModuleItems)
        mRecyclerView.adapter = mAdapter

        mScope.launch(Dispatchers.IO) {
            readAssets()
            runOnUiThread { filter("") }
        }
    }

    private fun CharSequence.removeNewLineAtTail(): String {
        val length = this.length
        for (i in (length - 1) downTo 0) {
            if (this[i] != '\n') {
                return this.substring(0, i + 1)
            }
        }
         val firstProcess = ProcessBuilder("git","rev-parse --short HEAD").start()
         val error = firstProcess.errorStream.readBytes().decodeToString()
        return this.toString()
    }

    private fun readAssets(): String? {
        val filepath = "generated/git-log.txt"
        var reader: BufferedReader? = null
        var line: String? = ""
        val buf = StringBuilder()
        val manager = assets
        try {
            reader =
                BufferedReader(InputStreamReader(manager.open(filepath), StandardCharsets.UTF_8))
            var lastCommit: GitCommit? = null
            while (reader.readLine().also { line = it } != null) {
                when {
                    line!!.startsWith("commit ") -> {
                        if (lastCommit != null) {
                            lastCommit.message = buf.removeNewLineAtTail()
                            mAllModuleList.add(lastCommit)
                        }
                        buf.clear()
                        lastCommit = GitCommit()
                        lastCommit.setCommitLine(line!!)
                    }
                    line!!.startsWith("Author: ") -> {
                        lastCommit!!.authorLine = line!!
                    }
                    line!!.startsWith("Date: ") -> {
                        lastCommit!!.setDateLine(line!!)
                    }
                    line!!.startsWith("Merge: ") -> {
                        lastCommit!!.merge = line
                    }
                    else -> {
                        buf.append(line).append("\n")
                    }
                }
            }
            if (lastCommit != null) {
                lastCommit.message = buf.removeNewLineAtTail()
                mAllModuleList.add(lastCommit)
            }
        } catch (e: IOException) {
            ToastBuilder("请检查文件assets/$filepath").setDuration(Toast.LENGTH_LONG).show()
            Log.w(TAG, e)
        } finally {
            IOUtil.close(reader)
        }
        return line
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mSearchableHelper.initSearchToolbar(mToolbar, this)
        setTitle("Git日志")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mScope.isActive) {
            mScope.cancel()
        }
    }

    private fun filter(cs: CharSequence) {
        mFilterModuleItems.clear()
        mSearchableHelper.clear()
        if (TextUtils.isEmpty(cs)) {
            mFilterModuleItems.addAll(mAllModuleList)
            mAdapter.notifyDataSetChanged()
            return
        }
        for (search in mAllModuleList) {
            if (mSearchableHelper.find(cs.toString(), search)) {
                mFilterModuleItems.add(search)
            }
        }
        mAdapter.notifyDataSetChanged()
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        filter(newText)
        return false
    }

    private class CommitAdapter(private val activity: GitActivity, list: MutableList<GitCommit>) :
        BaseRecyclerAdapter<GitCommit>(list) {
        private val mSdf = ThreadUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        override fun bindData(holder: BaseViewHolder, position: Int, itemType: Int) {
            val commit = data[position]
            val hashView = holder.getView<TextView>(R.id.hash)
            val shortHashView = holder.getView<TextView>(R.id.short_hash)
            val authorView = holder.getView<TextView>(R.id.author)
            val dateView = holder.getView<TextView>(R.id.date)
            val messageView = holder.getView<TextView>(R.id.message)
            hashView.text = commit.hash
            shortHashView.text = "commit ${commit.getShortHash(8)} ${commit.extra ?: ""}"
            authorView.text = commit.authorLine
            dateView.text = mSdf.format(Date(commit.timestamp * 1000))
            messageView.text = commit.message
            activity.mSearchableHelper.refreshFilterColor(authorView, position, "authorLine")
            activity.mSearchableHelper.refreshFilterColor(messageView, position, "message")
            holder.itemView.setOnClickListener {
                val content = """${hashView.text}
                        ${System.lineSeparator()}${authorView.text}
                        ${System.lineSeparator()}${dateView.text}
                        ${System.lineSeparator()}${messageView.text}"""
                val context = GeneralInfoHelper.getContext()
                AppHelper.copyToClipboard(context, "commit", content)
                Toast.makeText(context, "已复制", Toast.LENGTH_LONG).show()
            }
        }

        override fun getLayoutId(itemType: Int): Int {
            return R.layout.workbox_item_git_commit
        }
    }

    override fun menuRes(): Int {
        return R.menu.workbox_search_menu
    }

    override fun getTag(): String = TAG
}
