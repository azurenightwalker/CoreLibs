package com.androidproductions.corelibs.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.widget.ImageView;

import java.io.InputStream;

public class ContactImageLoader extends ImageLoader {

    public ContactImageLoader(ImageView imageView, Context context, ImageLoader.Callback callback) {
        super(imageView, context, callback);
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Long... params) {
        return loadContactPhoto(context.getContentResolver(), params[0]);
    }

    public static Bitmap loadContactPhoto(ContentResolver cr, long  id) {
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
        if (input == null) {
            return null;
        }
        return BitmapFactory.decodeStream(input);
    }
}