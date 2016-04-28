package org.gluu.oxpush2.app.Fragments.KeysFragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.gluu.oxpush2.app.CustomGluuAlertView.CustomGluuAlert;
import org.gluu.oxpush2.app.Fragments.LicenseFragment.LicenseFragment;
import org.gluu.oxpush2.app.GluuMainActivity;
import org.gluu.oxpush2.app.KeyHandleInfoFragment;
import org.gluu.oxpush2.app.R;
import org.gluu.oxpush2.store.AndroidKeyDataStore;
import org.gluu.oxpush2.u2f.v2.model.TokenEntry;
import org.gluu.oxpush2.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nazaryavornytskyy on 3/1/16.
 */
public class KeyFragmentListAdapter extends BaseAdapter {

    final SimpleDateFormat isoDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    final SimpleDateFormat userDateTimeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    private List<TokenEntry> list;
    private LayoutInflater mInflater;
    private Activity activity;
    private KeyFragmentListFragment.KeyHandleInfo mListener;

    public KeyFragmentListAdapter(Activity activity, List<TokenEntry> listContact, KeyFragmentListFragment.KeyHandleInfo keyHandleInfo) {
        list = listContact;
        this.activity = activity;
        mInflater = LayoutInflater.from(activity);
        mListener = keyHandleInfo;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = mInflater;
            view = inflater.inflate(R.layout.fragment_key, null);
        }
        view.setTag(position);
        final TokenEntry token = (TokenEntry) list.get(position);
        if (token != null) {
            TextView contentView = (TextView) view.findViewById(R.id.content);

            if (contentView != null) {
                contentView.setText(token.getKeyName());
            }
            TextView createdDate = (TextView) view.findViewById(R.id.created_date);
            String date = token.getCreatedDate();
            if (createdDate != null && date != null) {
                Date createDate = null;
                if (Utils.isNotEmpty(date)) {
                    try {
                        createDate = isoDateTimeFormat.parse(date);
                    } catch (ParseException ex) {
                        Log.e(this.getClass().getName(), "Failed to parse ISO date/time: " + date, ex);
                    }
                }

                String createdString = "";
                if (createdDate != null) {
                    createdString = userDateTimeFormat.format(createDate);
                }
//                Date date = null;
//                try {
//                    date = token.getPairingDate();//isoDateTimeFormat.parse(token.getPairingDate());
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                if (date != null) {
//                    String pairingDate = userDateTimeFormat.format(date);
//                int timeInMiliseconds = Integer.parseInt(token.getPairingDate());
//                String timeAgo = (String) DateUtils.getRelativeTimeSpanString(System.currentTimeMillis(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
                    createdDate.setText(createdString);
//                }
            } else {
                createdDate.setText(R.string.no_date);
            }
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                String tokenString = new Gson().toJson(list.get(position));
                KeyHandleInfoFragment infoFragment = new KeyHandleInfoFragment().newInstance(tokenString);
                if (mListener != null) {
                    mListener.onKeyHandleInfo(infoFragment);
                }
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = (int) v.getTag();
                String token = new Gson().toJson(list.get(position));
                final TokenEntry tokenEntry = new Gson().fromJson(token, TokenEntry.class);
                CustomGluuAlert gluuAlert = new CustomGluuAlert(activity);
                gluuAlert.setMessage(activity.getApplicationContext().getString(R.string.rename_key_name));
                gluuAlert.setYesTitle(activity.getApplicationContext().getString(R.string.yes));
                gluuAlert.setNoTitle(activity.getApplicationContext().getString(R.string.no));
                gluuAlert.setmListener(new GluuMainActivity.GluuAlertCallback() {
                    @Override
                    public void onPositiveButton() {
                        showRenameDialog(tokenEntry.getIssuer());
                    }
                });
                gluuAlert.show();
                return true;
            }
        });

        return view;
    }

    private void showRenameDialog(final String keyHandleID){
        final CustomGluuAlert gluuAlert = new CustomGluuAlert(activity);
        gluuAlert.setMessage(activity.getApplicationContext().getString(R.string.enter_new_key_name));
        gluuAlert.setYesTitle(activity.getApplicationContext().getString(R.string.save));
        gluuAlert.setNoTitle(activity.getApplicationContext().getString(R.string.cancel));
        gluuAlert.setIsTextView(true);
        gluuAlert.setmListener(new GluuMainActivity.GluuAlertCallback() {
            @Override
            public void onPositiveButton() {
                Context context = activity.getApplicationContext();
                AndroidKeyDataStore dataStore = new AndroidKeyDataStore(context);
                dataStore.changeKeyHandleName(keyHandleID, gluuAlert.getText());
                updateResults(dataStore);
            }
        });
        gluuAlert.show();
    }

    public void updateResults(AndroidKeyDataStore dataStore) {
        List<String> tokensString = dataStore.getTokenEntries();
        List<TokenEntry> tokens = new ArrayList<TokenEntry>();
        for (String tokenString : tokensString){
            TokenEntry token = new Gson().fromJson(tokenString, TokenEntry.class);
            tokens.add(token);
        }
        list = tokens;
        //Triggers the list update
        notifyDataSetChanged();
    }
}