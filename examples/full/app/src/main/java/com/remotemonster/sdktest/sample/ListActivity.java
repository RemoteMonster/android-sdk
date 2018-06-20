package com.remotemonster.sdktest.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.remotemonster.sdk.RemonCall;
import com.remotemonster.sdk.RemonCast;
import com.remotemonster.sdk.data.Room;
import com.remon.sdktest.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lucas on 2018. 5. 16..
 */

public class ListActivity extends AppCompatActivity {
    @BindView(R.id.lvChannel)
    ListView lvChannel;
    @BindView(R.id.btnCreate)
    Button btnCreate;

    private RoomAdapter mAdapter;
    private RemonCall remonCall;
    private RemonCast remonCast;
    private int remonType = 0;
    private RemonApplication remonApplication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);

        remonApplication = (RemonApplication) getApplicationContext();

        Intent intent = getIntent();
        remonType = intent.getIntExtra("remonType", 0);

        mAdapter = new RoomAdapter();
        mRoomList = new ArrayList<>();
        lvChannel.setAdapter(mAdapter);

        btnCreate.setOnClickListener(v -> {
            if (remonType == 0) {
                remonCall.close();
                Intent intentCreate = new Intent(ListActivity.this, CallActivity.class);
                intentCreate.putExtra("isCreate", true);
                startActivity(intentCreate);
            } else if (remonType == 1){
                remonCast.close();
                Intent intentCreate = new Intent(ListActivity.this, CastActivity.class);
                intentCreate.putExtra("isCreate", true);
                startActivity(intentCreate);
            }
        });
    }

    private void getChannelList() {
        mRoomList.clear();
        if (remonType == 0) {
            /* type = 0 통신일 경우 */
            remonCall = RemonCall.builder()
                    .context(ListActivity.this)
                    .serviceId(remonApplication.getConfig().getServiceId())
                    .key(remonApplication.getConfig().getKey())
                    .restUrl(remonApplication.getConfig().restHost)
                    .wssUrl(remonApplication.getConfig().socketUrl)
                    .build();
            remonCall.onInit(() -> remonCall.fetchCalls());
            remonCall.onFetch(rooms -> {
                for (Room room : rooms) {
                    mRoomList.add(room);
                }
                runOnUiThread(() -> mAdapter.notifyDataSetChanged());
            });
        } else {
            /* type = 1 방송일 경우 */
            remonCast = RemonCast.builder()
                    .context(ListActivity.this)
                    .serviceId(remonApplication.getConfig().getServiceId())
                    .key(remonApplication.getConfig().getKey())
                    .restUrl(remonApplication.getConfig().restHost)
                    .wssUrl(remonApplication.getConfig().socketUrl)
                    .build();
            remonCast.onInit(() -> remonCast.fetchCasts());
            remonCast.onFetch(rooms -> {
                for (Room room : rooms) {
                    mRoomList.add(room);
                }
                runOnUiThread(() -> mAdapter.notifyDataSetChanged());
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.setServiceId) {
            ServiceIdDialog serviceIdDialog = new ServiceIdDialog(ListActivity.this);
            serviceIdDialog.show();
            return true;
        }
        if (id == R.id.itemRefresh) {
            getChannelList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<Room> mRoomList;

    private class RoomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mRoomList.size();
        }

        @Override
        public Object getItem(int position) {
            return mRoomList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ChannelItemViewHolder channelItemViewHolder = null;
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_item_channel, null);

                channelItemViewHolder = new ChannelItemViewHolder(convertView);
                convertView.setTag(channelItemViewHolder);
            } else {
                channelItemViewHolder = (ChannelItemViewHolder) convertView.getTag();
            }

            channelItemViewHolder.tvRoomInfo.setText(mRoomList.get(position).getId());
            channelItemViewHolder.tvStatus.setText(mRoomList.get(position).getStatus());
            channelItemViewHolder.tvRoomInfo.setOnClickListener(v -> {
                if (remonType == 0) {
                    remonCall.close();
                    Intent intent = new Intent(ListActivity.this, CallActivity.class);
                    intent.putExtra("isCreate", false);
                    intent.putExtra("chid", mRoomList.get(position).getId());
                    startActivity(intent);
                } else if (remonType == 1) {
                    remonCast.close();
                    Intent intent = new Intent(ListActivity.this, CastActivity.class);
                    intent.putExtra("isCreate", false);
                    intent.putExtra("chid", mRoomList.get(position).getId());
                    startActivity(intent);
                }
            });

            channelItemViewHolder.imvSetConfig.setOnClickListener(v -> {
                if (!mRoomList.get(position).getStatus().equals("COMPLETE")) {
                    if (remonType == 0) {
                        remonCall.close();
                        Intent intent = new Intent(ListActivity.this, CallActivity.class);
                        intent.putExtra("isCreate", false);
                        intent.putExtra("setConfig", true);
                        intent.putExtra("chid", mRoomList.get(position).getId());
                        startActivity(intent);
                    } else if(remonType == 1){
                        remonCast.close();
                        Intent intent = new Intent(ListActivity.this, CastActivity.class);
                        intent.putExtra("isCreate", false);
                        intent.putExtra("setConfig", true);
                        intent.putExtra("chid", mRoomList.get(position).getId());
                        startActivity(intent);
                    }
                }
            });

            return convertView;
        }

    }

    static class ChannelItemViewHolder {
        @BindView(R.id.tvRoomInfo)
        TextView tvRoomInfo;
        @BindView(R.id.tvStatus)
        TextView tvStatus;
        @BindView(R.id.imvSetConfig)
        ImageView imvSetConfig;

        public ChannelItemViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        getChannelList();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

