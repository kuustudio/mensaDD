package com.pasta.mensadd.ui.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pasta.mensadd.R;
import com.pasta.mensadd.Utils;
import com.pasta.mensadd.database.AppDatabase;
import com.pasta.mensadd.database.repository.NewsRepository;
import com.pasta.mensadd.networking.ApiServiceClient;
import com.pasta.mensadd.ui.adapter.NewsListAdapter;
import com.pasta.mensadd.ui.viewmodel.NewsViewModel;
import com.pasta.mensadd.ui.viewmodel.NewsViewModelFactory;

import static com.pasta.mensadd.networking.ApiServiceClient.FETCH_ERROR;
import static com.pasta.mensadd.networking.ApiServiceClient.IS_FETCHING;

public class NewsFragment extends Fragment {

    private NewsViewModel mNewsViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        setHasOptionsMenu(true);
        LinearLayoutManager layoutParams = new LinearLayoutManager(requireActivity());
        RecyclerView newsListRecyclerView = view.findViewById(R.id.newsList);
        NewsListAdapter newsListAdapter = new NewsListAdapter(this.requireContext());
        newsListRecyclerView.setLayoutManager(layoutParams);
        newsListRecyclerView.setAdapter(newsListAdapter);
        NewsViewModelFactory newsViewModelFactory = new NewsViewModelFactory(
                new NewsRepository(AppDatabase.getInstance(requireContext()),
                        ApiServiceClient.getInstance(
                                getString(R.string.api_base_url),
                                getString(R.string.api_user),
                                getString(R.string.api_key)
                        )
                )
        );
        mNewsViewModel = new ViewModelProvider(this, newsViewModelFactory).get(NewsViewModel.class);
        mNewsViewModel.triggerNewsFetching(false);
        mNewsViewModel.getFetchState().observe(getViewLifecycleOwner(), fetchState -> {
            ProgressBar progressBar = view.findViewById(R.id.newsListProgressBar);
            progressBar.setVisibility(fetchState == IS_FETCHING ? View.VISIBLE : View.GONE);
            if (fetchState == FETCH_ERROR) {
                int errorMsgId = !Utils.isOnline(requireContext()) ? R.string.error_no_internet : R.string.error_unknown;
                Toast.makeText(requireContext(), getString(R.string.error_fetching_news, getString(errorMsgId)), Toast.LENGTH_SHORT).show();
            }
        });
        mNewsViewModel.getNews().observe(getViewLifecycleOwner(), newsListAdapter::submitList);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.fragment_news_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_news_refresh:
                mNewsViewModel.triggerNewsFetching(true);
        }
        return super.onOptionsItemSelected(item);
    }
}
