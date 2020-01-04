package com.su.workbox.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.su.workbox.AppHelper;
import com.su.workbox.R;
import com.su.workbox.Workbox;
import com.su.workbox.entity.JsFunction;
import com.su.workbox.entity.NoteJsFunction;
import com.su.workbox.ui.base.BaseAppCompatActivity;
import com.su.workbox.utils.IOUtil;
import com.su.workbox.widget.ToastBuilder;
import com.su.workbox.widget.recycler.BaseRecyclerAdapter;
import com.su.workbox.widget.recycler.PreferenceItemDecoration;

import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by su on 17-10-24.
 * 调试rhino
 *
 * warning: 此类暂时不要使用lambda表达式，com.android.tools.build:gradle:3.2.1编译时会报错
 */

public class JsListActivity extends BaseAppCompatActivity implements View.OnClickListener {
    private static final String TAG = JsListActivity.class.getSimpleName();

    private File mJsDir = new File(Workbox.getWorkboxSdcardDir(), "js");
    private BottomSheetBehavior mBehavior;
    private TextView mJsFilepathView;
    private TextView mJsFileNameView;
    private EditText mJsContentView;
    private View mDeleteMenuView;
    private List<String> mGroupList = new ArrayList<>();
    private List<Functions> mFunctionsList = new ArrayList<>();
    private FileAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workbox_activity_js_list);
        if (!mJsDir.exists()) {
            mJsDir.mkdirs();
        }
        mRecyclerView = findViewById(R.id.recycler_view);
        mAdapter = new FileAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        PreferenceItemDecoration decoration = new PreferenceItemDecoration(this, 0, 0);
        mRecyclerView.addItemDecoration(decoration);

        View bottomSheet = findViewById(R.id.bottomSheet);
        mDeleteMenuView = findViewById(R.id.delete);
        mJsFilepathView = findViewById(R.id.js_filepath);
        mJsFileNameView = findViewById(R.id.js_file_name);
        mJsContentView = findViewById(R.id.content);
        findViewById(R.id.close).setOnClickListener(this);
        mBehavior = BottomSheetBehavior.from(bottomSheet);
        mBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onStateChanged(@NonNull View view, int state) {
                if (state == BottomSheetBehavior.STATE_COLLAPSED || state == BottomSheetBehavior.STATE_HIDDEN) {
                    File file = new File((String) mJsFileNameView.getTag());
                    createOrUpdateJs(file.getAbsolutePath(), mJsContentView.getText().toString());
                    loadFiles();
                    mAdapter.notifyDataSetChanged();
                    AppHelper.hideSoftInputFromWindow(getWindow());
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {}
        });
        loadFiles();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("Js函数列表");
    }

    private void loadFiles() {
        mGroupList.clear();
        mFunctionsList.clear();
        if (mJsDir.listFiles() == null) {
            return;
        }
        List<File> files = new ArrayList<>(Arrays.asList(mJsDir.listFiles()));
        String data = IOUtil.readAssetsFile(this, "generated/js.json");
        if (!TextUtils.isEmpty(data)) {
            List<NoteJsFunction> list = JSON.parseArray(data, NoteJsFunction.class);
            for (NoteJsFunction noteJsFunction : list) {
                String url = noteJsFunction.getJsFilepath().getFilepath();
                String sourceString = null;
                String filepath = "";
                if (URLUtil.isAssetUrl(url)) {
                    filepath = IOUtil.getAssetFilePath(url);
                    sourceString = IOUtil.readAssetsFile(this, filepath);
                } else if (URLUtil.isFileUrl(url)) {
                    try {
                        File file = new File(new URI(url));
                        filepath = file.getAbsolutePath();
                        sourceString = IOUtil.readFile(file);
                    } catch (URISyntaxException e) {
                        Log.w(TAG, e);
                    }
                } else if (URLUtil.isNetworkUrl(url)) {
                    sourceString = "";
                }
                if (TextUtils.isEmpty(sourceString)) {
                    new ToastBuilder("请检查文件: " + filepath).show();
                } else {
                    excludeJsFiles(files, new File(filepath));
                    parseJs(sourceString, url);
                }
            }
        }

        if (files.isEmpty()) {
            return;
        }

        String sourceString;
        for (File file : files) {
            sourceString = IOUtil.readFile(file);
            parseJs(sourceString, Uri.fromFile(file).toString());
        }
    }

    private void excludeJsFiles(List<File> files, File fromAnnotations) {
        if (files == null || files.isEmpty()) {
            return;
        }

        Iterator<File> iterator = files.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();
            try {
                if (IOUtil.isSameFile(file, fromAnnotations)) {
                    iterator.remove();
                    return;
                }
            } catch (IOException e) {
                //ignore
            }
        }
    }

    @Override
    public void onClick(View v) {
        mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private class FileAdapter extends RecyclerView.Adapter<BaseRecyclerAdapter.BaseViewHolder> {

        private Context mContext;

        FileAdapter(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public BaseRecyclerAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(viewType), parent, false);
            return new BaseRecyclerAdapter.BaseViewHolder(view);
        }

        public int getLayoutId(int itemType) {
            if (itemType == BaseRecyclerAdapter.ITEM_TYPE_GROUP) {
                return R.layout.workbox_item_group_js_file;
            } else {
                return R.layout.workbox_item_js_function;
            }
        }

        private void bindGroupData(@NonNull BaseRecyclerAdapter.BaseViewHolder holder, int position) {
            final String filepath = mGroupList.get(getPositions(position)[0]);
            TextView filenameView = holder.getView(R.id.filename);
            filenameView.setText(new File(filepath).getName());
            int[] positions = getPositions(position);
            final Functions functions = mFunctionsList.get(positions[0]);
            holder.getView(R.id.arrow).setSelected(!functions.collapse);
            //warning 暂时不要改为lambda表达式，com.android.tools.build:gradle:3.2.1编译时会报错
            holder.getView(R.id.view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    mJsFilepathView.setText(filepath);
                    try {
                        String content = "";
                        if (URLUtil.isAssetUrl(filepath)) {
                            String realFilePath = IOUtil.getAssetFilePath(filepath);
                            content = IOUtil.readAssetsFile(mContext, realFilePath);
                            mJsContentView.setEnabled(false);
                        } else if (URLUtil.isFileUrl(filepath)) {
                            try {
                                File file = new File(new URI(filepath));
                                content = IOUtil.readFile(file);
                                mJsContentView.setEnabled(true);
                            } catch (URISyntaxException e) {
                                Log.w(TAG, e);
                            }
                        }
                        File file = new File(new URI(filepath));
                        mJsFileNameView.setText(file.getName());
                        mJsFileNameView.setTag(file.getAbsolutePath());
                        mJsContentView.setText(content);
                        mDeleteMenuView.setVisibility(URLUtil.isAssetUrl(filepath) ? View.GONE : View.VISIBLE);
                        mDeleteMenuView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDeleteFileDialog(getPositions(position)[0], filepath);
                            }
                        });
                    } catch (URISyntaxException e) {
                        Log.w(TAG, e);
                    }
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    functions.collapse = !functions.collapse;
                    holder.getView(R.id.arrow).setSelected(!functions.collapse);
                    mAdapter.notifyDataSetChanged();
                }
            });
        }

        private void bindChildData(@NonNull BaseRecyclerAdapter.BaseViewHolder holder, int position) {
            int[] positions = getPositions(position);
            JsFunction jsFunction = mFunctionsList.get(positions[0]).jsFunctionList.get(positions[1]);
            TextView functionNameView = holder.getView(R.id.function_name);
            TextView parametersView = holder.getView(R.id.parameters);
            functionNameView.setText(jsFunction.getName());
            String parameters = jsFunction.getParametersString();
            if (TextUtils.isEmpty(parameters)) {
                parametersView.setText("无参数");
            } else {
                parametersView.setText(parameters);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] currentPositions = mAdapter.getPositions(holder.getAdapterPosition());
                    Functions functions = mFunctionsList.get(currentPositions[0]);
                    String filepath = functions.sourceUri;
                    JsFunction currentJsFunction = mFunctionsList.get(currentPositions[0]).jsFunctionList.get(currentPositions[1]);
                    Intent intent = new Intent(mContext, ExecJsActivity.class);
                    intent.putExtra("filepath", filepath);
                    intent.putExtra("function", currentJsFunction);
                    startActivity(intent);
                }
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
            for (Functions functions : mFunctionsList) {
                pointer++;
                if (pointer == position) {
                    return BaseRecyclerAdapter.ITEM_TYPE_GROUP;
                }
                int childrenSize = functions.collapse ? 0 : functions.jsFunctionList.size();
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
            for (Functions functions : mFunctionsList) {
                pointer++;
                groupPosition++;
                positions[0] = groupPosition;
                int childrenSize = functions.collapse ? 0 : functions.jsFunctionList.size();
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
            for (Functions functions : mFunctionsList) {
                size++;
                int childrenSize = functions.collapse ? 0 : functions.jsFunctionList.size();
                size += childrenSize;
            }
            return size;
        }
    }

    private static class Functions {
        private boolean collapse;
        private String sourceUri;
        private List<JsFunction> jsFunctionList;

        private Functions(String sourceUri, List<JsFunction> jsFunctionList) {
            this.sourceUri = sourceUri;
            this.jsFunctionList = jsFunctionList;
        }
    }

    private void parseJs(String sourceString, String sourceUri) {
        if (TextUtils.isEmpty(sourceString)) {
            mGroupList.add(sourceUri);
            mFunctionsList.add(new Functions(sourceUri, new ArrayList<>()));
            Log.d(TAG, "functions: remote js file. load later.");
        } else {
            AstNode node = new Parser().parse(sourceString, sourceUri, 0);
            FunctionNodeVisitor visitor = new FunctionNodeVisitor(sourceUri);
            node.visit(visitor);
            mGroupList.add(sourceUri);
            mFunctionsList.add(new Functions(sourceUri, visitor.getJsFunctions()));
            Log.d(TAG, "functions: " + visitor.getJsFunctions());
        }
    }

    private static class FunctionNodeVisitor implements NodeVisitor {
        private String filepath;
        private List<JsFunction> jsFunctionList = new ArrayList<>();

        private FunctionNodeVisitor(@NonNull String filepath) {
            this.filepath = filepath;
        }

        @Override
        public boolean visit(AstNode node) {
            if (node instanceof FunctionCall) {
                // How do I get the name of the function being called?
                FunctionCall functionCall = (FunctionCall) node;
                Log.d(TAG, "functionCall: " + functionCall);
            } else if (node instanceof FunctionNode) {
                FunctionNode functionNode = (FunctionNode) node;
                List<AstNode> params = functionNode.getParams();
                int size = params.size();
                Log.d(TAG, "functionNode: " + functionNode);
                if (functionNode.getFunctionName() == null) {
                    //匿名函数
                    if (params.isEmpty()) {
                        Log.w(TAG, "no params.");
                    } else {
                        for (int i = 0; i < size; i++) {
                            Name name = (Name) params.get(i);
                            Log.d(TAG, "params[" + i + "]: " + name.getIdentifier());
                        }
                    }
                    return true;
                }

                String functionName = functionNode.getFunctionName().getIdentifier();
                List<String> paramNames = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    paramNames.add(((Name) params.get(i)).getIdentifier());
                }
                JsFunction jsFunction = new JsFunction(functionName, paramNames);
                jsFunctionList.add(jsFunction);
            }
            return true;
        }

        public String getFilepath() {
            return filepath;
        }

        String getFileName() {
            return new File(filepath).getName();
        }

        List<JsFunction> getJsFunctions() {
            return jsFunctionList;
        }

        @NonNull
        @Override
        public String toString() {
            return "FunctionNodeVisitor{" +
                    "filepath='" + filepath + '\'' +
                    ", jsFunctionList=" + jsFunctionList +
                    '}';
        }
    }

    @Override
    public void onBackPressed() {
        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            super.onBackPressed();
        }
    }

    private void showDeleteFileDialog(final int groupPosition, String filepath) {
        final File file = new File(Uri.parse(filepath).getPath());
        new AlertDialog.Builder(this)
                .setMessage("确定要将" + file.getName() + "删除吗")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (file.delete()) {
                            mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            mGroupList.remove(groupPosition);
                            mFunctionsList.remove(groupPosition);
                            mAdapter.notifyDataSetChanged();
                            new ToastBuilder(file.getName() + "删除成功").show();
                        } else {
                            new ToastBuilder(file.getName() + "删除失败").show();
                        }
                    }
                })
                .show();
    }

    public void add(@NonNull MenuItem item) {
        final View v = LayoutInflater.from(this).inflate(R.layout.workbox_dialog_new_js_file, null);
        final EditText titleView = v.findViewById(R.id.title);
        final EditText contentView = v.findViewById(R.id.content);
        new AlertDialog.Builder(this)
                .setTitle("创建js文件")
                .setView(v)
                .setNegativeButton("取消", null)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tryCreateJsFile(titleView, contentView);
                    }
                })
                .show();
    }

    private void tryCreateJsFile(final EditText titleView, final EditText contentView) {
        String fileName = titleView.getText().toString();
        if (!fileName.endsWith(".js")) {
            fileName += ".js";
        }
        File file = new File(mJsDir, fileName);
        if (file.exists()) {
            new AlertDialog.Builder(JsListActivity.this)
                    .setMessage("同名js文件已存在，是否要覆盖现有js文件？")
                    .setPositiveButton("覆盖", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            createOrUpdateJs(file.getAbsolutePath(), contentView.getText().toString());
                            loadFiles();
                            mAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("", null)
                    .show();
            return;
        }
        createOrUpdateJs(file.getAbsolutePath(), contentView.getText().toString());
        loadFiles();
        mAdapter.notifyDataSetChanged();
    }

    private void createOrUpdateJs(String filepath, String content) {
        IOUtil.writeFile(filepath, content);
    }

    @Override
    public int menuRes() {
        return R.menu.workbox_add_menu;
    }

    @Override
    public String getTag() {
        return TAG;
    }
}
