package com.su.workbox.ui.data;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.su.workbox.R;
import com.su.workbox.Workbox;
import com.su.workbox.entity.FileResults;
import com.su.workbox.entity.Line;
import com.su.workbox.ui.base.BaseAppCompatActivity;
import com.su.workbox.ui.base.BaseFragment;
import com.su.workbox.utils.AppExecutors;
import com.su.workbox.utils.GeneralInfoHelper;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseAppCompatActivity {

    public static final String TAG = SearchActivity.class.getSimpleName();
    private String mRoot;
    private String mCurrentPath;
    private SearchFragment mFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_container);
        Intent intent = getIntent();
        mRoot = intent.getStringExtra("root");
        mCurrentPath = intent.getStringExtra("current");
        mFragment = SearchFragment.newInstance(mRoot, mCurrentPath);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.root_layout, mFragment)
                .commit();
    }

    public void up(@NonNull MenuItem item) {
        File file = new File(mCurrentPath);
        if (TextUtils.equals(file.getParent(), mRoot)) {
            item.setVisible(false);
        }
        mCurrentPath = file.getParent();
        mFragment.mCurrentPath = mCurrentPath;
        mFragment.mContent = "";
        mFragment.clearResults();
        Menu menu = mToolbar.getMenu();
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        searchMenuItem.collapseActionView();
        searchMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_ALWAYS);
        mFragment.mSearchView.onActionViewCollapsed();
    }

    public static class SearchFragment extends BaseFragment implements SearchView.OnQueryTextListener {

        public static final String TAG = SearchFragment.class.getSimpleName();

        private String mRoot;
        private String mCurrentPath;
        private List<Line> mResults = new ArrayList<>();
        private String mContent;
        private Finder mCurrentFinder;
        private RecyclerView mRecyclerView;
        private ResultAdapter mAdapter;
        private List<String> mGroupList = new ArrayList<>();
        private List<FileResults> mFileResultsList = new ArrayList<>();
        private SearchView mSearchView;

        public static SearchFragment newInstance(@NonNull String root, @NonNull String current) {
            Bundle args = new Bundle();
            args.putString("root", root);
            args.putString("current", current);
            SearchFragment fragment = new SearchFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle bundle = getArguments();
            mRoot = bundle.getString("root");
            mCurrentPath = bundle.getString("current");
        }

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.workbox_template_recycler_list, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            Toolbar toolbar = view.findViewById(R.id.id_toolbar);
            toolbar.inflateMenu(R.menu.workbox_text_search_menu);

            Menu menu = toolbar.getMenu();
            if (TextUtils.equals(mRoot, mCurrentPath)) {
                menu.findItem(R.id.up).setVisible(false);
            }

            MenuItem menuItem = toolbar.getMenu().findItem(R.id.search);
            mSearchView = (SearchView) menuItem.getActionView();
            EditText searchEdit = mSearchView.findViewById(R.id.search_src_text);
            mSearchView.findViewById(R.id.search_plate)
                    .setBackgroundResource(android.R.color.transparent);
            searchEdit.setTextColor(toolbar.getResources().getColor(android.R.color.white));
            searchEdit.setHintTextColor(Color.WHITE);
            searchEdit.setBackgroundResource(android.R.color.transparent);
            mSearchView.onActionViewExpanded();
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setIconifiedByDefault(false);
            mSearchView.setQueryHint("");

            MenuItem searchMenuItem = menu.findItem(R.id.search);
            searchMenuItem.expandActionView();

            mRecyclerView = view.findViewById(R.id.recycler_view);
            mAdapter = new ResultAdapter(this, mGroupList, mFileResultsList);
            mRecyclerView.setAdapter(mAdapter);
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextSubmit(String s) {
            if (!TextUtils.equals(mContent, s)) {
                if (mCurrentFinder != null) {
                    mCurrentFinder.cancel();
                    clearResults();
                    Log.d("Finder", "new query: " + mContent);
                }
                mContent = s;
                mCurrentFinder = new Finder(this, new File(mCurrentPath), s);
                AppExecutors.getInstance()
                        .diskIO()
                        .execute(mCurrentFinder);
            }
            return false;
        }

        private synchronized void addResult(Line line) {
            if (mResults.isEmpty()) {
                mResults.add(line);
                addNewFile(line);
                return;
            }

            int lastIndex = mResults.size() - 1;
            Line last = mResults.get(lastIndex);
            Line last2 = null;
            if (mResults.size() > 1) {
                last2 = mResults.get(lastIndex - 1);
            }

            if (TextUtils.equals(last.getFilePath(), line.getFilePath())) {
                mFileResultsList.get(mFileResultsList.size() - 1).getLineList().add(line);
                if (last2 == null) {
                    Line titleLine = new Line(last.getFilePath(), true);
                    mResults.add(lastIndex, titleLine);
                    mResults.add(line);
                } else {
                    if (TextUtils.equals(last2.getFilePath(), last.getFilePath())) {
                        mResults.add(line);
                        mGroupList.add(line.getFilePath());
                        mFileResultsList.get(mFileResultsList.size() - 1).getLineList().add(line);
                    } else {
                        Line titleLine = new Line(last.getFilePath(), true);
                        mResults.add(lastIndex, titleLine);
                        mResults.add(line);
                        mFileResultsList.get(mFileResultsList.size() - 1).getLineList().add(line);
                    }
                }
            } else {
                mResults.add(line);
                addNewFile(line);
            }
        }

        private void addNewFile(Line line) {
            FileResults results = new FileResults(line.getFilePath(), !line.isText());
            results.getLineList().add(line);
            mGroupList.add(line.getFilePath());
            mFileResultsList.add(results);
        }

        private synchronized void clearResults() {
            mResults.clear();
            mGroupList.clear();
            mFileResultsList.clear();
            mAdapter.notifyDataSetChanged();
        }

        private static class ResultAdapter extends RecyclerView.Adapter<BaseRecyclerAdapter.BaseViewHolder> {

            private SearchFragment mFragment;
            private List<String> mGroupList;
            private List<FileResults> mFileResultsList;
            private File mExportedBaseDir = new File(Workbox.getWorkboxSdcardDir(), GeneralInfoHelper.getPackageName());

            ResultAdapter(SearchFragment fragment, List<String> groupList, List<FileResults> functionsList) {
                mFragment = fragment;
                mGroupList = groupList;
                mFileResultsList = functionsList;
            }

            @NonNull
            @Override
            public BaseRecyclerAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(viewType), parent, false);
                return new BaseRecyclerAdapter.BaseViewHolder(view);
            }

            public int getLayoutId(int itemType) {
                if (itemType == BaseRecyclerAdapter.ITEM_TYPE_GROUP) {
                    return R.layout.workbox_item_group_search_file;
                } else {
                    return R.layout.workbox_item_search_file;
                }
            }

            private void export(String filepath) {
                File file = new File(filepath);
                String fileDirPath = file.getParent();
                int index = fileDirPath.indexOf(mFragment.mRoot);
                String path = fileDirPath.substring(index + mFragment.mRoot.length());
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                File dir = new File(mExportedBaseDir, path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File destFile = new File(dir, file.getName());
                String msg = "已将文件" + file.getName() + "导出到" + dir.getAbsolutePath();
                IOUtil.copyDirectory(new File(filepath), destFile);
                mFragment.getActivity().runOnUiThread(() -> new ToastBuilder(msg).setDuration(Toast.LENGTH_LONG).show());
            }

            private void bindGroupData(@NonNull BaseRecyclerAdapter.BaseViewHolder holder, int position) {
                final String filepath = mGroupList.get(getPositions(position)[0]);
                TextView filenameView = holder.getView(R.id.filename);
                filenameView.setText(new File(filepath).getName());
                int[] positions = getPositions(position);
                final FileResults fileResults = mFileResultsList.get(positions[0]);
                holder.getView(R.id.arrow).setSelected(!fileResults.isCollapse());
                //warning 暂时不要改为lambda表达式，com.android.tools.build:gradle:3.2.1编译时会报错
                holder.getView(R.id.view).setOnClickListener(v -> export(filepath));
                holder.itemView.setOnClickListener(v -> {
                    fileResults.setCollapse(!fileResults.isCollapse());
                    holder.getView(R.id.arrow).setSelected(!fileResults.isCollapse());
                    notifyDataSetChanged();
                });
            }

            private void bindChildData(@NonNull BaseRecyclerAdapter.BaseViewHolder holder, int position) {
                int[] positions = getPositions(position);
                Line line = mFileResultsList.get(positions[0]).getLineList().get(positions[1]);
                TextView lineView = holder.getView(R.id.line);
                if (line.isText()) {
                    lineView.setText(line.getNumber() + ": " + line.getContent());
                } else {
                    lineView.setText("Binary file");
                }
                holder.itemView.setOnClickListener(v -> {
                    new ToastBuilder(line.getContent()).setDuration(Toast.LENGTH_LONG).show();
                    FileActivity.startActivity(mFragment.getContext(), mFragment.mRoot, line.getFilePath());
                });
            }

            @Override
            public void onBindViewHolder(@NonNull BaseRecyclerAdapter.BaseViewHolder holder, int position) {
                int type = getItemViewType(position);
                if (type == BaseRecyclerAdapter.ITEM_TYPE_GROUP) {
                    bindGroupData(holder, position);
                } else {
                    bindChildData(holder, position);
                }
            }

            @Override
            public int getItemViewType(int position) {
                int pointer = -1;
                for (FileResults functions : mFileResultsList) {
                    pointer++;
                    if (pointer == position) {
                        return BaseRecyclerAdapter.ITEM_TYPE_GROUP;
                    }
                    int childrenSize = functions.isCollapse() ? 0 : functions.getLineList().size();
                    pointer += childrenSize;
                    if (pointer >= position) {
                        return BaseRecyclerAdapter.ITEM_TYPE_NORMAL;
                    }
                }
                throw new IllegalStateException("wrong state");
            }

            private int[] getPositions(int position) {
                int[] positions = new int[2];
                int pointer = -1;
                int groupPosition = -1;
                int childPosition = -1;
                positions[0] = groupPosition;
                positions[1] = childPosition;
                for (FileResults functions : mFileResultsList) {
                    pointer++;
                    groupPosition++;
                    positions[0] = groupPosition;
                    int childrenSize = functions.isCollapse() ? 0 : functions.getLineList().size();
                    if (pointer + childrenSize >= position) {
                        childPosition = position - pointer - 1;
                        positions[1] = childPosition;
                        return positions;
                    }
                    pointer += childrenSize;
                }
                return positions;
            }

            @Override
            public int getItemCount() {
                int size = 0;
                for (FileResults functions : mFileResultsList) {
                    size++;
                    int childrenSize = functions.isCollapse() ? 0 : functions.getLineList().size();
                    size += childrenSize;
                }
                return size;
            }
        }

        private static class Finder implements Runnable {

            private volatile boolean mCancel;
            private Handler mHandler = new Handler(GeneralInfoHelper.getContext().getMainLooper());
            private File mFile;
            private String mContent;
            private SearchFragment mFragment;

            Finder(SearchFragment fragment, File file, String content) {
                this.mFragment = fragment;
                this.mFile = file;
                this.mContent = content;
            }

            @Override
            public void run() {
                Log.i(TAG, "start search " + mContent);
                searchFromFile2(mFile, mContent);
                mHandler.post(() -> {
                    mFragment.mAdapter.notifyDataSetChanged();
                    Log.i(TAG, "finish search " + mContent + ". result size: " + mFragment.mResults.size());
                });
            }

            void cancel() {
                mCancel = true;
            }

            private void searchFromFile2(@NonNull File file, @NonNull String content) {
                InputStream is;
                InputStreamReader reader;
                BufferedReader br = null;
                try {
                    Process process = Runtime.getRuntime().exec("grep -rn " + content + " " + file.getAbsolutePath());
                    is = process.getInputStream();
                    reader = new InputStreamReader(is);
                    br = new BufferedReader(reader);

                    String line;
                    while ((line = br.readLine()) != null && !mCancel) {
                        boolean binary = line.startsWith("Binary file ") && line.endsWith(" matches");
                        String filepath;
                        String resultContent;
                        int lineNo;
                        if (binary) {
                            filepath = line.substring(12, line.length() - 8);
                            lineNo = -1;
                            resultContent = line;
                        } else {
                            String[] array = line.split(":");
                            filepath = array[0];
                            lineNo = Integer.parseInt(array[1]);
                            resultContent = array[2];
                        }
                        Line resultLine = new Line(filepath, !binary, resultContent, lineNo);
                        Log.d(TAG, "line: " + resultLine);
                        mHandler.post(() -> {
                            if (!mCancel) {
                                mFragment.addResult(resultLine);
                            }
                        });
                    }
                } catch (IOException e) {
                    Log.w(TAG, e);
                } finally {
                    IOUtil.closeQuietly(br);
                }
            }
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
