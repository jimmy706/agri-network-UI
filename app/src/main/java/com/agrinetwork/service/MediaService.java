package com.agrinetwork.service;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;

import com.agrinetwork.config.Variables;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MediaService {

    private static final String SERVICE_URL = Variables.SERVICE_DOMAIN + "/medias";

    private final Context context;
    private OkHttpClient client = new OkHttpClient();


    public MediaService(Context context) {
        this.context = context;
    }

    public Call uploadImage(Uri uri, String token) throws Exception {
        InputStream ips = context.getContentResolver().openInputStream(uri);
        String fileId = DocumentsContract.getDocumentId(uri);
        File file = new File(context.getCacheDir().getAbsolutePath() + "/" + fileId);

        writeFile(ips, file);
        String contentType = "image/jpeg";
        RequestBody fileBody = RequestBody.create(MediaType.parse(contentType), file);

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("key", Variables.UPLOAD_MEDIA_TOKEN)
                .addFormDataPart("image", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(SERVICE_URL + "/img/upload")
                .post(body)
                .addHeader("Authorization", token)
                .build();

        return client.newCall(request);
    }

    private void writeFile(InputStream in, File file) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[2048];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if ( out != null ) {
                    out.close();
                }
                in.close();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }
}
