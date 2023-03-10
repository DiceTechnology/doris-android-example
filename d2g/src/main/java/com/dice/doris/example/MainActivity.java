package com.dice.doris.example;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dice.shield.downloads.DownloadProvider;
import com.dice.shield.downloads.DownloadProviderImpl;
import com.dice.shield.downloads.VideoQuality;
import com.dice.shield.downloads.entity.AssetData;
import com.dice.shield.downloads.entity.DownloadState;
import com.dice.shield.downloads.entity.DownloadUpdateInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends FragmentActivity {

    private static final String TAG = MainActivity.class.getName();

    private static final String BASE_URL = "https://dce-frontoffice.imggaming.com";
    private static final String DEVICE_ID = "device-id";
    private static final String REALM = "";
    private static final String API_KEY = "";
    private static final String AUTH_TOKEN = "";

    private DownloadProvider downloadProvider;
    private AssetDataAdapter assetDataAdapter;
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        compositeDisposable = new CompositeDisposable();

        // Setup download provider.
        setupDownloadProvider();

        // Initialize list.
        initializeRecyclerView();

        // Download button action.
        findViewById(R.id.button_download).setOnClickListener(view -> {
            EditText editText = findViewById(R.id.edit_text);
            onVideoDownload(editText.getText().toString());
        });

        // Fetch and show all downloads in the list.
        showAllDownloads();

        // Listen for any changes.
        listenForDownloadChanges();
    }

    private void initializeRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        assetDataAdapter = new AssetDataAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(assetDataAdapter);
    }

    private void setupDownloadProvider() {
        downloadProvider = DownloadProviderImpl.getInstance(this);
        HashMap<String, String> headers = new HashMap<>(); // Additional headers, if needed.
        downloadProvider.setup(BASE_URL, REALM, API_KEY, DEVICE_ID, headers);
        downloadProvider.setToken(AUTH_TOKEN, () -> {
            Log.d(TAG, "Token expired");
            showError("Token expired");
            // TODO call setToken again with a valid token.
        });
    }

    private void showAllDownloads() {
        compositeDisposable.add(downloadProvider.getAllDownloads()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(assetData -> {
                    Log.d(TAG, "Get all downloads: " + assetData.size());
                    assetDataAdapter.setAssetData(assetData);
                    assetDataAdapter.notifyDataSetChanged();
                    renewLicenseIfNeeded(assetData);
                }, e -> {
                    Log.d(TAG, "Error - Get all downloads", e);
                    showError(e.getMessage());
                }));
    }

    private void renewLicenseIfNeeded(@NonNull List<AssetData> assetData) {
        compositeDisposable.add(Observable.fromIterable(assetData)
                .filter(asset -> asset.getExpiryDate() < System.currentTimeMillis() / 1000)
                .doOnNext(asset -> Log.d(TAG, "License expired for: " + asset.getId()))
                .flatMap(asset -> downloadProvider.renewLicense(asset.getId()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(ok -> Log.d(TAG, "License renewed successfully."),
                        e -> {
                            Log.d(TAG, "License renewal failed.");
                            showError(e.getMessage());
                        })
        );
    }

    private void listenForDownloadChanges() {
        compositeDisposable.add(
                downloadProvider.getDownloadsObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(this::updateList,
                                e -> {
                                    Log.d(TAG, "listenForDownloadChanges: error", e);
                                    showError(e.getMessage());
                                })
        );
    }

    private void updateList(DownloadUpdateInfo downloadUpdate) {
        if (assetDataAdapter.getAssetData() == null) {
            return;
        }
        List<AssetData> toRemove = new ArrayList<>();
        for (AssetData assetData : assetDataAdapter.getAssetData()) {
            if (assetData.getId().equals(downloadUpdate.getAssetId())) {
                assetData.setDownloadState(downloadUpdate.getState());
                assetData.setDownloadProgress(downloadUpdate.getProgress());
                if (assetData.getDownloadState() == DownloadState.NOT_DOWNLOADED) {
                    toRemove.add(assetData);
                }
            }
        }
        assetDataAdapter.getAssetData().removeAll(toRemove);
        // TODO not efficient, use more specific notify methods or DiffUtil.
        assetDataAdapter.notifyDataSetChanged();
    }

    private void onVideoSelected(@NonNull AssetData assetData) {
        if (assetData.getDownloadState() != DownloadState.DOWNLOADED) {
            showError("Can't be played, not downloaded.");
            return;
        }
        Intent intent = new Intent(this, PlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("video", VideoItem.fromAssetData(assetData));
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void onVideoDownload(String vodId) {
        Log.d(TAG, "onVideoDownload " + vodId);
        compositeDisposable.add(
                downloadProvider.addDownload(
                                /* id */ vodId,
                                /* videoQuality */ VideoQuality.HIGH,
                                /* locale */ new Locale("eng"),
                                /* drmScheme */ "widevine",
                                /* Title */ "Title " + vodId,
                                /* extraData */ null,
                                /* images */ null,
                                /* externalSubtitleFormat */ "srt")
                        .flatMap(ok -> downloadProvider.getDownload(vodId))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(assetData -> {
                                    Log.d(TAG, "onVideoDownload: added");
                                    assetDataAdapter.getAssetData().add(assetData);
                                    assetDataAdapter.notifyDataSetChanged();
                                },
                                e -> {
                                    Log.d(TAG, "onVideoDownload: error", e);
                                    showError(e.getMessage());
                                })
        );
    }

    private void showError(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void onDownloadCancel(AssetData assetData) {
        compositeDisposable.add(
                downloadProvider.removeDownload(assetData.getId())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                ok -> Log.d(TAG, "onDownloadCancel - success"),
                                e -> {
                                    Log.d(TAG, "onDownloadCancel - error", e);
                                    showError(e.getMessage());
                                })
        );
    }

    private void onDownloadPause(AssetData assetData) {
        compositeDisposable.add(downloadProvider.pauseDownload(assetData.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        ok -> Log.d(TAG, "onDownloadPause - success"),
                        e -> {
                            Log.d(TAG, "onDownloadPause - error", e);
                            showError(e.getMessage());
                        })
        );
    }

    private void onDownloadResume(AssetData assetData) {
        compositeDisposable.add(downloadProvider.resumeDownload(assetData.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        ok -> Log.d(TAG, "onDownloadResume - success"),
                        e -> {
                            Log.d(TAG, "onDownloadResume - error", e);
                            showError(e.getMessage());
                        })
        );
    }

    private void onDownloadDelete(AssetData assetData) {
        compositeDisposable.add(downloadProvider.removeDownload(assetData.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        ok -> Log.d(TAG, "onDownloadDelete - success"),
                        e -> {
                            Log.d(TAG, "onDownloadDelete - error", e);
                            showError(e.getMessage());
                        })
        );
    }

    private void onDownloadRetry(AssetData assetData) {
        compositeDisposable.add(downloadProvider.removeDownload(assetData.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        ok -> {
                            onVideoDownload(assetData.getId());
                            Log.d(TAG, "onDownloadRetry - success");
                        },
                        throwable -> {
                            Log.d(TAG, "onDownloadRetry - error", throwable);
                            showError(throwable.getMessage());
                        })
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }

    /**
     * Recycler view adapter implementation.
     */
    public class AssetDataAdapter extends RecyclerView.Adapter<AssetDataAdapter.ViewHolder> {

        private List<AssetData> assetDataList = new ArrayList<>();

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final TextView textView;
            public final ProgressBar progressBar;
            public final ImageView buttonCancel;
            public final ImageView buttonPause;
            public final ImageView buttonResume;
            public final ImageView buttonDelete;
            public final ImageView buttonRetry;

            public ViewHolder(View view) {
                super(view);
                textView = view.findViewById(R.id.text_view_title);
                progressBar = view.findViewById(R.id.progress_bar);
                buttonCancel = view.findViewById(R.id.button_cancel);
                buttonPause = view.findViewById(R.id.button_pause);
                buttonResume = view.findViewById(R.id.button_resume);
                buttonDelete = view.findViewById(R.id.button_delete);
                buttonRetry = view.findViewById(R.id.button_retry);
            }
        }

        public void setAssetData(List<AssetData> assetDataList) {
            this.assetDataList = assetDataList;
        }

        public List<AssetData> getAssetData() {
            return assetDataList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.asset_data_row_item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            AssetData assetData = assetDataList.get(position);
            viewHolder.textView.setText(assetData.getId());
            viewHolder.itemView.setOnClickListener(view -> onVideoSelected(assetData));
            viewHolder.buttonCancel.setOnClickListener(view -> onDownloadCancel(assetData));
            viewHolder.buttonPause.setOnClickListener(view -> onDownloadPause(assetData));
            viewHolder.buttonResume.setOnClickListener(view -> onDownloadResume(assetData));
            viewHolder.buttonDelete.setOnClickListener(view -> onDownloadDelete(assetData));
            viewHolder.buttonRetry.setOnClickListener(view -> onDownloadRetry(assetData));

            switch (assetData.getDownloadState()) {
                case DOWNLOADED:
                    viewHolder.progressBar.setVisibility(View.GONE);
                    viewHolder.buttonCancel.setVisibility(View.GONE);
                    viewHolder.buttonPause.setVisibility(View.GONE);
                    viewHolder.buttonResume.setVisibility(View.GONE);
                    viewHolder.buttonDelete.setVisibility(View.VISIBLE);
                    viewHolder.buttonRetry.setVisibility(View.GONE);
                    break;
                case DOWNLOADING:
                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                    int progress = (int) (assetData.getDownloadProgress() * 100);
                    viewHolder.progressBar.setProgress(progress);
                    viewHolder.buttonCancel.setVisibility(View.VISIBLE);
                    viewHolder.buttonPause.setVisibility(View.VISIBLE);
                    viewHolder.buttonResume.setVisibility(View.GONE);
                    viewHolder.buttonDelete.setVisibility(View.GONE);
                    viewHolder.buttonRetry.setVisibility(View.GONE);
                    break;
                case PAUSED:
                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                    progress = (int) (assetData.getDownloadProgress() * 100);
                    viewHolder.progressBar.setProgress(progress);
                    viewHolder.buttonCancel.setVisibility(View.VISIBLE);
                    viewHolder.buttonPause.setVisibility(View.GONE);
                    viewHolder.buttonResume.setVisibility(View.VISIBLE);
                    viewHolder.buttonDelete.setVisibility(View.GONE);
                    viewHolder.buttonRetry.setVisibility(View.GONE);
                    break;
                case ERROR:
                    viewHolder.progressBar.setVisibility(View.GONE);
                    viewHolder.buttonCancel.setVisibility(View.GONE);
                    viewHolder.buttonPause.setVisibility(View.GONE);
                    viewHolder.buttonResume.setVisibility(View.GONE);
                    viewHolder.buttonDelete.setVisibility(View.GONE);
                    viewHolder.buttonRetry.setVisibility(View.VISIBLE);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return assetDataList.size();
        }
    }
}