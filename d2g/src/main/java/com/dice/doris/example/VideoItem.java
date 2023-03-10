package com.dice.doris.example;

import androidx.annotation.NonNull;

import com.dice.shield.downloads.entity.AssetData;

import java.io.Serializable;

public class VideoItem implements Serializable {

    private String id;
    private String url;
    private String offlineLicense;

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getOfflineLicense() {
        return offlineLicense;
    }

    @NonNull
    public static VideoItem fromAssetData(@NonNull AssetData assetData) {
        VideoItem videoItem = new VideoItem();
        videoItem.id = assetData.getId();
        videoItem.url = assetData.getUrl();
        videoItem.offlineLicense = assetData.getOfflineLicense();
        return videoItem;
    }
}
