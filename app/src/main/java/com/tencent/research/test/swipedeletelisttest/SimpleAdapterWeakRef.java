package com.tencent.research.test.swipedeletelisttest;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 双向弱引用避免加载乱序
 *
 * Created by niuniuzhang on 15/11/11.
 */
public class SimpleAdapterWeakRef extends ArrayAdapter<String> {

    private ListView mListView;

    private Bitmap mLoadingBitmap;

    private LruCache<String, BitmapDrawable> mMemoryCache;

    public SimpleAdapterWeakRef(Context context, int resource, int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);

        mLoadingBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.loading);

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
        BitmapDrawable drawable = getBitmapFromCache(url);
        if (drawable != null) {
            image.setImageDrawable(drawable);
        } else if (cancelPotentialWork(url, image)){
            BitmapWorkerTask task = new BitmapWorkerTask(image);
            AsyncDrawable asyncDrawable = new AsyncDrawable(getContext().getResources(), mLoadingBitmap, task);
            image.setImageDrawable(asyncDrawable);
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

    class AsyncDrawable extends BitmapDrawable {
        private WeakReference<BitmapWorkerTask> bitmapWorkerTaskWeakReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskWeakReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskWeakReference.get();
        }
    }

    private BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                AsyncDrawable asyncDrawable = (AsyncDrawable)drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public boolean cancelPotentialWork(String url, ImageView imageView) {
        BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            String imageUrl = bitmapWorkerTask.imageUrl;
            if (imageUrl == null || !imageUrl.equals(url)) {
                bitmapWorkerTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, BitmapDrawable> {

        private String imageUrl;

        private WeakReference<ImageView> imageViewWeakReference;

        public BitmapWorkerTask(ImageView imageView) {
            imageViewWeakReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected BitmapDrawable doInBackground(String... params) {
            String imageUrl = params[0];
            Bitmap bitmap = downloadBitmap(imageUrl);
            BitmapDrawable drawable = new BitmapDrawable(getContext().getResources(), bitmap);
            addBitmapToCache(imageUrl, drawable);
            return drawable;
        }

        @Override
        protected void onPostExecute(BitmapDrawable drawable) {
            ImageView imageView = getAttachedImageView();
            if (imageView != null && drawable != null) {
                imageView.setImageDrawable(drawable);
            }
        }

        private ImageView getAttachedImageView() {
            ImageView view = imageViewWeakReference.get();
            BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(view);
            if (this == bitmapWorkerTask) {
                return view;
            }
            return null;
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
