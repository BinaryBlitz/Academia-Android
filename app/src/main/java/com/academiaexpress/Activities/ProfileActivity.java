package com.academiaexpress.Activities;

import com.google.gson.JsonObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.academiaexpress.Base.BaseActivity;
import com.academiaexpress.Custom.ProgressDialog;
import com.academiaexpress.Data.DeliveryUser;
import com.academiaexpress.R;
import com.academiaexpress.Server.DeviceInfoStore;
import com.academiaexpress.Server.ServerApi;
import com.academiaexpress.Utils.AndroidUtilities;
import com.academiaexpress.Utils.Image;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        Image.loadPhoto(R.drawable.back1, (ImageView) findViewById(R.id.imageView21));

        setOnClickListeners();
    }

    private void setOnClickListeners() {
        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.textView7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AndroidUtilities.INSTANCE.isConnected(ProfileActivity.this)) return;

                if (check()) {
                    ProgressDialog dialog = new ProgressDialog();
                    dialog.show(getFragmentManager(), "delivery");

                    createUser();
                }
            }
        });
    }

    private void parse() {
        Intent intent = new Intent(ProfileActivity.this, FirstDeliveryScreen.class);
        intent.putExtra("name",
                ((EditText) findViewById(R.id.editText2)).getText().toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void createUser() {
        ServerApi.get(this).api().createUser(getUserFromFields()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) parse();
                else onInternetConnectionError();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                onInternetConnectionError();
            }
        });
    }

    public boolean check() {
        if (((EditText) findViewById(R.id.editText2)).getText().toString().isEmpty()) {
            Snackbar.make(findViewById(R.id.main), "Поле имени не заполнено.", Snackbar.LENGTH_SHORT).show();
            return false;
        } else if (((EditText) findViewById(R.id.editText)).getText().toString().isEmpty()) {
            Snackbar.make(findViewById(R.id.main), "Поле  фамилии не заполнено.", Snackbar.LENGTH_SHORT).show();
            return false;
        } else if (!isValidEmail(((EditText) findViewById(R.id.editText3)).getText().toString())) {
            Snackbar.make(findViewById(R.id.main), "Неверный email.", Snackbar.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public JsonObject getUserFromFields() {
        JsonObject object = new JsonObject();
        JsonObject user = new JsonObject();

        try {
            user.addProperty("first_name", ((EditText) findViewById(R.id.editText2)).getText().toString());
            user.addProperty("last_name", ((EditText) findViewById(R.id.editText)).getText().toString());
            user.addProperty("email", ((EditText) findViewById(R.id.editText3)).getText().toString());
            user.addProperty("phone_number", getIntent().getStringExtra("phone"));
            user.addProperty("verification_token", getIntent().getStringExtra("token"));
            object.add("user", user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DeliveryUser deliveryUser = new DeliveryUser(
                ((EditText) findViewById(R.id.editText2)).getText().toString(),
                ((EditText) findViewById(R.id.editText)).getText().toString(),
                ((EditText) findViewById(R.id.editText3)).getText().toString(),
                ""
        );

        DeviceInfoStore.saveUser(this, deliveryUser);

        return object;
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
