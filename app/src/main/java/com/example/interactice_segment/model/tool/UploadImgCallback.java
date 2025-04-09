package com.example.interactice_segment.model.tool;


// UploadBitmapTask的回调接口
public interface UploadImgCallback
{
    void onImageUploaded(String result); // 成功时回调
    void onUploadedFailed(); // 失败时回调
}
