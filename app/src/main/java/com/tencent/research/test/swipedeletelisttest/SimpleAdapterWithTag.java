package com.tencent.research.test.swipedeletelisttest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by niuniuzhang on 15/11/11.
 */
public class SimpleAdapterWithTag extends ArrayAdapter<String> {

    private LruCache<String, BitmapDrawable> mMemoryCache;
    private ListView mListView;

    public SimpleAdapterWithTag(Context context, int resource, int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);

        int maxMemory = (int)Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, BitmapDrawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, BitmapDrawable value) {
                return value.getBitmap().getByteCount();
            }
        };
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mListView == null) {
            mListView = (ListView)parent;
        }
        String url = getItem(position);

        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_view_item, null);
        } else {
            view = convertView;
        }
        ImageView image = (ImageView) view.findViewById(R.id.image);
        image.setImageDrawable(new ColorDrawable(0xffff00));
        image.setTag(url);
        BitmapDrawable drawable = getBitmapFromCache(url);
        if (drawable != null) {
            image.setImageDrawable(drawable);
        } else {
            BitmapWorkerTask task = new BitmapWorkerTask();
            task.execute(url);
        }
        return view;
    }


    public void addBitmapToCache(String key, BitmapDrawable drawable) {
        if (getBitmapFromCache(key) == null) {
            mMemoryCache.put(key, drawable);
        }
    }

    public BitmapDrawable getBitmapFromCache(String key) {
        return mMemoryCache.get(key);
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, BitmapDrawable> {

        private String imageUrl;

        @Override
        protected BitmapDrawable doInBackground(String... params) {
            imageUrl = params[0];
            Bitmap bitmap = downloadBitmap(imageUrl);
            BitmapDrawable drawable = new BitmapDrawable(getContext().getResources(), bitmap);
            addBitmapToCache(imageUrl, drawable);
            return drawable;
        }

        @Override
        protected void onPostExecute(BitmapDrawable drawable) {
            ImageView imageView = (ImageView) mListView.findViewWithTag(imageUrl);
            if (imageView != null && drawable != null) {
                imageView.setImageDrawable(drawable);
            }
        }

        private Bitmap downloadBitmap(String imageUrl) {
            Bitmap bitmap = null;
            HttpURLConnection con = null;
            try {
                URL url = new URL(imageUrl);
                con = (HttpURLConnection)url.openConnection();
                con.setConnectTimeout(5 * 1000);
                con.setReadTimeout(10 * 1000);
                bitmap = BitmapFactory.decodeStream(con.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }

            return bitmap;
        }
    }
}
