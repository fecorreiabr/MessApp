package br.iesb.messapp.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.iesb.messapp.Contact;
import br.iesb.messapp.R;

/**
 * Created by Felipe on 10/09/2016.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private List<Contact> contactList;

    public ContactsAdapter(List<Contact> contactList) {
        this.contactList = contactList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_row, null);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textName.setText(contactList.get(position).getName());
        holder.textPhone.setText(contactList.get(position).getPhone());
        holder.textEmail.setText(contactList.get(position).getEmail());
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView textName, textPhone, textEmail;

        public ViewHolder(View itemView) {
            super(itemView);
            this.textName = (TextView) itemView.findViewById(R.id.text_name_contact_row);
            this.textPhone = (TextView) itemView.findViewById(R.id.text_phone_contact_row);
            this.textEmail = (TextView) itemView.findViewById(R.id.text_email_contact_row);
        }
    }
}
