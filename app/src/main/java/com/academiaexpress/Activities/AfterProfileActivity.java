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

public class AfterProfileActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);

        Image.loadPhoto(R.drawable.back1, (ImageView) findViewById(R.id.imageView21));

        findViewById(R.id.textView7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!AndroidUtilities.INSTANCE.isConnected(AfterProfileActivity.this)) return;
                if (check()) {
                    ProgressDialog dialog = new ProgressDialog();
                    dialog.show(getFragmentManager(), "delivery");
                    updateUser(getUserFromFields(), false);
                }
            }
        });

        findViewById(R.id.textViewdsb7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceInfoStore.resetUser(AfterProfileActivity.this);
                DeviceInfoStore.resetToken(AfterProfileActivity.this);

                ProgressDialog dialog = new ProgressDialog();
                dialog.show(getFragmentManager(), "deliveryapp");

                updateUser(generateQuitJson(), true);
            }
        });

        findViewById(R.id.guillotine_hamburger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadInfo();
    }

    private JsonObject generateQuitJson() {
        JsonObject object = new JsonObject();
        JsonObject user = new JsonObject();

        try {
            user.addProperty("device_token", "");
            user.addProperty("platform", "");
            object.add("user", user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    private void parse(boolean flag) {
        if (flag) {
            Intent intent2 = new Intent(AfterProfileActivity.this, StartActivity.class);
            intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent2);
            finish();
        } else {
            Intent intent = new Intent(AfterProfileActivity.this, ProductsActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void updateUser(JsonObject object, final boolean flag) {
        ServerApi.get(this).api().updateUser(object, DeviceInfoStore.getToken(this)).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                parse(flag);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
            }
        });
    }

    public boolean check() {
        if(((EditText) findViewById(R.id.editText2)).getText().toString().isEmpty()) {
            Snackbar.make(findViewById(R.id.main), "Поле имени не заполнено.", Snackbar.LENGTH_SHORT).show();
            return false;
        } else if(((EditText) findViewById(R.id.editText)).getText().toString().isEmpty()) {
            Snackbar.make(findViewById(R.id.main), "Поле  фамилии не заполнено.", Snackbar.LENGTH_SHORT).show();
            return false;
        } else if(!isValidEmail(((EditText) findViewById(R.id.editText3)).getText().toString())) {
            Snackbar.make(findViewById(R.id.main), "Неверный email.", Snackbar.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public JsonObject getUserFromFields() {
        JsonObject object = new JsonObject();
        JsonObject user = new JsonObject();

        user.addProperty("first_name", ((EditText) findViewById(R.id.editText2)).getText().toString());
        user.addProperty("last_name", ((EditText) findViewById(R.id.editText)).getText().toString());
        user.addProperty("email", ((EditText) findViewById(R.id.editText3)).getText().toString());
        object.add("user", user);


        DeliveryUser deliveryUser = new DeliveryUser(
                ((EditText) findViewById(R.id.editText2)).getText().toString(),
                ((EditText) findViewById(R.id.editText)).getText().toString(),
                ((EditText) findViewById(R.id.editText3)).getText().toString(),
                ""
        );

        DeviceInfoStore.saveUser(this, deliveryUser);

        return object;
    }

    public void loadInfo() {
        if(DeviceInfoStore.getUser(this).equals("null")) {
            return;
        }

        DeliveryUser myProfile = DeliveryUser.Companion.fromString(DeviceInfoStore.getUser(this));

        ((EditText) findViewById(R.id.editText2)).setText(myProfile.getFirstName());
        ((EditText) findViewById(R.id.editText)).setText(myProfile.getSecondName());
        ((EditText) findViewById(R.id.editText3)).setText(myProfile.getEmail());

    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

}