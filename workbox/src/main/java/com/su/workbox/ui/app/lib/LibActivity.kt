package com.su.workbox.ui.app.lib

import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.fastjson.TypeReference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.su.workbox.R
import com.su.workbox.entity.MavenArtifact
import com.su.workbox.entity.Repositories
import com.su.workbox.net.RequestHelper
import com.su.workbox.net.SimpleCallback
import com.su.workbox.ui.base.BaseAppCompatActivity
import com.su.workbox.utils.IOUtil
import com.su.workbox.utils.SearchableHelper
import com.su.workbox.utils.ThreadUtil
import com.su.workbox.widget.recycler.BaseRecyclerAdapter
import com.su.workbox.widget.recycler.BaseRecyclerAdapter.BaseViewHolder
import com.su.workbox.widget.recycler.PreferenceItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class LibActivity : BaseAppCompatActivity(), SearchView.OnQueryTextListener {

    private val TAG: String = LibActivity::class.java.simpleName
    private val PATTERN_GOOGLE = Pattern.compile("<([\\w-]+) versions=\"(.*)\"/>")
    private val PATTERN_MAVEN_LATEST = Pattern.compile("<latest>(.*)</latest>")
    private val PATTERN_MAVEN_RELEASE = Pattern.compile("<release>(.*)</release>")
    private val PATTERN_MAVEN_REAL_RELEASE = Pattern.compile("<version>(.*)</version>")
    private val mArtifactServerList: Vector<MavenArtifact> = Vector()
    private val mAssetsList: MutableList<Module> = ArrayList()
    private val mNameFilterColorIndexList: MutableList<Map<Int, Int>> = ArrayList()
    private val mSearchableHelper = SearchableHelper()
    private val mScope = MainScope()
    private val mAllModuleList: MutableList<ModuleItem> = ArrayList()
    private val mFilterModuleItems: MutableList<ModuleItem> = ArrayList()
    private lateinit var mAdapter: ModuleAdapter
    private lateinit var mRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.workbox_template_recycler_list)
        mRecyclerView = findViewById(R.id.recycler_view)
        mAdapter = ModuleAdapter(this)
        val decoration = PreferenceItemDecoration(this, 0, 0)
        mRecyclerView.addItemDecoration(decoration)
        mRecyclerView.adapter = mAdapter
        val semaphore = Semaphore(4)
        mScope.launch(Dispatchers.IO) {
            readAssets()
            runOnUiThread { filter("") }
            for (moduleItem in mAllModuleList) {
                for (lib in moduleItem.libList) {
                    if (!mScope.isActive) {
                        return@launch
                    }
                    if (!TextUtils.isEmpty(lib.artifactLatestVersion) || !TextUtils.isEmpty(lib.artifactLatestStableVersion)) {
                        Log.d(TAG, "ignored: ${lib.groupId}:${lib.artifactId}")
                        continue
                    }
                    mScope.launch(Dispatchers.IO) {
                        semaphore.acquire()
                        search(lib.groupId, lib.artifactId, moduleItem.repositoryList)
                        semaphore.release()
                    }
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mSearchableHelper.initSearchToolbar(mToolbar, this)
        setTitle("项目依赖")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mScope.isActive) {
            mScope.cancel()
        }
    }

    private fun readAssets() {
        val data = IOUtil.readAssetsFile(this, "generated/dependencies.json")
        val downloadsFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        IOUtil.writeFile("${downloadsFile.absoluteFile}/debug-${packageName}-dependencies.json", data)
        runOnUiThread { Toast.makeText(this, "GSON: ${downloadsFile.absoluteFile}/debug-${packageName}-dependencies.json", Toast.LENGTH_LONG).show() }
        val type: Type = object : TypeToken<ArrayList<Module>>() {}.type
        val moduleList = Gson().fromJson<ArrayList<Module>>(data, type)
        moduleList.sort()
        mAssetsList.addAll(moduleList)
        for (module in moduleList) {
            module.libs.sort()
            val list = ArrayList<MavenArtifact>()
            for (lib in module.libs) {
                val artifact = MavenArtifact()
                artifact.groupId = lib.groupId
                artifact.artifactId = lib.artifactId
                artifact.artifactVersion = lib.version
                list.add(artifact)
            }
            for (repository in module.repositories) {
                if (repository.url == null) {
                    continue
                }
                if (!repository.url!!.endsWith("/")) {
                    repository.url += "/"
                }
            }
            val moduleItem = ModuleItem(module.name, list, module.repositories)
            mAllModuleList.add(moduleItem)
        }
    }

    private fun search(groupId: String, artifactId: String = "", repoList: List<Repository>) {
        for (repo in repoList) {
            // flatDir
            if (repo.url == null) {
                continue
            }
            val scheme = Uri.parse(repo.url).scheme
            if (scheme?.equals("file", ignoreCase = true) == true) {
                Log.d(TAG, "local repo: ${repo.url}")
                continue
            }

            val repository = Repositories.getRepository(repo.url)
            if (repository == Repositories.GOOGLE) {
                queryFromGoogle(groupId, repo.url!!)
            } else if (repository == Repositories.JCENTER) {
                queryFromJCenter(groupId, artifactId, repo.url!!)
            } else if (repository == Repositories.MAVEN_CENTER
                || repository == Repositories.JITPACK
                || repository == Repositories.ALIBABA_NEXUS
                || repository == Repositories.ALIBABA) {
                queryFromMaven(groupId, artifactId, repo.url!!)
            } else if (repository == Repositories.LOCAL) {
                val artifact = MavenArtifact()
                artifact.groupId = groupId
                artifact.artifactId = artifactId
                artifact.repository = repo.url
                runOnUiThread { merge(listOf(artifact)) }
            }
        }
    }

    private fun queryFromMaven(groupId: String, artifactId: String, repo: String) {
        val url = Repositories.makeUrl(repo, groupId, artifactId)
        RequestHelper.getRequest(url, "GET", object : TypeReference<String?>() {}, object : SimpleCallback<String?>() {
            override fun onResponseSuccessful(xml: String?) {
                if (!TextUtils.isEmpty(xml)) {
                    val result = parseXml(url, groupId, artifactId, xml!!)
                    Log.d(TAG, "size: ${result.size}")
                    runOnUiThread { merge(result) }
                }
            }
        }.showErrorToast(false).setHandler(null)).execute()
    }

    private fun queryFromJCenter(groupId: String, artifactId: String, repo: String) {
        val url = Repositories.makeUrl(repo, groupId, artifactId)
        RequestHelper.getRequest(url, "GET", object : TypeReference<String?>() {}, object : SimpleCallback<String?>() {
            override fun onResponseSuccessful(xml: String?) {
                if (!TextUtils.isEmpty(xml)) {
                    val result = parseXml(url, groupId, artifactId, xml!!)
                    Log.d(TAG, "size: ${result.size}")
                    runOnUiThread { merge(result) }
                }
            }
        }.showErrorToast(false).setHandler(null)).execute()
    }

    private fun queryFromGoogle(groupId: String, repo: String) {
        var realGroupId: String = groupId
        if (!groupId.startsWith("androidx.")
                && !groupId.startsWith("android.")
                && !groupId.startsWith("com.android.")
                && !groupId.startsWith("com.google.android.")) {
            return
        }
        if (TextUtils.equals(groupId, "android.support.design")) {
            realGroupId = "com.android.support"
        }

        val url = Repositories.makeUrl(repo, groupId, null)
        RequestHelper.getRequest(url, "GET", object : TypeReference<String?>() {}, object : SimpleCallback<String?>() {
            override fun onResponseSuccessful(xml: String?) {
                if (!TextUtils.isEmpty(xml)) {
                    val result = parseGoogleXml(url, realGroupId, xml!!)
                    Log.d(TAG, "group size: ${result.size}")
                    runOnUiThread { merge(result) }
                }
            }
        }.showErrorToast(false).setHandler(null)).execute()
    }

    private fun parseXml(url: String, groupId: String, artifactId: String, xml: String): List<MavenArtifact> {
        val list: MutableList<MavenArtifact> = ArrayList()
        val artifact = MavenArtifact()
        var latestMatcher: Matcher = PATTERN_MAVEN_LATEST.matcher(xml)
        var artifactLatestVersion: String? = null
        while (latestMatcher.find()) {
            artifactLatestVersion = latestMatcher.group(1)
            break
        }
        if (TextUtils.isEmpty(artifactLatestVersion)) {
            latestMatcher = PATTERN_MAVEN_REAL_RELEASE.matcher(xml)
            while (latestMatcher.find()) {
                artifactLatestVersion = latestMatcher.group(1)
            }
        }
        //release标签未必都是stable版本
        val releaseMatcher: Matcher = PATTERN_MAVEN_RELEASE.matcher(xml)
        var releaseLatestVersion: String? = null
        var realStable = true
        while (releaseMatcher.find()) {
            releaseLatestVersion = releaseMatcher.group(1)
            if (releaseLatestVersion?.contains(Regex("(?<![a-z])(?:alpha|beta|rc)(?![a-z])", RegexOption.IGNORE_CASE)) == true) {
                realStable = false
            }
            break
        }

        var last: String? = null
        val stableMatcher: Matcher = PATTERN_MAVEN_REAL_RELEASE.matcher(xml)
        if (!realStable) {
            while (stableMatcher.find()) {
                val result = stableMatcher.group(1)
                if (result?.contains(Regex("(?<![a-z])(?:alpha|beta|rc)(?![a-z])", RegexOption.IGNORE_CASE)) == false) {
                    last = result
                }
            }
            releaseLatestVersion = last
        }

        artifact.groupId = groupId
        artifact.artifactId = artifactId
        artifact.repository = url
        artifact.artifactLatestVersion = artifactLatestVersion
        artifact.artifactLatestStableVersion = releaseLatestVersion
        artifact.time = System.currentTimeMillis()
        list.add(artifact)
        mArtifactServerList.add(artifact)
        return list
    }

    private fun parseGoogleXml(url: String, groupId: String, xml: String): List<MavenArtifact> {
        val list: MutableList<MavenArtifact> = ArrayList()
        val matcher: Matcher = PATTERN_GOOGLE.matcher(xml)
        while (matcher.find()) {
            val artifact = MavenArtifact()
            val artifactId = matcher.group(1)
            val versions = matcher.group(2)
            artifact.groupId = groupId
            artifact.artifactId = artifactId
            artifact.artifactVersions = versions
            artifact.repository = url
            artifact.artifactLatestVersion = MavenArtifact.getArtifactLatestVersion(versions)
            artifact.artifactLatestStableVersion = MavenArtifact.getArtifactLatestStableVersion(versions?.split(",")?.toTypedArray())
            artifact.time = System.currentTimeMillis()
            list.add(artifact)
        }
        Log.d(TAG, "xml: " + list.size)
        return list
    }

    private fun merge(list: List<MavenArtifact>) {
        loop@ for (mavenArtifact in list) {
            for (moduleItem in mAllModuleList) {
                val libList = moduleItem.libList
                for (artifact in libList) {
                    if (!TextUtils.equals(artifact.groupId, mavenArtifact.groupId)
                        || !TextUtils.equals(artifact.artifactId, mavenArtifact.artifactId)) {
                        continue
                    }

                    if (MavenArtifact.versionCompare(artifact.artifactVersion
                            ?: "", mavenArtifact.artifactLatestStableVersion ?: "") > 0) {
                        Log.d(TAG, "not newest: $mavenArtifact")
                        continue@loop
                    }

                    var changed = false
                    if (!TextUtils.equals(artifact.artifactLatestStableVersion, mavenArtifact.artifactLatestStableVersion)) {
                        artifact.artifactLatestStableVersion = mavenArtifact.artifactLatestStableVersion
                        changed = true
                    }
                    if (!TextUtils.equals(artifact.artifactLatestVersion, mavenArtifact.artifactLatestVersion)) {
                        artifact.artifactLatestVersion = mavenArtifact.artifactLatestVersion
                        changed = true
                    }
                    if (!TextUtils.equals(artifact.artifactVersions, mavenArtifact.artifactVersions)) {
                        artifact.artifactVersions = mavenArtifact.artifactVersions
                        changed = true
                    }
                    if (!TextUtils.equals(artifact.repository, mavenArtifact.repository)) {
                        artifact.repository = mavenArtifact.repository
                        changed = true
                    }
                    if (artifact.time != mavenArtifact.time) {
                        artifact.time = mavenArtifact.time
                        changed = true
                    }
                    if (!changed) {
                        continue
                    }
                    Log.d(TAG, "merge: $artifact")
                    notifyRecyclerView(artifact)
                }
            }
        }
    }

    private fun notifyRecyclerView(artifact: MavenArtifact) {
        var index = -1
        for (i in 0 until mFilterModuleItems.size) {
            index++
            val libList = mFilterModuleItems[i].libList
            Log.d("moduleItem", "size: ${libList.size}")
            for (j in 0 until libList.size) {
                val mavenArtifact = libList[j]
                if (TextUtils.equals(artifact.groupId, mavenArtifact.groupId)
                        && TextUtils.equals(artifact.artifactId, mavenArtifact.artifactId)) {
                    index += j + 1
                    mAdapter.notifyItemChanged(index)
                    return
                }
            }
        }
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        for (fileItem in mAllModuleList) {
            fileItem.collapse = false
        }
        filter(newText)
        return false
    }

    private fun filter(cs: CharSequence) {
        mFilterModuleItems.clear()
        mNameFilterColorIndexList.clear()
        if (TextUtils.isEmpty(cs)) {
            mFilterModuleItems.addAll(mAllModuleList)
            mAdapter.notifyDataSetChanged()
            return
        }
        for (fileItem in mAllModuleList) {
            if (fileItem.collapse) {
                continue
            }
            val libList: MutableList<MavenArtifact> = ArrayList()
            val newFileItem = ModuleItem(fileItem.name, libList, fileItem.repositoryList)
            val libs = fileItem.libList
            var has = false
            for (lib in libs) {
                val usedInfo: String = if (TextUtils.isEmpty(lib.artifactVersion)) {
                    lib.groupId + ":" + lib.artifactId
                } else {
                    lib.groupId + ":" + lib.artifactId + ":" + lib.artifactVersion
                }

                if (mSearchableHelper.find(cs, usedInfo, mNameFilterColorIndexList)) {
                    libList.add(lib)
                    if (!has) {
                        mFilterModuleItems.add(newFileItem)
                        has = true
                    }
                }
            }
        }
        mAdapter.notifyDataSetChanged()
    }

    private class ModuleAdapter(private val activity: LibActivity) : RecyclerView.Adapter<BaseViewHolder>() {
        private val mFormat: SimpleDateFormat = ThreadUtil.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        private var mResources: Resources = activity.resources

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(getLayoutId(viewType), parent, false)
            return BaseViewHolder(view)
        }

        fun getLayoutId(itemType: Int): Int {
            return if (itemType == BaseRecyclerAdapter.ITEM_TYPE_GROUP) {
                R.layout.workbox_item_function_file
            } else {
                R.layout.workbox_item_lib
            }
        }

        private fun bindGroupData(holder: BaseViewHolder, position: Int) {
            val moduleItem: ModuleItem = activity.mFilterModuleItems[getPositions(position)[0]]
            val filenameView = holder.getView<TextView>(R.id.filename)
            filenameView.text = moduleItem.name
            holder.getView<View>(R.id.arrow).isSelected = !moduleItem.collapse
            holder.itemView.setOnClickListener {
                moduleItem.collapse = !moduleItem.collapse
                holder.getView<View>(R.id.arrow).isSelected = !moduleItem.collapse
                Log.d("moduleItem", "collapse: ${moduleItem.collapse}")
                activity.filter(activity.mSearchableHelper.queryText)
            }
        }

        private fun bindChildData(holder: BaseViewHolder, position: Int) {
            val positions = getPositions(position)
            val moduleItem: ModuleItem = activity.mFilterModuleItems[positions[0]]
            val libItem = moduleItem.libList[positions[1]]

            val usedInfoView: TextView = holder.getView(R.id.used_info)
            val stableNewestView: TextView = holder.getView(R.id.stable_newest)
            val unstableNewestView: TextView = holder.getView(R.id.unstable_newest)
            val timeView: TextView = holder.getView(R.id.time)
            val repoView: TextView = holder.getView(R.id.repo)

            val usedInfo: String
            usedInfo = if (TextUtils.isEmpty(libItem.artifactVersion)) {
                libItem.groupId + ":" + libItem.artifactId
            } else {
                libItem.groupId + ":" + libItem.artifactId + ":" + libItem.artifactVersion
            }
            usedInfoView.text = usedInfo

            val stableNewest: String = libItem.artifactLatestStableVersion ?: "-"
            val unstableNewest: String = libItem.artifactLatestVersion ?: "-"
            if (TextUtils.isEmpty(libItem.artifactVersion)) {
                libItem.artifactVersion = "-"
            }
            val newest: Boolean
            newest = if (TextUtils.equals(stableNewest, "-")) {
                true
            } else if (TextUtils.equals(libItem.artifactVersion, "-")) {
                false
            } else {
                TextUtils.equals(libItem.artifactVersion, stableNewest)
            }
            if (TextUtils.equals(stableNewest, unstableNewest)) {
                unstableNewestView.text = activity.getString(R.string.workbox_unstable_newest, unstableNewest)
                stableNewestView.visibility = View.GONE
            } else {
                unstableNewestView.text = activity.getString(R.string.workbox_unstable_newest, unstableNewest)
                stableNewestView.text = activity.getString(R.string.workbox_stable_newest, stableNewest)
                stableNewestView.visibility = View.VISIBLE
            }
            if (newest) {
                unstableNewestView.setTextColor(mResources.getColor(R.color.workbox_second_text))
                stableNewestView.setTextColor(mResources.getColor(R.color.workbox_second_text))
            } else {
                if (TextUtils.equals(stableNewest, unstableNewest)) {
                    unstableNewestView.setTextColor(mResources.getColor(R.color.workbox_color_accent))
                } else {
                    unstableNewestView.setTextColor(mResources.getColor(R.color.workbox_second_text))
                    stableNewestView.setTextColor(mResources.getColor(R.color.workbox_color_accent))
                }
            }
            if (libItem.time == 0L) {
                timeView.text = activity.getString(R.string.workbox_update_time, "-")
            } else {
                timeView.text = activity.getString(R.string.workbox_update_time, mFormat.format(Date(libItem.time)))
            }

            val repositoryString = if (TextUtils.isEmpty(libItem.repository)) {
                null
            } else {
                val repository = Repositories.getRepository(libItem.repository)
                repository?.name ?: libItem.repository
            }

            if (TextUtils.isEmpty(repositoryString)) {
                repoView.visibility = View.GONE
            } else {
                repoView.visibility = View.VISIBLE
                repoView.text = repositoryString
            }

            val colorIndex = position - positions[0] - 1
            activity.mSearchableHelper.refreshFilterColor(usedInfoView, colorIndex, activity.mNameFilterColorIndexList)
        }

        override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
            val type = getItemViewType(position)
            if (type == BaseRecyclerAdapter.ITEM_TYPE_GROUP) {
                bindGroupData(holder, position)
            } else {
                bindChildData(holder, position)
            }
        }

        override fun getItemViewType(position: Int): Int {
            var pointer = -1
            for (fileItem in activity.mFilterModuleItems) {
                pointer++
                if (pointer == position) {
                    return BaseRecyclerAdapter.ITEM_TYPE_GROUP
                }
                val childrenSize = if (fileItem.collapse) 0 else fileItem.libList.size
                pointer += childrenSize
                if (pointer >= position) {
                    return BaseRecyclerAdapter.ITEM_TYPE_NORMAL
                }
            }
            throw IllegalStateException("wrong state")
        }

        private fun getPositions(position: Int): IntArray {
            val positions = IntArray(2)
            var pointer = -1
            var groupPosition = -1
            var childPosition = -1
            positions[0] = groupPosition
            positions[1] = childPosition
            for (fileItem in activity.mFilterModuleItems) {
                pointer++
                groupPosition++
                positions[0] = groupPosition
                val childrenSize = if (fileItem.collapse) 0 else fileItem.libList.size
                if (pointer + childrenSize >= position) {
                    childPosition = position - pointer - 1
                    positions[1] = childPosition
                    return positions
                }
                pointer += childrenSize
            }
            return positions
        }

        override fun getItemCount(): Int {
            var size = 0
            for (fileItem in activity.mFilterModuleItems) {
                size++
                val childrenSize = if (fileItem.collapse) 0 else fileItem.libList.size
                size += childrenSize
            }
            return size
        }
    }

    private class ModuleItem constructor(val name: String, val libList: List<MavenArtifact>, val repositoryList: List<Repository>) {
        var collapse = false

        override fun toString(): String {
            return "FileItem{" +
                    "injectName='" + name + '\'' +
                    ", methodItemList=" + libList +
                    '}'
        }
    }

    override fun menuRes(): Int = R.menu.workbox_search_menu

    override fun getTag(): String = TAG
}
