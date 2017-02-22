package com.example.java.retrofit2;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.java.retrofit2.flow.repos.Presenter.ReposPresenter;
import com.example.java.retrofit2.flow.repos.View.ReposView;
import com.example.java.retrofit2.model.RecyclerAdapter;
import com.example.java.retrofit2.model.Repo;
import com.jakewharton.rxbinding.support.v7.widget.RxSearchView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class MainActivity extends AppCompatActivity implements ReposView ,  View.OnClickListener {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ReposPresenter reposPresenter = new ReposPresenter();
    private Observable<CharSequence> queryObservable = null;
    private CustomTabsIntent.Builder build;

    private static final String EXTRA_CUSTOM_TABS_SESSION = "android.support.customtabs.extra.SESSION";
    final String CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome";
    CustomTabsServiceConnection mCustomTabsServiceConnection = null;
    CustomTabsSession mCustomTabsSession = null;
    CustomTabsIntent mCustomTabsIntent;
    CustomTabsClient mCustomTabsClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        reposPresenter.onAttach(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        //String[] myDataset = getDataSet();

        // если мы уверены, что изменения в контенте не изменят размер layout-а RecyclerView
        // передаем параметр true - это увеличивает производительность
        mRecyclerView.setHasFixedSize(true);
        // используем linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mCustomTabsClient= null;
            }

            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                mCustomTabsClient = customTabsClient;
                mCustomTabsClient.warmup(0L);
                mCustomTabsSession = mCustomTabsClient.newSession(null);
            }
        };
        CustomTabsClient.bindCustomTabsService(this, CUSTOM_TAB_PACKAGE_NAME, mCustomTabsServiceConnection);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search...");

        //queryObservable =
        RxSearchView.queryTextChanges(searchView)
                .debounce(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(query -> reposPresenter.getRepos(query.toString()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showRepos(List<Repo> list) {
        mAdapter = new RecyclerAdapter(list, this);
        mRecyclerView.setAdapter(mAdapter);
     //   mAdapter.se
    }
    @Override
    public Context getContex() {
        return this;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        reposPresenter.onDetach();
    }

    @Override
    public void onClick(View view) {
        RecyclerAdapter.MyViewHolder holder = (RecyclerAdapter.MyViewHolder) mRecyclerView.findContainingViewHolder(view);
        if (holder == null) return;
      //  Toast.makeText(this, holder.getRepo().getUrl(), Toast.LENGTH_SHORT).show();

        String url = holder.getRepo().getHtmlUrl();
//        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
//        CustomTabsIntent customTabsIntent = builder.build();
//       customTabsIntent.launchUrl(this, Uri.parse(url));


        mCustomTabsIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
                .setShowTitle(true)
                .build();

        mCustomTabsIntent.launchUrl(this, Uri.parse(url));
    }
}
