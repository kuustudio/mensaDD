package com.pasta.mensadd.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.pasta.mensadd.database.entity.News;

import java.util.List;

@Dao
public interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNews(News news);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNews(List<News> news);

    @Query("SELECT * FROM table_news ORDER BY id DESC")
    LiveData<List<News>> getNews();

}
