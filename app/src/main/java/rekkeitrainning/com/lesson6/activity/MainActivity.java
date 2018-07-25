package rekkeitrainning.com.lesson6.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;

import rekkeitrainning.com.lesson6.R;
import rekkeitrainning.com.lesson6.adapter.ContactsAdapter;
import rekkeitrainning.com.lesson6.db.DBContact;
import rekkeitrainning.com.lesson6.model.Contact;

public class MainActivity extends AppCompatActivity implements ContactsAdapter.ItemClickListener{
    DBContact mDbContact;
    BottomNavigationView btNavigation;
    RecyclerView rc_contact;
    ArrayList<Contact> mListContact = null;
    ContactsAdapter mContactAdapter;
    private boolean isGridlayout = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
        setDataVertical();
    }

    private void setDataVertical(){
        if (mContactAdapter == null){
            mContactAdapter = new ContactsAdapter(this);
        }
        mListContact = (ArrayList<Contact>) mDbContact.getAllContact();
        mContactAdapter.setmListContact(mListContact);
        rc_contact.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        mContactAdapter.onItemClickListener(this);
        rc_contact.setAdapter(mContactAdapter);
    }
    private void setDataGrid(){
        if (mContactAdapter == null){
            mContactAdapter = new ContactsAdapter(this);
        }
        mListContact = (ArrayList<Contact>) mDbContact.getAllContact();
        mContactAdapter.setmListContact(mListContact);
        rc_contact.setLayoutManager(new GridLayoutManager(this,2));
        mContactAdapter.onItemClickListener(this);
        rc_contact.setAdapter(mContactAdapter);
    }
    private void initListener() {
        btNavigation.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private void initView() {
        btNavigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        disableShiftMode(btNavigation);
        rc_contact = findViewById(R.id.rcContact);
        mDbContact = new DBContact(this);
    }
    private boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_transaction:
                isGridlayout = !isGridlayout;
                if (isGridlayout){
                    setDataGrid();
                } else {
                    setDataVertical();
                }
                break;
            case R.id.action_add:
                showDialogAddContact();
                break;

        }
        return true;
    }

    private void showDialogAddContact() {
        final Dialog mDialog = new Dialog(this);
        mDialog.setContentView(R.layout.dialog_contact);
        TextView tv_title = mDialog.findViewById(R.id.tv_title);
        final TextInputEditText et_name = mDialog.findViewById(R.id.etName);
        final TextInputEditText et_phone = mDialog.findViewById(R.id.etPhone);
        Button btn_save = mDialog.findViewById(R.id.btnSave);
        Button btn_cancle = mDialog.findViewById(R.id.btnCancle);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString();
                String phone = et_phone.getText().toString();
                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(phone)){
                    Contact mContact = new Contact(name, phone);
//                    mListContact.add(mContact);
                    mDbContact.insertContact(mContact);
                    mListContact = (ArrayList<Contact>) mDbContact.getAllContact();
                    mContactAdapter.setmListContact(mListContact);
                    mContactAdapter.notifyDataSetChanged();
                    rc_contact.smoothScrollToPosition(mContactAdapter.getItemCount() - 1);
                    mDialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this,"Bạn vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        tv_title.setText(getResources().getString(R.string.insert));
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(mDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mDialog.show();
        mDialog.getWindow().setAttributes(lWindowParams);
    }

    @SuppressLint("RestrictedApi")
    public void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("TAG", "Unable to get shift mode field");
        } catch (IllegalAccessException e) {
            Log.e("TAG", "Unable to change value of shift mode");
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        showDialogUpdateContact(position);
    }

    private void showDialogUpdateContact(int position) {
        Dialog mDialog = new Dialog(this);
        mDialog.setContentView(R.layout.dialog_contact);
        TextView tv_title = mDialog.findViewById(R.id.tv_title);
        TextInputEditText et_name = mDialog.findViewById(R.id.etName);
        TextInputEditText et_phone = mDialog.findViewById(R.id.etPhone);
        Button btn_save = mDialog.findViewById(R.id.btnSave);
        Button btn_cancle = mDialog.findViewById(R.id.btnCancle);
        int id = mListContact.get(position).getId();
        et_name.setText(mListContact.get(position).getNameContact());
        et_phone.setText(mListContact.get(position).getPhoneNumber());
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString();
                String phone = et_phone.getText().toString();
                if (!TextUtils.isEmpty(name) &&
                    !TextUtils.isEmpty(phone) && (
                    !name.equalsIgnoreCase(mListContact.get(position).getNameContact()) ||
                    !phone.equalsIgnoreCase(mListContact.get(position).getPhoneNumber()))){
//                    mListContact.get(position).setNameContact(name);
//                    mListContact.get(position).setPhoneNumber(phone);
//                    mContactAdapter.notifyDataSetChanged();
                    mDbContact.updateContact(new Contact(id,name,phone));
                    mListContact = (ArrayList<Contact>) mDbContact.getAllContact();
                    mContactAdapter.setmListContact(mListContact);
                    mContactAdapter.notifyDataSetChanged();
                    rc_contact.smoothScrollToPosition(position);
                    mDialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this,"Thông tin chưa thay đổi hoặc không đúng", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        tv_title.setText(getResources().getString(R.string.update));
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(mDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mDialog.show();
        mDialog.getWindow().setAttributes(lWindowParams);
    }
}
