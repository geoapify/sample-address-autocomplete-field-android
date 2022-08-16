package com.autocomplete.view;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.autocomplete.view.API.APIClient;
import com.autocomplete.view.API.APIInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationSearchActivity extends AppCompatActivity {
    List<String> data = new ArrayList<>();
    ArrayAdapter<String> adapter;
    ImageView img_close;
    AutoCompleteTextView actv;
    APIInterface apiInterface;

    Runnable userStoppedTyping = new Runnable() {

        @Override
        public void run() {
            if(actv.getText().toString().trim().length() != 0) {
                img_close.setVisibility(View.VISIBLE);
            } else {
                img_close.setVisibility(View.INVISIBLE);
            }
            retrieveData(actv.getText().toString().trim());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiInterface = APIClient.getClient().create(APIInterface.class);

        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        img_close = (ImageView) findViewById(R.id.close_search);
        img_close.setVisibility(View.INVISIBLE);

        adapter = new ArrayAdapter<>(LocationSearchActivity.this, android.R.layout.select_dialog_item, data);
        actv.setThreshold(3);
        actv.setAdapter(adapter);
        actv.setTextColor(Color.BLACK);
        final Handler mHandler = new Handler();

        actv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                //avoid triggering event when text is empty
                mHandler.removeCallbacksAndMessages(null);
                mHandler.postDelayed(userStoppedTyping, 1000); // 1 second
            }
        });

        actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                img_close.setVisibility(View.VISIBLE);
            }
        });
        actv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                img_close.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                img_close.setVisibility(View.INVISIBLE);
            }
        });

        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actv.setText("");
                data = new ArrayList<>();
                adapter = new ArrayAdapter<>(LocationSearchActivity.this, android.R.layout.select_dialog_item, data);
                actv.setThreshold(3);
                actv.setAdapter(adapter);
                actv.setTextColor(Color.BLACK);
                adapter.notifyDataSetChanged();
                img_close.setVisibility(View.INVISIBLE);
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(actv.getWindowToken(), 0);
            }
        });

        if (LocationSearch.checkConnection(LocationSearchActivity.this)) {}
        else {
            noInternetError();
        }

    }
    public void noInternetError()
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        finish();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(LocationSearchActivity.this);
        builder.setCancelable(false).setMessage("No Internet available. Please check your Internet connection.").setPositiveButton("OK", dialogClickListener).show();
    }
    private void retrieveData(String query)
    {
        String text = query.trim();
        if(text.equals("") || text.length() < 3)
        {
            data = new ArrayList<>();
            adapter = new ArrayAdapter<>(LocationSearchActivity.this, android.R.layout.select_dialog_item, data);
            actv.setThreshold(3);
            actv.setAdapter(adapter);
            actv.setTextColor(Color.BLACK);
            adapter.notifyDataSetChanged();
            return;
        }

        try {
            retrofit2.Call<ResponseBody> call = apiInterface.getLocationresult("?text=" + URLEncoder.encode(text, "UTF-8") + "&format=json&limit=5&apiKey=" + APIClient.API_KEY);
            Log.d("URL", APIClient.BASE_URL + "?text=" + URLEncoder.encode(text, "UTF-8") + "&format=json&limit=5&apiKey=" + APIClient.API_KEY);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        data = new ArrayList<>();
                        ResponseBody resource = response.body();
                        String responseBodyString = "";
                        try {
                            responseBodyString = resource.string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        JSONObject responses = new JSONObject(responseBodyString);
                        JSONArray resultData = responses.getJSONArray("results");
                        for (int i = 0; i < resultData.length(); i++) {
                            try {
                                JSONObject location_ = resultData.getJSONObject(i);
                                String formattedAddress = location_.getString("formatted");
                                data.add(formattedAddress);

                                if (i == data.size() - 1) {
                                    adapter = new ArrayAdapter<String>(LocationSearchActivity.this, android.R.layout.select_dialog_item, data);
                                    actv.setThreshold(3);
                                    actv.setAdapter(adapter);
                                    actv.setTextColor(Color.BLACK);
                                    adapter.notifyDataSetChanged();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                    call.cancel();
                    Toast.makeText(LocationSearchActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (UnsupportedEncodingException e) {
            Log.e("Unsupported encoding", "Your device doesn't support UTF-8 encoding");
        }
    }
}