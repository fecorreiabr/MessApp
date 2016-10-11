package br.iesb.messapp.adapters;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.io.File;
import java.util.List;

import br.iesb.messapp.Contact;
import br.iesb.messapp.R;

/**
 * Created by Felipe on 10/09/2016.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    private Context context;
    private List<Contact> contactList;
    private File imageFolder;

    public ContactsAdapter(Context context, List<Contact> contactList) {
        this.contactList = contactList;
        this.context = context;
        String externalStorage = Environment.getExternalStorageDirectory().toString();
        this.imageFolder = new File(externalStorage, context.getString(R.string.image_directory));
    }

    public void onItemLongClickListener(int position){

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_row, null);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Contact contact = contactList.get(position);
        holder.textName.setText(contact.getName());
        holder.textPhone.setText(contact.getPhone());
        holder.textEmail.setText(contact.getEmail());
        holder.itemView.setLongClickable(true);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onItemLongClickListener(position);
                return true;
            }
        });


        File imageFile = new File(imageFolder.getAbsolutePath(), contact.getId() + ".jpg");

        Glide.with(context)
                .load(imageFile)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL.NONE)
                .centerCrop()
                .placeholder(R.drawable.ic_account_circle_40dp)
                .into(new BitmapImageViewTarget(holder.contactImage){
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable roundedBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        roundedBitmapDrawable.setCircular(true);
                        holder.contactImage.setImageDrawable(roundedBitmapDrawable);
                    }
                });

    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView textName, textPhone, textEmail;
        protected ImageView contactImage;

        public ViewHolder(View itemView) {
            super(itemView);
            this.textName = (TextView) itemView.findViewById(R.id.text_name_contact_row);
            this.textPhone = (TextView) itemView.findViewById(R.id.text_phone_contact_row);
            this.textEmail = (TextView) itemView.findViewById(R.id.text_email_contact_row);
            this.contactImage = (ImageView) itemView.findViewById(R.id.img_contact_row);
        }

    }
}
