package com.androidpourtous.uploadfile;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import is.arontibo.library.ElasticDownloadView;


public class MainActivity extends AppCompatActivity {

    /**
     *  Le lien du script, qu'on appellera pour uploader notre fichier
     */
    private static final String URL_UPLOAD = "http://192.168.0.23/uploadtest/upload.php";
    private static final String TAG = "D" ;
    private static final String file = Environment.getExternalStorageDirectory().toString() + "/Divitech/unzipped/files/ads/" + "80.jpg";

    private final OkHttpClient client = new OkHttpClient();

    private static final String CONTENT_TYPE = "application/octet-stream";

    private static final int FILE_CODE = 9999;


    private static final String FORM_NAME = "file";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private ElasticDownloadView mElasticDownloadView;
    private Button button;

    private File fileToUpload;

    /**
     *  la taile de notre fichier
     */
    private long totalSize = 0;



    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler(getMainLooper());

        fileToUpload = new File(Environment.getExternalStorageDirectory().toString() + "/Divitech/unzipped/files/ads/" + "80.jpg");

        mElasticDownloadView = (ElasticDownloadView) findViewById(R.id.elastic_download_view);
        button = (Button) findViewById(R.id.btn_choose_file);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    upload(fileToUpload);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }



/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {



            try {
                upload(fileToUpload);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }*/


    /**
     * Upload Image
     *
     * @return
     */
    public void upload(final File file) throws Exception {

        new AsyncTask<Void, Integer, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                mElasticDownloadView.startIntro();
            }

            @Override
            protected String doInBackground(Void... voids) {

                totalSize = file.length();

                RequestBody requestBody = new MultipartBuilder()
                        .type(MultipartBuilder.FORM)
                        .addFormDataPart(FORM_NAME, file.getName(),
                                new CountingFileRequestBody(file, CONTENT_TYPE, new CountingFileRequestBody.ProgressListener() {
                                    @Override
                                    public void transferred(long num) {

                                        final float progress = (num / (float) totalSize) * 100;

                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                onProgressUpdate((int) progress);
                                            }
                                        });
                                    }
                                }))
                        .build();

                Request request = new Request.Builder()
                        .url(URL_UPLOAD)
                        .post(requestBody)
                        .build();


                Response response = null;

                try {
                    // On exécute la requête
                    response = client.newCall(request).execute();

                    String responseStr = response.body().string();

                    return responseStr;


                } catch (IOException e) {
                    e.printStackTrace();
                }

                return  null;

            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                // On affiche le pourcentage d'ulpoad
            }


            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                try {
                    JSONObject jsonObject = new JSONObject(s);

                    int success = Integer.valueOf(jsonObject.getString(TAG_SUCCESS));
                    String message = jsonObject.getString(TAG_MESSAGE);

                    // Si c'est 1 donc l'upload s'est bien faite
                    if (success == 1)
                        mElasticDownloadView.success();
                    else
                        mElasticDownloadView.fail();

                    // On affiche le message à l'utilisateur
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


        }.execute();


    }
    }

