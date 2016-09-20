package br.iesb.messapp.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.iesb.messapp.model.Device;
import br.iesb.messapp.R;

/**
 * Created by Felipe on 17/09/2016.
 */
public class BlueToothListAdapter extends RecyclerView.Adapter<BlueToothListAdapter.ViewHolder> {
    private List<Device> deviceList;

    public BlueToothListAdapter(List<Device> deviceList) {
        this.deviceList = deviceList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_row, null);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textMac.setText(deviceList.get(position).getMac());
        holder.textName.setText(deviceList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView textMac, textName;
        public ViewHolder(View itemView) {
            super(itemView);
            this.textMac = (TextView) itemView.findViewById(R.id.text_device_mac_bt_row);
            this.textName = (TextView) itemView.findViewById(R.id.text_device_name_bt_row);
        }
    }
}
